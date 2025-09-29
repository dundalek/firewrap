(ns firewrap.sandbox
  (:require
   [babashka.fs :as fs]
   [clojure.set :as set]
   [firewrap.tracer :as tracer]))

(def ^:private all-namespaces
  #{"user" "ipc" "pid" "net" "uts" "cgroup"})

(defn unsafe-escaped-arg [s]
  {::escaped s})

(defn- add-raw-args [ctx & args]
  (update ctx ::args (fnil into []) args))

(defn add-heredoc-args [ctx & args]
  (update ctx ::heredoc-args (fnil into []) args))

(defn cmd-args [ctx]
  (::cmd-args ctx))

(defn set-cmd-args [ctx args]
  (assoc ctx ::cmd-args args))

(defn bind [ctx src dest {:keys [perms try access]}]
  (let [option (cond-> (case access
                         :rw "--bind"
                         :ro "--ro-bind"
                         :dev "--dev-bind")
                 try (str "-try"))]
    (cond-> ctx
      perms (add-raw-args ["--perms " perms])
      :always (add-raw-args [option src dest]))))

(defn bind-ro-try
  ([ctx path] (bind-ro-try ctx path path))
  ([ctx path dest-or-opts]
   (if (string? dest-or-opts)
     (bind-ro-try ctx path dest-or-opts {})
     (bind-ro-try ctx path path dest-or-opts)))
  ([ctx src dest {:keys [perms]}]
   (bind ctx src dest {:perms perms :try true :access :ro})))

(defn bind-ro
  ([ctx path] (bind-ro ctx path path))
  ([ctx path dest-or-opts]
   (if (string? dest-or-opts)
     (bind-ro ctx path dest-or-opts {})
     (bind-ro ctx path path dest-or-opts)))
  ([ctx src dest {:keys [perms try]}]
   (bind ctx src dest {:perms perms :try try :access :ro})))

(defn bind-rw
  ([ctx path] (bind-rw ctx path path))
  ([ctx path dest-or-opts]
   (if (string? dest-or-opts)
     (bind-rw ctx path dest-or-opts {})
     (bind-rw ctx path path dest-or-opts)))
  ([ctx src dest {:keys [perms try]}]
   (bind ctx src dest {:perms perms :try try :access :rw})))

(defn bind-rw-try
  ([ctx path] (bind-rw-try ctx path path))
  ([ctx path dest-or-opts]
   (if (string? dest-or-opts)
     (bind-rw-try ctx path dest-or-opts {})
     (bind-rw-try ctx path path dest-or-opts)))
  ([ctx src dest {:keys [perms try]}]
   (bind ctx src dest {:perms perms :try true :access :rw})))

(defn bind-dev
  ([ctx path] (bind-dev ctx path path))
  ([ctx path dest-or-opts]
   (if (string? dest-or-opts)
     (bind-dev ctx path dest-or-opts {})
     (bind-dev ctx path path dest-or-opts)))
  ([ctx src dest {:keys [perms try]}]
   (bind ctx src dest {:perms perms :try try :access :dev})))

(defn bind-dev-try
  ([ctx path] (bind-dev-try ctx path path))
  ([ctx path dest-or-opts]
   (if (string? dest-or-opts)
     (bind-dev-try ctx path dest-or-opts {})
     (bind-dev-try ctx path path dest-or-opts)))
  ([ctx src dest {:keys [perms try]}]
   (bind ctx src dest {:perms perms :try true :access :dev})))

(def ^:private heredoc-terminator "FIREWRAP_HEREDOC_TERMINATOR")

;; maybe have separate functions like bind-file-ro bind-content-ro
(defn bind-data-ro [ctx {:keys [perms fd path file content]}]
  (assert (nat-int? fd))
  (assert (or (and file (not content))
              (and (not file) content)))
  (cond-> ctx
    perms (add-raw-args ["--perms" perms])
    :always (add-raw-args ["--ro-bind-data" fd path])
    file (add-raw-args (unsafe-escaped-arg (str fd "<")) file)
    ;; quote initial terminator to prevent shell expansion in the content
    content (add-heredoc-args (unsafe-escaped-arg (str fd "<<"))
                              (unsafe-escaped-arg (str "\"" heredoc-terminator "\"\n" content "\n" heredoc-terminator)))))

(defn dev [ctx path]
  (add-raw-args ctx ["--dev" path]))

(defn proc [ctx path]
  (add-raw-args ctx ["--proc" path]))

(defn tmpfs [ctx path]
  (add-raw-args ctx ["--tmpfs" path]))

(defn symlink [ctx target link]
  (add-raw-args ctx ["--symlink" target link]))

(defn chdir [ctx path]
  (add-raw-args ctx ["--chdir" path]))

(defn die-with-parent [ctx]
  (add-raw-args ctx ["--die-with-parent"]))

(defn new-session [ctx]
  (add-raw-args ctx ["--new-session"]))

(defn new-session-disable [ctx])

(defn unshare-all [ctx]
  (assoc ctx ::shared-namespaces #{}))

(defn- add-shared-namespace [ctx ns-type]
  (update ctx ::shared-namespaces (fnil conj #{}) ns-type))

(defn share-net [ctx]
  (add-shared-namespace ctx "net"))

(defn share-user [ctx]
  (add-shared-namespace ctx "user"))

(defn share-ipc [ctx]
  (add-shared-namespace ctx "ipc"))

(defn share-pid [ctx]
  (add-shared-namespace ctx "pid"))

(defn share-uts [ctx]
  (add-shared-namespace ctx "uts"))

(defn share-cgroup [ctx]
  (add-shared-namespace ctx "cgroup"))

(defn env-set [ctx k v]
  (assoc-in ctx [::envs-sandbox k] v))

(defn env-pass [ctx k]
  (assoc-in ctx [::envs-sandbox k] ::pass))

(defn env-pass-many [ctx ks]
  (reduce env-pass ctx ks))

(defn getenvs [ctx]
  (::envs-system ctx))

(defn getenv [ctx x]
  ;; maybe lookup also in ::envs-sandbox
  (get (getenvs ctx) x))

(defn ^:dynamic *populate-env!* [ctx]
  (-> ctx
      (assoc ::envs-system (into {} (System/getenv)))
      (assoc ::system-cwd (str (fs/absolutize (fs/cwd))))))

(defn cwd [ctx]
  (::system-cwd ctx))

(defn- env-args [ctx]
  ;; We want to follow Default Deny principle and only pass through allowed vars.
  ;; Although we use --unsetenv, we are still effectivelly Default Deny, because we compare to currently set vars and unset not allowed vars.
  ;; Alternative would be to --clearenv first and then --setenv, but that can leak variables to process list.
  (let [{::keys [envs-sandbox envs-system]} ctx
        unallowed (-> (set/difference
                       (set (keys envs-system))
                       (set (keys envs-sandbox)))
                      sort)
        setting (->> envs-sandbox
                     (filter (comp string? val))
                     (sort-by key))]
    (concat
     (for [v unallowed]
       ["--unsetenv" v])
     (for [[k v] setting]
       ["--setenv" k v]))))

(defn skip-own-symlink [[cmd & args]]
  (when cmd
    ;; maybe will also need to take into account PATH inside sandbox
    (let [cmd-name (fs/file-name cmd)
          [target other] (fs/which-all cmd-name)
          cmd-links-to-firewrap? (and target
                                      (fs/sym-link? target)
                                      (= "firewrap" (fs/file-name (fs/read-link target))))
          resolved-cmd (if (and cmd-links-to-firewrap? other) (str other) cmd)]
      (cons resolved-cmd args))))

(defn fx-create-dirs [ctx path]
  (update ctx ::fx (fnil conj []) [::fx-create-dirs path]))

(defn- namespace-args [ctx]
  (let [shared-namespaces (::shared-namespaces ctx)]
    (cond
      (empty? shared-namespaces)
      [["--unshare-all"]]

      (= shared-namespaces #{"net"})
      [["--unshare-all"]
       ["--share-net"]]

      :else
      (let [unshared-namespaces (sort (set/difference all-namespaces shared-namespaces))]
        (for [nspace unshared-namespaces]
          [(str "--unshare-" nspace)])))))

(defn ctx->args [ctx]
  (flatten (concat
            (namespace-args ctx)
            (::args ctx)
            (env-args ctx)
            (skip-own-symlink (::cmd-args ctx))
            (::heredoc-args ctx))))

(defn ^:dynamic *run-effects!* [ctx]
  (doseq [[fx arg] (::fx ctx)]
    (case fx
      ::fx-create-dirs (fs/create-dirs arg))))

(defmacro $->
  [& forms]
  `(tracer/span-> ~@forms))
