(ns firewrap.profiles.chatall
  (:require
   [firewrap.system :as system]))

(defn profile [appimage]
  [(system/base)
   (system/isolated-home "chatall")
     ;; Figure out which specific binaries are needed
   "--ro-bind-try /usr/bin /usr/bin"
   (system/libs)
   (system/processes)
   (system/tmp)
   (system/dev-bind "/dev/null")
   (system/dev-bind "/dev/urandom")
   (system/dev-bind "/dev/shm")
   (system/gpu)
   (system/fonts)
   (system/system-network)
   (system/xdg-open)
   (system/run-appimage appimage)])
