(ns firewrap2.profile.windsurf
  (:require
   [firewrap2.preset.base :as base]
   [firewrap2.preset.dumpster :as dumpster]
   [firewrap2.preset.vscode :as vscode]))

(defn profile []
  (-> (base/base5)
      (dumpster/bind-isolated-home "windsurf")
      (dumpster/network)
      (vscode/vscode-nvim)))
