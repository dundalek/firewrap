(ns init
  (:require
   [firewrap.preset.appimage :as appimage]
   [firewrap.preset.base :as base]
   [firewrap.preset.dumpster :as dumpster]
   [firewrap.preset.oldsystem :as system]
   [firewrap.profile :as profile]
   [firewrap.profile.claude :as claude]
   [firewrap.profile.cursor :as cursor]
   [firewrap.profile.godmode :as godmode]
   [firewrap.profile.windsurf :as windsurf]
   [firewrap.sandbox :as sb]))

(alter-var-root #'base/bind-user-programs (constantly dumpster/bind-nix-profile))
(alter-var-root #'base/bind-extra-system-programs (constantly dumpster/bind-nix-root))

(profile/register!
 "godmode"
 (fn [_]
   (godmode/profile
    #_(dumpster/glob-one (str (System/getenv "HOME") "/bin/vendor/GodMode/release/build/") "GodMode-*.AppImage")
    ;; hardcoding version because glob picks up then -arm64 appimage variant
    (str (System/getenv "HOME") "/bin/vendor/GodMode-1.0.0-beta.9.AppImage"))))

(profile/register!
 "windsurf"
 (partial windsurf/profile-with-options
          {:windsurf-dir (dumpster/glob-one (str (System/getenv "HOME") "/bin/vendor") "Windsurf-linux-x64-*/Windsurf")}))

;; try to run windsurf from nix-env
#_(profile/register!
   "windsurf"
   (fn [{:keys [args opts]}]
     (let [windsurf-bin "windsurf"
           windsurf-args (concat [windsurf-bin]
                                 ;; when restricting to cwd, opening a different folder will load it in existing instance so files will not be visible
                                 ;; or specify different --user-data-dir ?
                                 (when (:cwd opts) ["--new-window"])
                                 (rest args))]
       (-> (windsurf/profile nil)
           (sb/set-cmd-args windsurf-args)))))

(profile/register!
 "cursor"
 (fn [{:keys [args opts]}]
   (let [cursor-args (concat
                      ;; when restricting to cwd, opening a different folder will load it in existing instance so files will not be visible
                      ;; or specify different --user-data-dir ?
                      (when (:cwd opts) ["--new-window"])
                      (rest args))
         appimage (dumpster/glob-one (str (System/getenv "HOME") "/Applications") "Cursor-*-x86_64.AppImage")
         ctx (-> (cursor/profile nil))]
     (apply appimage/run ctx appimage cursor-args))))

(profile/register!
 "wscribe"
 (fn [_]
   (-> (base/base5)
       (base/configurable {:opts {:cwd true
                                  ; :net true
                                  :home "wscribe"}})
       (sb/env-set "WSCRIBE_MODELS_DIR" "wscribe_models"))))

(defn jank [ctx]
  (let [jank-dir (dumpster/home ctx "Downloads/git/jank")]
    (sb/$-> ctx
      (sb/bind-ro jank-dir)
      (sb/env-set "PATH" (str jank-dir "/compiler+runtime/build:" (sb/getenv ctx "PATH"))))))

(defn agents-common [ctx]
  (sb/$-> ctx
    (sb/warning
     "too broad - should restrict to subfolder per project"
     (sb/bind-rw-try (dumpster/home ctx "Dropbox/other/logseq/beans")))
    (sb/bind-rw-try (dumpster/home ctx "Dropbox/myfiles/obsidian/beans"))
    (sb/bind-rw-try (dumpster/home ctx "Dropbox/other/logseq/skills"))
    (sb/bind-rw-try (dumpster/home ctx "Dropbox/other/logseq/commands"))))

(defn claude [opts]
  (let [ctx (claude/wide opts)
        aineko-dir (dumpster/home ctx "projects/aineko/code/aineko")]
    (sb/$-> ctx
      (jank)
      (system/command "aineko")
      (system/command "claude-stream")
      ;; only bind aineko when not running from within it as cwd
      ((if (not= (sb/cwd ctx) aineko-dir)
         #(sb/bind-ro % aineko-dir)
         identity))
      (sb/env-pass "AINEKO_SEANCE_ID")
      (sb/env-pass "AINEKO_SOCKET_PATH")
      (sb/env-pass "CURRENT_BEANS_ID")
      (sb/env-pass "BD_NO_DAEMON")
              ;; temporary for testing D-BUS implementation
              ; (system/dbus-system-bus))]
      (agents-common))))

(profile/register! "claude" claude)
;; Babashka does not work in narrow sandbox, bb fails with:
;; Fatal error: Failed to create the main Isolate. (code 32)
; (profile/register! "claude" claude/narrow)

(profile/register!
 "cljdev"
 (fn [opts]
   (sb/$-> opts
     (claude)
     ;; claude profile shares pid namespace which causes issue with Ctrl+C, unsharing all back as a workaround
     (sb/unshare-all))))

(defn lazygit-wide [opts]
  ;; base5 to get /dev/pty etc.
  (let [ctx (base/base5 {:unsafe-session true})]
    (sb/$->
      ctx
      ;; override to always include CWD to be able to change local sources and .git
      (base/configurable (assoc-in opts [:opts :cwd] true))
      ;; needs RW access because tries to store state.yml in config dir
      (system/bind-rw-try-many (system/xdg-config-home-paths ctx "jesseduffield/lazygit"))
      (system/bind-rw-try-many (system/xdg-config-home-paths ctx "lazygit"))
      ;; target where configs are symlinked using stow
      (sb/bind-rw-try (dumpster/home ctx "dotfiles/git"))

      ;; Consider extract to a separate git profile and compose
      (sb/bind-ro-try (dumpster/home ctx ".gitconfig")))))

(profile/register! "lazygit" lazygit-wide)

(defn claude-skills [ctx]
  (sb/$->
    ctx
    (sb/bind-ro-try (dumpster/home ctx ".claude"))
    ; (sb/bind-ro-try (dumpster/home ctx ".config/claude"))
    (agents-common)))

(defn gemini-wide [opts]
  (let [ctx (base/base4)]
    (sb/$->
      ctx
      ;; override to always include CWD to be able to change local sources and .git
      (base/configurable (update opts :opts assoc
                                 :cwd true
                                 :net true
                                 :home "gemini"))

      (sb/bind-rw-try (dumpster/home ctx ".gemini"))
      (claude-skills))))

(profile/register! "gemini" gemini-wide)

(defn kilocode-wide [opts]
  (let [ctx (base/base4)]
    (sb/$->
      ctx
      ;; override to always include CWD to be able to change local sources and .git
      (base/configurable (update opts :opts assoc
                                 :cwd true
                                 :net true
                                 :home "cursor-agent"))
      (claude-skills))))

(profile/register! "cursor-agent" kilocode-wide)

(defn amp-wide [opts]
  ;; Amp needs unsafe-session otherwise fails to run, can't open /dev/tty
  (let [ctx (base/base4 {:unsafe-session true})]
    (sb/$->
      ctx
      ;; override to always include CWD to be able to change local sources and .git
      (base/configurable (update opts :opts assoc
                                 :cwd true
                                 :net true
                                 :home "amp"))
      (claude-skills))))

(profile/register! "amp" amp-wide)

(defn chromium-wide [opts]
  (let [ctx (base/base5)]
    (sb/$->
      ctx
      (base/configurable (-> opts
                             (update :opts assoc
                                     :net true)
                             (update-in [:opts :home] #(or % (first (:args opts)))))))))

(profile/register! "browser" chromium-wide)

(defn kilocode-wide [opts]
  (let [ctx (base/base4)]
    (sb/$->
      ctx
      ;; override to always include CWD to be able to change local sources and .git
      (base/configurable (update opts :opts assoc
                                 :cwd true
                                 :net true
                                 :home "kilocode"))
      (claude-skills))))

(profile/register! "kilocode" kilocode-wide)
