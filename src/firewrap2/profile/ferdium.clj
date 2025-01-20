(ns firewrap2.profile.ferdium
  (:require
   [firewrap2.preset.base :as base]
   [firewrap2.preset.dumpster :as dumpster]))

(defn profile [_]
  (-> (base/base5)
      (dumpster/bind-isolated-home-with-user-programs "ferdium")
      (dumpster/network)))
