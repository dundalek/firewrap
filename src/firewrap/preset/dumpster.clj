(ns firewrap.preset.dumpster
  (:require
   [babashka.fs :as fs]
   [clojure.string :as str]
   [firewrap.sandbox :as sb]))

(defn path->appname [path]
  (some-> (re-find #"([^/]+)$" path)
          second
          (str/lower-case)))

(defn glob-one [root pattern]
  (-> (fs/glob root pattern)
      (first)
      (str)))

(defn home
  ([ctx]
   (sb/getenv ctx "HOME"))
  ([ctx path]
   (str (home ctx) "/" path)))

(defn bind-isolated-home [ctx appname]
  (let [source-path (home ctx (str "sandboxes/" appname))]
    (-> ctx
        (sb/fx-create-dirs source-path)
        (sb/bind-rw source-path (home ctx)))))

(defn bind-isolated-tmphome [ctx]
  (let [sandbox (str "tmp-" (-> (str (java.time.LocalDateTime/now))
                                (str/replace #"[^\w-]" "-")))]
    (-> ctx
        (bind-isolated-home sandbox))))

(defn bind-cwd-rw [ctx]
  (let [cwd (sb/cwd ctx)]
    (-> ctx
        (sb/bind-rw cwd)
        ;; Make sure cwd is set to current dir when we bind cwd
        (sb/chdir cwd))))

(defn network [ctx]
  (-> ctx
      (sb/share-net)
      ;; /run/systemd/resolve needs to be before /etc/resolv.conf which links to it
      (sb/bind-ro-try "/run/systemd/resolve")
      (sb/bind-ro-try "/etc/resolv.conf")
      (sb/bind-ro-try "/etc/gai.conf") ; getaddressinfo
      (sb/bind-ro-try "/etc/host.conf")
      (sb/bind-ro-try "/etc/hosts")
      (sb/bind-ro-try "/etc/nsswitch.conf")
      (sb/bind-ro-try "/etc/ssl/certs")))

(defn shell-profile [ctx]
  (-> ctx
      (sb/bind-ro-try "/etc/profile")
      (sb/bind-ro-try "/etc/profile.d")))

(defn bind-nix-profile [ctx]
  ;; to make nixpkgs channels available and be able to run nix-shell inside sandbox
  (-> ctx
      ;; need to rebind nix-profile again over home
      (sb/bind-ro (str (home ctx) "/.nix-profile"))
      (sb/bind-ro (str (home ctx) "/.local/state/nix"))
      (sb/bind-ro (str (home ctx) "/.nix-defexpr"))))

(defn bind-nix-root [ctx]
  (-> ctx
      (sb/bind-ro "/nix")))
