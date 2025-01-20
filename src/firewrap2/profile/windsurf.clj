(ns firewrap2.profile.windsurf
  (:require
   [firewrap2.preset.base :as base]
   [firewrap2.preset.dumpster :as dumpster]
   [firewrap2.preset.vscode :as vscode]))

(defn profile []
  (-> (base/base5)
      (base/bind-isolated-home-with-user-programs "windsurf")
      (dumpster/network)
      (vscode/vscode-nvim)))
