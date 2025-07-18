(ns init
  (:require
   [firewrap.preset.appimage :as appimage]
   [firewrap.preset.base :as base]
   [firewrap.preset.dumpster :as dumpster]
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

(profile/register! "claude" claude/wide)
;; Babashka does not work in narrow sandbox, bb fails with:
;; Fatal error: Failed to create the main Isolate. (code 32)
; (profile/register! "claude" claude/narrow)
