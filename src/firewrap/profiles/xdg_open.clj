(ns firewrap.profiles.xdg-open
  (:require
   [firewrap.env :as env]
   [firewrap.system :as system]))

;; Minimal sandbox, not really that useful in practice, but useful for debugging
;; issues with xdg-open and verifying it works inside a sandbox when other apps
;; depend on it for example for opening browser links.
(defn profile []
  (->
   (system/base)
   (env/set-allowed-vars env/allowed)
   (system/libs)
   (system/xdg-open)))
