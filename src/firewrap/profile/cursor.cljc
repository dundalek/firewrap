(ns firewrap.profile.cursor
  (:require
   [firewrap.preset.base :as base]
   [firewrap.preset.dumpster :as dumpster]
   [firewrap.preset.vscode :as vscode]
   [firewrap.sandbox :as sb]))

(defn profile [_]
  (sb/$-> (base/base5)
      (base/bind-isolated-home-with-user-programs "cursor")
      (dumpster/network)
      (vscode/vscode-nvim)))
