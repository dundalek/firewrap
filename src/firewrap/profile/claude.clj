(ns firewrap.profile.claude
  (:require
   [firewrap.preset.base :as base]
   [firewrap.preset.dumpster :as dumpster]
   [firewrap.preset.env :as env]
   [firewrap.preset.oldsystem :as system]
   [firewrap.sandbox :as sb]))

(defn- common [ctx]
  (-> ctx
      (base/configurable {:opts {:cwd true
                                 :net true
                                 :home "claude"}})
      (sb/bind-ro-try "/etc/claude-code")
      ;; bind claude files, alternative consider symlinking to sandbox dir
      (sb/bind-rw-try (dumpster/home ctx ".claude"))
      (sb/bind-rw-try (dumpster/home ctx ".claude.json"))
      ;; Installed via Bun
      ;; have its own packages, install inside sandbox with:
      ;; fw --profile claude -- bun install -g @anthropic-ai/claude-code
      ;; (sb/bind-ro-try (str (dumpster/home ctx) "/.bun"))
      ;; To use the usual name when committing
      (sb/bind-ro-try (dumpster/home ctx "/.gitconfig"))))

(defn wide [_]
  (-> (base/base4)
      (common)))

(defn narrow [_]
  (-> (base/base)
      (sb/env-pass-many env/allowed)
      (base/bind-system-and-extra-programs)
      (system/dev-null)
      (system/dev-pts)
      (system/locale)
      (system/timezone)
      (system/tmp)
      ; (dumpster/shell-profile)
      ; seems to use following, but works without:
      ; `/proc/sys/vm/overcommit_memory` - `/proc/version_signature` - `/proc/meminfo` - `/proc/self`
      ; (dumpster/proc)
      (common)))
