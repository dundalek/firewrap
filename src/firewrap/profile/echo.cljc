(ns firewrap.profile.echo
  (:require
   [firewrap.preset.base :as base]
   [firewrap.preset.oldsystem :as system]
   [firewrap.sandbox :as sb]))

(defn profile [_]
  (sb/$-> (base/base)
          (system/libs)
          (system/command "echo")))
