(ns firewrap.profiles.gnome-calculator
  (:require
   [firewrap.system :as system]))

(defn profile [{:keys [executable]}]
  (->
   (system/base)
   ;; network for currency exchange rate downloads
   (system/network)
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
