(ns firewrap.main
  (:require
   [babashka.fs :as fs]
   [babashka.process :as process]
   [clojure.string :as str]
   [firewrap.profiles.chatall :as chatall]
   [firewrap.profiles.ferdium :as ferdium]
   [firewrap.profiles.godmode :as godmode]
   [firewrap.profiles.xdg-open :as xdg-open]
   [firewrap.system :as system]))

;; Workaround to write bwrap command as temporary script because process/exec
;; can't pass content via file descriptors.
(defn run-bwrap-sh [{:keys [bwrap-args executable]}]
  (let [script (->> ["exec bwrap" bwrap-args executable]
                    (flatten)
                    (str/join " "))
        f (fs/file (fs/create-temp-file {:prefix "firewrap"}))]
    (spit f script)
    (process/exec "sh" f)))

(defn run-bwrap [{:keys [bwrap-args executable]}]
  (let [params (flatten (concat ["bwrap"]
                                bwrap-args
                                (when executable [executable])))]
    (println params)
    (apply process/exec params)))

(defn with-strace [ctx cmd]
  (-> ctx
      (system/bind-ro "/usr/bin/strace")
      (system/add-bwrap-args "strace -s 1024 -f" cmd)))

(def shell "/usr/bin/bash")

;; printing stats inside sandbox with:
;; for f in * /usr/*; do echo -en "$f\t"; find $f | wc -l; done
(defn with-shell [ctx]
  (-> ctx
      (system/bind-ro shell)
      (system/bind-ro "/usr/bin/ls")
      (system/bind-ro "/usr/bin/find")
      (system/bind-ro "/usr/bin/wc")
      (system/add-bwrap-args shell)))

(defn resolve-profile [appname]
  (try
    (requiring-resolve (symbol (str "firewrap.profiles." appname) "profile"))
    (catch Exception _)))

(defn fw-small []
  (-> (system/base)
      ;; Make it tighter instead of dev binding /
      ;; bins and libs
      (system/bind-dev "/")
      (system/tmpfs (System/getenv "HOME"))
      (system/tmp)))

(defn fw-net []
  (-> (fw-small)
      (system/network)))

(defn fw-home [appname]
  (-> (fw-small)
      (system/isolated-home appname)))

(defn fw-nethome [appname]
  (-> (fw-home appname)
      (system/network)))

(defn -main [cmd & args]
  (let [appname (some-> (re-find #"([^/]+)$" cmd)
                        second
                        (str/lower-case))]
    (case appname
      "chatall" (run-bwrap-sh (chatall/profile
                               (system/glob-one (str (System/getenv "HOME") "/Applications/")
                                                "ChatALL-*.AppImage")))
      "godmode" (run-bwrap-sh (godmode/profile
                               (system/glob-one (str (System/getenv "HOME") "/Applications/")
                                                "GodMode-*.AppImage")))
        ; "cheese" (run-bwrap (-> (cheese/profile {:executable "/usr/bin/cheese"})
        ;                         (system/add-bwrap-args cmd)))
        ;                          ; (with-strace cmd)))
      "ferdium" (run-bwrap-sh (ferdium/profile
                               (system/glob-one (str (System/getenv "HOME") "/Applications/")
                                                "Ferdium-*.AppImage")))
         ; "gedit" (run-bwrap (-> (gedit/profile {:executable "/usr/bin/gedit"})
         ;                         ; (system/add-bwrap-args cmd)
         ;                        (with-strace cmd)))
        ; "notify-send" (run-bwrap (-> (notify-send/profile {:executable cmd})
        ;                               ; (with-shell
        ;                              (system/add-bwrap-args cmd)
        ;                                ; (with-strace cmd)
        ;                              (system/add-bwrap-args args)))
      "xdg-open" (run-bwrap (-> (xdg-open/profile)
                                (system/add-bwrap-args cmd args)))
      "firewrap"
      (let [[cmd & args] args]
        (case cmd
          "--small" (run-bwrap (-> (fw-small)
                                   (system/add-bwrap-args args)))
          "--home" (run-bwrap (-> (fw-home (first args))
                                  (system/add-bwrap-args args)))
          "--net" (run-bwrap (-> (fw-net)
                                 (system/add-bwrap-args args)))
          "--nethome" (run-bwrap (-> (fw-nethome (first args))
                                     (system/add-bwrap-args args)))))
      (if-let [profile (resolve-profile appname)]
        (run-bwrap
         (-> (profile {:executable cmd})
             (system/add-bwrap-args cmd args)))
        ;; maybe just print it to stderr?
        (println "echo Cannot resolve profile" (system/escape-shell appname))))))

; (defn -main [cmd & args]
;   (let [appname (some-> (re-find #"([^/]+)$" cmd)
;                         second
;                         (str/lower-case))]
;     (println "cmd:" cmd)
;     (println "appname:" appname)))
