(ns firewrap.profile.claude
  (:require
   [babashka.fs :as fs]
   [babashka.process :refer [shell]]
   [clojure.string :as str]
   [firewrap.preset.base :as base]
   [firewrap.preset.dumpster :as dumpster]
   [firewrap.preset.env :as env]
   [firewrap.preset.oldsystem :as system]
   [firewrap.sandbox :as sb]))

;; Binding git dir in worktrees so that Claude is able to commit, view diffs, etc
;; But isolating .git might also have its appeal, it would prevent potentially destroying contents of .git
(defn- bind-worktree-git [ctx]
  (try
    (let [git-common-dir (-> (shell {:dir (sb/cwd ctx) :out :string} "git" "rev-parse" "--git-common-dir")
                             :out
                             str/trim)
          worktree-outside? (and (not (str/blank? git-common-dir))
                                 (not (fs/starts-with? (fs/canonicalize git-common-dir)
                                                       (fs/canonicalize (sb/cwd ctx)))))]
      (cond-> ctx
        worktree-outside?
        (sb/bind-rw-try (str (fs/canonicalize git-common-dir)))))
    (catch Exception e
      (binding [*out* *err*]
        (println "Warning: Failed during worktree git dir detection" e))
      ctx)))

(defn- common [ctx]
  (-> ctx
      (base/configurable {:opts {:cwd true
                                 :net true
                                 :home "claude"}})
      (sb/bind-ro-try "/etc/claude-code")
      ;; bind claude files, alternative consider symlinking to sandbox dir
      (sb/bind-rw-try (dumpster/home ctx ".claude"))
      (sb/bind-rw-try (dumpster/home ctx ".claude.json"))
      (sb/bind-rw-try (dumpster/home ctx ".config/claude"))
      (sb/bind-rw-try (dumpster/home ctx ".cache/claude-cli-nodejs"))
      ;; Installed via Bun
      ;; have its own packages, install inside sandbox with:
      ;; fw --profile claude -- bun install -g @anthropic-ai/claude-code
      ;; (sb/bind-ro-try (str (dumpster/home ctx) "/.bun"))
      ;; To use the usual name when committing
      (sb/bind-ro-try (dumpster/home ctx ".gitconfig"))
      ;; bind git common dir for worktrees
      (bind-worktree-git)
      ;; For development of lua plugins
      (sb/env-pass "LUA_PATH")
      ;; for IDE integration
      (sb/env-pass-many ["CLAUDE_CODE_SSE_PORT" "ENABLE_IDE_INTEGRATION"])
      (sb/share-pid)))

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
