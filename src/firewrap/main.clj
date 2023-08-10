(ns firewrap.main
  (:require
   [clojure.string :as str]
   [firewrap.profiles.chatall :as chatall]
   [firewrap.profiles.ferdium :as ferdium]
   [firewrap.profiles.xdg-open :as xdg-open]
   [firewrap.system :as system]))

(defn run-bwrap [{:keys [bwrap-args executable]}]
  (->> ["exec bwrap" bwrap-args executable]
       (flatten)
       (str/join " ")
       (println)))

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

(defn -main [cmd & args]
  (let [appname (some-> (re-find #"([^/]+)$" cmd)
                        second
                        (str/lower-case))]
    (case appname
      "chatall" (run-bwrap (chatall/profile
                            (system/glob-one (str (System/getenv "HOME") "/Applications/")
                                             "ChatALL-*.AppImage")))
        ; "cheese" (run-bwrap (-> (cheese/profile {:executable "/usr/bin/cheese"})
        ;                         (system/add-bwrap-args cmd)))
        ;                          ; (with-strace cmd)))
      "ferdium" (run-bwrap (ferdium/profile
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
      (if-let [profile (resolve-profile appname)]
        (run-bwrap
         (-> (profile {:executable cmd})
             (system/add-bwrap-args cmd args)))
        (println "echo Cannot resolve profile" (system/escape-shell appname))))))
