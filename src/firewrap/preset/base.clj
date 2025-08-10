(ns firewrap.preset.base
  (:require
   [clojure.string :as str]
   [firewrap.sandbox :as sb]
   [firewrap.preset.dumpster :as dumpster]
   [firewrap.preset.env :as env]))

(defn bind-system-programs [ctx]
  (-> ctx
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
  (-> ctx
      (bind-system-programs)
      (bind-extra-system-programs)))

(defn bind-user-programs [ctx]
  ;; no-op as a placeholder for extenion point
  ctx)

(defn bind-isolated-home-with-user-programs [ctx appname]
  (-> ctx
      (dumpster/bind-isolated-home appname)
      (bind-user-programs)))

(defn bind-isolated-tmphome-with-user-programs [ctx]
  (-> ctx
      (dumpster/bind-isolated-tmphome)
      (bind-user-programs)))

(defn base
  "Base with basic bubblewrap flags, does not grant any resources."
  ([] (base {}))
  ([{:keys [unsafe-session]}]
   (-> {}
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
     (-> ctx
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
     (-> ctx
         (sb/env-pass-many env/allowed)
         (sb/bind-dev "/")
         (sb/tmpfs (dumpster/home ctx))
         (sb/tmpfs "/tmp")
         (bind-user-programs)))))

(defn base-gui []
  (-> (base5)
      ;; would need x11 proxying for better security
      (sb/bind-ro-try "/tmp/.X11-unix/X1")))

(defn base6
  ([] (base6 {}))
  ([{:keys [unsafe-session]}]
   (let [ctx (base5 {:unsafe-session unsafe-session})]
     (-> ctx
         ;; would need x11 proxying for better security
         (sb/bind-ro-try "/tmp/.X11-unix/X1")))))

(defn base8
  "Lower effort wider sandbox, does not filter env vars and /tmp, should work better for GUI programs"
  ([] (base8 {}))
  ([{:keys [unsafe-session]}]
   (let [ctx (base {:unsafe-session unsafe-session})]
     (-> ctx
         ;; passing all env vars
         (sb/env-pass-many (keys (sb/getenvs ctx)))
         (sb/bind-dev "/")
         (sb/tmpfs (dumpster/home ctx))
         ;(sb/tmpfs "/tmp")
         (bind-user-programs)))))

(defn base9 [ctx]
  (-> ctx
      (sb/bind-dev "/")
      (sb/tmpfs (dumpster/home ctx))))

(defn apply-bindings [ctx bindings]
  (reduce (fn [ctx [binding-type & args]]
            (let [bind-fn (case binding-type
                            :bind-ro sb/bind-ro
                            :bind-rw sb/bind-rw
                            :bind-dev sb/bind-dev)]
              (apply bind-fn ctx args)))
          ctx
          bindings))

(defn configurable [ctx params]
  (let [{:keys [opts args]} params
        {:keys [profile home tmphome cwd net bindings]} opts
        appname (or
                 (when (string? home) home)
                 profile
                 (dumpster/path->appname (first args)))]
    (cond-> ctx
      home (bind-isolated-home-with-user-programs appname)
      tmphome (bind-isolated-tmphome-with-user-programs)
      cwd (dumpster/bind-cwd-rw)
      net (dumpster/network)
      bindings (apply-bindings bindings))))
