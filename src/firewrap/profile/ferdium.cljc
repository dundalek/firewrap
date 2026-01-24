(ns firewrap.profile.ferdium
  (:require
   [firewrap.preset.base :as base]
   [firewrap.sandbox :as sb]))

(defn profile [parsed]
  (sb/$-> (base/base5)
    (base/configurable parsed {:home "ferdium" :net true})))
