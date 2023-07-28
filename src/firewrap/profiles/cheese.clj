(ns firewrap.profiles.cheese
  (:require
   [firewrap.system :as system]))

(defn profile []
  (->
   (system/base)
   ;; Why does cheese need network? - perhaps tries wifi connected webcams?
   (system/network)
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
   (system/dev-bind "/dev/null")

   (system/bind-ro "/usr/bin/cheese")
   (system/dev-bind "/run/user/1000")
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
   (system/dev-bind "/dev/urandom")
   (system/bind-ro-try "/usr/share/thumbnailers")
   (system/bind-ro-try "/usr/bin/gdk-pixbuf-thumbnailer")
   (system/bind-rw "/home/me/.cache/gnome-desktop-thumbnailer")
   (system/bind-rw "/home/me/.cache/thumbnails")))

   ; (system/dev-bind "/tmp/.X11-unix")])
