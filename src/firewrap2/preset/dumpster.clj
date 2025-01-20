(ns firewrap2.preset.dumpster
  (:require
   [babashka.fs :as fs]
   [clojure.string :as str]
   [firewrap2.bwrap :as bwrap]))

(defn path->appname [path]
  (some-> (re-find #"([^/]+)$" path)
          second
          (str/lower-case)))

(defn glob-one [root pattern]
  (-> (fs/glob root pattern)
      (first)
      (str)))

(defn home [ctx]
  (bwrap/getenv ctx "HOME"))

(defn bind-isolated-home [ctx appname]
  (let [HOME (home ctx)
        source-path (str HOME "/sandboxes/" appname)]
    (println  "WARNING: TODO: create-dirs side-effect! Ideally make it pure and interpret side effects separately")
    (fs/create-dirs source-path)
    (-> ctx
        (bwrap/bind-rw source-path HOME))))

(defn bind-isolated-tmphome [ctx]
  (let [sandbox (str "tmp-" (-> (str (java.time.LocalDateTime/now))
                                (str/replace #"[^\w-]" "-")))]
    (-> ctx
        (bind-isolated-home sandbox))))

(defn bind-cwd-rw [ctx]
  (println "WARNING: side-effect getting cwd")
  (let [cwd (str (fs/absolutize (fs/cwd)))]
    (-> ctx
        (bwrap/bind-rw cwd)
        ;; Make sure cwd is set to current dir when we bind cwd
        (bwrap/chdir cwd))))

(defn bind-user-programs [ctx]
  (-> ctx
      (bwrap/bind-ro (str (home ctx) "/.nix-profile/bin"))))

(defn bind-isolated-home-with-user-programs [ctx appname]
  (-> ctx
      (bind-isolated-home appname)
      (bind-user-programs))) ; need to rebind nix-profile again over home

(defn network [ctx]
  (-> ctx
      (bwrap/share-net)
      (bwrap/bind-ro "/etc/resolv.conf")
      (bwrap/bind-ro-try "/run/systemd/resolve")))
