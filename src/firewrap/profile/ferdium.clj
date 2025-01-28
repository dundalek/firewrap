(ns firewrap.profile.ferdium
  (:require
   [firewrap.preset.base :as base]
   [firewrap.preset.dumpster :as dumpster]))

(defn profile [_]
  (-> (base/base5)
      (base/bind-isolated-home-with-user-programs "ferdium")
      (dumpster/network)))
