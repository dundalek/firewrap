(ns firewrap.profiles.chatall
  (:require
   [firewrap.system :as system]))

(defn profile [appimage]
  (->
   (system/base)
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
   (system/network)
   (system/xdg-open)
   (system/run-appimage appimage)))
