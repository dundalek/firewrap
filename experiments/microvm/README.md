
Motivation:

- containers big attack surface
- CLI agents don't have that much system integration, might work fine in VMs for additional security

Great summary is [The Case Against Containers](https://microvm-nix.github.io/microvm.nix/#the-case-against-containers) from microvm.nix docs: 

> Linux containers are not a single technology but a plethora of kernel features that serve to isolate various system resources so that the running system appears as one. It is still one shared Linux kernel with a huge attack surface.

But traditional VMs come with overhead and slow boots.
As an alternative MicroVMs are more light-weight.

The [microvm.nix](https://github.com/microvm-nix/microvm.nix) project cleverly wraps nix to conveniently start a microvm with defined environment. Could it be used to start a microvm that would appear similar to sandboxed shell?

Currently using QEMU backend as it is most featured with virtiofsd for directory sharing, but boots slowly in around 15s.

Alternative hypervisors to try besides QEMU (booting in 5s):
- cloud-supervisor - to enable network needs extra setup on host
- firecracker - does not seem to support virtiofs (based on comparison table in readme)

## Usage

Start using the wrapper script:

```bash
# Start with default packages
nix run

# Or using the runner script
nix run .#my-microvm

# Start with additional packages (like nix-shell -p)
./run-microvm.sh -p git vim htop

# Start with nested packages
./run-microvm.sh -p python3Packages.numpy python3Packages.pandas

# Specify project directory
./run-microvm.sh -p git vim /path/to/project

# Or specify project directory first
./run-microvm.sh /path/to/project -p git vim
```

Shutdown inside microvm:

```
poweroff
```

Shutdown QEMU from outside:

```sh
echo '{"execute": "qmp_capabilities"}{"execute": "system_powerdown"}' | socat - UNIX-CONNECT:./control.socket
```

Stopping Firecracker from outside:

```sh
 curl --unix-socket firecracker-microvm.sock -i \
    -X PUT 'http://localhost/actions' \
    -H 'Accept: application/json' \
    -H 'Content-Type: application/json' \
    -d '{
      "action_type": "SendCtrlAltDel"
    }'
```

Starting virtiofsd separately for debugging:

```
nix shell .#my-microvm -c virtiofsd-run

nix run .#virtiofsd
```

## Notes

- Directory sharing
	- was not able to write to shared directories using 9p, using virtiofs instead
- Sandboxing Claude Code
  - virtiofsd sharing seems to only work when sharing directories, problem is that Claude needs writable file at `~/.claude.json`. We obviously don't want to share whole HOME.
  - As a workaround on host put it inside `~/.claude/` and symnlink it.
    - `mv ~/.claude.json ~/.claude/config.json && ln -s ~/.claude/config.json ~/.claude.json`
  - We then share `~/.claude/` directory and also create the symlink inside microvm.
  - There is an [issue](https://github.com/anthropics/claude-code/issues/8939) for claude code to put config .claude.json into a single directory that would make the workaround unnecessary.
- Can run `nix-shell` inside with writeable overlay
- Auto-login enabled for serial console only accessible via direct VM console (SSH should be disabled)

Boot time measurements (second run with dependencies fetched in nix store):
- `nix run microvm#qemu-example` 15.4s
- `nix run microvm#firecracker-example` 4.9s
- `nix run microvm#cloud-hypervisor-example` 4.8s
- `nix run microvm#crosvm-example` fails to start
