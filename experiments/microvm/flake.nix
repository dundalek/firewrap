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
      # All data is pre-computed by Clojure - this function just passes it through
      lib.mkMicroVM =
        {
          userName,
          userHome,
          userUid,
          userGid,
          socketDirBase,
          extraPackages ? [ ],
          # Pre-parsed port forwarding: [{ from, host = { address, port }, guest = { port } }]
          forwardPorts ? [ ],
          # Pre-computed firewall ports: [ guestPort1, guestPort2, ... ]
          firewallPorts ? [ ],
          # Pre-computed VirtioFS shares: [{ proto, tag, source, mountPoint, readOnly }]
          virtiofsShares ? [ ],
          # Pre-computed .profile content as a string
          profileContent ? "",
          # Enable/disable network (user-mode networking)
          networkEnabled ? true,
        }:
        let
          pkgs = import nixpkgs { inherit system; };

          # Generate a placeholder socket dir for build-time configuration.
          # The actual socket dir is created at runtime with a unique ID.
          socketDirPlaceholder = "@@SOCKET_DIR@@";

          # Build the NixOS configuration with placeholder socket dir
          mkNixosConfig = socketDir: nixpkgs.lib.nixosSystem {
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

                  # Link pre-computed .profile content to user's home directory
                  systemd.tmpfiles.rules =
                    let
                      profileFile = pkgs.writeText "user-profile" profileContent;
                    in
                    [
                      "L+ ${userHome}/.profile - - - - ${profileFile}"
                    ];

                  # Open firewall for forwarded ports (only when network is enabled)
                  networking.firewall.allowedTCPPorts = lib.optionals networkEnabled firewallPorts;

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
                    ] ++ (map (s: s // { socket = "${socketDir}/virtiofs-${s.tag}.sock"; }) virtiofsShares);

                    interfaces = lib.optionals networkEnabled [
                      {
                        type = "user";
                        id = "qemu";
                        mac = "02:00:00:01:01:01";
                      }
                    ];

                    # Use pre-computed forwardPorts directly from Clojure
                    forwardPorts = lib.optionals networkEnabled forwardPorts;

                    # "qemu" has 9p built-in!
                    hypervisor = "qemu";
                    socket = "${socketDir}/control.socket";
                  };
                }
              )
            ];
          };

          # Build config with placeholder for generating scripts
          nixosConfigTemplate = mkNixosConfig socketDirPlaceholder;
          runnerTemplate = nixosConfigTemplate.config.microvm.declaredRunner;

          # Get share info for virtiofsd configuration
          sharesTemplate = builtins.filter (s: s.proto == "virtiofs") nixosConfigTemplate.config.microvm.shares;

          # Wrapper script that creates unique socket dir at runtime and starts everything
          wrappedRunner = pkgs.writeShellApplication {
            name = "microvm-with-virtiofs";
            runtimeInputs = with pkgs; [
              coreutils
              gnused
              python3Packages.supervisor
              virtiofsd
              qemu_kvm
            ];
            text = ''
              set -euo pipefail

              # Generate unique socket directory at runtime
              SOCKET_DIR=$(mktemp -d "${socketDirBase}/microvm.XXXXXX")
              echo "Using socket directory: $SOCKET_DIR"

              # Function to cleanup on exit
              cleanup() {
                echo "Cleaning up..."
                if [ -n "''${VIRTIOFSD_PID:-}" ] && kill -0 "$VIRTIOFSD_PID" 2>/dev/null; then
                  echo "Stopping virtiofsd..."
                  kill "$VIRTIOFSD_PID" 2>/dev/null || true
                  wait "$VIRTIOFSD_PID" 2>/dev/null || true
                fi
                if [ -d "$SOCKET_DIR" ]; then
                  echo "Removing socket directory: $SOCKET_DIR"
                  rm -rf "$SOCKET_DIR"
                fi
              }
              trap cleanup EXIT INT TERM

              # Generate supervisord config at runtime with actual socket paths
              SUPERVISORD_CONF="$SOCKET_DIR/supervisord.conf"
              cat > "$SUPERVISORD_CONF" << 'SUPERVISORD_EOF'
[supervisord]
nodaemon=true
logfile=%(ENV_SOCKET_DIR)s/supervisord.log
pidfile=%(ENV_SOCKET_DIR)s/supervisord.pid
childlogdir=%(ENV_SOCKET_DIR)s
directory=%(ENV_SOCKET_DIR)s

${builtins.concatStringsSep "\n" (map (share: ''
[program:virtiofsd-${share.tag}]
stderr_syslog=true
stdout_syslog=true
autorestart=true
directory=%(ENV_SOCKET_DIR)s
command=${pkgs.virtiofsd}/bin/virtiofsd --socket-path=%(ENV_SOCKET_DIR)s/virtiofs-${share.tag}.sock --shared-dir=${share.source} --thread-pool-size 1 --posix-acl --xattr ${if share.readOnly or false then "--readonly" else ""}
'') sharesTemplate)}
SUPERVISORD_EOF

              # Start virtiofsd via supervisord
              echo "Starting virtiofsd..."
              export SOCKET_DIR
              supervisord --configuration "$SUPERVISORD_CONF" &
              VIRTIOFSD_PID=$!

              # Generate runner script with actual socket paths by substituting placeholder
              RUNNER_SCRIPT="$SOCKET_DIR/microvm-run.sh"
              sed "s|${socketDirPlaceholder}|$SOCKET_DIR|g" "${runnerTemplate}/bin/microvm-run" > "$RUNNER_SCRIPT"
              chmod +x "$RUNNER_SCRIPT"

              # Try to start microvm with retries for connection failures
              echo "Starting MicroVM..."
              max_retries=10
              retry_delay=1

              for attempt in $(seq 1 $max_retries); do
                if "$RUNNER_SCRIPT"; then
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
            '';
          };
        in
        {
          nixosConfiguration = nixosConfigTemplate;
          runner = runnerTemplate;
          wrappedRunner = wrappedRunner;
        };
    };
}
