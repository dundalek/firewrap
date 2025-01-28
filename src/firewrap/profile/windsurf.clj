(ns firewrap.profile.windsurf
  (:require
   [firewrap.preset.base :as base]
   [firewrap.preset.dumpster :as dumpster]
   [firewrap.preset.vscode :as vscode]))

(defn profile []
  (-> (base/base5)
      (base/bind-isolated-home-with-user-programs "windsurf")
      (dumpster/network)
      (vscode/vscode-nvim)))
