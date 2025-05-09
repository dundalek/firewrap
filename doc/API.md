# Table of contents
-  [`firewrap.main`](#firewrap.main) 
    -  [`*exec-fn*`](#firewrap.main/*exec-fn*)
    -  [`base-options`](#firewrap.main/base-options)
    -  [`bind-extra-system-programs`](#firewrap.main/bind-extra-system-programs)
    -  [`bind-nix-shell`](#firewrap.main/bind-nix-shell)
    -  [`bind-user-programs`](#firewrap.main/bind-user-programs)
    -  [`bwrap-args`](#firewrap.main/bwrap-args)
    -  [`cli-options`](#firewrap.main/cli-options)
    -  [`cli-spec`](#firewrap.main/cli-spec)
    -  [`escape-shell`](#firewrap.main/escape-shell)
    -  [`main`](#firewrap.main/main)
    -  [`needs-bwrap-sh-wrapper?`](#firewrap.main/needs-bwrap-sh-wrapper?)
    -  [`parse-args`](#firewrap.main/parse-args)
    -  [`print-help`](#firewrap.main/print-help)
    -  [`run-bwrap`](#firewrap.main/run-bwrap)
    -  [`run-bwrap-exec`](#firewrap.main/run-bwrap-exec)
    -  [`run-bwrap-sh-wrapper`](#firewrap.main/run-bwrap-sh-wrapper)
    -  [`unwrap-escaping`](#firewrap.main/unwrap-escaping)
    -  [`unwrap-raw`](#firewrap.main/unwrap-raw)
-  [`firewrap.preset.appimage`](#firewrap.preset.appimage) 
    -  [`appimage-command?`](#firewrap.preset.appimage/appimage-command?)
    -  [`run`](#firewrap.preset.appimage/run)
-  [`firewrap.preset.base`](#firewrap.preset.base) 
    -  [`base`](#firewrap.preset.base/base) - Base with basic bubblewrap flags, does not grant any resources.
    -  [`base-gui`](#firewrap.preset.base/base-gui)
    -  [`base4`](#firewrap.preset.base/base4) - More granular base with system files.
    -  [`base5`](#firewrap.preset.base/base5) - Low effort sandbox, incluedes system files with temporary home and empty tmp.
    -  [`base9`](#firewrap.preset.base/base9)
    -  [`bind-extra-system-programs`](#firewrap.preset.base/bind-extra-system-programs)
    -  [`bind-isolated-home-with-user-programs`](#firewrap.preset.base/bind-isolated-home-with-user-programs)
    -  [`bind-isolated-tmphome-with-user-programs`](#firewrap.preset.base/bind-isolated-tmphome-with-user-programs)
    -  [`bind-system-programs`](#firewrap.preset.base/bind-system-programs)
    -  [`bind-user-programs`](#firewrap.preset.base/bind-user-programs)
    -  [`configurable`](#firewrap.preset.base/configurable)
-  [`firewrap.preset.dumpster`](#firewrap.preset.dumpster) 
    -  [`bind-cwd-rw`](#firewrap.preset.dumpster/bind-cwd-rw)
    -  [`bind-isolated-home`](#firewrap.preset.dumpster/bind-isolated-home)
    -  [`bind-isolated-tmphome`](#firewrap.preset.dumpster/bind-isolated-tmphome)
    -  [`glob-one`](#firewrap.preset.dumpster/glob-one)
    -  [`home`](#firewrap.preset.dumpster/home)
    -  [`network`](#firewrap.preset.dumpster/network)
    -  [`path->appname`](#firewrap.preset.dumpster/path->appname)
-  [`firewrap.preset.env`](#firewrap.preset.env) 
    -  [`allowed`](#firewrap.preset.env/allowed)
    -  [`desktop-environment`](#firewrap.preset.env/desktop-environment)
    -  [`gpg-ssh`](#firewrap.preset.env/gpg-ssh)
    -  [`locales`](#firewrap.preset.env/locales)
    -  [`programming-languages`](#firewrap.preset.env/programming-languages)
    -  [`systemd`](#firewrap.preset.env/systemd)
    -  [`terminal`](#firewrap.preset.env/terminal)
-  [`firewrap.preset.oldprofiles`](#firewrap.preset.oldprofiles) 
    -  [`chatall`](#firewrap.preset.oldprofiles/chatall)
    -  [`cheese`](#firewrap.preset.oldprofiles/cheese)
    -  [`gedit`](#firewrap.preset.oldprofiles/gedit)
    -  [`gnome-calculator`](#firewrap.preset.oldprofiles/gnome-calculator)
    -  [`notify-send`](#firewrap.preset.oldprofiles/notify-send)
    -  [`xdg-open`](#firewrap.preset.oldprofiles/xdg-open)
-  [`firewrap.preset.oldsystem`](#firewrap.preset.oldsystem) 
    -  [`add-bwrap-args`](#firewrap.preset.oldsystem/add-bwrap-args)
    -  [`at-spi`](#firewrap.preset.oldsystem/at-spi)
    -  [`bind-dev`](#firewrap.preset.oldsystem/bind-dev)
    -  [`bind-dev-try`](#firewrap.preset.oldsystem/bind-dev-try)
    -  [`bind-ro`](#firewrap.preset.oldsystem/bind-ro)
    -  [`bind-ro-try`](#firewrap.preset.oldsystem/bind-ro-try)
    -  [`bind-ro-try-many`](#firewrap.preset.oldsystem/bind-ro-try-many)
    -  [`bind-rw`](#firewrap.preset.oldsystem/bind-rw)
    -  [`bind-rw-try`](#firewrap.preset.oldsystem/bind-rw-try)
    -  [`command`](#firewrap.preset.oldsystem/command)
    -  [`dbus-bus-path`](#firewrap.preset.oldsystem/dbus-bus-path)
    -  [`dbus-system-bus`](#firewrap.preset.oldsystem/dbus-system-bus)
    -  [`dbus-talk`](#firewrap.preset.oldsystem/dbus-talk)
    -  [`dbus-unrestricted`](#firewrap.preset.oldsystem/dbus-unrestricted)
    -  [`dconf`](#firewrap.preset.oldsystem/dconf)
    -  [`dev-null`](#firewrap.preset.oldsystem/dev-null)
    -  [`dev-urandom`](#firewrap.preset.oldsystem/dev-urandom)
    -  [`escape-shell`](#firewrap.preset.oldsystem/escape-shell)
    -  [`fontconfig`](#firewrap.preset.oldsystem/fontconfig)
    -  [`fontconfig-shared-cache`](#firewrap.preset.oldsystem/fontconfig-shared-cache)
    -  [`fonts`](#firewrap.preset.oldsystem/fonts)
    -  [`glib`](#firewrap.preset.oldsystem/glib)
    -  [`gpu`](#firewrap.preset.oldsystem/gpu)
    -  [`gtk`](#firewrap.preset.oldsystem/gtk)
    -  [`icons`](#firewrap.preset.oldsystem/icons)
    -  [`isolated-home`](#firewrap.preset.oldsystem/isolated-home)
    -  [`libs`](#firewrap.preset.oldsystem/libs)
    -  [`locale`](#firewrap.preset.oldsystem/locale)
    -  [`mime-cache`](#firewrap.preset.oldsystem/mime-cache)
    -  [`nop`](#firewrap.preset.oldsystem/nop)
    -  [`processes`](#firewrap.preset.oldsystem/processes)
    -  [`themes`](#firewrap.preset.oldsystem/themes)
    -  [`tmp`](#firewrap.preset.oldsystem/tmp)
    -  [`tmpfs`](#firewrap.preset.oldsystem/tmpfs)
    -  [`x11`](#firewrap.preset.oldsystem/x11)
    -  [`xdg-cache-home`](#firewrap.preset.oldsystem/xdg-cache-home)
    -  [`xdg-cache-home-path`](#firewrap.preset.oldsystem/xdg-cache-home-path)
    -  [`xdg-cache-home-paths`](#firewrap.preset.oldsystem/xdg-cache-home-paths)
    -  [`xdg-config-dir-paths`](#firewrap.preset.oldsystem/xdg-config-dir-paths)
    -  [`xdg-config-dirs-path`](#firewrap.preset.oldsystem/xdg-config-dirs-path)
    -  [`xdg-config-home`](#firewrap.preset.oldsystem/xdg-config-home)
    -  [`xdg-config-home-path`](#firewrap.preset.oldsystem/xdg-config-home-path)
    -  [`xdg-config-home-paths`](#firewrap.preset.oldsystem/xdg-config-home-paths)
    -  [`xdg-data-dir`](#firewrap.preset.oldsystem/xdg-data-dir)
    -  [`xdg-data-dir-paths`](#firewrap.preset.oldsystem/xdg-data-dir-paths)
    -  [`xdg-data-dirs-path`](#firewrap.preset.oldsystem/xdg-data-dirs-path)
    -  [`xdg-data-home`](#firewrap.preset.oldsystem/xdg-data-home)
    -  [`xdg-data-home-path`](#firewrap.preset.oldsystem/xdg-data-home-path)
    -  [`xdg-data-home-paths`](#firewrap.preset.oldsystem/xdg-data-home-paths)
    -  [`xdg-open`](#firewrap.preset.oldsystem/xdg-open)
    -  [`xdg-runtime-dir`](#firewrap.preset.oldsystem/xdg-runtime-dir)
    -  [`xdg-runtime-dir-path`](#firewrap.preset.oldsystem/xdg-runtime-dir-path)
    -  [`xdg-state-home`](#firewrap.preset.oldsystem/xdg-state-home)
    -  [`xdg-state-home-path`](#firewrap.preset.oldsystem/xdg-state-home-path)
    -  [`xdg-state-home-paths`](#firewrap.preset.oldsystem/xdg-state-home-paths)
-  [`firewrap.preset.vscode`](#firewrap.preset.vscode) 
    -  [`vscode-nvim`](#firewrap.preset.vscode/vscode-nvim)
-  [`firewrap.profile`](#firewrap.profile) 
    -  [`register!`](#firewrap.profile/register!)
    -  [`resolve`](#firewrap.profile/resolve)
    -  [`resolve-builtin-profile`](#firewrap.profile/resolve-builtin-profile)
-  [`firewrap.profile.bash`](#firewrap.profile.bash) 
    -  [`profile`](#firewrap.profile.bash/profile)
-  [`firewrap.profile.date`](#firewrap.profile.date) 
    -  [`profile`](#firewrap.profile.date/profile)
-  [`firewrap.profile.echo`](#firewrap.profile.echo) 
    -  [`profile`](#firewrap.profile.echo/profile)
-  [`firewrap.profile.ferdium`](#firewrap.profile.ferdium) 
    -  [`profile`](#firewrap.profile.ferdium/profile)
-  [`firewrap.profile.godmode`](#firewrap.profile.godmode) 
    -  [`profile`](#firewrap.profile.godmode/profile)
-  [`firewrap.profile.java`](#firewrap.profile.java) 
    -  [`profile`](#firewrap.profile.java/profile)
-  [`firewrap.profile.windsurf`](#firewrap.profile.windsurf) 
    -  [`profile`](#firewrap.profile.windsurf/profile)
-  [`firewrap.sandbox`](#firewrap.sandbox) 
    -  [`*populate-env!*`](#firewrap.sandbox/*populate-env!*)
    -  [`*run-effects!*`](#firewrap.sandbox/*run-effects!*)
    -  [`add-heredoc-args`](#firewrap.sandbox/add-heredoc-args)
    -  [`bind`](#firewrap.sandbox/bind)
    -  [`bind-data-ro`](#firewrap.sandbox/bind-data-ro)
    -  [`bind-dev`](#firewrap.sandbox/bind-dev)
    -  [`bind-dev-try`](#firewrap.sandbox/bind-dev-try)
    -  [`bind-ro`](#firewrap.sandbox/bind-ro)
    -  [`bind-ro-try`](#firewrap.sandbox/bind-ro-try)
    -  [`bind-rw`](#firewrap.sandbox/bind-rw)
    -  [`bind-rw-try`](#firewrap.sandbox/bind-rw-try)
    -  [`chdir`](#firewrap.sandbox/chdir)
    -  [`cmd-args`](#firewrap.sandbox/cmd-args)
    -  [`ctx->args`](#firewrap.sandbox/ctx->args)
    -  [`cwd`](#firewrap.sandbox/cwd)
    -  [`die-with-parent`](#firewrap.sandbox/die-with-parent)
    -  [`env-pass`](#firewrap.sandbox/env-pass)
    -  [`env-pass-many`](#firewrap.sandbox/env-pass-many)
    -  [`env-set`](#firewrap.sandbox/env-set)
    -  [`fx-create-dirs`](#firewrap.sandbox/fx-create-dirs)
    -  [`getenv`](#firewrap.sandbox/getenv)
    -  [`getenvs`](#firewrap.sandbox/getenvs)
    -  [`new-session`](#firewrap.sandbox/new-session)
    -  [`new-session-disable`](#firewrap.sandbox/new-session-disable)
    -  [`set-cmd-args`](#firewrap.sandbox/set-cmd-args)
    -  [`share-net`](#firewrap.sandbox/share-net)
    -  [`skip-own-symlink`](#firewrap.sandbox/skip-own-symlink)
    -  [`symlink`](#firewrap.sandbox/symlink)
    -  [`tmpfs`](#firewrap.sandbox/tmpfs)
    -  [`unsafe-escaped-arg`](#firewrap.sandbox/unsafe-escaped-arg)
    -  [`unshare-all`](#firewrap.sandbox/unshare-all)
-  [`firewrap.tool.strace`](#firewrap.tool.strace) 
    -  [`-main`](#firewrap.tool.strace/-main)
    -  [`bind-params`](#firewrap.tool.strace/bind-params)
    -  [`bwrap->paths`](#firewrap.tool.strace/bwrap->paths)
    -  [`cli-table`](#firewrap.tool.strace/cli-table)
    -  [`data-call?`](#firewrap.tool.strace/data-call?)
    -  [`fs-call?`](#firewrap.tool.strace/fs-call?)
    -  [`generate-rules`](#firewrap.tool.strace/generate-rules)
    -  [`match-command`](#firewrap.tool.strace/match-command)
    -  [`match-path`](#firewrap.tool.strace/match-path)
    -  [`match-xdg-cache-home`](#firewrap.tool.strace/match-xdg-cache-home)
    -  [`match-xdg-config-dir`](#firewrap.tool.strace/match-xdg-config-dir)
    -  [`match-xdg-config-home`](#firewrap.tool.strace/match-xdg-config-home)
    -  [`match-xdg-data-dir`](#firewrap.tool.strace/match-xdg-data-dir)
    -  [`match-xdg-data-home`](#firewrap.tool.strace/match-xdg-data-home)
    -  [`match-xdg-dir`](#firewrap.tool.strace/match-xdg-dir)
    -  [`match-xdg-runtime-dir`](#firewrap.tool.strace/match-xdg-runtime-dir)
    -  [`match-xdg-state-home`](#firewrap.tool.strace/match-xdg-state-home)
    -  [`matchers`](#firewrap.tool.strace/matchers)
    -  [`print-help`](#firewrap.tool.strace/print-help)
    -  [`read-trace`](#firewrap.tool.strace/read-trace)
    -  [`static-matchers`](#firewrap.tool.strace/static-matchers)
    -  [`trace->file-syscalls`](#firewrap.tool.strace/trace->file-syscalls)
    -  [`trace->suggest`](#firewrap.tool.strace/trace->suggest)
    -  [`write-rules`](#firewrap.tool.strace/write-rules)
-  [`firewrap.tool.syscalls`](#firewrap.tool.syscalls) 
    -  [`syscalls`](#firewrap.tool.syscalls/syscalls)

-----
# <a name="firewrap.main">firewrap.main</a>






## <a name="firewrap.main/*exec-fn*">`*exec-fn*`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L15-L15">Source</a></sub></p>

## <a name="firewrap.main/base-options">`base-options`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L23-L35">Source</a></sub></p>

## <a name="firewrap.main/bind-extra-system-programs">`bind-extra-system-programs`</a>
``` clojure

(bind-extra-system-programs ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L147-L149">Source</a></sub></p>

## <a name="firewrap.main/bind-nix-shell">`bind-nix-shell`</a>
``` clojure

(bind-nix-shell ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L135-L139">Source</a></sub></p>

## <a name="firewrap.main/bind-user-programs">`bind-user-programs`</a>
``` clojure

(bind-user-programs ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L141-L145">Source</a></sub></p>

## <a name="firewrap.main/bwrap-args">`bwrap-args`</a>
``` clojure

(bwrap-args args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L93-L94">Source</a></sub></p>

## <a name="firewrap.main/cli-options">`cli-options`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L17-L21">Source</a></sub></p>

## <a name="firewrap.main/cli-spec">`cli-spec`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L37-L37">Source</a></sub></p>

## <a name="firewrap.main/escape-shell">`escape-shell`</a>
``` clojure

(escape-shell s)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L65-L68">Source</a></sub></p>

## <a name="firewrap.main/main">`main`</a>
``` clojure

(main & root-args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L114-L131">Source</a></sub></p>

## <a name="firewrap.main/needs-bwrap-sh-wrapper?">`needs-bwrap-sh-wrapper?`</a>
``` clojure

(needs-bwrap-sh-wrapper? args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L102-L105">Source</a></sub></p>

## <a name="firewrap.main/parse-args">`parse-args`</a>
``` clojure

(parse-args args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L39-L52">Source</a></sub></p>

## <a name="firewrap.main/print-help">`print-help`</a>
``` clojure

(print-help)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L54-L63">Source</a></sub></p>

## <a name="firewrap.main/run-bwrap">`run-bwrap`</a>
``` clojure

(run-bwrap ctx opts)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L107-L112">Source</a></sub></p>

## <a name="firewrap.main/run-bwrap-exec">`run-bwrap-exec`</a>
``` clojure

(run-bwrap-exec args {:keys [dry-run]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L96-L100">Source</a></sub></p>

## <a name="firewrap.main/run-bwrap-sh-wrapper">`run-bwrap-sh-wrapper`</a>
``` clojure

(run-bwrap-sh-wrapper args {:keys [dry-run]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L84-L91">Source</a></sub></p>

## <a name="firewrap.main/unwrap-escaping">`unwrap-escaping`</a>
``` clojure

(unwrap-escaping args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L70-L74">Source</a></sub></p>

## <a name="firewrap.main/unwrap-raw">`unwrap-raw`</a>
``` clojure

(unwrap-raw args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.clj#L76-L80">Source</a></sub></p>

-----
# <a name="firewrap.preset.appimage">firewrap.preset.appimage</a>






## <a name="firewrap.preset.appimage/appimage-command?">`appimage-command?`</a>
``` clojure

(appimage-command? s)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/appimage.clj#L5-L6">Source</a></sub></p>

## <a name="firewrap.preset.appimage/run">`run`</a>
``` clojure

(run ctx appimage & args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/appimage.clj#L8-L26">Source</a></sub></p>

-----
# <a name="firewrap.preset.base">firewrap.preset.base</a>






## <a name="firewrap.preset.base/base">`base`</a>
``` clojure

(base)
```
Function.

Base with basic bubblewrap flags, does not grant any resources.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.clj#L33-L44">Source</a></sub></p>

## <a name="firewrap.preset.base/base-gui">`base-gui`</a>
``` clojure

(base-gui)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.clj#L64-L67">Source</a></sub></p>

## <a name="firewrap.preset.base/base4">`base4`</a>
``` clojure

(base4 ctx)
```
Function.

More granular base with system files
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.clj#L46-L51">Source</a></sub></p>

## <a name="firewrap.preset.base/base5">`base5`</a>
``` clojure

(base5)
```
Function.

Low effort sandbox, incluedes system files with temporary home and empty tmp
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.clj#L53-L62">Source</a></sub></p>

## <a name="firewrap.preset.base/base9">`base9`</a>
``` clojure

(base9 ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.clj#L69-L72">Source</a></sub></p>

## <a name="firewrap.preset.base/bind-extra-system-programs">`bind-extra-system-programs`</a>
``` clojure

(bind-extra-system-programs ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.clj#L15-L17">Source</a></sub></p>

## <a name="firewrap.preset.base/bind-isolated-home-with-user-programs">`bind-isolated-home-with-user-programs`</a>
``` clojure

(bind-isolated-home-with-user-programs ctx appname)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.clj#L23-L26">Source</a></sub></p>

## <a name="firewrap.preset.base/bind-isolated-tmphome-with-user-programs">`bind-isolated-tmphome-with-user-programs`</a>
``` clojure

(bind-isolated-tmphome-with-user-programs ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.clj#L28-L31">Source</a></sub></p>

## <a name="firewrap.preset.base/bind-system-programs">`bind-system-programs`</a>
``` clojure

(bind-system-programs ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.clj#L7-L13">Source</a></sub></p>

## <a name="firewrap.preset.base/bind-user-programs">`bind-user-programs`</a>
``` clojure

(bind-user-programs ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.clj#L19-L21">Source</a></sub></p>

## <a name="firewrap.preset.base/configurable">`configurable`</a>
``` clojure

(configurable ctx params)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.clj#L74-L85">Source</a></sub></p>

-----
# <a name="firewrap.preset.dumpster">firewrap.preset.dumpster</a>






## <a name="firewrap.preset.dumpster/bind-cwd-rw">`bind-cwd-rw`</a>
``` clojure

(bind-cwd-rw ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.clj#L33-L38">Source</a></sub></p>

## <a name="firewrap.preset.dumpster/bind-isolated-home">`bind-isolated-home`</a>
``` clojure

(bind-isolated-home ctx appname)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.clj#L20-L25">Source</a></sub></p>

## <a name="firewrap.preset.dumpster/bind-isolated-tmphome">`bind-isolated-tmphome`</a>
``` clojure

(bind-isolated-tmphome ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.clj#L27-L31">Source</a></sub></p>

## <a name="firewrap.preset.dumpster/glob-one">`glob-one`</a>
``` clojure

(glob-one root pattern)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.clj#L12-L15">Source</a></sub></p>

## <a name="firewrap.preset.dumpster/home">`home`</a>
``` clojure

(home ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.clj#L17-L18">Source</a></sub></p>

## <a name="firewrap.preset.dumpster/network">`network`</a>
``` clojure

(network ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.clj#L40-L44">Source</a></sub></p>

## <a name="firewrap.preset.dumpster/path->appname">`path->appname`</a>
``` clojure

(path->appname path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.clj#L7-L10">Source</a></sub></p>

-----
# <a name="firewrap.preset.env">firewrap.preset.env</a>






## <a name="firewrap.preset.env/allowed">`allowed`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/env.clj#L82-L108">Source</a></sub></p>

## <a name="firewrap.preset.env/desktop-environment">`desktop-environment`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/env.clj#L38-L70">Source</a></sub></p>

## <a name="firewrap.preset.env/gpg-ssh">`gpg-ssh`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/env.clj#L77-L80">Source</a></sub></p>

## <a name="firewrap.preset.env/locales">`locales`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/env.clj#L24-L36">Source</a></sub></p>

## <a name="firewrap.preset.env/programming-languages">`programming-languages`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/env.clj#L18-L22">Source</a></sub></p>

## <a name="firewrap.preset.env/systemd">`systemd`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/env.clj#L72-L75">Source</a></sub></p>

## <a name="firewrap.preset.env/terminal">`terminal`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/env.clj#L5-L16">Source</a></sub></p>

-----
# <a name="firewrap.preset.oldprofiles">firewrap.preset.oldprofiles</a>






## <a name="firewrap.preset.oldprofiles/chatall">`chatall`</a>
``` clojure

(chatall)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldprofiles.clj#L8-L24">Source</a></sub></p>

## <a name="firewrap.preset.oldprofiles/cheese">`cheese`</a>
``` clojure

(cheese {:keys [executable]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldprofiles.clj#L26-L83">Source</a></sub></p>

## <a name="firewrap.preset.oldprofiles/gedit">`gedit`</a>
``` clojure

(gedit {:keys [executable]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldprofiles.clj#L87-L131">Source</a></sub></p>

## <a name="firewrap.preset.oldprofiles/gnome-calculator">`gnome-calculator`</a>
``` clojure

(gnome-calculator {:keys [executable]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldprofiles.clj#L134-L164">Source</a></sub></p>

## <a name="firewrap.preset.oldprofiles/notify-send">`notify-send`</a>
``` clojure

(notify-send {:keys [executable]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldprofiles.clj#L166-L171">Source</a></sub></p>

## <a name="firewrap.preset.oldprofiles/xdg-open">`xdg-open`</a>
``` clojure

(xdg-open)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldprofiles.clj#L183-L187">Source</a></sub></p>

-----
# <a name="firewrap.preset.oldsystem">firewrap.preset.oldsystem</a>






## <a name="firewrap.preset.oldsystem/add-bwrap-args">`add-bwrap-args`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L14-L14">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/at-spi">`at-spi`</a>
``` clojure

(at-spi ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L163-L167">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/bind-dev">`bind-dev`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L27-L27">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/bind-dev-try">`bind-dev-try`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L28-L28">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/bind-ro">`bind-ro`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L16-L16">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/bind-ro-try">`bind-ro-try`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L17-L17">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/bind-ro-try-many">`bind-ro-try-many`</a>
``` clojure

(bind-ro-try-many ctx paths)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L22-L23">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/bind-rw">`bind-rw`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L25-L25">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/bind-rw-try">`bind-rw-try`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L26-L26">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/command">`command`</a>
``` clojure

(command ctx cmd)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L272-L279">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/dbus-bus-path">`dbus-bus-path`</a>
``` clojure

(dbus-bus-path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L213-L216">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/dbus-system-bus">`dbus-system-bus`</a>
``` clojure

(dbus-system-bus ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L218-L221">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/dbus-talk">`dbus-talk`</a>
``` clojure

(dbus-talk ctx name)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L231-L236">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/dbus-unrestricted">`dbus-unrestricted`</a>
``` clojure

(dbus-unrestricted ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L223-L227">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/dconf">`dconf`</a>
``` clojure

(dconf ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L150-L160">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/dev-null">`dev-null`</a>
``` clojure

(dev-null ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L209-L211">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/dev-urandom">`dev-urandom`</a>
``` clojure

(dev-urandom ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L204-L206">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/escape-shell">`escape-shell`</a>
``` clojure

(escape-shell s)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L7-L9">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/fontconfig">`fontconfig`</a>
``` clojure

(fontconfig ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L111-L114">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/fontconfig-shared-cache">`fontconfig-shared-cache`</a>
``` clojure

(fontconfig-shared-cache ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L116-L122">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/fonts">`fonts`</a>
``` clojure

(fonts ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L124-L130">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/glib">`glib`</a>
``` clojure

(glib ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L255-L257">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/gpu">`gpu`</a>
``` clojure

(gpu ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L169-L173">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/gtk">`gtk`</a>
``` clojure

(gtk ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L259-L266">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/icons">`icons`</a>
``` clojure

(icons ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L132-L136">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/isolated-home">`isolated-home`</a>
``` clojure

(isolated-home ctx appname)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L175-L181">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/libs">`libs`</a>
``` clojure

(libs ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L183-L190">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/locale">`locale`</a>
``` clojure

(locale ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L138-L141">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/mime-cache">`mime-cache`</a>
``` clojure

(mime-cache ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L268-L270">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/nop">`nop`</a>
``` clojure

(nop ctx & _)
```
Macro.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L11-L12">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/processes">`processes`</a>
``` clojure

(processes ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L192-L194">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/themes">`themes`</a>
``` clojure

(themes ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L143-L148">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/tmp">`tmp`</a>
``` clojure

(tmp ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L200-L201">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/tmpfs">`tmpfs`</a>
``` clojure

(tmpfs ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L197-L198">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/x11">`x11`</a>
``` clojure

(x11 ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L250-L253">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-cache-home">`xdg-cache-home`</a>
``` clojure

(xdg-cache-home ctx & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L93-L94">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-cache-home-path">`xdg-cache-home-path`</a>
``` clojure

(xdg-cache-home-path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L73-L75">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-cache-home-paths">`xdg-cache-home-paths`</a>
``` clojure

(xdg-cache-home-paths & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L77-L78">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-config-dir-paths">`xdg-config-dir-paths`</a>
``` clojure

(xdg-config-dir-paths & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L53-L54">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-config-dirs-path">`xdg-config-dirs-path`</a>
``` clojure

(xdg-config-dirs-path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L49-L51">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-config-home">`xdg-config-home`</a>
``` clojure

(xdg-config-home ctx & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L96-L97">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-config-home-path">`xdg-config-home-path`</a>
``` clojure

(xdg-config-home-path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L66-L68">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-config-home-paths">`xdg-config-home-paths`</a>
``` clojure

(xdg-config-home-paths & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L70-L71">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-data-dir">`xdg-data-dir`</a>
``` clojure

(xdg-data-dir ctx & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L87-L88">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-data-dir-paths">`xdg-data-dir-paths`</a>
``` clojure

(xdg-data-dir-paths & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L42-L44">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-data-dirs-path">`xdg-data-dirs-path`</a>
``` clojure

(xdg-data-dirs-path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L38-L40">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-data-home">`xdg-data-home`</a>
``` clojure

(xdg-data-home ctx & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L90-L91">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-data-home-path">`xdg-data-home-path`</a>
``` clojure

(xdg-data-home-path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L59-L61">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-data-home-paths">`xdg-data-home-paths`</a>
``` clojure

(xdg-data-home-paths & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L63-L64">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-open">`xdg-open`</a>
``` clojure

(xdg-open ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L240-L248">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-runtime-dir">`xdg-runtime-dir`</a>
``` clojure

(xdg-runtime-dir ctx subdir)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L106-L109">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-runtime-dir-path">`xdg-runtime-dir-path`</a>
``` clojure

(xdg-runtime-dir-path ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L102-L104">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-state-home">`xdg-state-home`</a>
``` clojure

(xdg-state-home ctx & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L99-L100">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-state-home-path">`xdg-state-home-path`</a>
``` clojure

(xdg-state-home-path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L80-L82">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-state-home-paths">`xdg-state-home-paths`</a>
``` clojure

(xdg-state-home-paths & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.clj#L84-L85">Source</a></sub></p>

-----
# <a name="firewrap.preset.vscode">firewrap.preset.vscode</a>






## <a name="firewrap.preset.vscode/vscode-nvim">`vscode-nvim`</a>
``` clojure

(vscode-nvim ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/vscode.clj#L6-L19">Source</a></sub></p>

-----
# <a name="firewrap.profile">firewrap.profile</a>






## <a name="firewrap.profile/register!">`register!`</a>
``` clojure

(register! name profile)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile.clj#L15-L16">Source</a></sub></p>

## <a name="firewrap.profile/resolve">`resolve`</a>
``` clojure

(resolve name)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile.clj#L11-L13">Source</a></sub></p>

## <a name="firewrap.profile/resolve-builtin-profile">`resolve-builtin-profile`</a>
``` clojure

(resolve-builtin-profile appname)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile.clj#L6-L9">Source</a></sub></p>

-----
# <a name="firewrap.profile.bash">firewrap.profile.bash</a>






## <a name="firewrap.profile.bash/profile">`profile`</a>
``` clojure

(profile _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/bash.clj#L6-L10">Source</a></sub></p>

-----
# <a name="firewrap.profile.date">firewrap.profile.date</a>






## <a name="firewrap.profile.date/profile">`profile`</a>
``` clojure

(profile _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/date.clj#L6-L10">Source</a></sub></p>

-----
# <a name="firewrap.profile.echo">firewrap.profile.echo</a>






## <a name="firewrap.profile.echo/profile">`profile`</a>
``` clojure

(profile _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/echo.clj#L6-L9">Source</a></sub></p>

-----
# <a name="firewrap.profile.ferdium">firewrap.profile.ferdium</a>






## <a name="firewrap.profile.ferdium/profile">`profile`</a>
``` clojure

(profile _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/ferdium.clj#L6-L9">Source</a></sub></p>

-----
# <a name="firewrap.profile.godmode">firewrap.profile.godmode</a>






## <a name="firewrap.profile.godmode/profile">`profile`</a>
``` clojure

(profile appimage)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/godmode.clj#L7-L11">Source</a></sub></p>

-----
# <a name="firewrap.profile.java">firewrap.profile.java</a>






## <a name="firewrap.profile.java/profile">`profile`</a>
``` clojure

(profile _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/java.clj#L12-L24">Source</a></sub></p>

-----
# <a name="firewrap.profile.windsurf">firewrap.profile.windsurf</a>






## <a name="firewrap.profile.windsurf/profile">`profile`</a>
``` clojure

(profile _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/windsurf.clj#L7-L11">Source</a></sub></p>

-----
# <a name="firewrap.sandbox">firewrap.sandbox</a>






## <a name="firewrap.sandbox/*populate-env!*">`*populate-env!*`</a>
``` clojure

(*populate-env!* ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L139-L142">Source</a></sub></p>

## <a name="firewrap.sandbox/*run-effects!*">`*run-effects!*`</a>
``` clojure

(*run-effects!* ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L186-L189">Source</a></sub></p>

## <a name="firewrap.sandbox/add-heredoc-args">`add-heredoc-args`</a>
``` clojure

(add-heredoc-args ctx & args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L12-L13">Source</a></sub></p>

## <a name="firewrap.sandbox/bind">`bind`</a>
``` clojure

(bind ctx src dest {:keys [perms try access]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L21-L29">Source</a></sub></p>

## <a name="firewrap.sandbox/bind-data-ro">`bind-data-ro`</a>
``` clojure

(bind-data-ro ctx {:keys [perms fd path file content]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L88-L98">Source</a></sub></p>

## <a name="firewrap.sandbox/bind-dev">`bind-dev`</a>
``` clojure

(bind-dev ctx path)
(bind-dev ctx path dest-or-opts)
(bind-dev ctx src dest {:keys [perms try]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L67-L74">Source</a></sub></p>

## <a name="firewrap.sandbox/bind-dev-try">`bind-dev-try`</a>
``` clojure

(bind-dev-try ctx path)
(bind-dev-try ctx path dest-or-opts)
(bind-dev-try ctx src dest {:keys [perms try]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L76-L83">Source</a></sub></p>

## <a name="firewrap.sandbox/bind-ro">`bind-ro`</a>
``` clojure

(bind-ro ctx path)
(bind-ro ctx path dest-or-opts)
(bind-ro ctx src dest {:keys [perms try]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L40-L47">Source</a></sub></p>

## <a name="firewrap.sandbox/bind-ro-try">`bind-ro-try`</a>
``` clojure

(bind-ro-try ctx path)
(bind-ro-try ctx path dest-or-opts)
(bind-ro-try ctx src dest {:keys [perms]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L31-L38">Source</a></sub></p>

## <a name="firewrap.sandbox/bind-rw">`bind-rw`</a>
``` clojure

(bind-rw ctx path)
(bind-rw ctx path dest-or-opts)
(bind-rw ctx src dest {:keys [perms try]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L49-L56">Source</a></sub></p>

## <a name="firewrap.sandbox/bind-rw-try">`bind-rw-try`</a>
``` clojure

(bind-rw-try ctx path)
(bind-rw-try ctx path dest-or-opts)
(bind-rw-try ctx src dest {:keys [perms try]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L58-L65">Source</a></sub></p>

## <a name="firewrap.sandbox/chdir">`chdir`</a>
``` clojure

(chdir ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L106-L107">Source</a></sub></p>

## <a name="firewrap.sandbox/cmd-args">`cmd-args`</a>
``` clojure

(cmd-args ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L15-L16">Source</a></sub></p>

## <a name="firewrap.sandbox/ctx->args">`ctx->args`</a>
``` clojure

(ctx->args ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L179-L184">Source</a></sub></p>

## <a name="firewrap.sandbox/cwd">`cwd`</a>
``` clojure

(cwd ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L144-L145">Source</a></sub></p>

## <a name="firewrap.sandbox/die-with-parent">`die-with-parent`</a>
``` clojure

(die-with-parent ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L109-L110">Source</a></sub></p>

## <a name="firewrap.sandbox/env-pass">`env-pass`</a>
``` clojure

(env-pass ctx k)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L126-L127">Source</a></sub></p>

## <a name="firewrap.sandbox/env-pass-many">`env-pass-many`</a>
``` clojure

(env-pass-many ctx ks)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L129-L130">Source</a></sub></p>

## <a name="firewrap.sandbox/env-set">`env-set`</a>
``` clojure

(env-set ctx k v)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L123-L124">Source</a></sub></p>

## <a name="firewrap.sandbox/fx-create-dirs">`fx-create-dirs`</a>
``` clojure

(fx-create-dirs ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L176-L177">Source</a></sub></p>

## <a name="firewrap.sandbox/getenv">`getenv`</a>
``` clojure

(getenv ctx x)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L135-L137">Source</a></sub></p>

## <a name="firewrap.sandbox/getenvs">`getenvs`</a>
``` clojure

(getenvs ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L132-L133">Source</a></sub></p>

## <a name="firewrap.sandbox/new-session">`new-session`</a>
``` clojure

(new-session ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L112-L113">Source</a></sub></p>

## <a name="firewrap.sandbox/new-session-disable">`new-session-disable`</a>
``` clojure

(new-session-disable ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L115-L115">Source</a></sub></p>

## <a name="firewrap.sandbox/set-cmd-args">`set-cmd-args`</a>
``` clojure

(set-cmd-args ctx args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L18-L19">Source</a></sub></p>

## <a name="firewrap.sandbox/share-net">`share-net`</a>
``` clojure

(share-net ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L120-L121">Source</a></sub></p>

## <a name="firewrap.sandbox/skip-own-symlink">`skip-own-symlink`</a>
``` clojure

(skip-own-symlink [cmd & args])
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L165-L174">Source</a></sub></p>

## <a name="firewrap.sandbox/symlink">`symlink`</a>
``` clojure

(symlink ctx target link)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L103-L104">Source</a></sub></p>

## <a name="firewrap.sandbox/tmpfs">`tmpfs`</a>
``` clojure

(tmpfs ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L100-L101">Source</a></sub></p>

## <a name="firewrap.sandbox/unsafe-escaped-arg">`unsafe-escaped-arg`</a>
``` clojure

(unsafe-escaped-arg s)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L6-L7">Source</a></sub></p>

## <a name="firewrap.sandbox/unshare-all">`unshare-all`</a>
``` clojure

(unshare-all ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.clj#L117-L118">Source</a></sub></p>

-----
# <a name="firewrap.tool.strace">firewrap.tool.strace</a>






## <a name="firewrap.tool.strace/-main">`-main`</a>
``` clojure

(-main & args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L314-L315">Source</a></sub></p>

## <a name="firewrap.tool.strace/bind-params">`bind-params`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L111-L118">Source</a></sub></p>

## <a name="firewrap.tool.strace/bwrap->paths">`bwrap->paths`</a>
``` clojure

(bwrap->paths bwrap-args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L120-L128">Source</a></sub></p>

## <a name="firewrap.tool.strace/cli-table">`cli-table`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L307-L312">Source</a></sub></p>

## <a name="firewrap.tool.strace/data-call?">`data-call?`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L35-L41">Source</a></sub></p>

## <a name="firewrap.tool.strace/fs-call?">`fs-call?`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L23-L33">Source</a></sub></p>

## <a name="firewrap.tool.strace/generate-rules">`generate-rules`</a>
``` clojure

(generate-rules _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L290-L294">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-command">`match-command`</a>
``` clojure

(match-command path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L200-L207">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-path">`match-path`</a>
``` clojure

(match-path path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L229-L239">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-xdg-cache-home">`match-xdg-cache-home`</a>
``` clojure

(match-xdg-cache-home path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L188-L190">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-xdg-config-dir">`match-xdg-config-dir`</a>
``` clojure

(match-xdg-config-dir path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L176-L178">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-xdg-config-home">`match-xdg-config-home`</a>
``` clojure

(match-xdg-config-home path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L184-L186">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-xdg-data-dir">`match-xdg-data-dir`</a>
``` clojure

(match-xdg-data-dir path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L172-L174">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-xdg-data-home">`match-xdg-data-home`</a>
``` clojure

(match-xdg-data-home path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L180-L182">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-xdg-dir">`match-xdg-dir`</a>
``` clojure

(match-xdg-dir dirs-str path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L162-L170">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-xdg-runtime-dir">`match-xdg-runtime-dir`</a>
``` clojure

(match-xdg-runtime-dir path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L155-L158">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-xdg-state-home">`match-xdg-state-home`</a>
``` clojure

(match-xdg-state-home path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L192-L194">Source</a></sub></p>

## <a name="firewrap.tool.strace/matchers">`matchers`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L219-L227">Source</a></sub></p>

## <a name="firewrap.tool.strace/print-help">`print-help`</a>
``` clojure

(print-help _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L298-L305">Source</a></sub></p>

## <a name="firewrap.tool.strace/read-trace">`read-trace`</a>
``` clojure

(read-trace file-path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L64-L70">Source</a></sub></p>

## <a name="firewrap.tool.strace/static-matchers">`static-matchers`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L131-L153">Source</a></sub></p>

## <a name="firewrap.tool.strace/trace->file-syscalls">`trace->file-syscalls`</a>
``` clojure

(trace->file-syscalls trace)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L74-L88">Source</a></sub></p>

## <a name="firewrap.tool.strace/trace->suggest">`trace->suggest`</a>
``` clojure

(trace->suggest trace)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L256-L280">Source</a></sub></p>

## <a name="firewrap.tool.strace/write-rules">`write-rules`</a>
``` clojure

(write-rules writer rules)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.clj#L282-L288">Source</a></sub></p>

-----
# <a name="firewrap.tool.syscalls">firewrap.tool.syscalls</a>






## <a name="firewrap.tool.syscalls/syscalls">`syscalls`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/syscalls.clj#L8-L16">Source</a></sub></p>
