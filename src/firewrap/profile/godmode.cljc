(ns firewrap.profile.godmode
  (:require
   [firewrap.preset.appimage :as appimage]
   [firewrap.preset.base :as base]
   [firewrap.sandbox :as sb]))

(defn profile [appimage parsed]
  (sb/$-> (base/base5)
    (base/configurable parsed {:home "godmode" :net true})
    (appimage/run appimage)))
