(ns firewrap.preset.base
  (:require
   [firewrap.sandbox :as sb]
   [firewrap.preset.dumpster :as dumpster]
   [firewrap.preset.env :as env]))

(defn bind-system-programs [ctx]
  (-> ctx
      (sb/bind-ro "/usr")
      (sb/symlink "usr/bin" "/bin")
      (sb/symlink "usr/bin" "/sbin")
      (sb/symlink "usr/lib" "/lib")
      (sb/symlink "usr/lib" "/lib64")))

(defn bind-extra-system-programs [ctx]
  (-> ctx
      (sb/bind-ro "/nix")))

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

(defn base []
  (-> {}
      (sb/*populate-env!*)
      (sb/die-with-parent)
      (sb/unshare-all)
      ;; Create a new session to prevent using the TIOCSTI ioctl to push
      ;; characters into the terminal's input buffer, allowing an attacker to
      ;; escape the sandbox.
      ;; See https://github.com/containers/bubblewrap/issues/555
      (sb/new-session)))

(defn base4 [ctx]
  (-> (base)
      (bind-system-programs)))

(defn base5 []
  (let [ctx (base)]
    (-> ctx
        (sb/env-pass-many env/allowed)
        (sb/bind-dev "/")
        (sb/tmpfs (dumpster/home ctx))
        (sb/tmpfs "/tmp")
        (bind-user-programs))))

(defn base-gui []
  (-> (base5)
      ;; would need x11 proxying for better security
      (sb/bind-ro-try "/tmp/.X11-unix/X1")))

(defn base9 [ctx]
  (-> ctx
      (sb/bind-dev "/")
      (sb/tmpfs (dumpster/home ctx))))

(defn configurable [ctx params]
  (let [{:keys [opts args]} params
        {:keys [profile home tmphome cwd net]} opts
        appname (or
                 (when (string? home) home)
                 profile
                 (dumpster/path->appname (first args)))]
    (cond-> ctx
      home (bind-isolated-home-with-user-programs appname)
      tmphome (bind-isolated-tmphome-with-user-programs)
      cwd (dumpster/bind-cwd-rw)
      net (dumpster/network))))
