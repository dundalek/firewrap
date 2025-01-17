(ns firewrap.profiles.ferdium
  (:require
   [firewrap.env :as env]
   [firewrap.system :as system]))

(defn profile [appimage]
  (->
   (system/base)
   (env/set-allowed-vars env/allowed)
   (system/network)
   ;; Make it tighter instead of dev binding /
   (system/bind-dev "/")
   (system/isolated-home "ferdium")
   (system/tmp)
   ; (system/xdg-open)
   (system/run-appimage appimage)))
