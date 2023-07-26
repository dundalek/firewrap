(ns firewrap.profiles.notify-send
  (:require
   [firewrap.system :as system]))

(defn profile []
  [(system/base)
   (system/libs)
   ;; hardcoded binary path - take as parameter?
   (system/ro-bind "/usr/bin/notify-send")
   ;; hardcoded userid in the bus path
   ; (system/ro-bind "/run/user/1000/bus")
   (system/ro-bind "/tmp/my-dbus-proxy" "/run/user/1000/bus")])
   ;; also seems to try to read locales, are those needed for anything?
