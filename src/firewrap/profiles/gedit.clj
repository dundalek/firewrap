(ns firewrap.profiles.gedit
  (:require
   [firewrap.system :as system]))

(defn gtk [ctx]
  (-> ctx
      (system/bind-ro-try "/etc/gtk-3.0")
      (system/bind-ro-try "/home/me/.config/gtk-3.0")))

(defn profile [{:keys [executable]}]
  (->
   (system/base)
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
    (system/xdg-data-dirs "icons" "mime"))

   (system/bind-ro-try "/home/me/.config/dconf/user")

   ; (system/ro-bind-try "/usr/share/gnome/mime")
   (system/bind-ro-try "/proc/filesystems")
   (system/bind-ro-try "/proc/mounts")

   (gtk)
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
