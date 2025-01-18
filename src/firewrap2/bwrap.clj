(ns firewrap2.bwrap)

(defn add-raw-args [ctx & args]
  (update ctx ::args (fnil into []) args))

(defn add-bind-args [ctx & args]
  (update ctx ::bind-args (fnil into []) args))

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

(defn tmpfs [ctx path]
  (add-raw-args ctx ["--tmpfs" path]))

(defn symlink [ctx target link])

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
  (flatten (::args ctx)))
