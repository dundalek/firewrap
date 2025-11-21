(ns firewrap.preset.base
  (:require
   [firewrap.preset.dumpster :as dumpster]
   [firewrap.preset.env :as env]
   [firewrap.preset.oldsystem :as system]
   [firewrap.sandbox :as sb]))

(defn bind-system-programs [ctx]
  (sb/$-> ctx
    (sb/bind-ro "/usr")
    (sb/symlink "usr/bin" "/bin")
    (sb/symlink "usr/sbin" "/sbin")
    (sb/symlink "usr/lib" "/lib")
    (sb/symlink "usr/lib64" "/lib64")
    (sb/bind-ro-try "/lib32")))

(defn bind-extra-system-programs [ctx]
  ;; no-op as a placeholder for extenion point
  ctx)

(defn bind-system-and-extra-programs [ctx]
  (sb/$-> ctx
    (bind-system-programs)
    (bind-extra-system-programs)))

(defn bind-user-programs [ctx]
  ;; no-op as a placeholder for extenion point
  ctx)

(defn bind-isolated-home-with-user-programs [ctx appname]
  (sb/$-> ctx
    (dumpster/bind-isolated-home appname)
    (bind-user-programs)))

(defn bind-isolated-tmphome-with-user-programs [ctx]
  (sb/$-> ctx
    (dumpster/bind-isolated-tmphome)
    (bind-user-programs)))

(defn base
  "Base with basic bubblewrap flags, does not grant any resources"
  ([] (base {}))
  ([{:keys [unsafe-session]}]
   (sb/$-> {}
     (sb/*populate-env!*)
     (sb/die-with-parent)
     (sb/unshare-all)
       ;; Create a new session to prevent using the TIOCSTI ioctl to push
       ;; characters into the terminal's input buffer, allowing an attacker to
       ;; escape the sandbox.
       ;; See https://github.com/containers/bubblewrap/issues/555
     (cond->
      (not unsafe-session) (sb/new-session)))))

(defn base4
  "More granular base with system files"
  ([] (base4 {}))
  ([{:keys [unsafe-session]}]
   (let [ctx (base {:unsafe-session unsafe-session})]
     (sb/$-> ctx
       (sb/env-pass-many env/allowed)
       (sb/dev "/dev")
       (sb/bind-ro "/etc")
       (sb/proc "/proc")
       (sb/tmpfs "/tmp")
       (bind-system-and-extra-programs)
       (sb/tmpfs (dumpster/home ctx))
       (bind-user-programs)))))
     ;; media, mnt, opt, root, run, srv, sys, var?

(defn base5
  "Low effort sandbox, includes system files with temporary home and empty tmp"
  ([] (base5 {}))
  ([{:keys [unsafe-session]}]
   (let [ctx (base {:unsafe-session unsafe-session})]
     (sb/$-> ctx
       (sb/env-pass-many env/allowed)
       (sb/bind-dev "/")
       (sb/tmpfs (dumpster/home ctx))
       (sb/tmpfs "/tmp")
       (bind-user-programs)))))

(defn base-gui []
  (sb/$-> (base5)
    (system/x11)))

(defn base6
  "Low effort sandbox with GUI support, includes X11 display binding"
  ([] (base6 {}))
  ([{:keys [unsafe-session]}]
   (let [ctx (base5 {:unsafe-session unsafe-session})]
     (sb/$-> ctx
       (system/x11)))))

(defn base8
  "Lower effort wider sandbox, does not filter env vars and /tmp, should work better for GUI programs"
  ([] (base8 {}))
  ([{:keys [unsafe-session]}]
   (let [ctx (base {:unsafe-session unsafe-session})]
     (sb/$-> ctx
       (sb/warning
        "passing all env vars"
        (sb/env-pass-many (keys (sb/getenvs ctx))))
       (sb/bind-dev "/")
       (sb/tmpfs (dumpster/home ctx))
         ;(sb/tmpfs "/tmp")
       (bind-user-programs)))))

(defn base9
  "Simplest wide sandbox with device bind mount and temporary home"
  ([] (base9 {}))
  ([{:keys [unsafe-session]}]
   (let [ctx (base {:unsafe-session unsafe-session})]
     (sb/$-> ctx
       (sb/bind-dev "/")
       (sb/tmpfs (dumpster/home ctx))))))

(defn apply-bindings [ctx bindings]
  (reduce (fn [ctx [binding-type & args]]
            (let [bind-fn (case binding-type
                            :bind-ro sb/bind-ro
                            :bind-rw sb/bind-rw
                            :bind-dev sb/bind-dev)]
              (apply bind-fn ctx args)))
          ctx
          bindings))

(defn- apply-env-vars [ctx env-vars]
  (reduce (fn [ctx [env-op & args]]
            (case env-op
              :setenv (let [[var-name var-value] args]
                        (sb/env-set ctx var-name var-value))
              :unsetenv (let [[var-name] args]
                          (update ctx ::sb/envs-sandbox dissoc var-name))))
          ctx
          env-vars))

(defn configurable [ctx params]
  (let [{:keys [opts args]} params
        {:keys [profile home tmphome cwd net bindings env-pass env-vars]} opts
        appname (or
                 (when (string? home) home)
                 profile
                 (dumpster/path->appname (first args)))]
    (cond-> ctx
      home (bind-isolated-home-with-user-programs appname)
      tmphome (bind-isolated-tmphome-with-user-programs)
      cwd (dumpster/bind-cwd-rw)
      net (dumpster/network)
      bindings (apply-bindings bindings)
      env-pass (sb/env-pass-many env-pass)
      env-vars (apply-env-vars env-vars))))
