(ns firewrap.internal.strace
  (:require
   [babashka.process :refer [process]]
   [cheshire.core :as json]
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]
   [clojure.string :as str]
   [firewrap.system :as system]))

; Using strace parser:
;     https://github.com/dannykopping/b3
; Alternative strace parsers (did not try any)
;     https://gitlab.com/gitlab-com/support/toolbox/strace-parser
;     https://github.com/chrahunt/strace-parser
;     https://pypi.org/project/strace-parser/
; alternative to strace with JSON output
;     https://github.com/JakWai01/lurk

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

(def data-call?
  #{"read"
    "write"
    "recvfrom"
    "sendto"
    "pread64"
    "pwrite64"})

(defn arg->path [arg]
  (cond
    (and (string? arg) (str/includes? arg "/")) arg
    (map? arg) (:sun_path arg))) ; connect syscall

(defn syscall->file-paths [{:keys [syscall args]}]
  (when-not (data-call? syscall)
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
                                          (cond-> [(list 'system/bind-ro-try path)]
                                            (some? children) (conj children)))))
                              seq)]
      ;; could also wrap it in vectors or (->)
      (concat ['do] bindings))))

(def bind-params
  #{"--ro-bind"
    "--ro-bind-try"
    "--bind"
    "--bind-try"
    "--dev-bind"
    "--dev-bind-try"})

(defn bwrap->paths [ctx]
  (->> (:bwrap-args ctx)
       (keep (fn [argv]
               ;; does not handle nesting which works with flatten
               (when (and (vector? argv)
                          (bind-params (first argv)))
                 (let [[_type _source destination] argv]
                   destination))))
       (into #{})))

;; abstractions are static matches, taking no arguments besides context
(def abstractions
  (let [ctx {:getenv (fn [k] (System/getenv k))}]
    (->> ['system/network
          'system/fontconfig
          'system/fontconfig-shared-cache
          'system/fonts
          'system/icons
          'system/locale
          'system/themes
          'system/dconf
          'system/gpu
          'system/libs
          'system/dbus-unrestricted
          'system/x11
          'system/gtk
          'system/dev-urandom
          'system/dev-null
          'system/at-spi]
         (map (fn [sym]
                [sym (bwrap->paths ((resolve sym) ctx))]))
         (into {}))))

(defn match-xdg-runtime-dir [path]
  (let [runtime-dir (System/getenv "XDG_RUNTIME_DIR")]
    (when (str/starts-with? path runtime-dir)
      [(list 'system/xdg-runtime-dir (str/replace path (str runtime-dir "/") ""))])))

;; will need to take into account what kind of access is being made to decide if to use bind-ro, bind-rw or other variants
;; todo proper path handling to normalize slashes
(defn match-xdg-dir [dirs-str path]
  (let [dirs (some-> dirs-str (str/split #":"))]
    (when-some [matched (some (fn [xdg-dir]
                                (when (str/starts-with? path xdg-dir)
                                  xdg-dir))
                              dirs)]
      (str/replace-first path matched ""))))

(defn match-xdg-data-dir [path]
  (when-some [subdir (match-xdg-dir (System/getenv "XDG_DATA_DIRS") path)]
    [(list 'system/bind-ro-try (list 'system/xdg-data-dir-paths subdir))]))

(defn match-xdg-config-dir [path]
  (when-some [subdir (match-xdg-dir (System/getenv "XDG_CONFIG_DIRS") path)]
    [(list 'system/bind-ro-try (list 'system/xdg-data-dir-paths subdir))]))

(comment
  (match-xdg-data-dir "/usr/share/somedir/somefile")
  (match-xdg-data-dir "/tmp/somedir/somefile")

  (match-xdg-config-dir "/etc/xdg/somedir/somefile")
  (match-xdg-config-dir "/tmp/somedir/somefile"))

;; matchers are dynamic abstractions, taking some parameter
;; taking it all the way bind-* fns could be viewed as dynamic matchers as well,
;; although the fact they are hierarchical makes it more complicated
(def matchers
  [match-xdg-runtime-dir
   match-xdg-data-dir
   match-xdg-config-dir])

(defn match-path [path]
  (let [matches (->> abstractions
                     (keep (fn [[k prefixes]]
                             (when-some [match (->> prefixes
                                                    (filter #(str/starts-with? path %))
                                                    first)]
                               [k match]))))
        matches (if (seq matches)
                  matches
                  (keep (fn [matcher] (matcher path)) matchers))]
    matches))

(comment
  (match-xdg-runtime-dir "/run/user/1000/at-spi/bus_1")
  (match-xdg-runtime-dir "/dev/null"))

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

  (def lines (read-trace  "tmp/_usr_bin_gnome-calculator-strace")))

(comment

  (->> lines
       (mapcat syscall->file-paths)
       (count))

  (def matches
    (->> lines
         (mapcat syscall->file-paths)
         (reduce
          (fn [m path]
            (let [matches (match-path path)]
              (if (seq matches)
                (reduce
                 (fn [m match]
                   (update-in m match (fnil conj []) path))
                 m
                 matches)
                (update-in m [:binding :paths] (fnil conj []) path))))
          {})))

  (update-vals matches #(update-vals % count))

  (->> matches
       (reduce-kv
        (fn [m k v]
          (let [total (count (get abstractions k))]
            (assoc m k
                   (when (pos? total)
                     (* 1.0 (/ (count v) total))))))
        {}))
       ; (sort-by first)))

  (->> lines
       (mapcat (fn [item]
                 (->> (syscall->file-paths item)
                      (map (fn [path]
                             [path item])))))
       (remove (fn [[path item]]
                 (seq (match-path path))))
       (map first)
       (distinct)
       sort))
       ; count)
       ; (take 200))

(comment
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
