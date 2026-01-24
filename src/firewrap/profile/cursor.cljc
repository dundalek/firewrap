(ns firewrap.profile.cursor
  (:require
   [firewrap.preset.base :as base]
   [firewrap.preset.vscode :as vscode]
   [firewrap.sandbox :as sb]))

(defn profile [parsed]
  (sb/$-> (base/base5)
    (base/configurable parsed {:home "cursor" :net true})
    (vscode/vscode-nvim)))
