(ns firewrap.preset.vscode
  (:require
   [firewrap.sandbox :as bwrap]
   [firewrap.preset.dumpster :as dumpster]))

(defn vscode-nvim [ctx]
  (-> ctx
      ;; Applications because using nvim via app image
      (bwrap/bind-ro (str (dumpster/home ctx) "/Applications"))
      (bwrap/bind-ro (str (dumpster/home ctx) "/.config/nvim"))
      ;; where plugins are stored
      (bwrap/bind-ro (str (dumpster/home ctx) "/.local/share/nvim"))
      ;; local plugins
      (bwrap/bind-ro (str (dumpster/home ctx) "/code/parpar.nvim"))
      (bwrap/bind-ro (str (dumpster/home ctx) "/projects/keysensei/keysensei.nvim"))
      ;; RW
      (bwrap/bind-rw (str (dumpster/home ctx) "/.local/share/nvim/lazy/nvim-treesitter/parser"))
      (bwrap/bind-rw (str (dumpster/home ctx) "/.config/nvim/lazy-lock.json"))
      (bwrap/bind-rw (str (dumpster/home ctx) "/.local/share/nvim/lazy/lazy.nvim/doc/tags"))))
