(ns firewrap.main
  (:require
   [babashka.cli :as cli]
   [babashka.fs :as fs]
   [babashka.process :as process]
   [clojure.string :as str]
   [firewrap.preset.appimage :as appimage]
   [firewrap.preset.base :as base]
   [firewrap.preset.dumpster :as dumpster]
   [firewrap.profile :as profile]
   [firewrap.profile.cursor :as cursor]
   [firewrap.profile.godmode :as godmode]
   [firewrap.profile.windsurf :as windsurf]
   [firewrap.sandbox :as sb]))

(def ^:dynamic *exec-fn* process/exec)

(def cli-options
  {:profile {:desc ""
             :ref "<profile>"}
   :dry-run {:desc "Only print bubblewrap arguments but don't execute"}
   :unsafe-session {:desc "Don't use --new-session option for bubblewrap (less secure)"}
   :help {:desc "Show help"}})

(def base-options
  {:base {:desc ""
          :alias :b}
   :gui {:desc ""
         :alias :g}
   :home {:desc ""
          :alias :h}
   :tmphome {:desc ""
             :alias :t}
   :cwd {:desc ""
         :alias :c}
   :net {:desc ""
         :alias :n}})

(def cli-spec (merge cli-options base-options))

(defn preprocess-short-options [args]
  (reduce
   (fn [processed arg]
     (or (when (and (string? arg)
                    (str/starts-with? arg "-")
                    (not (str/starts-with? arg "--")))
           (when-some [[_ base-val] (re-find #"b([0-9]+)" arg)]
             (let [remaining-opts (str/replace arg (str "b" base-val) "")]
               (cond-> processed
                 (not= remaining-opts "-") (conj remaining-opts)
                 :always (into ["--base" base-val])))))
         (conj processed arg)))
   [] args))

(defn parse-args [args]
  (let [appname (dumpster/path->appname (first args))
        firewrap? (#{"firewrap" "frap" "fw"} appname)
        parse (if (some #{"--"} args)
                (fn [args]
                  (let [[firewrap-args cmd-args] (split-with (complement #{"--"}) args)
                        args (concat (preprocess-short-options firewrap-args) cmd-args)]
                    (cli/parse-args args {:spec cli-spec})))
                (fn [args] {:args args :opts {}}))
        result (cond-> (parse (rest args))
                 (not firewrap?)
                 (-> (update :opts #(merge {:profile appname} %))
                     (update :args #(into [(first args)] %))))]
    (if (= result {:opts {} :args ["--help"]})
      {:opts {:help true}}
      result)))

(defn print-help []
  (println "Run program in sandbox")
  (println)
  (println "Usage: firewrap [<options> --] <command> [<args>]")
  (println)
  (println "Options:")
  (println (cli/format-opts {:spec cli-options}))
  (println)
  (println "Ad-hoc profile options:")
  (println (cli/format-opts {:spec base-options})))

(defn escape-shell [s]
  (if (re-matches #"^[-_a-zA-Z0-9]+$" s)
    s
    (str "'" (str/replace s "'" "'\\''") "'")))

(defn unwrap-escaping [args]
  (for [arg args]
    (if (map? arg)
      (::sb/escaped arg)
      (escape-shell (str arg)))))

(defn unwrap-raw [args]
  (for [arg args]
    (if (map? arg)
      (::sb/escaped arg)
      arg)))

;; Workaround to write bwrap command as temporary script because process/exec
;; can't pass content via file descriptors.
(defn run-bwrap-sh-wrapper [args {:keys [dry-run]}]
  (let [script (->> (cons "exec bwrap" (unwrap-escaping args))
                    (str/join " "))]
    (println "Firewrap sandbox:" script)
    (when-not dry-run
      (let [f (fs/file (fs/create-temp-file {:prefix "firewrap"}))]
        (spit f script)
        (*exec-fn* "sh" f)))))

(defn bwrap-args [args]
  (cons "bwrap" (unwrap-raw args)))

(defn run-bwrap-exec [args {:keys [dry-run]}]
  (let [params (bwrap-args args)]
    (println "Firewrap sandbox:" params)
    (when-not dry-run
      (apply *exec-fn* params))))

(defn needs-bwrap-sh-wrapper? [args]
  (->> args
       (some (fn [s] (when (string? s)
                       (str/includes? s "--ro-bind-data"))))))

(defn run-bwrap [ctx opts]
  (let [args (sb/ctx->args ctx)]
    (sb/*run-effects!* ctx)
    (if (needs-bwrap-sh-wrapper? args)
      (run-bwrap-sh-wrapper args opts)
      (run-bwrap-exec args opts))))

(defn main [& root-args]
  (let [{:keys [opts args] :as parsed} (parse-args root-args)
        {:keys [profile base gui dry-run help unsafe-session]} opts]
    (if (or help (empty? args))
      (print-help)
      (let [profile-fn (or (profile/resolve profile)
                           (constantly (cond
                                         (or (= base 4) (true? base)) (base/base4 {:unsafe-session unsafe-session})
                                         (= base 5) (base/base5 {:unsafe-session unsafe-session})
                                         gui (base/base-gui)
                                         :else (base/base {:unsafe-session unsafe-session}))))
            ctx (base/configurable (profile-fn parsed) parsed)
            ctx (if (sb/cmd-args ctx)
                  ctx
                  (if (appimage/appimage-command? (first args))
                    (apply appimage/run ctx args)
                    (sb/set-cmd-args ctx args)))]
        (run-bwrap ctx
                   {:dry-run dry-run})))))

;; == "user" config

(defn bind-nix-shell [ctx]
  (-> ctx
      ;; to make nixpkgs channels available and be able to run nix-shell inside sandbox
      (sb/bind-ro (str (dumpster/home ctx) "/.local/state/nix"))
      (sb/bind-ro (str (dumpster/home ctx) "/.nix-defexpr"))))

(defn bind-user-programs [ctx]
  (-> ctx
      ;; need to rebind nix-profile again over home
      (sb/bind-ro (str (dumpster/home ctx) "/.nix-profile"))
      (bind-nix-shell)))

(defn bind-extra-system-programs [ctx]
  (-> ctx
      (sb/bind-ro "/nix")))

(alter-var-root #'base/bind-user-programs (constantly bind-user-programs))
(alter-var-root #'base/bind-extra-system-programs (constantly bind-extra-system-programs))

(profile/register!
 "godmode"
 (fn [_]
   (godmode/profile
    #_(dumpster/glob-one (str (System/getenv "HOME") "/bin/vendor/GodMode/release/build/") "GodMode-*.AppImage")
    ;; hardcoding version because glob picks up then -arm64 appimage variant
    (str (System/getenv "HOME") "/bin/vendor/GodMode-1.0.0-beta.9.AppImage"))))

;; distributed as tarball
(defn windsurf-profile [{:keys [windsurf-dir]} {:keys [args opts]}]
  (let [windsurf-bin (str windsurf-dir "/windsurf")
        windsurf-args (concat [windsurf-bin]
                               ;; when restricting to cwd, opening a different folder will load it in existing instance so files will not be visible
                               ;; or specify different --user-data-dir ?
                              (when (:cwd opts) ["--new-window"])
                              (rest args))]
    (-> (windsurf/profile nil)
        (sb/bind-ro windsurf-dir)
        (sb/set-cmd-args windsurf-args))))

(profile/register!
 "windsurf"
 (partial windsurf-profile
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

(profile/register!
 "claude"
 (fn [_]
   (let [ctx (base/base5)]
     (-> ctx
         (base/configurable {:opts {:cwd true
                                    :net true
                                    :home "claude"}})
         ;; bind claude files, alternative consider symlinking to sandbox dir
         (sb/bind-rw-try (str (dumpster/home ctx) "/.claude"))
         (sb/bind-rw-try (str (dumpster/home ctx) "/.claude.json"))
         ;; Installed via Bun
         ;; have its own packages, install inside sandbox with:
         ;; fw --profile claude -- bun install -g @anthropic-ai/claude-code
         ; (sb/bind-ro-try (str (dumpster/home ctx) "/.bun"))
         ;; To use the usual name when committing
         (sb/bind-ro-try (str (dumpster/home ctx) "/.gitconfig"))))))

(comment
  (main "chrome")
  (main "xx")
  (main "godmode"))
