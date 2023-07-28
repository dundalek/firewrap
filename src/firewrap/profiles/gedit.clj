(ns firewrap.profiles.gedit
  (:require
   [firewrap.system :as system]))

(defn gtk []
  [(system/ro-bind-try "/etc/gtk-3.0")
   (system/ro-bind-try "/home/me/.config/gtk-3.0")])

(defn profile []
  [(system/base)
   (system/libs)
   ; (system/rw-bind "/home")
   ; (system/isolated-home "gedit")
   ; (system/dev-bind "/dev")
   ; (system/ro-bind "/etc")
   ; (system/dev-bind "/proc")

   ;; uses `enchant` which is a generic wrapper supporting multiple spell checkers
   ;; tries to also read some aspell/hspell/hunspell configs related to spellcheck

   (system/ro-bind-try "/etc/localtime")

   (system/ro-bind-try "/etc/locale.alias")

   (system/ro-bind-try (str (System/getenv "HOME") "/.config/ibus/bus"))

   (system/ro-bind-try "/home/me/.local/share/icons")
   (system/ro-bind-try "/home/me/.nix-profile/share/icons")

   (system/ro-bind-try "/home/me/.local/share/mime")
   (system/ro-bind-try "/home/me/.config/dconf/user")

   ; (system/ro-bind-try "/usr/share/gnome/mime")
   (system/ro-bind-try "/proc/filesystems")
   (system/ro-bind-try "/proc/mounts")

   (gtk)
   (system/dev-bind "/run")
   (system/dev-bind "/dev/urandom")
   (system/dbus-unrestricted)
   ; (system/dev-bind "/sys")
   (system/ro-bind "/usr")
   (system/fonts)
   (system/ro-bind-try "/usr/share/glib-2.0")
   (system/ro-bind "/usr/bin/gedit")
   ; (system/ro-bind "/usr/bin")
   ; (system/ro-bind "/usr/share")
   ; (system/dev-bind "/var")
   (system/ro-bind-try "/home/me/.cache/fontconfig")
   (system/ro-bind-try "/var/cache/fontconfig")
   ; (system/dev-bind "/tmp")])
   (system/tmp)
   (system/dev-bind "/tmp/.X11-unix")])
   ; (system/dev-bind "/tmp/.X11-unix/X11")])
