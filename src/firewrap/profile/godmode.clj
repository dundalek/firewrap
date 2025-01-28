(ns firewrap2.profile.godmode
  (:require
   [firewrap2.preset.appimage :as appimage]
   [firewrap2.preset.base :as base]
   [firewrap2.preset.dumpster :as dumpster]))

(defn profile [appimage]
  (-> (base/base5)
      (dumpster/bind-isolated-home "godmode")
      (dumpster/network)
      (appimage/run appimage)))
