(ns firewrap2.bwrap)

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

(defn bind-dev
  ([ctx path] (bind-dev ctx path path))
  ([ctx path dest-or-opts]
   (if (string? dest-or-opts)
     (bind-dev ctx path dest-or-opts {})
     (bind-dev ctx path path dest-or-opts)))
  ([ctx src dest {:keys [perms try]}]
   (bind ctx src dest {:perms perms :try try :access :dev})))

(def heredoc-terminator "FIREWRAP_HEREDOC_TERMINATOR")

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

(defn tmpfs [ctx path]
  (add-raw-args ctx ["--tmpfs" path]))

(defn symlink [ctx target link])

(defn chdir [ctx path]
  (add-raw-args ctx ["--chdir" path]))

(defn die-with-parent [ctx]
  (add-raw-args ctx ["--die-with-parent"]))

(defn new-session [ctx]
  (add-raw-args ctx ["--new-session"]))

(defn new-session-disable [ctx])

(defn unshare-all [ctx]
  (add-raw-args ctx ["--unshare-all"]))

(defn share-net [ctx]
  (add-raw-args ctx ["--share-net"]))

(defn env-set [ctx k v]
  (assoc-in ctx [::envs-sandbox k] v))

(defn env-pass [ctx k]
  (assoc-in ctx [::envs-sandbox k] ::pass))

(defn getenvs [ctx]
  (::envs-system ctx))

(defn getenv [ctx x]
  ;; maybe lookup also in ::envs-sandbox
  (get (getenvs ctx) x))

(defn populate-envs! [ctx]
  (assoc ctx ::envs-system (into {} (System/getenv))))

(defn ctx->args [ctx]
  (flatten (concat
            (::args ctx)
            (::cmd-args ctx)
            (::heredoc-args ctx))))
