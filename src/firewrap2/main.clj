(ns firewrap2.main
  (:require
   [babashka.cli :as cli]
   [babashka.fs :as fs]
   [babashka.process :as process]
   [clojure.string :as str]
   [firewrap2.bwrap :as bwrap]
   [firewrap2.preset.base :as base]
   [firewrap2.preset.dumpster :as dumpster]
   [firewrap2.profile :as profile]
   [firewrap2.profile.godmode :as godmode]
   [firewrap2.profile.windsurf :as windsurf]))

(def ^:dynamic *exec-fn* process/exec)

(def cli-options
  {:profile {:desc ""
             :ref "<profile>"}
   :dry-run {:desc "Only print bubblewrap arguments but don't execute"}
   :help {:desc "Show help"}})

(def base-options
  {:base {:desc ""
          :alias :b}
   :home {:desc ""
          :alias :h}
   :tmphome {:desc ""
             :alias :t}
   :cwd {:desc ""
         :alias :c}
   :net {:desc ""
         :alias :n}})

(def cli-spec (merge cli-options base-options))

(defn parse-args [args]
  (let [appname (dumpster/path->appname (first args))
        firewrap? (#{"firewrap" "frap" "fw"} appname)
        parse (if (some #{"--"} args)
                #(cli/parse-args % {:spec cli-spec})
                (fn [args] {:args args :opts {}}))
        result (if firewrap?
                 (parse (rest args))
                 (-> (parse (rest args))
                     (update :opts #(merge {:profile appname} %))
                     (update :args #(into [(first args)] %))))]
    (if (= result {:opts {} :args ["--help"]})
      {:opts {:help true}}
      result)))

(defn print-help []
  (println "Run program in sanbox")
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
      (::bwrap/escaped arg)
      (escape-shell (str arg)))))

(defn unwrap-raw [args]
  (for [arg args]
    (if (map? arg)
      (::bwrap/escaped arg)
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
  (let [args (bwrap/ctx->args ctx)]
    (bwrap/*run-effects!* ctx)
    (if (needs-bwrap-sh-wrapper? args)
      (run-bwrap-sh-wrapper args opts)
      (run-bwrap-exec args opts))))

(defn main [& root-args]
  (let [{:keys [opts args] :as parsed} (parse-args root-args)
        {:keys [profile base dry-run help]} opts]
    (if (or help (empty? args))
      (print-help)
      (let [profile-fn (or (profile/resolve profile)
                           (constantly (if base (base/base5) (base/base))))
            ctx (base/configurable (profile-fn parsed) parsed)
            ctx (cond-> ctx
                  (nil? (bwrap/cmd-args ctx)) (bwrap/set-cmd-args args))]
        (run-bwrap ctx
                   {:dry-run dry-run})))))

;; == "user" config

(defn bind-user-programs [ctx]
  (-> ctx
      ;; need to rebind nix-profile again over home
      (bwrap/bind-ro (str (dumpster/home ctx) "/.nix-profile/bin"))))

(alter-var-root #'base/bind-user-programs (constantly bind-user-programs))

(profile/register!
 "godmode"
 (fn [_]
   (godmode/profile "/home/me/bin/vendor/GodMode/release/build/GodMode-1.0.0-beta.9.AppImage")))

(profile/register!
 "windsurf"
 (fn [{:keys [args opts]}]
   (let [windsurf-dir (dumpster/glob-one (str (System/getenv "HOME") "/bin/vendor") "Windsurf-linux-x64-*/Windsurf")
         windsurf-bin (str windsurf-dir "/windsurf")
         windsurf-args (concat [windsurf-bin]
                               ;; when restricting to cwd, opening a different folder will load it in existing instance so files will not be visible
                               ;; or specify different --user-data-dir ?
                               (when (:cwd opts) ["--new-window"])
                               (rest args))]
     (-> (windsurf/profile)
         (bwrap/bind-ro windsurf-dir)
         (bwrap/set-cmd-args windsurf-args)))))

(comment
  (main "chrome")
  (main "xx")
  (main "godmode"))
