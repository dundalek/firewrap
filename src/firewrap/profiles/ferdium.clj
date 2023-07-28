(ns firewrap.profiles.ferdium
  (:require
   [firewrap.system :as system]))

(defn profile [appimage]
  (->
   (system/base)
   (system/network)
   (system/dev-bind "/")
   (system/isolated-home "ferdium")
   (system/tmp)
   ; (system/xdg-open)
   (system/run-appimage appimage)))
