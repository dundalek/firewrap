(ns firewrap.profiles.godmode
  (:require
   [firewrap.system :as system]))

(defn profile [appimage]
  (->
   ;; lazy sandbox
   (system/base)
   (system/network)
   (system/bind-dev "/")
   (system/isolated-home "godmode")
   (system/tmp)
   (system/run-appimage appimage)))
