# Firewrap

Firewrap is a tool to run programs in an isolated runtime environment using a container type sandbox.

It is similar to [Firejail](https://github.com/netblue30/firejail), but based on [Bubblewrap](https://github.com/containers/bubblewrap).

> ⚠️ **Disclaimer**:  
This is an experimental and incomplete project.  
Always verify the source before running anything.

## Install

Dependencies:
- [Babashka](https://github.com/babashka/babashka)
- [Bubblewrap](https://github.com/containers/bubblewrap)
- Posix shell (optional for appimage wrapper)

```
git clone https://github.com/dundalek/firewrap.git
cd firewrap
bin/firewrap
```

TODO using [bbin](https://github.com/babashka/bbin)

## Getting started

To preview sandbox parameters when wanting to run a `cmd`:

```
firewrap -b --dry-run -- cmd
```

It is a good practice to start with `--dry-run` which prints sandbox parameters without executing anything.

By default the sandbox has minimal privileges and most programs would not be able to run.
Therefore we use `-b` option for `--base` which is a preset which includes a reasonable base files but excludes home directory.

To actually run the command:

```
firewrap -b -- cmd
```

## Philosophy and Design

#### Full lisp-based programming language for specifying rules

- Flexibility in creating composable abstractions
  - Similar to Infrastructure-as-Code principles but for security.
- Production grade tooling and IDE integration
  - Completion, Signature help, Go to definition, References, Refactoring

#### The Principle of Least Privilege

- Process should have the lowest level of privilege needed to accomplish its task.
- Default Deny as a baseline: Everything is forbidden unless explicitly allowed.
- Sometimes hard to achieve in practice.
  - Be pragmatic to allow starting with wider sandboxes,
    but have mechanisms to show warnings as a reminder to nudge to tighten as future improvements.

#### Tooling (ideas)

Creating sandbox profiles takes a lot of effort.
A good sandboxing solution should provide tools to assist with the effort.
Worfklow to create a profile is an iteration loop of:

1. Record trace
    - Log program behavior inside an isolated environment like a VM using strace.
    - There is an initial [strace parsing](src/firewrap/tool/strace.clj) prototype.
2. Map to abstractions composed out of presets
    - Using generated rules based on a trace as-is would make it unmanageable to have a confidence that sandbox is secure and does not contain extraneous rules.
    - Composing the rules using higher-level presents makes it possible to reason about security of the sandbox.
3. Audit rules
    - To make iteration and testing easier a tool could provide static analysis and report if rules are too tight or too loose. And only test the sandbox end-to-end by running it once static issues resolved.
    - Possible sandbox rules problems:
      - too tight - breaks program functionality
      - too loose - exposes unnecessary resources, violates principle of least privilege

## Concepts

- Presets - Are reusable pieces that define security policy.
- Profiles - Define sandbox environment for an application and are automatically used if application name matches, usually use one or more presets.
- Levels (idea) - It is usually hard to create minimal profile that does not break any functionality.
  - The idea is to have common levels that add privileges.
  Then based on risk one can:
    - Start with tightest profile, observe broken program and increase level by trial-and-error until the program works.
    - Or start with a wider profile and working program, and tighten level just until before the program stops working.
  - For practical purposes have an easy to use best-effort base, which does not provide much extra security, but is still useful for the most common case of isolating user data.

## Usage

```
Run program in sandbox

Usage: firewrap [<options> --] <command> [<args>]

Options:
  --profile <profile>
  --dry-run           Only print bubblewrap arguments but don't execute
  --help              Show help

Ad-hoc profile options:
  -b, --base
  -g, --gui
  -h, --home
  -t, --tmphome
  -c, --cwd
  -n, --net
```

For convenience `firewrap` can be aliased to `fw`.

#### Automatic profiles

Builtin app profiles are applied automatically based on a command name (case insensitive lookup).

For example to apply profile from `nexus.profile.windsurf` namespace:

```
firewrap Windsurf
````

is same as:

```
firewrap --profile windsurf -- Windsurf
```

TODO make the profile registry opt-in?

#### Aliasing with symlinks

For convenience it is possible to create symlinks to alias commands, for example:

```
ln -s /path/to/firewrap ~/bin/windsurf
```

Then just run simple command and matching sandbox profile will be applied:
```
windsurf
```

It is possible to use profile options to add additional privileges (`-c` is treated as option to firewrap, `.` is an argument for windsurf):

```
windsurf -c -- .
```

For now installation/management of symlinks needs to be done manually. In the future perhaps there could be a tool similar to Firejail's `firecfg`.

#### Ad-hoc profile options

There is a set of opinionated options available to run ad-hoc profile conveniently from the command line, without needing to create a proper profile first.

Usually, start with the base `-b` or `--base` options, then specify additional privileges to add.

Example of fairly wide set of privileges is `fw -bcnh -- appname`,  
which is equivalent to `fw --base --cwd --network --home -- appname`.

#### Private home

The default base uses an ephemeral tmpfs is as home directory inside a sandbox.
Use `--home` or `-h` for isolated home for a given app.
This is also useful to avoid home being polluted by additional config files and removing all traces of an app is done by deleting a single folder.

Following will use `~/sandboxes/appname` as home.

```
firewrap -b --home appname -- appname
```

When no argument is passed for home, the appname is inferred. Following is the same as above:

```
firewrap -bh -- appname
```

Use `--tmphome` or `-t` for persistent temporary home, this will create `~/sandboxes/tmp-<current-date-time>`. This is useful for having a temporary home, but it is preserved even after app finishes.

```
firewrap -bh -- appname
```

#### Appimages

When a command ends with `.appimage` (case insensitive comparison) it will be executed using a special appimage handler.  
*(Mounts that appimages rely on are not allowed in sandbox, therefore contents is first extracted and then executed.)*

```
fw -b -- SomeApp.AppImage
```

#### User config

TODO

#### Two-step execution

This is a pattern that separates usage of a program into two steps to protect user data: install and execute.
- During the first install step, there is access to internet, but not to user data.
- During the second execute step, we allow access to user data, but remove network permission so that data can't potentially leave the device.

As an example using [pdfannots](https://github.com/0xabu/pdfannots) script to extract highlights from a potentially sensitive PDF.
Create a working directory including only the PDF that needs to be extracted.

1) Install (or fetch to cache), allow internet with `-n` flag:
```
fw -bn --home pdfannots -- uvx pdfannots
```

2) Execute, we add user data by including current folder with `-c`, but there is no `-n` for network:
```
fw -bc --home pdfannots -- uvx pdfannots my.pdf
```

Note we pass `--home` to use the same home sandbox folder, so that cached files can be shared between runs.
Also make sure not to run the program with network privilege again after working with data.
Instead use a different home sandbox folder or first wipe the sandbox data.

#### Limitations

Fish shell does not run inside sandbox. When trying to run it:

`firewrap -b -- fish`

it fails with:
```
warning: No TTY for interactive shell (tcgetpgrp failed)
setpgid: Inappropriate ioctl for device
```

As a workaround use bash for restricted shells:

`firewrap -b -- bash`

## Implemented mechanisms

Implemented:

- [x] Basic containment using Bubblewrap
- [x] Environment variables restriction

Unimplemented:

- [ ] D-Bus filtering using xdg-dbus-proxy
- [ ] Syscall filtering using seccomp
- [ ] XDG Portals
- [ ] Network filtering
- [ ] ...

## Related projects

- SELinux and AppArmor Linux Security Modules (LSM) systems
  - Difficult for an end-user to configure.
- Containers like Docker, Podman
  - Complected packaging, distribution and containment.
  - I would like to have security independent of distribution.
- Flatpak, Snap
  - Complected distribution and containment.
  - Vendor provided security policies incentivize to make them too loose.
- Firejail
  - Larger attack surface, needs SUID.
  - Profiles often use Default Allow instead of Default Deny principle.
