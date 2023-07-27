(ns firewrap.main
  (:require
   [clojure.string :as str]
   [firewrap.profiles.chatall :as chatall]
   [firewrap.profiles.cheese :as cheese]
   [firewrap.profiles.notify-send :as notify-send]
   [firewrap.profiles.xdg-open :as xdg-open]
   [firewrap.system :as system]))

(defn run-bwrap [args]
  (->> ["exec bwrap" args]
       (flatten)
       (str/join " ")
       (println)))

(defn with-strace [cmd]
  [(system/ro-bind "/usr/bin/strace") "strace -f" cmd])

(defn -main [cmd & args]
  (let [[_ appname] (re-find #"([^/]+)$" cmd)]
    (case appname
      "ChatALL" (run-bwrap (chatall/profile))
      "cheese" (run-bwrap [(cheese/profile)
                           ; cmd]))))
                           (with-strace cmd)])
      "notify-send" (run-bwrap [(notify-send/profile)
                                cmd
                                ; (with-strace cmd)
                                args])
      "xdg-open" (run-bwrap [(xdg-open/profile)
                             cmd
                             args]))))
