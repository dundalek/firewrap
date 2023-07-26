(ns firewrap.internal.strace
  (:require
   [babashka.process :refer [process]]
   [cheshire.core :as json]
   [clojure.java.io :as io]
   [clojure.string :as str]))

(def fs-call?
  #{"access"
    "connect"
    "execve"
    "inotify_add_watch"
    "mkdir"
    "openat"
    "readlink"
    "stat"
    "statx"
    "unlink"})

(defn arg->path [arg]
  (cond
    (and (string? arg) (str/includes? arg "/")) arg
    (map? arg) (:sun_path arg))) ; connect syscall

(do
  (defn args-with-path? [args]
    (boolean (some arg->path args)))

  [(args-with-path? ["a" 1])
   (args-with-path? ["/etc/fstab" 1])])

(defn open-flags [trace]
  (->> (:args trace)
       (some #(and (map? %)
                   (= (:name %) "O_")
                   (:value %)))))

(defn read-trace [file-path]
  (-> (process {:in (io/reader file-path)} "npm exec -y b3-strace-parser")
      :out
      io/reader
      (json/parsed-seq true)))

(comment
  (def lines (read-trace "tmp/cheese-trace-save"))
  (def lines (read-trace "tmp/notify-send-trace"))

  (count lines)
  (first lines)

  (->> lines
       (map :syscall)
       (frequencies))

  (-> (->> lines
           (filter #(some? (:syscall %)))
           ; (map :syscall)
           ; distinct
           (group-by :syscall))
      (get "mkdir"))

  (->> lines
       (filter (comp #{"connect"} :syscall)))
       ; (count))

  (->> lines
       (filter #(some? (:syscall %)))
       (filter #(args-with-path? (:args %)))
       (filter (comp #{"openat"} :syscall))
       (mapcat open-flags)
       (frequencies))

  (->> lines
       (mapcat :args)
       (keep arg->path)
       (distinct)
       (reduce (fn [m path]
                 (let [segments (str/split path #"/")]
                   (assoc-in m segments {})))
               {})
       (tap>))

  (->> lines
       (mapcat :args)
       (keep arg->path)
       (distinct)
       (filter #(str/starts-with? % "/usr/share/"))
       (map (fn [s]
              (->> (str/split s #"/")
                   (take 4)
                   (into []))))
       (distinct)
       (sort)
       (map #(str/join "/" %))))

       ; (filter #(re-find #"schema" %))))
       ; count))
