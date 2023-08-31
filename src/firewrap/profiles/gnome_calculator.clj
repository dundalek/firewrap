(ns firewrap.profiles.gnome-calculator
  (:require
   [firewrap.system :as system]))

(defn profile [{:keys [executable]}]
  (->
   (system/base)
   (system/libs)
   (system/fonts)
   (system/fontconfig)
   (system/fontconfig-shared-cache)
   (system/icons)
   (system/themes)
   ;; move glib to gtk?
   (system/bind-ro-try-many (system/xdg-data-dir-paths "glib-2.0" "pixmaps"))
   (system/dconf)
   (system/gtk)
   (system/dbus-unrestricted)

   (system/bind-ro-try "/dev/urandom")
   ;; there was no /usr/share/mime ?
   (system/bind-ro-try "/home/me/.local/share/mime")

   (system/bind-ro-try "/home/me/.cache/gnome-calculator")

   (system/bind-ro-try "/home/me/.config/ibus/bus")
   (system/bind-ro-try "/home/me/.local/share/gnome-calculator")

   ;; why is calculator trying to read filesystems?
   ; (system/bind-ro-try "/proc/filesystems")

   (system/bind-ro-try "/run/user/1000/at-spi")
   (system/bind-ro-try "/run/user/1000/at-spi/bus_1")

   (system/bind-ro-try "/run/user/1000/gdm")
   (system/bind-ro-try "/run/user/1000/gdm/Xauthority")

   (system/bind-ro-try "/run/user/1000/wayland-0")

   (system/bind-ro-try "/tmp/.X11-unix/X1")
   (system/bind-ro-try executable)))
