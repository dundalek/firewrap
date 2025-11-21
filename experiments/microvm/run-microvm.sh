#!/usr/bin/env bash
set -euo pipefail

# Parse command line arguments
EXTRA_PACKAGES=()
PUBLISH_PORTS=()
PROJECT_DIR=""

while [[ $# -gt 0 ]]; do
  case $1 in
    --publish)
      PUBLISH_PORTS+=("$2")
      shift 2
      ;;
    -p|--packages)
      shift
      # Collect all package names until we hit another flag or the project dir
      while [[ $# -gt 0 ]]; do
        # Stop if we hit another flag
        if [[ "$1" =~ ^- ]]; then
          break
        fi
        # Check if this looks like a path (contains /)
        if [[ "$1" =~ / ]]; then
          PROJECT_DIR="$1"
          shift
          break
        else
          EXTRA_PACKAGES+=("$1")
          shift
        fi
      done
      ;;
    *)
      PROJECT_DIR="$1"
      shift
      ;;
  esac
done

# Detect user information from environment
USER_NAME="${USER:-$(whoami)}"
USER_HOME="${HOME}"
USER_UID="$(id -u)"
USER_GID="$(id -g)"

# Get project directory from argument or use current directory
PROJECT_DIR="${PROJECT_DIR:-$(pwd)}"

# Resolve to absolute path
PROJECT_DIR="$(cd "$PROJECT_DIR" && pwd)"

# Optional: Claude config directory (defaults to $HOME/.claude)
CLAUDE_CONFIG_DIR="${USER_HOME}/.claude"

# Get the directory where this script is located, resolving symlinks
SCRIPT_DIR="$(dirname "$(realpath "${BASH_SOURCE[0]}")")"

# Generate a unique instance ID for this VM (timestamp + random suffix)
INSTANCE_ID="$(date +%s)-$(head -c 4 /dev/urandom | xxd -p)"

# Socket directory - unique per instance
SOCKET_DIR="/tmp/microvm-${INSTANCE_ID}"
mkdir -p "$SOCKET_DIR"

echo "Starting MicroVM with configuration:"
echo "  Project Directory: $PROJECT_DIR"
echo "  User: $USER_NAME (UID: $USER_UID, GID: $USER_GID)"
echo "  Home: $USER_HOME"
echo "  Claude Config: $CLAUDE_CONFIG_DIR"
echo "  Socket Directory: $SOCKET_DIR"
echo "  Instance ID: $INSTANCE_ID"
if [ "${#EXTRA_PACKAGES[@]}" -gt 0 ]; then
  echo "  Extra Packages: ${EXTRA_PACKAGES[*]}"
fi
if [ "${#PUBLISH_PORTS[@]}" -gt 0 ]; then
  echo "  Published Ports: ${PUBLISH_PORTS[*]}"
fi
echo ""

# Cleanup socket directory on exit
cleanup() {
  echo "Cleaning up socket directory: $SOCKET_DIR"
  rm -rf "$SOCKET_DIR"
}
trap cleanup EXIT INT TERM

# Build and run the VM using nix with the parameterized function
# Note: --impure is required to use builtins.getFlake with path references
# The flake itself remains pure - we're just passing runtime parameters

# Convert bash arrays to Nix list syntax
NIX_PACKAGES_LIST="["
for pkg in "${EXTRA_PACKAGES[@]}"; do
  NIX_PACKAGES_LIST+=" \"$pkg\""
done
NIX_PACKAGES_LIST+=" ]"

NIX_PORTS_LIST="["
for port in "${PUBLISH_PORTS[@]}"; do
  NIX_PORTS_LIST+=" \"$port\""
done
NIX_PORTS_LIST+=" ]"

nix run --impure --expr "
  let
    flake = builtins.getFlake \"git+file://${SCRIPT_DIR}\";
    pkgs = flake.inputs.nixpkgs.legacyPackages.x86_64-linux;
    extraPackagesList = ${NIX_PACKAGES_LIST};
    forwardPortsList = ${NIX_PORTS_LIST};
    vmConfig = flake.lib.mkMicroVM {
      projectDir = \"${PROJECT_DIR}\";
      userName = \"${USER_NAME}\";
      userHome = \"${USER_HOME}\";
      userUid = ${USER_UID};
      userGid = ${USER_GID};
      claudeConfigDir = \"${CLAUDE_CONFIG_DIR}\";
      socketDir = \"${SOCKET_DIR}\";
      extraPackages = extraPackagesList;
      forwardPorts = forwardPortsList;
    };
  in
    vmConfig.wrappedRunner
"
