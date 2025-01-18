(ns firewrap2.preset.dumpster
  (:require
   [babashka.fs :as fs]
   [firewrap2.bwrap :as bwrap]))

(defn home [ctx]
  (bwrap/getenv ctx "HOME"))

(defn escape-shell [s]
  (println "WARNING: TODO: escape-shell not implemented")
  s)

(defn bind-isolated-home [ctx appname]
  (let [HOME (home ctx)
        source-path (str HOME "/sandboxes/" appname)]
    (println  "WARNING: TODO: Side-effect! Ideally make it pure and interpret side effects separately")
    (fs/create-dirs source-path)
    (-> ctx
        (bwrap/bind-rw (escape-shell source-path) (escape-shell HOME)))))

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
