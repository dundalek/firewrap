(ns firewrap2.main
  (:require
   [babashka.cli :as cli]
   [babashka.fs :as fs]
   [babashka.process :as process]
   [clojure.string :as str]
   [firewrap2.bwrap :as bwrap]
   [firewrap2.preset.appimage :as appimage]
   [firewrap2.profile.godmode :as godmode]
   [firewrap2.profile.windsurf :as windsurf]))

(defonce !registry (atom {}))

(defn resolve-builtin-profile [appname]
  (try
    (requiring-resolve (symbol (str "firewrap2.profile." appname) "profile"))
    (catch Exception _)))

(defn profile-resolve [name]
  (or (get @!registry name)
      (resolve-builtin-profile name)))

(defn profile-register! [name profile]
  (swap! !registry assoc name profile))

(defn path->appname [path]
  (some-> (re-find #"([^/]+)$" path)
          second
          (str/lower-case)))

(def cli-spec
  {:profile {:desc ""
             :ref "<profile>"}
   :dry-run {:desc "Only print bubblewrap arguments but don't execute"}
   :base {:desc ""
          :alias :b}
   :home {:desc ""
          :alias :h}
   :tmphome {:desc ""
             :alias :t}
   :cwd {:desc ""
         :alias :c}
   :net {:desc ""
         :alias :n}})

(defn parse-args [args]
  (let [appname (path->appname (first args))
        firewrap? (#{"firewrap" "frap" "fw"} appname)]
    (if (some #{"--"} args)
      (if firewrap?
        (cli/parse-args (rest args) {:spec cli-spec})
        (-> (merge
             (cli/parse-args (rest args) {:spec cli-spec}))
            (update :opts #(merge {:profile appname} %))
            (update :args #(into [(first args)] %))))
      (if firewrap?
        {:args (rest args) :opts {}}
        {:args args :opts {:profile appname}}))))

(comment
  (parse-args ["fw" "-chn" "--" "cmd"])
  (parse-args ["fw" "-hnc" "--" "cmd"]))

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
        (process/exec "sh" f)))))

(defn run-bwrap-exec [args {:keys [dry-run]}]
  (let [params (cons "bwrap" (unwrap-raw args))]
    (println "Firewrap sandbox:" params)
    (when-not dry-run
      (apply process/exec params))))

(defn needs-bwrap-sh-wrapper? [args]
  (->> args
       (some (fn [s] (when (string? s)
                       (str/includes? s "--ro-bind-data"))))))

(defn run-bwrap [ctx opts]
  (let [args (bwrap/ctx->args ctx)]
    (if (needs-bwrap-sh-wrapper? args)
      (run-bwrap-sh-wrapper args opts)
      (run-bwrap-exec args opts))))

(defn main [& root-args]
  (let [{:keys [opts args] :as parsed} (parse-args root-args)
        {:keys [profile dry-run]} opts]
    (if-some [profile-fn (profile-resolve profile)]
      (run-bwrap (profile-fn parsed) {:dry-run dry-run})
      (println "help"))))

(profile-register!
 "godmode"
 (fn [_]
   (godmode/profile "/home/me/bin/vendor/GodMode/release/build/GodMode-1.0.0-beta.9.AppImage")))

(profile-register!
 "windsurf"
 (fn [{:keys [args]}]
   (-> (windsurf/profile)
       (bwrap/add-raw-args (cons "/home/me/bin/vendor/Windsurf-linux-x64-1.1.2/Windsurf/windsurf" (rest args))))))

(comment
  (main "chrome")
  (main "xx")
  (main "godmode"))
