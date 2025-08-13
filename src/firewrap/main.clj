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

(def ^:dynamic *interactive* (some? (System/console)))

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

(def binding-options
  {:bind-ro {:desc "Read-only bind mount <src>:<dest> or <path>"
             :ref "<src>:<dest>"}
   :bind-rw {:desc "Read-write bind mount <src>:<dest> or <path>"
             :ref "<src>:<dest>"}
   :bind-dev {:desc "Device bind mount <src>:<dest> or <path>"
              :ref "<src>:<dest>"}})

(def env-options
  {:env-pass {:desc "Pass environment variable to sandbox"
              :ref "<env>"
              :collect []}
   :env-set {:desc "Set an environment variable"
             :ref "<var> <value>"
             :coerce [str]}
   :env-unset {:desc "Unset an environment variable"
               :ref "<var>"
               :collect []}})

(def cli-spec (merge cli-options base-options binding-options env-options))

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

(defn- parse-bind-spec [binding-type bind-spec]
  (if (re-find #":" bind-spec)
    (let [[src dest] (str/split bind-spec #":" 2)]
      [binding-type src dest])
    [binding-type bind-spec bind-spec]))

(defn parse-args [args]
  (let [bind-collector (atom [])
        local-cli-spec (reduce (fn [cli-spec binding-type]
                                 (assoc-in cli-spec [binding-type :collect]
                                           (fn [_acc path]
                                             (swap! bind-collector conj (parse-bind-spec binding-type path))
                                             nil)))
                               cli-spec
                               (keys binding-options))
        appname (dumpster/path->appname (first args))
        firewrap? (#{"firewrap" "frap" "fw"} appname)
        parse (if (some #{"--"} args)
                (fn [args]
                  (let [[firewrap-args cmd-args] (split-with (complement #{"--"}) args)
                        args (concat (preprocess-short-options firewrap-args) cmd-args)]
                    (cli/parse-args args {:spec local-cli-spec})))
                (fn [args] {:args args :opts {}}))
        result (cond-> (parse (rest args))
                 (not firewrap?)
                 (-> (update :opts #(merge {:profile appname} %))
                     (update :args #(into [(first args)] %))))
        bindings @bind-collector
        ;; Extract environment variables from the parsed options
        env-vars (let [{:keys [env-set env-unset]} (:opts result)]
                   (concat
                    ;; Process env-set values in pairs: ["VAR1" "value1" "VAR2" "value2"] -> [[:setenv "VAR1" "value1"] [:setenv "VAR2" "value2"]]
                    (when env-set
                      (if (even? (count env-set))
                        (map (fn [[var-name var-value]] [:setenv var-name var-value])
                             (partition 2 env-set))
                        (throw (ex-info "Invalid --env-set usage: each variable must have a value" {:args env-set}))))
                    (map (fn [v] [:unsetenv v]) (if (sequential? env-unset) env-unset (when env-unset [env-unset])))))]
    (if (= result {:opts {} :args ["--help"]})
      {:opts {:help true}}
      (cond-> result
        (seq bindings) (assoc-in [:opts :bindings] bindings)
        (seq env-vars) (assoc-in [:opts :env-vars] env-vars)
        ;; Remove the raw env-set/env-unset from opts since we've processed them
        :always (update :opts dissoc :env-set :env-unset)))))

(defn print-help []
  (println "Run program in sandbox")
  (println)
  (println "Usage: firewrap [<options> --] <command> [<args>]")
  (println)
  (println "Options:")
  (println (cli/format-opts {:spec cli-options}))
  (println)
  (println "Ad-hoc profile options:")
  (println (cli/format-opts {:spec base-options}))
  (println)
  (println "Sandbox options:")
  (println (cli/format-opts {:spec (merge binding-options env-options)})))

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

(defn print-sandbox-info [print-fn]
  (binding [*out* *err*]
    (when *interactive* (print "\033[90m"))
    (print-fn)
    (when *interactive* (print "\033[0m"))
    (println)))

;; Workaround to write bwrap command as temporary script because process/exec
;; can't pass content via file descriptors.
(defn run-bwrap-sh-wrapper [args {:keys [dry-run]}]
  (let [script (->> (cons "exec bwrap" (unwrap-escaping args))
                    (str/join " "))]
    (print-sandbox-info
     (fn []
       (println "Firewrap sandbox:" script)))
    (when-not dry-run
      (let [f (fs/file (fs/create-temp-file {:prefix "firewrap"}))]
        (spit f script)
        (*exec-fn* "sh" f)))))

(defn bwrap-args [args]
  (cons "bwrap" (unwrap-raw args)))

(defn run-bwrap-exec [args {:keys [dry-run]}]
  (let [params (bwrap-args args)
        formatted-lines (format-bwrap-args-preview params)]
    (print-sandbox-info
     (fn []
       (println "Firewrap sandbox:")
       (doseq [line formatted-lines]
         (println " " line))))
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
                                         (= base 6) (base/base6 {:unsafe-session unsafe-session})
                                         (= base 8) (base/base8 {:unsafe-session unsafe-session})
                                         (= base 9) (base/base9 {:unsafe-session unsafe-session})
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
