(ns firewrap2.preset.oldprofiles
  (:require
   [firewrap2.bwrap :as bwrap]
   [firewrap2.preset.base :as base]
   [firewrap2.preset.dumpster :as dumpster]
   [firewrap2.preset.oldsystem :as system]))

(defn chatall []
  (->
   (base/base)
   (system/isolated-home "chatall")
   ;; Figure out which specific binaries are needed
   (system/bind-ro "/usr/bin")
   (system/libs)
   (system/processes)
   (system/tmp)
   (system/bind-dev "/dev/null")
   (system/bind-dev "/dev/urandom")
   (system/bind-dev "/dev/shm")
   (system/gpu)
   (system/fonts)
   (dumpster/network)
   (system/xdg-open)
   #_(system/run-appimage appimage)))

(defn cheese [{:keys [executable]}]
  (->
   (base/base)
   ;; Why does cheese need network? - perhaps tries wifi connected webcams?
   (dumpster/network)
   (system/isolated-home "cheese")

   (system/libs)

   ; (system/dev-bind "/dev")
   ; (system/ro-bind "/etc")
   ; (system/dev-bind "/proc")
   ; (system/dev-bind "/run")
   ; (system/dev-bind "/sys")
   ; (system/ro-bind "/usr")
   ; ; (system/ro-bind "/usr/bin/my-app")
   ; ; (system/ro-bind "/usr/bin")
   ; ; (system/ro-bind "/usr/share")
   ; (system/dev-bind "/var")
   (system/tmp)

   (system/bind-ro "/etc/localtime")
   (system/bind-dev "/dev/null")

   (system/bind-ro executable)
   ;; whole runtime dir likely too wide
   ; (system/bind-dev (system/xdg-runtime-dir-path ctx))
   (system/dbus-unrestricted)
   (system/gpu)
   (system/fonts)

   ;; These need to be abstracted
   (system/bind-ro-try "/usr/share/X11")
   (system/bind-ro-try "/usr/share/dconf")
   (system/bind-ro-try "/usr/share/drirc.d")
   (system/bind-ro-try "/usr/share/fontconfig")
   (system/bind-ro-try "/usr/share/glib-2.0")
   ; (system/ro-bind-try "/usr/share/gnome")
   (system/bind-ro-try "/usr/share/gnome-video-effects")
   (system/bind-ro-try "/usr/share/gstreamer-1.0")
   ; (system/ro-bind-try "/usr/share/gtk-3.0")
   (system/bind-ro-try "/usr/share/icons")
   (system/bind-ro-try "/usr/share/locale")
   (system/bind-ro-try "/usr/share/locale-langpack")
   (system/bind-ro-try "/usr/share/mime")
   (system/bind-ro-try "/usr/share/pipewire")
   ; (system/ro-bind-try "/usr/share/pixmaps")
   ; (system/ro-bind-try "/usr/share/pop")
   (system/bind-ro-try "/usr/share/themes")

   ;; Unsuccessfull trial and error trying to make thumbnails to work
   ;; It is hard to read strace because Cheese is continuously reading from camera
   ;; it seems unsandboxed cheese runs the  pixbuf-thumbnailer in bwrap sandbox and has oldroot and newroot paths
   (system/bind-dev "/dev/urandom")
   (system/bind-ro-try "/usr/share/thumbnailers")
   (system/bind-ro-try "/usr/bin/gdk-pixbuf-thumbnailer")
   (system/bind-rw "/home/me/.cache/gnome-desktop-thumbnailer")
   (system/bind-rw "/home/me/.cache/thumbnails")))

   ; (system/dev-bind "/tmp/.X11-unix")])

(defn gedit [{:keys [executable]}]
  (->
   (base/base)
   (system/libs)
   ; (system/rw-bind "/home")
   ; (system/isolated-home "gedit")
   ; (system/dev-bind "/dev")
   ; (system/ro-bind "/etc")
   ; (system/dev-bind "/proc")

   ;; uses `enchant` which is a generic wrapper supporting multiple spell checkers
   ;; tries to also read some aspell/hspell/hunspell configs related to spellcheck

   (system/bind-ro-try "/etc/localtime")

   (system/bind-ro-try "/etc/locale.alias")

   (system/bind-ro-try (str (System/getenv "HOME") "/.config/ibus/bus"))

   (system/bind-ro-try-many
    (system/xdg-data-dir-paths "icons" "mime"))

   (system/bind-ro-try "/home/me/.config/dconf/user")

   ; (system/ro-bind-try "/usr/share/gnome/mime")
   (system/bind-ro-try "/proc/filesystems")
   (system/bind-ro-try "/proc/mounts")

   (system/gtk)
   (system/bind-dev "/run")
   (system/bind-dev "/dev/urandom")
   (system/dbus-unrestricted)
   ; (system/dev-bind "/sys")
   (system/bind-ro "/usr")
   (system/fonts)
   (system/bind-ro-try "/usr/share/glib-2.0")
   (system/bind-ro executable)
   ; (system/ro-bind "/usr/bin")
   ; (system/ro-bind "/usr/share")
   ; (system/dev-bind "/var")
   (system/bind-ro-try "/home/me/.cache/fontconfig")
   (system/bind-ro-try "/var/cache/fontconfig")
   ; (system/dev-bind "/tmp")])
   (system/tmp)
   (system/bind-dev "/tmp/.X11-unix")))
   ; (system/dev-bind "/tmp/.X11-unix/X11")])

(defn gnome-calculator [{:keys [executable]}]
  (->
   (base/base)
   ;; network for currency exchange rate downloads
   (dumpster/network)
   (system/at-spi)
   (system/dbus-unrestricted)
   (system/dconf)
   (system/dev-urandom)
   (system/fontconfig)
   (system/fontconfig-shared-cache)
   (system/fonts)
   (system/gtk)
   (system/icons)
   (system/libs)
   (system/locale)
   (system/mime-cache)
   (system/themes)
   (system/x11)
   (system/xdg-cache-home "gnome-calculator")
   (system/xdg-config-home "ibus/bus")
   (system/xdg-data-dir "gnome-calculator")
   (system/xdg-data-dir "gtksourceview-4/styles")
   (system/xdg-data-dir "pixmaps")
   (system/xdg-data-home "gnome-calculator")
   (system/xdg-data-home "pixmaps")
   (system/xdg-runtime-dir "gdm/Xauthority")
   (system/xdg-runtime-dir "wayland-0")
   ;; why is calculator trying to read filesystems?
   ; (system/bind-ro-try "/proc/filesystems")
   (system/bind-ro-try executable)))

(defn notify-send [{:keys [executable]}]
  (->
   (base/base)
   (system/libs)
   (system/bind-ro executable)
   (system/dbus-talk "org.freedesktop.Notifications")))

   ;; notify-send tries to read locales for some reason
   ; (system/ro-bind-try "/proc/filesystems")])

   ;; also seems to try to read locales, are those needed for anything?
   ; (system/ro-bind-try "/usr/share/locale")
   ; (system/ro-bind-try "/usr/share/locale-langpack")])

;; Minimal sandbox, not really that useful in practice, but useful for debugging
;; issues with xdg-open and verifying it works inside a sandbox when other apps
;; depend on it for example for opening browser links.
(defn xdg-open []
  (->
   (base/base)
   (system/libs)
   (system/xdg-open)))

(comment
  (bwrap/ctx->args (chatall))

  (bwrap/ctx->args (cheese {:executable "/usr/bin/cheese"}))

  (bwrap/ctx->args (gedit {:executable "gedit"}))

  (bwrap/ctx->args (gnome-calculator {:executable "gnome-calculator"}))

  (bwrap/ctx->args (notify-send {:executable "notify-send"}))

  (bwrap/ctx->args (xdg-open)))
