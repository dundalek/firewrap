(ns firewrap.profiles.godmode
  (:require
   [firewrap.env :as env]
   [firewrap.system :as system]))

(defn profile [appimage]
  (->
   ;; lazy sandbox
   (system/base)
   (env/set-allowed-vars env/allowed)
   (system/network)
   (system/bind-dev "/")
   (system/isolated-home "godmode")
   (system/tmp)
   (system/run-appimage appimage)))
