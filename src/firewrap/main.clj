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

(defn run-bwrap [args]
  (->> ["exec bwrap" args]
       (flatten)
       (str/join " ")
       (println)))

(defn with-strace [cmd]
  [(system/ro-bind "/usr/bin/strace") "strace -s 1024 -f" cmd])

(defn -main [cmd & args]
  (let [[_ appname] (re-find #"([^/]+)$" cmd)]
    (case appname
      "ChatALL" (run-bwrap (chatall/profile
                            (system/glob-one (str (System/getenv "HOME") "/Applications/")
                                             "ChatALL-*.AppImage")))
      "cheese" (run-bwrap [(cheese/profile)
                           ; cmd
                           (with-strace cmd)])
      "ferdium" (run-bwrap [(ferdium/profile
                             (system/glob-one (str (System/getenv "HOME") "/Applications/")
                                              "Ferdium-*.AppImage"))])
      "gedit" (run-bwrap [(gedit/profile)
                          ; cmd
                          (with-strace cmd)])
      "notify-send" (run-bwrap [(notify-send/profile)
                                ; cmd
                                (with-strace cmd)
                                args])
      "xdg-open" (run-bwrap [(xdg-open/profile)
                             cmd
                             args]))))
