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

(defn home [ctx]
  (sb/getenv ctx "HOME"))

(defn bind-isolated-home [ctx appname]
  (let [HOME (home ctx)
        source-path (str HOME "/sandboxes/" appname)]
    (-> ctx
        (sb/fx-create-dirs source-path)
        (sb/bind-rw source-path HOME))))

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
      (sb/bind-ro-try "/etc/resolv.conf")
      (sb/bind-ro-try "/run/systemd/resolve")))
