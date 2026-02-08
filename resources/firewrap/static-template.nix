# Static MicroVM configuration
#
# This file contains the MicroVM builder and is user-editable.
# It will NOT be overwritten when you re-export the flake.
#
# Customize settings like mem, vcpu, packages in the microvm section below.

{ nixpkgs, microvm }:

{
  userName,
  userHome,
  userUid,
  userGid,
  socketDirBase,
  extraPackages ? [ ],
  forwardPorts ? [ ],
  firewallPorts ? [ ],
  virtiofsShares ? [ ],
  profileContent ? "",
  networkEnabled ? false,
  ...
}:

let
  system = "x86_64-linux";
  pkgs = import nixpkgs { inherit system; };

  # Generate a placeholder socket dir for build-time configuration.
  # The actual socket dir is created at runtime with a unique ID.
  socketDirPlaceholder = "@@SOCKET_DIR@@";

  mkNixosConfig =
    socketDir:
    nixpkgs.lib.nixosSystem {
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

            environment.systemPackages = map (
              pkgName: lib.getAttrFromPath (lib.splitString "." pkgName) pkgs
            ) extraPackages;

            # Enable Nix to allow nix-shell and other Nix commands
            nix.enable = true;
            nix.settings.experimental-features = [
              "nix-command"
              "flakes"
            ];

            systemd.tmpfiles.rules =
              let
                profileFile = pkgs.writeText "user-profile" profileContent;
              in
              [ "L+ ${userHome}/.profile - - - - ${profileFile}" ];

            # Open firewall for forwarded ports (only when network is enabled)
            networking.firewall.allowedTCPPorts = lib.optionals networkEnabled firewallPorts;

            # =====================================================================
            # USER CUSTOMIZATION - Edit these values as needed
            # =====================================================================
            microvm = {
              # Enable writable overlay for /nix/store to allow nix-shell and package installation
              writableStoreOverlay = "/nix/.rw-store";

              # Memory in MB
              mem = 2047;

              # Number of virtual CPUs (uncomment to change)
              # vcpu = 4;

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
              ++ (map (s: s // { socket = "${socketDir}/virtiofs-${s.tag}.sock"; }) virtiofsShares);

              interfaces = lib.optionals networkEnabled [
                {
                  type = "user";
                  id = "qemu";
                  mac = "02:00:00:01:01:01";
                }
              ];

              forwardPorts = lib.optionals networkEnabled forwardPorts;

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
  sharesTemplate = builtins.filter (
    s: s.proto == "virtiofs"
  ) nixosConfigTemplate.config.microvm.shares;

  # Wrapper script that creates unique socket dir at runtime and starts everything
  wrappedRunner = pkgs.writeShellApplication {
    name = "microvm-with-virtiofs";
    runtimeInputs = with pkgs; [
      gnused
      python3Packages.supervisor
      virtiofsd
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

      ${builtins.concatStringsSep "\n" (
        map (share: ''
          [program:virtiofsd-${share.tag}]
          stderr_syslog=true
          stdout_syslog=true
          autorestart=true
          directory=%(ENV_SOCKET_DIR)s
          command=${pkgs.virtiofsd}/bin/virtiofsd --socket-path=%(ENV_SOCKET_DIR)s/virtiofs-${share.tag}.sock --shared-dir=${share.source} --thread-pool-size 1 --posix-acl --xattr ${
            if share.readOnly or false then "--readonly" else ""
          }
        '') sharesTemplate
      )}
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
  packages.${system}.default = wrappedRunner;
}
