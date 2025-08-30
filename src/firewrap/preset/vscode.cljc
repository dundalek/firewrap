(ns firewrap.preset.vscode
  (:require
   [firewrap.sandbox :as sb]
   [firewrap.preset.dumpster :as dumpster]))

(defn vscode-nvim [ctx]
  (-> ctx
      ;; Applications because using nvim via app image
      (sb/bind-ro (str (dumpster/home ctx) "/Applications"))
      (sb/bind-ro (str (dumpster/home ctx) "/.config/nvim"))
      ;; where plugins are stored
      (sb/bind-ro (str (dumpster/home ctx) "/.local/share/nvim"))
      ;; local plugins
      (sb/bind-ro (str (dumpster/home ctx) "/code/parpar.nvim"))
      (sb/bind-ro (str (dumpster/home ctx) "/projects/keysensei/keysensei.nvim"))
      ;; RW
      (sb/bind-rw (str (dumpster/home ctx) "/.local/share/nvim/lazy/nvim-treesitter/parser"))
      (sb/bind-rw (str (dumpster/home ctx) "/.config/nvim/lazy-lock.json"))
      (sb/bind-rw (str (dumpster/home ctx) "/.local/share/nvim/lazy/lazy.nvim/doc/tags"))))
