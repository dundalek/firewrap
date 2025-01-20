(ns firewrap2.preset.base
  (:require
   [firewrap2.bwrap :as bwrap]
   [firewrap2.preset.dumpster :as dumpster]))

(defn bind-system-programs [ctx]
  (-> ctx
      (bwrap/bind-ro "/usr")
      (bwrap/symlink "usr/bin" "/bin")
      (bwrap/symlink "usr/bin" "/sbin")
      (bwrap/symlink "usr/lib" "/lib")
      (bwrap/symlink "usr/lib" "/lib64")))

(defn bind-extra-system-programs [ctx]
  (-> ctx
      (bwrap/bind-ro "/nix")))

(defn bind-user-programs [ctx]
  (-> ctx
      (bwrap/bind-ro (str (dumpster/home ctx) "/.nix-profile/bin"))))

(defn base []
  (-> {}
      (bwrap/populate-envs!)
      (bwrap/die-with-parent)
      (bwrap/unshare-all)
      ;; Create a new session to prevent using the TIOCSTI ioctl to push
      ;; characters into the terminal's input buffer, allowing an attacker to
      ;; escape the sandbox.
      ;; See https://github.com/containers/bubblewrap/issues/555
      (bwrap/new-session)))

(defn base4 [ctx]
  (-> (base)
      (bind-system-programs)))

(defn base5 []
  (let [ctx (base)]
    (-> ctx
      ; (env/set-allowed-vars env/allowed)
        (bwrap/bind-dev "/")
        (bwrap/tmpfs (dumpster/home ctx))
        (bwrap/tmpfs "/tmp"))))

(defn base9 [ctx]
  (-> ctx
      (bwrap/bind-dev "/")
      (bwrap/tmpfs (dumpster/home ctx))))

(defn configurable [ctx params]
  (let [{:keys [opts args]} params
        {:keys [profile home tmphome cwd net]} opts
        appname (or profile (dumpster/path->appname (first args)))]
    (cond-> ctx
      home (dumpster/bind-isolated-home appname)
      tmphome (dumpster/bind-isolated-tmphome)
      cwd (dumpster/bind-cwd-rw)
      net (dumpster/network))))
