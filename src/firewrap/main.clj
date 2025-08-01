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
   [firewrap.sandbox :as sb]))

(def ^:dynamic *exec-fn* process/exec)

(defn load-user-config []
  (let [config-file (fs/xdg-config-home "firewrap/init.clj")]
    (when (fs/exists? config-file)
      (try
        (load-file (fs/file config-file))
        (catch Exception e
          (throw (ex-info "Warning: Failed to load user config" {:path (str config-file)} e)))))))

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

(def two-arg-opt? #{"--bind" "--ro-bind" "--dev-bind" "--bind-try" "--ro-bind-try" "--dev-bind-try" "--ro-bind-data" "--symlink"})
(def single-arg-opt? #{"-dev" "--dev" "--proc" "--tmpfs" "--chdir"})

(defn format-bwrap-args-preview [args]
  (loop [args args
         current-line []
         lines []]
    (if-let [[arg & xs] args]
      (cond
        (two-arg-opt? arg)
        (let [[binding-line remaining] (split-at 3 args)]
          (recur remaining
                 []
                 (cond-> lines
                   (seq current-line) (conj current-line)
                   :always (conj binding-line))))

        (single-arg-opt? arg)
        (let [[binding-line remaining] (split-at 2 args)]
          (recur remaining
                 []
                 (cond-> lines
                   (seq current-line) (conj current-line)
                   :always (conj binding-line))))

        :else
        (recur xs (conj current-line arg) lines))

      (->> (cond-> lines
             (seq current-line) (conj current-line))
           (map #(str/join " " %))))))

;; Workaround to write bwrap command as temporary script because process/exec
;; can't pass content via file descriptors.
(defn run-bwrap-sh-wrapper [args {:keys [dry-run]}]
  (let [script (->> (cons "exec bwrap" (unwrap-escaping args))
                    (str/join " "))]
    (binding [*out* *err*]
      (println "Firewrap sandbox:" script))
    (when-not dry-run
      (let [f (fs/file (fs/create-temp-file {:prefix "firewrap"}))]
        (spit f script)
        (*exec-fn* "sh" f)))))

(defn bwrap-args [args]
  (cons "bwrap" (unwrap-raw args)))

(defn run-bwrap-exec [args {:keys [dry-run]}]
  (let [params (bwrap-args args)
        formatted-lines (format-bwrap-args-preview params)]
    (binding [*out* *err*]
      (println "Firewrap sandbox:")
      (doseq [line formatted-lines]
        (println " " line)))
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
            ctx (sb/interpret-hiccup (profile-fn parsed))
            ctx (base/configurable ctx parsed)
            ctx (if (sb/cmd-args ctx)
                  ctx
                  (if (appimage/appimage-command? (first args))
                    (apply appimage/run ctx args)
                    (sb/set-cmd-args ctx args)))]
        (run-bwrap ctx
                   {:dry-run dry-run})))))

(comment
  (main "chrome")
  (main "xx")
  (main "godmode"))
