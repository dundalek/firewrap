(ns firewrap.main
  (:require
   [clojure.string :as str]
   [firewrap.profiles.chatall :as chatall]
   [firewrap.profiles.cheese :as cheese]
   [firewrap.profiles.ferdium :as ferdium]
   [firewrap.profiles.gedit :as gedit]
   [firewrap.profiles.notify-send :as notify-send]
   [firewrap.profiles.xdg-open :as xdg-open]
   [firewrap.system :as system]))

(defn run-bwrap [{:keys [bwrap-args]}]
  (->> ["exec bwrap" bwrap-args]
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

(defn -main [cmd & args]
  (let [[_ appname] (re-find #"([^/]+)$" cmd)]
    (case appname
      "ChatALL" (run-bwrap (chatall/profile
                            (system/glob-one (str (System/getenv "HOME") "/Applications/")
                                             "ChatALL-*.AppImage")))
      "cheese" (run-bwrap (-> (cheese/profile {:executable "/usr/bin/cheese"})
                              (system/add-bwrap-args cmd)))
                              ; (with-strace cmd)))
      "ferdium" (run-bwrap (ferdium/profile
                            (system/glob-one (str (System/getenv "HOME") "/Applications/")
                                             "Ferdium-*.AppImage")))
      "gedit" (run-bwrap (-> (gedit/profile {:executable "/usr/bin/gedit"})
                              ; (system/add-bwrap-args cmd)
                             (with-strace cmd)))
      "notify-send" (run-bwrap (-> (notify-send/profile {:executable cmd})
                                   (with-shell)))
                                   ; (system/add-bwrap-args cmd)
                                   ; (with-strace cmd)
                                   ; (system/add-bwrap-args args)))
      "xdg-open" (run-bwrap (-> (xdg-open/profile)
                                (system/add-bwrap-args cmd args))))))
