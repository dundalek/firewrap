(ns firewrap2.main
  (:require
   [babashka.cli :as cli]
   [babashka.fs :as fs]
   [babashka.process :as process]
   [clojure.string :as str]
   [firewrap2.bwrap :as bwrap]
   [firewrap2.preset.godmode :as godmode]))

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
             :ref "<profile>"}})

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

;; Workaround to write bwrap command as temporary script because process/exec
;; can't pass content via file descriptors.
(defn run-bwrap-sh-wrapper [args]
  (println "WARNING: TODO: sanitize args")
  (let [script (->> (cons "exec bwrap" args)
                    (str/join " "))
        f (fs/file (fs/create-temp-file {:prefix "firewrap"}))]
    (spit f script)
    (println "Firewrap sandbox:" script)
    (process/exec "sh" f)))

(defn run-bwrap-exec [args]
  (let [params (cons "bwrap" args)]
    (println "Firewrap sandbox:" params)
    (apply process/exec params)))

(defn needs-bwrap-sh-wrapper? [args]
  (->> args
       (some (fn [s] (when (string? s)
                       (str/includes? s "--ro-bind-data"))))))

(defn run-bwrap [ctx]
  (let [args (bwrap/ctx->args ctx)]
    (if (needs-bwrap-sh-wrapper? args)
      (run-bwrap-sh-wrapper args)
      (run-bwrap-exec args))))

(defn main [& root-args]
  (let [{:keys [opts args]} (parse-args root-args)
        {:keys [profile]} opts]
    (if-some [profile-fn (profile-resolve profile)]
      (run-bwrap (profile-fn))
      :help)))

(profile-register! "chrome" (fn [& args] :chrome))
(profile-register! "godmode" (fn [& args]
                               (godmode/preset "/home/me/bin/vendor/GodMode/release/build/GodMode-1.0.0-beta.9.AppImage")))

(comment
  (main "chrome")
  (main "xx")
  (main "godmode"))
