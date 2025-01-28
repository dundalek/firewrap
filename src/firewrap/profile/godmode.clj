(ns firewrap.profile.godmode
  (:require
   [firewrap.preset.appimage :as appimage]
   [firewrap.preset.base :as base]
   [firewrap.preset.dumpster :as dumpster]))

(defn profile [appimage]
  (-> (base/base5)
      (dumpster/bind-isolated-home "godmode")
      (dumpster/network)
      (appimage/run appimage)))
