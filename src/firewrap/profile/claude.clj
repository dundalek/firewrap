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
      (sb/env-pass-many ["CLAUDE_CODE_SSE_PORT" "ENABLE_IDE_INTEGRATION"])
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
      ;; For development of lua plugins
      (sb/env-pass "LUA_PATH")))

(defn- custom-base
  "Base with basic bubblewrap flags, does not grant any resources."
  ([] (custom-base {}))
  ([{:keys [unsafe-session]}]
   (-> {}
       (sb/*populate-env!*)
       (sb/die-with-parent)
       ; (sb/unshare-all)
       (#'sb/add-raw-args
        ["--unshare-user"
         "--unshare-ipc"
         ;; We need same pid namespace for the claude IDE integration to work.
         #_"--unshare-pid"
         #_"--unshare-net"
         "--unshare-uts"
         "--unshare-cgroup"])
       ;; Create a new session to prevent using the TIOCSTI ioctl to push
       ;; characters into the terminal's input buffer, allowing an attacker to
       ;; escape the sandbox.
       ;; See https://github.com/containers/bubblewrap/issues/555
       (cond->
        (not unsafe-session) (sb/new-session)))))

(defn wide [_]
  (with-redefs [base/base custom-base]
    (-> (base/base4)
        (common))))

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
