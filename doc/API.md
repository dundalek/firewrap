# Table of contents
-  [`firewrap.main`](#firewrap.main) 
    -  [`*exec-fn*`](#firewrap.main/*exec-fn*)
    -  [`*interactive*`](#firewrap.main/*interactive*)
    -  [`bwrap-args`](#firewrap.main/bwrap-args)
    -  [`format-bwrap-args-preview`](#firewrap.main/format-bwrap-args-preview)
    -  [`load-user-config`](#firewrap.main/load-user-config)
    -  [`main`](#firewrap.main/main)
    -  [`parse-args`](#firewrap.main/parse-args)
    -  [`preprocess-short-options`](#firewrap.main/preprocess-short-options)
    -  [`print-help`](#firewrap.main/print-help)
    -  [`print-sandbox-info`](#firewrap.main/print-sandbox-info)
    -  [`run-bwrap`](#firewrap.main/run-bwrap)
    -  [`unwrap-raw`](#firewrap.main/unwrap-raw)
-  [`firewrap.preset.appimage`](#firewrap.preset.appimage) 
    -  [`appimage-command?`](#firewrap.preset.appimage/appimage-command?)
    -  [`run`](#firewrap.preset.appimage/run)
-  [`firewrap.preset.base`](#firewrap.preset.base) 
    -  [`apply-bindings`](#firewrap.preset.base/apply-bindings)
    -  [`base`](#firewrap.preset.base/base) - Base with basic bubblewrap flags, does not grant any resources.
    -  [`base-gui`](#firewrap.preset.base/base-gui)
    -  [`base4`](#firewrap.preset.base/base4) - More granular base with system files.
    -  [`base5`](#firewrap.preset.base/base5) - Low effort sandbox, includes system files with temporary home and empty tmp.
    -  [`base6`](#firewrap.preset.base/base6)
    -  [`base8`](#firewrap.preset.base/base8) - Lower effort wider sandbox, does not filter env vars and /tmp, should work better for GUI programs.
    -  [`base9`](#firewrap.preset.base/base9)
    -  [`bind-extra-system-programs`](#firewrap.preset.base/bind-extra-system-programs)
    -  [`bind-isolated-home-with-user-programs`](#firewrap.preset.base/bind-isolated-home-with-user-programs)
    -  [`bind-isolated-tmphome-with-user-programs`](#firewrap.preset.base/bind-isolated-tmphome-with-user-programs)
    -  [`bind-system-and-extra-programs`](#firewrap.preset.base/bind-system-and-extra-programs)
    -  [`bind-system-programs`](#firewrap.preset.base/bind-system-programs)
    -  [`bind-user-programs`](#firewrap.preset.base/bind-user-programs)
    -  [`configurable`](#firewrap.preset.base/configurable)
-  [`firewrap.preset.dumpster`](#firewrap.preset.dumpster) 
    -  [`bind-cwd-rw`](#firewrap.preset.dumpster/bind-cwd-rw)
    -  [`bind-isolated-home`](#firewrap.preset.dumpster/bind-isolated-home)
    -  [`bind-isolated-tmphome`](#firewrap.preset.dumpster/bind-isolated-tmphome)
    -  [`bind-nix-profile`](#firewrap.preset.dumpster/bind-nix-profile)
    -  [`bind-nix-root`](#firewrap.preset.dumpster/bind-nix-root)
    -  [`glob-one`](#firewrap.preset.dumpster/glob-one)
    -  [`home`](#firewrap.preset.dumpster/home)
    -  [`network`](#firewrap.preset.dumpster/network)
    -  [`path->appname`](#firewrap.preset.dumpster/path->appname)
    -  [`proc`](#firewrap.preset.dumpster/proc)
    -  [`shell-profile`](#firewrap.preset.dumpster/shell-profile)
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
    -  [`dev-pts`](#firewrap.preset.oldsystem/dev-pts)
    -  [`dev-urandom`](#firewrap.preset.oldsystem/dev-urandom)
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
    -  [`not-exists`](#firewrap.preset.oldsystem/not-exists)
    -  [`processes`](#firewrap.preset.oldsystem/processes)
    -  [`themes`](#firewrap.preset.oldsystem/themes)
    -  [`timezone`](#firewrap.preset.oldsystem/timezone)
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
-  [`firewrap.profile.claude`](#firewrap.profile.claude) 
    -  [`narrow`](#firewrap.profile.claude/narrow)
    -  [`wide`](#firewrap.profile.claude/wide)
-  [`firewrap.profile.clojure`](#firewrap.profile.clojure) 
    -  [`profile`](#firewrap.profile.clojure/profile)
-  [`firewrap.profile.cursor`](#firewrap.profile.cursor) 
    -  [`profile`](#firewrap.profile.cursor/profile)
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
    -  [`profile-with-options`](#firewrap.profile.windsurf/profile-with-options)
-  [`firewrap.sandbox`](#firewrap.sandbox) 
    -  [`$->`](#firewrap.sandbox/$->)
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
    -  [`dev`](#firewrap.sandbox/dev)
    -  [`die-with-parent`](#firewrap.sandbox/die-with-parent)
    -  [`env-pass`](#firewrap.sandbox/env-pass)
    -  [`env-pass-many`](#firewrap.sandbox/env-pass-many)
    -  [`env-set`](#firewrap.sandbox/env-set)
    -  [`fx-create-dirs`](#firewrap.sandbox/fx-create-dirs)
    -  [`getenv`](#firewrap.sandbox/getenv)
    -  [`getenvs`](#firewrap.sandbox/getenvs)
    -  [`interpret-hiccup`](#firewrap.sandbox/interpret-hiccup)
    -  [`interpret-instrumenting`](#firewrap.sandbox/interpret-instrumenting)
    -  [`new-session`](#firewrap.sandbox/new-session)
    -  [`new-session-disable`](#firewrap.sandbox/new-session-disable)
    -  [`proc`](#firewrap.sandbox/proc)
    -  [`set-cmd-args`](#firewrap.sandbox/set-cmd-args)
    -  [`share-cgroup`](#firewrap.sandbox/share-cgroup)
    -  [`share-ipc`](#firewrap.sandbox/share-ipc)
    -  [`share-net`](#firewrap.sandbox/share-net)
    -  [`share-pid`](#firewrap.sandbox/share-pid)
    -  [`share-user`](#firewrap.sandbox/share-user)
    -  [`share-uts`](#firewrap.sandbox/share-uts)
    -  [`skip-own-symlink`](#firewrap.sandbox/skip-own-symlink)
    -  [`symlink`](#firewrap.sandbox/symlink)
    -  [`tmpfs`](#firewrap.sandbox/tmpfs)
    -  [`unsafe-escaped-arg`](#firewrap.sandbox/unsafe-escaped-arg)
    -  [`unshare-all`](#firewrap.sandbox/unshare-all)
-  [`firewrap.tool.portlet`](#firewrap.tool.portlet) 
    -  [`load-viewers!`](#firewrap.tool.portlet/load-viewers!)
-  [`firewrap.tool.portlet.viewers`](#firewrap.tool.portlet.viewers) 
    -  [`count-children`](#firewrap.tool.portlet.viewers/count-children)
    -  [`details-panel`](#firewrap.tool.portlet.viewers/details-panel)
    -  [`profile-tree-viewer`](#firewrap.tool.portlet.viewers/profile-tree-viewer)
    -  [`profile-tree?`](#firewrap.tool.portlet.viewers/profile-tree?)
    -  [`register-viewers!`](#firewrap.tool.portlet.viewers/register-viewers!)
    -  [`tree-node-view`](#firewrap.tool.portlet.viewers/tree-node-view)
-  [`firewrap.tool.strace`](#firewrap.tool.strace) 
    -  [`-main`](#firewrap.tool.strace/-main)
    -  [`bwrap->paths`](#firewrap.tool.strace/bwrap->paths)
    -  [`cli-table`](#firewrap.tool.strace/cli-table)
    -  [`data-call?`](#firewrap.tool.strace/data-call?)
    -  [`fs-call?`](#firewrap.tool.strace/fs-call?)
    -  [`generate-rules`](#firewrap.tool.strace/generate-rules)
    -  [`ignored-path-prefixes`](#firewrap.tool.strace/ignored-path-prefixes)
    -  [`ignored-paths`](#firewrap.tool.strace/ignored-paths)
    -  [`make-matchers`](#firewrap.tool.strace/make-matchers)
    -  [`match-command`](#firewrap.tool.strace/match-command)
    -  [`match-home`](#firewrap.tool.strace/match-home)
    -  [`match-path`](#firewrap.tool.strace/match-path)
    -  [`match-tmp`](#firewrap.tool.strace/match-tmp)
    -  [`match-xdg-cache-home`](#firewrap.tool.strace/match-xdg-cache-home)
    -  [`match-xdg-config-dir`](#firewrap.tool.strace/match-xdg-config-dir)
    -  [`match-xdg-config-home`](#firewrap.tool.strace/match-xdg-config-home)
    -  [`match-xdg-data-dir`](#firewrap.tool.strace/match-xdg-data-dir)
    -  [`match-xdg-data-home`](#firewrap.tool.strace/match-xdg-data-home)
    -  [`match-xdg-dir`](#firewrap.tool.strace/match-xdg-dir)
    -  [`match-xdg-runtime-dir`](#firewrap.tool.strace/match-xdg-runtime-dir)
    -  [`match-xdg-state-home`](#firewrap.tool.strace/match-xdg-state-home)
    -  [`print-help`](#firewrap.tool.strace/print-help)
    -  [`read-json-trace`](#firewrap.tool.strace/read-json-trace)
    -  [`single-arity-bind-params`](#firewrap.tool.strace/single-arity-bind-params)
    -  [`syscall->file-paths`](#firewrap.tool.strace/syscall->file-paths)
    -  [`trace->file-syscalls`](#firewrap.tool.strace/trace->file-syscalls)
    -  [`trace->suggest`](#firewrap.tool.strace/trace->suggest)
    -  [`two-arity-bind-params`](#firewrap.tool.strace/two-arity-bind-params)
    -  [`write-rules`](#firewrap.tool.strace/write-rules)

-----
# <a name="firewrap.main">firewrap.main</a>






## <a name="firewrap.main/*exec-fn*">`*exec-fn*`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.cljc#L13-L13">Source</a></sub></p>

## <a name="firewrap.main/*interactive*">`*interactive*`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.cljc#L15-L15">Source</a></sub></p>

## <a name="firewrap.main/bwrap-args">`bwrap-args`</a>
``` clojure

(bwrap-args args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.cljc#L214-L215">Source</a></sub></p>

## <a name="firewrap.main/format-bwrap-args-preview">`format-bwrap-args-preview`</a>
``` clojure

(format-bwrap-args-preview args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.cljc#L165-L192">Source</a></sub></p>

## <a name="firewrap.main/load-user-config">`load-user-config`</a>
``` clojure

(load-user-config)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.cljc#L17-L23">Source</a></sub></p>

## <a name="firewrap.main/main">`main`</a>
``` clojure

(main & root-args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.cljc#L240-L262">Source</a></sub></p>

## <a name="firewrap.main/parse-args">`parse-args`</a>
``` clojure

(parse-args args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.cljc#L88-L126">Source</a></sub></p>

## <a name="firewrap.main/preprocess-short-options">`preprocess-short-options`</a>
``` clojure

(preprocess-short-options args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.cljc#L68-L80">Source</a></sub></p>

## <a name="firewrap.main/print-help">`print-help`</a>
``` clojure

(print-help)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.cljc#L128-L140">Source</a></sub></p>

## <a name="firewrap.main/print-sandbox-info">`print-sandbox-info`</a>
``` clojure

(print-sandbox-info print-fn)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.cljc#L194-L199">Source</a></sub></p>

## <a name="firewrap.main/run-bwrap">`run-bwrap`</a>
``` clojure

(run-bwrap ctx opts)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.cljc#L233-L238">Source</a></sub></p>

## <a name="firewrap.main/unwrap-raw">`unwrap-raw`</a>
``` clojure

(unwrap-raw args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/main.cljc#L153-L157">Source</a></sub></p>

-----
# <a name="firewrap.preset.appimage">firewrap.preset.appimage</a>






## <a name="firewrap.preset.appimage/appimage-command?">`appimage-command?`</a>
``` clojure

(appimage-command? s)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/appimage.cljc#L5-L6">Source</a></sub></p>

## <a name="firewrap.preset.appimage/run">`run`</a>
``` clojure

(run ctx appimage & args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/appimage.cljc#L8-L26">Source</a></sub></p>

-----
# <a name="firewrap.preset.base">firewrap.preset.base</a>






## <a name="firewrap.preset.base/apply-bindings">`apply-bindings`</a>
``` clojure

(apply-bindings ctx bindings)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.cljc#L114-L122">Source</a></sub></p>

## <a name="firewrap.preset.base/base">`base`</a>
``` clojure

(base)
(base {:keys [unsafe-session]})
```
Function.

Base with basic bubblewrap flags, does not grant any resources.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.cljc#L40-L53">Source</a></sub></p>

## <a name="firewrap.preset.base/base-gui">`base-gui`</a>
``` clojure

(base-gui)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.cljc#L83-L86">Source</a></sub></p>

## <a name="firewrap.preset.base/base4">`base4`</a>
``` clojure

(base4)
(base4 {:keys [unsafe-session]})
```
Function.

More granular base with system files
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.cljc#L55-L68">Source</a></sub></p>

## <a name="firewrap.preset.base/base5">`base5`</a>
``` clojure

(base5)
(base5 {:keys [unsafe-session]})
```
Function.

Low effort sandbox, includes system files with temporary home and empty tmp
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.cljc#L71-L81">Source</a></sub></p>

## <a name="firewrap.preset.base/base6">`base6`</a>
``` clojure

(base6)
(base6 {:keys [unsafe-session]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.cljc#L88-L94">Source</a></sub></p>

## <a name="firewrap.preset.base/base8">`base8`</a>
``` clojure

(base8)
(base8 {:keys [unsafe-session]})
```
Function.

Lower effort wider sandbox, does not filter env vars and /tmp, should work better for GUI programs
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.cljc#L96-L107">Source</a></sub></p>

## <a name="firewrap.preset.base/base9">`base9`</a>
``` clojure

(base9 ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.cljc#L109-L112">Source</a></sub></p>

## <a name="firewrap.preset.base/bind-extra-system-programs">`bind-extra-system-programs`</a>
``` clojure

(bind-extra-system-programs ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.cljc#L17-L19">Source</a></sub></p>

## <a name="firewrap.preset.base/bind-isolated-home-with-user-programs">`bind-isolated-home-with-user-programs`</a>
``` clojure

(bind-isolated-home-with-user-programs ctx appname)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.cljc#L30-L33">Source</a></sub></p>

## <a name="firewrap.preset.base/bind-isolated-tmphome-with-user-programs">`bind-isolated-tmphome-with-user-programs`</a>
``` clojure

(bind-isolated-tmphome-with-user-programs ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.cljc#L35-L38">Source</a></sub></p>

## <a name="firewrap.preset.base/bind-system-and-extra-programs">`bind-system-and-extra-programs`</a>
``` clojure

(bind-system-and-extra-programs ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.cljc#L21-L24">Source</a></sub></p>

## <a name="firewrap.preset.base/bind-system-programs">`bind-system-programs`</a>
``` clojure

(bind-system-programs ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.cljc#L8-L15">Source</a></sub></p>

## <a name="firewrap.preset.base/bind-user-programs">`bind-user-programs`</a>
``` clojure

(bind-user-programs ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.cljc#L26-L28">Source</a></sub></p>

## <a name="firewrap.preset.base/configurable">`configurable`</a>
``` clojure

(configurable ctx params)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/base.cljc#L134-L148">Source</a></sub></p>

-----
# <a name="firewrap.preset.dumpster">firewrap.preset.dumpster</a>






## <a name="firewrap.preset.dumpster/bind-cwd-rw">`bind-cwd-rw`</a>
``` clojure

(bind-cwd-rw ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.cljc#L39-L44">Source</a></sub></p>

## <a name="firewrap.preset.dumpster/bind-isolated-home">`bind-isolated-home`</a>
``` clojure

(bind-isolated-home ctx appname)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.cljc#L27-L31">Source</a></sub></p>

## <a name="firewrap.preset.dumpster/bind-isolated-tmphome">`bind-isolated-tmphome`</a>
``` clojure

(bind-isolated-tmphome ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.cljc#L33-L37">Source</a></sub></p>

## <a name="firewrap.preset.dumpster/bind-nix-profile">`bind-nix-profile`</a>
``` clojure

(bind-nix-profile ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.cljc#L63-L69">Source</a></sub></p>

## <a name="firewrap.preset.dumpster/bind-nix-root">`bind-nix-root`</a>
``` clojure

(bind-nix-root ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.cljc#L71-L73">Source</a></sub></p>

## <a name="firewrap.preset.dumpster/glob-one">`glob-one`</a>
``` clojure

(glob-one root pattern)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.cljc#L12-L15">Source</a></sub></p>

## <a name="firewrap.preset.dumpster/home">`home`</a>
``` clojure

(home ctx)
(home ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.cljc#L21-L25">Source</a></sub></p>

## <a name="firewrap.preset.dumpster/network">`network`</a>
``` clojure

(network ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.cljc#L46-L56">Source</a></sub></p>

## <a name="firewrap.preset.dumpster/path->appname">`path->appname`</a>
``` clojure

(path->appname path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.cljc#L7-L10">Source</a></sub></p>

## <a name="firewrap.preset.dumpster/proc">`proc`</a>
``` clojure

(proc ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.cljc#L17-L19">Source</a></sub></p>

## <a name="firewrap.preset.dumpster/shell-profile">`shell-profile`</a>
``` clojure

(shell-profile ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/dumpster.cljc#L58-L61">Source</a></sub></p>

-----
# <a name="firewrap.preset.env">firewrap.preset.env</a>






## <a name="firewrap.preset.env/allowed">`allowed`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/env.cljc#L82-L108">Source</a></sub></p>

## <a name="firewrap.preset.env/desktop-environment">`desktop-environment`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/env.cljc#L38-L70">Source</a></sub></p>

## <a name="firewrap.preset.env/gpg-ssh">`gpg-ssh`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/env.cljc#L77-L80">Source</a></sub></p>

## <a name="firewrap.preset.env/locales">`locales`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/env.cljc#L24-L36">Source</a></sub></p>

## <a name="firewrap.preset.env/programming-languages">`programming-languages`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/env.cljc#L18-L22">Source</a></sub></p>

## <a name="firewrap.preset.env/systemd">`systemd`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/env.cljc#L72-L75">Source</a></sub></p>

## <a name="firewrap.preset.env/terminal">`terminal`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/env.cljc#L5-L16">Source</a></sub></p>

-----
# <a name="firewrap.preset.oldprofiles">firewrap.preset.oldprofiles</a>






## <a name="firewrap.preset.oldprofiles/chatall">`chatall`</a>
``` clojure

(chatall)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldprofiles.cljc#L8-L24">Source</a></sub></p>

## <a name="firewrap.preset.oldprofiles/cheese">`cheese`</a>
``` clojure

(cheese {:keys [executable]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldprofiles.cljc#L26-L83">Source</a></sub></p>

## <a name="firewrap.preset.oldprofiles/gedit">`gedit`</a>
``` clojure

(gedit {:keys [executable]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldprofiles.cljc#L87-L132">Source</a></sub></p>

## <a name="firewrap.preset.oldprofiles/gnome-calculator">`gnome-calculator`</a>
``` clojure

(gnome-calculator {:keys [executable]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldprofiles.cljc#L135-L165">Source</a></sub></p>

## <a name="firewrap.preset.oldprofiles/notify-send">`notify-send`</a>
``` clojure

(notify-send {:keys [executable]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldprofiles.cljc#L167-L172">Source</a></sub></p>

## <a name="firewrap.preset.oldprofiles/xdg-open">`xdg-open`</a>
``` clojure

(xdg-open)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldprofiles.cljc#L184-L188">Source</a></sub></p>

-----
# <a name="firewrap.preset.oldsystem">firewrap.preset.oldsystem</a>






## <a name="firewrap.preset.oldsystem/add-bwrap-args">`add-bwrap-args`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L13-L13">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/at-spi">`at-spi`</a>
``` clojure

(at-spi ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L164-L168">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/bind-dev">`bind-dev`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L26-L26">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/bind-dev-try">`bind-dev-try`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L27-L27">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/bind-ro">`bind-ro`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L15-L15">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/bind-ro-try">`bind-ro-try`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L16-L16">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/bind-ro-try-many">`bind-ro-try-many`</a>
``` clojure

(bind-ro-try-many ctx paths)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L21-L22">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/bind-rw">`bind-rw`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L24-L24">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/bind-rw-try">`bind-rw-try`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L25-L25">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/command">`command`</a>
``` clojure

(command ctx cmd)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L269-L276">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/dbus-bus-path">`dbus-bus-path`</a>
``` clojure

(dbus-bus-path ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L210-L213">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/dbus-system-bus">`dbus-system-bus`</a>
``` clojure

(dbus-system-bus ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L215-L218">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/dbus-talk">`dbus-talk`</a>
``` clojure

(dbus-talk ctx name)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L228-L233">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/dbus-unrestricted">`dbus-unrestricted`</a>
``` clojure

(dbus-unrestricted ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L220-L224">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/dconf">`dconf`</a>
``` clojure

(dconf ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L151-L161">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/dev-null">`dev-null`</a>
``` clojure

(dev-null ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L202-L204">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/dev-pts">`dev-pts`</a>
``` clojure

(dev-pts ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L206-L208">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/dev-urandom">`dev-urandom`</a>
``` clojure

(dev-urandom ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L197-L199">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/fontconfig">`fontconfig`</a>
``` clojure

(fontconfig ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L112-L115">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/fontconfig-shared-cache">`fontconfig-shared-cache`</a>
``` clojure

(fontconfig-shared-cache ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L117-L123">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/fonts">`fonts`</a>
``` clojure

(fonts ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L125-L131">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/glib">`glib`</a>
``` clojure

(glib ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L252-L254">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/gpu">`gpu`</a>
``` clojure

(gpu ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L170-L174">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/gtk">`gtk`</a>
``` clojure

(gtk ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L256-L263">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/icons">`icons`</a>
``` clojure

(icons ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L133-L137">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/isolated-home">`isolated-home`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L29-L29">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/libs">`libs`</a>
``` clojure

(libs ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L176-L183">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/locale">`locale`</a>
``` clojure

(locale ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L139-L142">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/mime-cache">`mime-cache`</a>
``` clojure

(mime-cache ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L265-L267">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/nop">`nop`</a>
``` clojure

(nop ctx & _)
```
Macro.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L7-L8">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/not-exists">`not-exists`</a>
``` clojure

(not-exists ctx & _)
```
Macro.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L10-L11">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/processes">`processes`</a>
``` clojure

(processes ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L185-L187">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/themes">`themes`</a>
``` clojure

(themes ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L144-L149">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/timezone">`timezone`</a>
``` clojure

(timezone ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L278-L281">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/tmp">`tmp`</a>
``` clojure

(tmp ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L193-L194">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/tmpfs">`tmpfs`</a>
``` clojure

(tmpfs ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L190-L191">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/x11">`x11`</a>
``` clojure

(x11 ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L247-L250">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-cache-home">`xdg-cache-home`</a>
``` clojure

(xdg-cache-home ctx & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L94-L95">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-cache-home-path">`xdg-cache-home-path`</a>
``` clojure

(xdg-cache-home-path ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L74-L76">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-cache-home-paths">`xdg-cache-home-paths`</a>
``` clojure

(xdg-cache-home-paths ctx & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L78-L79">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-config-dir-paths">`xdg-config-dir-paths`</a>
``` clojure

(xdg-config-dir-paths ctx & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L54-L55">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-config-dirs-path">`xdg-config-dirs-path`</a>
``` clojure

(xdg-config-dirs-path ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L50-L52">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-config-home">`xdg-config-home`</a>
``` clojure

(xdg-config-home ctx & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L97-L98">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-config-home-path">`xdg-config-home-path`</a>
``` clojure

(xdg-config-home-path ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L67-L69">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-config-home-paths">`xdg-config-home-paths`</a>
``` clojure

(xdg-config-home-paths ctx & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L71-L72">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-data-dir">`xdg-data-dir`</a>
``` clojure

(xdg-data-dir ctx & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L88-L89">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-data-dir-paths">`xdg-data-dir-paths`</a>
``` clojure

(xdg-data-dir-paths ctx & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L43-L45">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-data-dirs-path">`xdg-data-dirs-path`</a>
``` clojure

(xdg-data-dirs-path ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L39-L41">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-data-home">`xdg-data-home`</a>
``` clojure

(xdg-data-home ctx & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L91-L92">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-data-home-path">`xdg-data-home-path`</a>
``` clojure

(xdg-data-home-path ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L60-L62">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-data-home-paths">`xdg-data-home-paths`</a>
``` clojure

(xdg-data-home-paths ctx & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L64-L65">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-open">`xdg-open`</a>
``` clojure

(xdg-open ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L237-L245">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-runtime-dir">`xdg-runtime-dir`</a>
``` clojure

(xdg-runtime-dir ctx subdir)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L107-L110">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-runtime-dir-path">`xdg-runtime-dir-path`</a>
``` clojure

(xdg-runtime-dir-path ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L103-L105">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-state-home">`xdg-state-home`</a>
``` clojure

(xdg-state-home ctx & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L100-L101">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-state-home-path">`xdg-state-home-path`</a>
``` clojure

(xdg-state-home-path ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L81-L83">Source</a></sub></p>

## <a name="firewrap.preset.oldsystem/xdg-state-home-paths">`xdg-state-home-paths`</a>
``` clojure

(xdg-state-home-paths ctx & subfolders)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/oldsystem.cljc#L85-L86">Source</a></sub></p>

-----
# <a name="firewrap.preset.vscode">firewrap.preset.vscode</a>






## <a name="firewrap.preset.vscode/vscode-nvim">`vscode-nvim`</a>
``` clojure

(vscode-nvim ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/preset/vscode.cljc#L6-L19">Source</a></sub></p>

-----
# <a name="firewrap.profile">firewrap.profile</a>






## <a name="firewrap.profile/register!">`register!`</a>
``` clojure

(register! name profile)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile.cljc#L15-L16">Source</a></sub></p>

## <a name="firewrap.profile/resolve">`resolve`</a>
``` clojure

(resolve name)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile.cljc#L11-L13">Source</a></sub></p>

## <a name="firewrap.profile/resolve-builtin-profile">`resolve-builtin-profile`</a>
``` clojure

(resolve-builtin-profile appname)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile.cljc#L6-L9">Source</a></sub></p>

-----
# <a name="firewrap.profile.bash">firewrap.profile.bash</a>






## <a name="firewrap.profile.bash/profile">`profile`</a>
``` clojure

(profile _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/bash.cljc#L7-L11">Source</a></sub></p>

-----
# <a name="firewrap.profile.claude">firewrap.profile.claude</a>






## <a name="firewrap.profile.claude/narrow">`narrow`</a>
``` clojure

(narrow _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/claude.cljc#L71-L84">Source</a></sub></p>

## <a name="firewrap.profile.claude/wide">`wide`</a>
``` clojure

(wide _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/claude.cljc#L67-L69">Source</a></sub></p>

-----
# <a name="firewrap.profile.clojure">firewrap.profile.clojure</a>






## <a name="firewrap.profile.clojure/profile">`profile`</a>
``` clojure

(profile _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/clojure.cljc#L7-L23">Source</a></sub></p>

-----
# <a name="firewrap.profile.cursor">firewrap.profile.cursor</a>






## <a name="firewrap.profile.cursor/profile">`profile`</a>
``` clojure

(profile _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/cursor.cljc#L8-L12">Source</a></sub></p>

-----
# <a name="firewrap.profile.date">firewrap.profile.date</a>






## <a name="firewrap.profile.date/profile">`profile`</a>
``` clojure

(profile _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/date.cljc#L7-L11">Source</a></sub></p>

-----
# <a name="firewrap.profile.echo">firewrap.profile.echo</a>






## <a name="firewrap.profile.echo/profile">`profile`</a>
``` clojure

(profile _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/echo.cljc#L7-L10">Source</a></sub></p>

-----
# <a name="firewrap.profile.ferdium">firewrap.profile.ferdium</a>






## <a name="firewrap.profile.ferdium/profile">`profile`</a>
``` clojure

(profile _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/ferdium.cljc#L7-L10">Source</a></sub></p>

-----
# <a name="firewrap.profile.godmode">firewrap.profile.godmode</a>






## <a name="firewrap.profile.godmode/profile">`profile`</a>
``` clojure

(profile appimage)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/godmode.cljc#L8-L12">Source</a></sub></p>

-----
# <a name="firewrap.profile.java">firewrap.profile.java</a>






## <a name="firewrap.profile.java/profile">`profile`</a>
``` clojure

(profile _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/java.cljc#L12-L25">Source</a></sub></p>

-----
# <a name="firewrap.profile.windsurf">firewrap.profile.windsurf</a>






## <a name="firewrap.profile.windsurf/profile">`profile`</a>
``` clojure

(profile _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/windsurf.cljc#L8-L12">Source</a></sub></p>

## <a name="firewrap.profile.windsurf/profile-with-options">`profile-with-options`</a>
``` clojure

(profile-with-options {:keys [windsurf-dir]} {:keys [args opts]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/profile/windsurf.cljc#L15-L24">Source</a></sub></p>

-----
# <a name="firewrap.sandbox">firewrap.sandbox</a>






## <a name="firewrap.sandbox/$->">`$->`</a>
``` clojure

($-> & forms)
```
Macro.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L251-L261">Source</a></sub></p>

## <a name="firewrap.sandbox/*populate-env!*">`*populate-env!*`</a>
``` clojure

(*populate-env!* ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L166-L169">Source</a></sub></p>

## <a name="firewrap.sandbox/*run-effects!*">`*run-effects!*`</a>
``` clojure

(*run-effects!* ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L229-L232">Source</a></sub></p>

## <a name="firewrap.sandbox/add-heredoc-args">`add-heredoc-args`</a>
``` clojure

(add-heredoc-args ctx & args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L15-L16">Source</a></sub></p>

## <a name="firewrap.sandbox/bind">`bind`</a>
``` clojure

(bind ctx src dest {:keys [perms try access]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L24-L32">Source</a></sub></p>

## <a name="firewrap.sandbox/bind-data-ro">`bind-data-ro`</a>
``` clojure

(bind-data-ro ctx {:keys [perms fd path file content]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L91-L101">Source</a></sub></p>

## <a name="firewrap.sandbox/bind-dev">`bind-dev`</a>
``` clojure

(bind-dev ctx path)
(bind-dev ctx path dest-or-opts)
(bind-dev ctx src dest {:keys [perms try]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L70-L77">Source</a></sub></p>

## <a name="firewrap.sandbox/bind-dev-try">`bind-dev-try`</a>
``` clojure

(bind-dev-try ctx path)
(bind-dev-try ctx path dest-or-opts)
(bind-dev-try ctx src dest {:keys [perms try]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L79-L86">Source</a></sub></p>

## <a name="firewrap.sandbox/bind-ro">`bind-ro`</a>
``` clojure

(bind-ro ctx path)
(bind-ro ctx path dest-or-opts)
(bind-ro ctx src dest {:keys [perms try]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L43-L50">Source</a></sub></p>

## <a name="firewrap.sandbox/bind-ro-try">`bind-ro-try`</a>
``` clojure

(bind-ro-try ctx path)
(bind-ro-try ctx path dest-or-opts)
(bind-ro-try ctx src dest {:keys [perms]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L34-L41">Source</a></sub></p>

## <a name="firewrap.sandbox/bind-rw">`bind-rw`</a>
``` clojure

(bind-rw ctx path)
(bind-rw ctx path dest-or-opts)
(bind-rw ctx src dest {:keys [perms try]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L52-L59">Source</a></sub></p>

## <a name="firewrap.sandbox/bind-rw-try">`bind-rw-try`</a>
``` clojure

(bind-rw-try ctx path)
(bind-rw-try ctx path dest-or-opts)
(bind-rw-try ctx src dest {:keys [perms try]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L61-L68">Source</a></sub></p>

## <a name="firewrap.sandbox/chdir">`chdir`</a>
``` clojure

(chdir ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L115-L116">Source</a></sub></p>

## <a name="firewrap.sandbox/cmd-args">`cmd-args`</a>
``` clojure

(cmd-args ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L18-L19">Source</a></sub></p>

## <a name="firewrap.sandbox/ctx->args">`ctx->args`</a>
``` clojure

(ctx->args ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L221-L227">Source</a></sub></p>

## <a name="firewrap.sandbox/cwd">`cwd`</a>
``` clojure

(cwd ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L171-L172">Source</a></sub></p>

## <a name="firewrap.sandbox/dev">`dev`</a>
``` clojure

(dev ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L103-L104">Source</a></sub></p>

## <a name="firewrap.sandbox/die-with-parent">`die-with-parent`</a>
``` clojure

(die-with-parent ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L118-L119">Source</a></sub></p>

## <a name="firewrap.sandbox/env-pass">`env-pass`</a>
``` clojure

(env-pass ctx k)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L153-L154">Source</a></sub></p>

## <a name="firewrap.sandbox/env-pass-many">`env-pass-many`</a>
``` clojure

(env-pass-many ctx ks)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L156-L157">Source</a></sub></p>

## <a name="firewrap.sandbox/env-set">`env-set`</a>
``` clojure

(env-set ctx k v)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L150-L151">Source</a></sub></p>

## <a name="firewrap.sandbox/fx-create-dirs">`fx-create-dirs`</a>
``` clojure

(fx-create-dirs ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L203-L204">Source</a></sub></p>

## <a name="firewrap.sandbox/getenv">`getenv`</a>
``` clojure

(getenv ctx x)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L162-L164">Source</a></sub></p>

## <a name="firewrap.sandbox/getenvs">`getenvs`</a>
``` clojure

(getenvs ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L159-L160">Source</a></sub></p>

## <a name="firewrap.sandbox/interpret-hiccup">`interpret-hiccup`</a>
``` clojure

(interpret-hiccup forms)
(interpret-hiccup ctx forms)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L234-L249">Source</a></sub></p>

## <a name="firewrap.sandbox/interpret-instrumenting">`interpret-instrumenting`</a>
``` clojure

(interpret-instrumenting forms)
(interpret-instrumenting ctx forms)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L263-L284">Source</a></sub></p>

## <a name="firewrap.sandbox/new-session">`new-session`</a>
``` clojure

(new-session ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L121-L122">Source</a></sub></p>

## <a name="firewrap.sandbox/new-session-disable">`new-session-disable`</a>
``` clojure

(new-session-disable ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L124-L124">Source</a></sub></p>

## <a name="firewrap.sandbox/proc">`proc`</a>
``` clojure

(proc ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L106-L107">Source</a></sub></p>

## <a name="firewrap.sandbox/set-cmd-args">`set-cmd-args`</a>
``` clojure

(set-cmd-args ctx args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L21-L22">Source</a></sub></p>

## <a name="firewrap.sandbox/share-cgroup">`share-cgroup`</a>
``` clojure

(share-cgroup ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L147-L148">Source</a></sub></p>

## <a name="firewrap.sandbox/share-ipc">`share-ipc`</a>
``` clojure

(share-ipc ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L138-L139">Source</a></sub></p>

## <a name="firewrap.sandbox/share-net">`share-net`</a>
``` clojure

(share-net ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L132-L133">Source</a></sub></p>

## <a name="firewrap.sandbox/share-pid">`share-pid`</a>
``` clojure

(share-pid ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L141-L142">Source</a></sub></p>

## <a name="firewrap.sandbox/share-user">`share-user`</a>
``` clojure

(share-user ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L135-L136">Source</a></sub></p>

## <a name="firewrap.sandbox/share-uts">`share-uts`</a>
``` clojure

(share-uts ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L144-L145">Source</a></sub></p>

## <a name="firewrap.sandbox/skip-own-symlink">`skip-own-symlink`</a>
``` clojure

(skip-own-symlink [cmd & args])
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L192-L201">Source</a></sub></p>

## <a name="firewrap.sandbox/symlink">`symlink`</a>
``` clojure

(symlink ctx target link)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L112-L113">Source</a></sub></p>

## <a name="firewrap.sandbox/tmpfs">`tmpfs`</a>
``` clojure

(tmpfs ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L109-L110">Source</a></sub></p>

## <a name="firewrap.sandbox/unsafe-escaped-arg">`unsafe-escaped-arg`</a>
``` clojure

(unsafe-escaped-arg s)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L9-L10">Source</a></sub></p>

## <a name="firewrap.sandbox/unshare-all">`unshare-all`</a>
``` clojure

(unshare-all ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/sandbox.cljc#L126-L127">Source</a></sub></p>

-----
# <a name="firewrap.tool.portlet">firewrap.tool.portlet</a>






## <a name="firewrap.tool.portlet/load-viewers!">`load-viewers!`</a>
``` clojure

(load-viewers!)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/portlet.cljc#L8-L9">Source</a></sub></p>

-----
# <a name="firewrap.tool.portlet.viewers">firewrap.tool.portlet.viewers</a>






## <a name="firewrap.tool.portlet.viewers/count-children">`count-children`</a>
``` clojure

(count-children node)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/portlet/viewers.cljs#L14-L18">Source</a></sub></p>

## <a name="firewrap.tool.portlet.viewers/details-panel">`details-panel`</a>
``` clojure

(details-panel {:keys [selected-node]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/portlet/viewers.cljs#L63-L96">Source</a></sub></p>

## <a name="firewrap.tool.portlet.viewers/profile-tree-viewer">`profile-tree-viewer`</a>
``` clojure

(profile-tree-viewer tree-data)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/portlet/viewers.cljs#L98-L120">Source</a></sub></p>

## <a name="firewrap.tool.portlet.viewers/profile-tree?">`profile-tree?`</a>
``` clojure

(profile-tree? value)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/portlet/viewers.cljs#L8-L12">Source</a></sub></p>

## <a name="firewrap.tool.portlet.viewers/register-viewers!">`register-viewers!`</a>
``` clojure

(register-viewers!)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/portlet/viewers.cljs#L122-L128">Source</a></sub></p>

## <a name="firewrap.tool.portlet.viewers/tree-node-view">`tree-node-view`</a>
``` clojure

(tree-node-view {:keys [node selected-node on-select expanded-nodes on-toggle level]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/portlet/viewers.cljs#L20-L61">Source</a></sub></p>

-----
# <a name="firewrap.tool.strace">firewrap.tool.strace</a>






## <a name="firewrap.tool.strace/-main">`-main`</a>
``` clojure

(-main & args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L360-L361">Source</a></sub></p>

## <a name="firewrap.tool.strace/bwrap->paths">`bwrap->paths`</a>
``` clojure

(bwrap->paths bwrap-args)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L123-L137">Source</a></sub></p>

## <a name="firewrap.tool.strace/cli-table">`cli-table`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L353-L358">Source</a></sub></p>

## <a name="firewrap.tool.strace/data-call?">`data-call?`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L37-L43">Source</a></sub></p>

## <a name="firewrap.tool.strace/fs-call?">`fs-call?`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L25-L35">Source</a></sub></p>

## <a name="firewrap.tool.strace/generate-rules">`generate-rules`</a>
``` clojure

(generate-rules _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L333-L340">Source</a></sub></p>

## <a name="firewrap.tool.strace/ignored-path-prefixes">`ignored-path-prefixes`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L215-L216">Source</a></sub></p>

## <a name="firewrap.tool.strace/ignored-paths">`ignored-paths`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L210-L213">Source</a></sub></p>

## <a name="firewrap.tool.strace/make-matchers">`make-matchers`</a>
``` clojure

(make-matchers ctx)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L259-L272">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-command">`match-command`</a>
``` clojure

(match-command ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L185-L191">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-home">`match-home`</a>
``` clojure

(match-home ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L197-L201">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-path">`match-path`</a>
``` clojure

(match-path matchers path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L274-L281">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-tmp">`match-tmp`</a>
``` clojure

(match-tmp ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L193-L195">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-xdg-cache-home">`match-xdg-cache-home`</a>
``` clojure

(match-xdg-cache-home ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L172-L174">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-xdg-config-dir">`match-xdg-config-dir`</a>
``` clojure

(match-xdg-config-dir ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L160-L162">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-xdg-config-home">`match-xdg-config-home`</a>
``` clojure

(match-xdg-config-home ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L168-L170">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-xdg-data-dir">`match-xdg-data-dir`</a>
``` clojure

(match-xdg-data-dir ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L156-L158">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-xdg-data-home">`match-xdg-data-home`</a>
``` clojure

(match-xdg-data-home ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L164-L166">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-xdg-dir">`match-xdg-dir`</a>
``` clojure

(match-xdg-dir dirs-str path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L146-L154">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-xdg-runtime-dir">`match-xdg-runtime-dir`</a>
``` clojure

(match-xdg-runtime-dir ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L139-L142">Source</a></sub></p>

## <a name="firewrap.tool.strace/match-xdg-state-home">`match-xdg-state-home`</a>
``` clojure

(match-xdg-state-home ctx path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L176-L178">Source</a></sub></p>

## <a name="firewrap.tool.strace/print-help">`print-help`</a>
``` clojure

(print-help _)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L344-L351">Source</a></sub></p>

## <a name="firewrap.tool.strace/read-json-trace">`read-json-trace`</a>
``` clojure

(read-json-trace file-path)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L66-L67">Source</a></sub></p>

## <a name="firewrap.tool.strace/single-arity-bind-params">`single-arity-bind-params`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L111-L112">Source</a></sub></p>

## <a name="firewrap.tool.strace/syscall->file-paths">`syscall->file-paths`</a>
``` clojure

(syscall->file-paths {:keys [syscall args]})
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L55-L64">Source</a></sub></p>

## <a name="firewrap.tool.strace/trace->file-syscalls">`trace->file-syscalls`</a>
``` clojure

(trace->file-syscalls trace)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L71-L85">Source</a></sub></p>

## <a name="firewrap.tool.strace/trace->suggest">`trace->suggest`</a>
``` clojure

(trace->suggest matchers trace)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L299-L323">Source</a></sub></p>

## <a name="firewrap.tool.strace/two-arity-bind-params">`two-arity-bind-params`</a>



<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L114-L121">Source</a></sub></p>

## <a name="firewrap.tool.strace/write-rules">`write-rules`</a>
``` clojure

(write-rules writer rules)
```
Function.
<p><sub><a href="https://github.com/dundalek/firewrap/blob/master/src/firewrap/tool/strace.cljc#L325-L331">Source</a></sub></p>
