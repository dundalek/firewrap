(ns firewrap.profiles.notify-send
  (:require
   [firewrap.env :as env]
   [firewrap.system :as system]))

(defn profile [{:keys [executable]}]
  (->
   (system/base)
   (env/set-allowed-vars env/allowed)
   (system/libs)
   (system/bind-ro executable)
   (system/dbus-talk "org.freedesktop.Notifications")))

   ;; notify-send tries to read locales for some reason
   ; (system/ro-bind-try "/proc/filesystems")])

   ;; also seems to try to read locales, are those needed for anything?
   ; (system/ro-bind-try "/usr/share/locale")
   ; (system/ro-bind-try "/usr/share/locale-langpack")])
