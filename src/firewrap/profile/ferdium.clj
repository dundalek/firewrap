(ns firewrap.profile.ferdium
  (:require
   [firewrap.preset.base :as base]
   [firewrap.preset.dumpster :as dumpster]
   [firewrap.sandbox :as sb]))

(defn profile [_]
  (sb/$-> (base/base5)
      (base/bind-isolated-home-with-user-programs "ferdium")
      (dumpster/network)))
