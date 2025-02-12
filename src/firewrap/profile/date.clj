(ns firewrap.profile.date
  (:require
   [firewrap.preset.base :as base]
   [firewrap.preset.oldsystem :as system]))

(defn profile [_]
  (-> (base/base)
      (system/libs)
      (system/bind-ro-try "/etc/localtime")
      (system/command "date")))
