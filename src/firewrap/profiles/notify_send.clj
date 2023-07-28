(ns firewrap.profiles.notify-send
  (:require
   [firewrap.system :as system]))

(defn profile []
  (->
   (system/base)
   (system/libs)
   ;; hardcoded binary path - take as parameter?
   (system/ro-bind "/usr/bin/notify-send")
   (system/dbus-talk "org.freedesktop.Notifications")))

   ;; notify-send tries to read locales for some reason
   ; (system/ro-bind-try "/proc/filesystems")])

   ;; also seems to try to read locales, are those needed for anything?
   ; (system/ro-bind-try "/usr/share/locale")
   ; (system/ro-bind-try "/usr/share/locale-langpack")])
