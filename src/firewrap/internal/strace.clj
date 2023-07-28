(ns firewrap.internal.strace
  (:require
   [babashka.process :refer [process]]
   [cheshire.core :as json]
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]
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

(defn syscall->file-paths [{:keys [syscall args]}]
  (when-not (#{"read" "write" "recvfrom" "sendto" "pread64" "pwrite64"} syscall)
    ;; there are syscalls that take two file paths like `mount`, `rename`, `symlink` so keep vector of paths
    (let [paths (->> args
                     (keep arg->path))
          ;; second path for `readlink` and `readlinkat` is a buffer which can contain junk, so take only the first path
          paths (if (#{"readlink" "readlinkat"} syscall)
                  (take 1 paths)
                  paths)]
      (vec paths))))

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
  (with-open [rdr (-> (process {:in (io/reader file-path)} "npm exec -y b3-strace-parser")
                      :out
                      io/reader)]
    (doall (json/parsed-seq rdr true))))

;; in case of symlinks - we can bind path, but still will get not exists error if we also don't bind target location
;; this is harder to understand from deduplicated calls
(defn extract-filepaths [strace-filepath]
  (let [out-path (str strace-filepath "-paths")
        lines (read-trace strace-filepath)]
    (->> lines
         (keep (fn [{:keys [syscall result] :as item}]
                 (let [paths (syscall->file-paths item)
                       ;; regular result code for `openat` is FD, which can differ and is not relevant
                       ;; therefore only include errors starting with `-` negative return codes
                       include-result? (or
                                        (not= syscall "openat")
                                        (and
                                         (string? result)
                                         (str/starts-with? result "-")))]
                   (when (seq paths)
                     (cond-> paths
                       :always (conj (:syscall item))
                       include-result? (conj (str (:result item))))))))
         (map pr-str)
         ; (distinct)
         ; (sort)
         (str/join "\n")
         (spit out-path))))

(defn bind-autogen [tree parent-path]
  (when (map? tree)
    (when-some [bindings (->> tree
                              (filter (comp string? key))
                              (sort-by key)
                              (mapcat (fn [[k v]]
                                        (let [path (str parent-path "/" k)
                                              children (bind-autogen v path)]
                                          (cond-> [(list 'system/ro-bind-try path)]
                                            (some? children) (conj children)))))
                              seq)]
      ;; could also wrap it in vectors or (->)
      (concat ['do] bindings))))

(comment
  (extract-filepaths "tmp/gedit-strace")
  (extract-filepaths "tmp/gedit-strace-sandbox")

  (extract-filepaths "tmp/notify-send-trace")
  (extract-filepaths "tmp/notify-send-trace-sandbox")

  (extract-filepaths "tmp/cheese-trace-save3")
  (extract-filepaths "tmp/cheese-strace-sandbox3")

  (def lines (read-trace "tmp/cheese-trace-savex"))
  (def lines (read-trace "tmp/notify-send-trace"))
  (def lines (read-trace "tmp/gedit-strace"))
  (def lines (read-trace "tmp/xdg-open-strace"))

  (def lines (read-trace  "tmp/gedit-strace"))

  (count lines)
  (first lines)

  (->> lines
       (map :syscall)
       (frequencies))

  (->> lines
       (filter #(seq (syscall->file-paths %)))
       (take 10))

  (def tree
    (-> (->> lines
             (mapcat (fn [item]
                       (->> (syscall->file-paths item)
                            (map (fn [path]
                                   [path item])))))
             ; (take 10)
             (reduce (fn [m [path item]]
                       (let [segments (str/split path #"/")]
                         (-> m
                             (update-in segments update-in [:calls [(:syscall item) (:result item)]] (fnil conj []) item))))
                     {}))
        ;; only consider absolute paths starting with `/` for now
        (get "")))

  (tap> tree)

  (with-open [writer (io/writer "tmp/bindings.clj")]
    (.write writer "#_:clj-kondo/ignore")
    (.write writer "\n")
    (pprint (bind-autogen tree "") writer))

  (->> lines
       (mapcat (fn [item]
                 (->> (syscall->file-paths item)
                      (map (fn [path]
                             [path item])))))
       (reduce (fn [m [path item]]
                 (let [segments (str/split path #"/")]
                   (-> m
                       (update-in segments update-in [:calls [(:syscall item) (:result item)]] (fnil conj []) item))))
               {})
       (tap>))

  (->> lines
       (filter #(some? (:syscall %)))
       (filter #(args-with-path? (:args %)))
       (filter (comp #{"openat"} :syscall))
       (mapcat open-flags)
       (frequencies)))
