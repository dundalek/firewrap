{
  description = "NixOS in MicroVMs";

  nixConfig = {
    extra-substituters = [ "https://microvm.cachix.org" ];
    extra-trusted-public-keys = [ "microvm.cachix.org-1:oXnBc6hRE3eX5rSYdRyMYXnfzcCxC7yKPTbZXALsqys=" ];
  };

  inputs.microvm = {
    url = "github:microvm-nix/microvm.nix";
    inputs.nixpkgs.follows = "nixpkgs";
  };

  outputs =
    {
      self,
      nixpkgs,
      microvm,
    }:
    let
      system = "x86_64-linux";
    in
    {
      # Library function to create a parameterized MicroVM configuration
      lib.mkMicroVM =
        {
          userName,
          userHome,
          userUid,
          userGid,
          socketDir,
          chdir ? null,
          extraPackages ? [ ],
          # Port forwarding in Docker-style format: ["hostPort:guestPort"] or ["hostAddr:hostPort:guestPort"]
          forwardPorts ? [ ],
          # Additional VirtioFS shares: [{ source, target, readOnly }]
          virtiofsShares ? [ ],
          # Environment variables to set in VM: { "NAME" = "value"; }
          environmentVariables ? { },
          # Enable/disable network (user-mode networking)
          networkEnabled ? true,
        }:
        let
          pkgs = import nixpkgs { inherit system; };

          # Parse Docker-style port specs: "port", "hostPort:guestPort", or "hostAddr:hostPort:guestPort"
          parsePortSpec =
            spec:
            let
              parts = nixpkgs.lib.splitString ":" spec;
              numParts = builtins.length parts;
            in
            if numParts == 1 then
              {
                hostAddress = "127.0.0.1";
                hostPort = nixpkgs.lib.toInt (builtins.elemAt parts 0);
                guestPort = nixpkgs.lib.toInt (builtins.elemAt parts 0);
              }
            else if numParts == 2 then
              {
                hostAddress = "127.0.0.1";
                hostPort = nixpkgs.lib.toInt (builtins.elemAt parts 0);
                guestPort = nixpkgs.lib.toInt (builtins.elemAt parts 1);
              }
            else if numParts == 3 then
              {
                hostAddress = builtins.elemAt parts 0;
                hostPort = nixpkgs.lib.toInt (builtins.elemAt parts 1);
                guestPort = nixpkgs.lib.toInt (builtins.elemAt parts 2);
              }
            else
              throw "Invalid port spec '${spec}': expected 'port', 'hostPort:guestPort', or 'hostAddr:hostPort:guestPort'";

          parsedPorts = map parsePortSpec forwardPorts;

          # Generate VirtioFS share configs from virtiofsShares parameter
          mkVirtiofsShare = idx: share: {
            proto = "virtiofs";
            tag = "share-${toString idx}";
            source = share.source;
            mountPoint = share.target;
            socket = "${socketDir}/virtiofs-share-${toString idx}.sock";
            readOnly = share.readOnly;
          };

          additionalShares = nixpkgs.lib.imap0 mkVirtiofsShare virtiofsShares;

          # Build the NixOS configuration
          nixosConfig = nixpkgs.lib.nixosSystem {
            inherit system;
            modules = [
              microvm.nixosModules.microvm
              (
                { pkgs, lib, ... }:
                {
                  networking.hostName = "my-microvm";
                  users.users.root.password = "";

                  # Create a proper user matching the host
                  users.users.${userName} = {
                    isNormalUser = true;
                    home = userHome;
                    createHome = true;
                    password = "";
                    uid = userUid;
                    group = userName;
                    extraGroups = [ "wheel" ];
                  };

                  # Create matching group with same GID as host
                  users.groups.${userName} = {
                    gid = userGid;
                  };

                  # Enable auto-login on serial console (ttyS0)
                  # This is safe as it's only accessible via direct VM console, not network
                  services.getty.autologinUser = userName;

                  # Override getty to shutdown on exit instead of respawning
                  systemd.services."serial-getty@ttyS0" = {
                    serviceConfig = {
                      Restart = lib.mkForce "no";
                      ExecStopPost = "${pkgs.systemd}/bin/systemctl poweroff";
                    };
                  };

                  # Ensure SSH is disabled to prevent unauthenticated network access
                  # services.openssh.enable = false;

                  # Allow normal users to poweroff/reboot via polkit
                  security.polkit.enable = true;
                  security.polkit.extraConfig = ''
                    polkit.addRule(function(action, subject) {
                      if ((action.id == "org.freedesktop.login1.power-off" ||
                           action.id == "org.freedesktop.login1.reboot") &&
                          subject.isInGroup("wheel")) {
                        return polkit.Result.YES;
                      }
                    });
                  '';

                  environment.systemPackages = (
                    map (pkgName: lib.getAttrFromPath (lib.splitString "." pkgName) pkgs) extraPackages
                  );

                  # Enable Nix to allow nix-shell and other Nix commands
                  nix.enable = true;
                  nix.settings.experimental-features = [
                    "nix-command"
                    "flakes"
                  ];

                  # Create .profile file with environment variables and chdir
                  systemd.tmpfiles.rules =
                    let
                      envExports = lib.concatStringsSep "\n" (
                        lib.mapAttrsToList (name: value: "export ${name}=${lib.escapeShellArg value}") environmentVariables
                      );
                      chdirCmd = lib.optionalString (chdir != null) "cd ${lib.escapeShellArg chdir}";
                      profileFile = pkgs.writeText "user-profile" ''
                        ${envExports}
                        ${chdirCmd}
                      '';
                    in
                    [
                      # Link the profile file to user's home directory
                      "L+ ${userHome}/.profile - - - - ${profileFile}"
                    ];

                  # Open firewall for forwarded ports (only when network is enabled)
                  networking.firewall.allowedTCPPorts = lib.optionals networkEnabled (
                    map (p: p.guestPort) parsedPorts
                  );

                  microvm = {
                    # Enable writable overlay for /nix/store to allow nix-shell and package installation
                    writableStoreOverlay = "/nix/.rw-store";

                    # Allocate sufficient memory for Nix operations (in MB)
                    mem = 2048;
                    # vcpu = 4; # 4 virtual CPUs

                    shares = [
                      {
                        proto = "virtiofs";
                        tag = "ro-store";
                        # a host's /nix/store will be picked up so that no squashfs/erofs will be built for it.
                        source = "/nix/store";
                        mountPoint = "/nix/.ro-store";
                        socket = "${socketDir}/virtiofs-ro-store.sock";
                      }
                    ]
                    ++ additionalShares;

                    interfaces = lib.optionals networkEnabled [
                      {
                        type = "user";
                        id = "qemu";
                        mac = "02:00:00:01:01:01";
                      }
                    ];

                    forwardPorts = lib.optionals networkEnabled (
                      map (p: {
                        from = "host";
                        host.address = p.hostAddress;
                        host.port = p.hostPort;
                        guest.port = p.guestPort;
                      }) parsedPorts
                    );

                    # "qemu" has 9p built-in!
                    hypervisor = "qemu";
                    socket = "${socketDir}/control.socket";
                  };
                }
              )
            ];
          };

          runner = nixosConfig.config.microvm.declaredRunner;

          # Custom virtiofsd wrapper that uses socketDir for all files
          virtiofsdWrapper = pkgs.writeShellApplication {
            name = "virtiofsd-wrapper";
            runtimeInputs = with pkgs.python3Packages; [
              supervisor
              pkgs.virtiofsd
            ];
            text =
              let
                virtiofsShares = builtins.filter (
                  share: share.proto == "virtiofs"
                ) nixosConfig.config.microvm.shares;
                supervisordConfig = pkgs.writeText "supervisord.conf" (
                  pkgs.lib.generators.toINI { } (
                    {
                      supervisord = {
                        nodaemon = true;
                        logfile = "${socketDir}/supervisord.log";
                        pidfile = "${socketDir}/supervisord.pid";
                        childlogdir = "${socketDir}";
                        directory = "${socketDir}";
                      };
                    }
                    // builtins.listToAttrs (
                      map (share: {
                        name = "program:virtiofsd-${share.tag}";
                        value = {
                          stderr_syslog = true;
                          stdout_syslog = true;
                          autorestart = true;
                          directory = "${socketDir}";
                          command = "${pkgs.virtiofsd}/bin/virtiofsd --socket-path=${share.socket} --shared-dir=${share.source} --thread-pool-size 1 --posix-acl --xattr ${
                            if share.readOnly or false then "--readonly" else ""
                          }";
                        };
                      }) virtiofsShares
                    )
                  )
                );
              in
              ''
                set -euo pipefail

                # Ensure socket directory exists
                mkdir -p "${socketDir}"

                # Start supervisord with our custom config
                exec supervisord --configuration ${supervisordConfig}
              '';
          };

          # Wrapper script that starts virtiofsd before qemu
          wrappedRunner = pkgs.writeShellApplication {
            name = "microvm-with-virtiofs";
            runtimeInputs = [
              runner
              virtiofsdWrapper
            ];
            text = ''
              set -euo pipefail

              # Start virtiofsd in the background
              echo "Starting virtiofsd..."
              virtiofsd-wrapper &
              VIRTIOFSD_PID=$!

              # Function to cleanup virtiofsd on exit
              cleanup() {
                echo "Stopping virtiofsd..."
                if kill -0 "$VIRTIOFSD_PID" 2>/dev/null; then
                  kill "$VIRTIOFSD_PID" 2>/dev/null || true
                  wait "$VIRTIOFSD_PID" 2>/dev/null || true
                fi
              }
              trap cleanup EXIT INT TERM

              # Try to start microvm with retries for connection failures
              echo "Starting MicroVM..."
              max_retries=10
              retry_delay=1

              for attempt in $(seq 1 $max_retries); do
                if microvm-run; then
                  # VM exited cleanly (normal shutdown)
                  exit 0
                else
                  exit_code=$?
                  if [ "$attempt" -lt "$max_retries" ]; then
                    echo "MicroVM failed to start (exit code $exit_code), retrying in ''${retry_delay}s... (attempt $attempt/$max_retries)"
                    sleep "$retry_delay"
                  else
                    echo "MicroVM failed to start after $max_retries attempts"
                    exit "$exit_code"
                  fi
                fi
              done

              # Cleanup will happen automatically via trap
            '';
          };
        in
        {
          nixosConfiguration = nixosConfig;
          runner = runner;
          wrappedRunner = wrappedRunner;
        };
    };
}
