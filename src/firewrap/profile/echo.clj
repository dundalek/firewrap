(ns firewrap.profile.echo
  (:require
   [firewrap.preset.base :as base]
   [firewrap.preset.oldsystem :as system]))

(defn profile [_]
  (-> (base/base)
      (system/libs)
      (system/command "echo")))
