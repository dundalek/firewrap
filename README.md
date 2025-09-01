# Firewrap

Firewrap is a tool to run programs in an isolated runtime environment using a container-type sandbox.

It is similar to [Firejail](https://github.com/netblue30/firejail), but based on [Bubblewrap](https://github.com/containers/bubblewrap).

Motivation: There will be a lot of wasted effort creating wrappers and middlewares for AI agent tool use.
What we need is first-class security mechanisms in programming languages and operating systems.
In the end AIs will just execute commands and call APIs with given authority.

> ⚠️ **Disclaimer**:  
This is an experimental and incomplete project.  
There are many security mechanisms that are [not yet implemented](#implemented-mechanisms).  
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
- Secure by default
  - Default Deny as a baseline: Everything is forbidden unless explicitly allowed.
- Sometimes hard to achieve in practice.
  - Be pragmatic to allow starting with wider sandboxes,
    but have mechanisms to show warnings as a reminder to nudge to tighten in future iterations.

#### Tooling

Creating sandbox profiles takes a lot of effort.
A good sandboxing solution should provide tools to assist with the effort.
Worfklow to create a profile is an iteration loop of:

1. Record trace
    - Log program behavior inside an isolated environment like a VM using strace.
2. Map to abstractions composed out of presets
    - Using generated rules based on a trace as-is would make it unmanageable to have a confidence that sandbox is secure and does not contain extraneous rules.
    - For example tools like [minijail](https://www.chromium.org/chromium-os/developer-library/guides/development/sandboxing/) and [Sydbox Pandora](https://git.sr.ht/~alip/syd/tree/main/item/pandora/README.md) also use tracing to help create profiles.
	    However, the output can be overwhelming and some undesired access can be easily overlooked.
    - Composing the rules using higher-level presents makes it possible to reason about security of the sandbox.
      There is an experimental [Trace helper tool](#trace-helper-tool) that explores the approach.
3. Audit rules (idea)
    - To make iteration and testing easier a tool could provide static analysis and report if rules are too tight or too loose. And only test the sandbox end-to-end by running it once static issues resolved.
    - Possible sandbox rules problems:
      - too tight - breaks program functionality
      - too loose - exposes unnecessary resources, violates principle of least privilege

## Concepts

- [Presets](src/firewrap/preset) - Are reusable pieces that define security policy.
- [Profiles](src/firewrap/preset) - Define sandbox environment for an application and are automatically used if application name matches, usually use one or more presets.
- Levels - It is usually hard to create minimal profile that does not break any functionality.
  - The idea is to have common levels that add privileges, from tightest `-b0` to widest `-b9`.  
  Then based on risk one can:
    - Start with tightest profile, observe broken program and increase level by trial-and-error until the program works.
    - Or start with a wider profile and working program, and tighten level just until before the program stops working.
  - For practical purposes have an easy to use best-effort base, which does not provide much extra security, but is still useful for the most common case of isolating user data.

## Usage

<!-- FIREWRAP_HELP_BEGIN -->
```
Run program in sandbox

Usage: firewrap [<options> --] <command> [<args>]

Options:
  --profile        <profile>
  --dry-run                  Only print bubblewrap arguments but don't execute
  --unsafe-session           Don't use --new-session option for bubblewrap (less secure)
  --help                     Show help

Ad-hoc profile options:
  -b, --base
  -g, --gui
  -h, --home
  -t, --tmphome
  -c, --cwd
  -n, --net

Base levels (-b or --base is same as -b4):
  -b0  Base with basic bubblewrap flags, does not grant any resources
  -b4  More granular base with system files
  -b5  Low effort sandbox, includes system files with temporary home and empty tmp
  -b6  Low effort sandbox with GUI support, includes X11 display binding
  -b8  Lower effort wider sandbox, does not filter env vars and /tmp, should work better for GUI programs
  -b9  Simplest wide sandbox with device bind mount and temporary home

Sandbox options:
  --bind-ro   <src>:<dest>  Read-only bind mount <src>:<dest> or <path>
  --bind-rw   <src>:<dest>  Read-write bind mount <src>:<dest> or <path>
  --bind-dev  <src>:<dest>  Device bind mount <src>:<dest> or <path>
  --env-pass  <env>         Pass environment variable to sandbox
  --env-set   <var> <value> Set an environment variable
  --env-unset <var>         Unset an environment variable
```
<!-- /FIREWRAP_HELP_END -->

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
firewrap -bt -- appname
```

#### Appimages

When a command ends with `.appimage` (case insensitive comparison) it will be executed using a special appimage handler.  
*(Mounts that appimages rely on are not allowed in sandbox, therefore contents is first extracted and then executed.)*

```
fw -b -- SomeApp.AppImage
```

#### Auto-sandboxing using shell wrapper

When working on a project or trying out scripts from the internet,
we might want to run commands in a sandbox with reduced set of only necessary privileges.

One option is to run a shell in a sandbox and execute commands from it.
However, there are [limitations](#limitations) when running sandboxed shell.

Another experimental approach is a shell wrapper, that prefixes invoked commands with `firewrap` command to run them in sandbox. There is an experimental implementation [shell wrapper](./examples/firewrap_command_wrapper.fish) for fish that works in following way:

- If we type a command without prefix like `ls`, it will get prefixed like `fw -bc -- ls`. The default arguments are set using FIREWRAP_ARGS env variable.
- When a command already includes firewrap prefix, it will be passed as is. For example running in a sandbox with network `fw -bn -- ping 8.8.8.8` will execute it as is.

The challenge is security vs user experience, as different tasks need different priviledges.
It can be annoying to switch sandbox options, and tempting to just always run wide sandbox or disable sandbox completely.

An idea is to add shorthands for common uses in shell config. Here are some aliases/abbreviations for inspiration:

```fish
abbr -a fwe "set -gx FIREWRAP_ARGS -bc" # e - enable
abbr -a fwd "set -gx FIREWRAP_ARGS ''" # d - disable
abbr -a fwn "set -gx -bcn" # n - network
abbr -a fwc "set -gx FIREWRAP_ARGS '--profile cljdev'" # c - cljdev
```

It is also possible to use [direnv](https://direnv.net) to set custom FIREWRAP_ARGS to configure different sandboxes based on project directories.

#### User config

Config file is loaded from `firewrap/init.clj` in `$XDG_CONFIG_HOME` (by default `~/.confg/firewrap/init.clj`).
See the example [init.clj](./examples/init.clj).

Main uses are to register a custom profile function with `profile/register!` and hooking overrides using `alter-var-root`.

```clj
(ns init
  (:require
   [firewrap.preset.base :as base]
   [firewrap.profile :as profile]
   [firewrap.sandbox :as sb]))

;; Registering a custom profile function
(defn my-appname-profile [opts]
  (base/base5))

(profile/register! "appname" my-appname-profile)

;; Overriding existing functions as a hook
(defn my-bind-user-programs [ctx]
  (-> ctx
      (sb/bind-ro "/some/path/bin")))

(alter-var-root #'base/bind-user-programs
                (constantly my-bind-user-programs))
```

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

Another option is to use `--unsafe-session` option:

`firewrap -b --unsafe-session -- fish`

The `--unsafe-session` option makes the sandbox open to TIOCSTI ioctl attack.
However, it makes it less easier to forget to run a program under a sandbox by running the whole shell in sandbox.
So it can end up better when the risk profile is protection against mostly misconfigured programs rather then malicious programs.

## Trace helper tool

Capture a trace using strace (ideally on an isolated physical hardware or in a VM):

```
strace -f -o output.trace your-command
```

Tip: Set `XDG_` dirs to smaller number of candidates for less noisy trace:

```
export XDG_DATA_DIRS="$HOME/.local/share:/usr/share" XDG_CONFIG_DIRS="/etc/xdg"
```

We leverage [b3-strace-parser](https://github.com/dannykopping/b3) to parse strace file as JSON. 
Install it with:

```
npm install -g b3-strace-parser
```

Then use the `firehelper generate` command to generate a profile based on the captured trace.

```
cat output.trace | b3-strace-parser | bin/firehelper generate > profile/foo.clj
```

As you can see in the example output below,
the tool tries to match captured file paths to existing presets like `system/libs` `system/command`.

Remaining paths are then structured as a tree of `bind-ro-try` forms.
These can be left as is granularly binding leaf paths.
Alternatively, they can edited or extracted into higher-level presets.

```clj
(defn
 profile
 [_]
 (->
  (base/base)
  (system/libs)
  (->
   (system/nop system/bind-ro-try "/")
   (->
    (system/nop system/bind-ro-try "/etc")
    (-> (system/bind-ro-try "/etc/ld.so.preload"))))
  (-> (system/command "echo"))))
```

Now a smaller example will show why the forms are structured using nested threading `->` forms.
For example for input paths `/foo/a` and `/foo/b` we will get:

```clj
(->
 (system/nop system/bind-ro-try "/foo")
 (-> 
   (system/bind-ro-try "/foo/a")
   (system/bind-ro-try "/foo/b")))
```

Notice `system/nop` in the `/foo` form which makes the form to just pass through the context and ignore the binding function.
It is effectively same as following granular bindings (and we can remove the `nop` form manually when decided to use the granular approach):

```clj
(-> 
  (system/bind-ro-try "/foo/a")
  (system/bind-ro-try "/foo/b"))
```

The other option is to delete the whole tree under form and remove the `nop` form to apply the parent form.
This can be useful when nested paths are well scoped to the containing directory, for example when there are too many nested files or the filenames are dynamic/randomized.

```clj
(->
 (system/bind-ro-try "/foo"))
```

TODO consider using [sysdig](https://github.com/draios/sysdig) instead of `strace` for capturing traces since it includes builtin support to output JSON-formatted traces.

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

## Alternatives

- Linux Security Modules (LSM) systems like SELinux and AppArmor
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

### Other Bubblewrap-based projects

- [Bubblejail](https://github.com/igo95862/bubblejail)
  - Python, configuration in TOML, GUI configuration utility
- [Sandbubble](https://github.com/CauldronDevelopmentLLC/sandbubble)
  - Python, configuration in YAML
- [Bubblebox](https://github.com/RalfJung/bubblebox)
  - Python, configuration as Python scripts
