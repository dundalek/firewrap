(ns firewrap.presets
  (:require
   [babashka.fs :as fs]
   [clojure.string :as str]
   [firewrap.env :as env]
   [firewrap.system :as system]))

(defn bind-cwd-rw [ctx]
  (let [cwd (str (fs/absolutize (fs/cwd)))]
    (-> ctx
        (system/bind-rw cwd)
        ;; Make sure cwd is set to current dir when we bind cwd
        (system/add-bwrap-args "--chdir" cwd))))

(defn bind-system-programs [ctx]
  (-> ctx
      ;; Make it tighter instead of dev binding /
      ;; bins and libs
      (system/bind-dev "/")
      (system/bind-ro "/nix"))) ; by default user can write to nix (daemonless setup), maliciuos actor could rewrite some binary there? therefore rebind as ro

(defn bind-user-programs [ctx]
  (system/bind-ro ctx (str (System/getenv "HOME") "/.nix-profile/bin")))

(defn isolated-home-with-user-programs [ctx appname]
  (-> ctx
      (system/isolated-home appname)
      (bind-user-programs))) ; need to rebind nix-profile again over home

(defn path->appname [path]
  (some-> (re-find #"([^/]+)$" path)
          second
          (str/lower-case)))

;; presets

(defn fw-small-no-tmpfs [_]
  (-> (system/base)
      (env/set-allowed-vars env/allowed)
      (bind-system-programs)
      (system/tmpfs (System/getenv "HOME"))
      (bind-user-programs)))

(defn fw-small [_]
  (-> (fw-small-no-tmpfs _)
      (system/tmp)))

(defn fw-net [_]
  (-> (fw-small nil)
      (system/network)))

(defn fw-home [[cmd]]
  (let [appname (path->appname cmd)]
    (-> (fw-small nil)
        (isolated-home-with-user-programs appname))))

(defn fw-homenet [args]
  (-> (fw-home args)
      (system/network)))

(defn fw-homecwdnet [args]
  (-> (fw-home args)
      (bind-cwd-rw)
      (system/network)))

(defn fw-tmphome [_]
  (let [sandbox (str "tmp-" (-> (str (java.time.LocalDateTime/now))
                                (str/replace #"[^\w-]" "-")))]
    (-> (fw-small nil)
        (isolated-home-with-user-programs sandbox))))

(defn fw-tmphomenet [args]
  (-> (fw-tmphome args)
      (system/network)))

(defn fw-tmphomecwdnet [args]
  (-> (fw-tmphome args)
      (bind-cwd-rw)
      (system/network)))

(defn fw-cwd [_]
  (-> (fw-small nil)
      (bind-cwd-rw)))

(defn fw-cwdnet [_]
  (-> (fw-cwd nil)
      (system/network)))

(defn fw-godmodedev [_]
  (->
   ;; WARNING: overly broad
   (fw-small-no-tmpfs nil) ; with tmpfs seems can't connect to X server
   (isolated-home-with-user-programs "godmode")
   (bind-cwd-rw)
   (system/network)))

(def presets
  [["small" fw-small "small profile with temporary home"]
   ["cwd" fw-cwd "\tsmall including current working directory"]
   ["home" fw-home "isolated home based on app name"]
   ["net" fw-net "\tsmall with network"]
   ["cwdnet" fw-cwdnet "small with network and current working directory"]
   ["homenet" fw-homenet "isolated home and network"]
   ["homecwdnet" fw-homecwdnet "isolated home with network and current working directory"]
   ["tmphome" fw-tmphome "newly created isolated home"]
   ["tmphomenet" fw-tmphomenet "newly created isolated home and network"]
   ["tmphomecwdnet" fw-tmphomecwdnet "newly created isolated home with network and current working directory"]

   ["c" fw-cwd "alias for cwd"]
   ["h" fw-home "alias for home"]
   ["n" fw-net "alias for net"]
   ["t" fw-tmphome "alias for tmphome"]
   ; ["ch"]
   ["chn" fw-homecwdnet "cwd + home + net"]
   ["cnt" fw-tmphomecwdnet "cwd + net + tmphome"]
   ["hn" fw-homenet "home + net"]
   ["nt" fw-tmphomenet "net + tmphome"]

   ["godmodedev" fw-godmodedev ""]])

(def preset-map
  (into {}
        (map (fn [[name f]]
               [name f]))
        presets))
