(ns firewrap.tool.strace
  (:require
   [babashka.cli :as cli]
   [babashka.process :refer [process]]
   [cheshire.core :as json]
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]
   [clojure.string :as str]
   [clojure.walk :as walk]
   [firewrap.preset.oldsystem :as system]
   [firewrap.sandbox :as sb]
   [firewrap.preset.dumpster :as dumpster]))

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

;; todo do it properly
(defn- normalize-path [path]
  (str/replace path #"//" "/"))

(defn- arg->path [arg]
  (some-> (cond
            (and (string? arg) (str/includes? arg "/")) arg
            (map? arg) (:sun_path arg)) ; connect syscall
          (normalize-path)))

(defn- syscall->file-paths [{:keys [syscall args]}]
  (when-not (data-call? syscall)
    ;; there are syscalls that take two file paths like `mount`, `rename`, `symlink` so keep vector of paths
    (let [paths (->> args
                     (keep arg->path))
          ;; second path for `readlink` and `readlinkat` is a buffer which can contain junk, so take only the first path
          paths (if (#{"readlink" "readlinkat"} syscall)
                  (take 1 paths)
                  paths)]
      (vec paths))))

(defn read-trace [file-path]
  (with-open [rdr (-> (process {:in (io/reader file-path)}
                               "bunx b3-strace-parser"
                               #_"npm exec -y b3-strace-parser")
                      :out
                      io/reader)]
    (doall (json/parsed-seq rdr true))))

;; in case of symlinks - we can bind path, but still will get not exists error if we also don't bind target location
;; this is harder to understand from deduplicated calls
(defn trace->file-syscalls [trace]
  (->> trace
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
                     include-result? (conj (str (:result item))))))))))

(defn- bind-autogen
  ([tree parent-path]
   (bind-autogen tree parent-path 'system/bind-ro-try))
  ([tree parent-path symb]
   (when (map? tree)
     (when-some [bindings (->> tree
                               (filter (comp string? key))
                               (sort-by key)
                               (mapcat (fn [[k v]]
                                         (let [path (if parent-path
                                                      (str parent-path "/" k)
                                                      k)
                                               children (bind-autogen v path symb)
                                               form (list symb (if (= path "") "/" path))]
                                           (if (some? children)
                                             [(cons 'system/nop form) children]
                                             [form]))))
                               seq)]
       ;; could also wrap it in vectors or (->)
       (concat ['->] bindings)))))

(def bind-params
  #{"--ro-bind"
    "--ro-bind-try"
    "--bind"
    "--bind-try"
    "--dev-bind"
    "--dev-bind-try"
    "--symlink"})

(defn bwrap->paths [bwrap-args]
  (loop [ret #{}
         [arg & args] bwrap-args]
    (if (nil? arg)
      ret
      (if (bind-params arg)
        (let [[_source destination & args] args]
          (recur (conj ret destination) args))
        (recur ret args)))))

;; abstractions are static matches, taking no arguments besides context
(def static-matchers
  (let [ctx (sb/*populate-env!* {})]
    (->> ['dumpster/network
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
          'system/dbus-system-bus
          'system/x11
          'system/gtk
          'system/dev-urandom
          'system/dev-null
          'system/at-spi
          'system/mime-cache]
         (map (fn [sym]
                [(vector sym) (bwrap->paths (sb/ctx->args ((resolve sym) ctx)))]))
         (into {}))))

(defn match-xdg-runtime-dir [path]
  (let [runtime-dir (System/getenv "XDG_RUNTIME_DIR")]
    (when (str/starts-with? path runtime-dir)
      [(vector 'system/xdg-runtime-dir (str/replace path (str runtime-dir "/") ""))])))

;; will need to take into account what kind of access is being made to decide if to use bind-ro, bind-rw or other variants
;; todo proper path handling to normalize slashes
(defn match-xdg-dir [dirs-str path]
  (let [dirs (some-> dirs-str (str/split #":"))]
    (when-some [matched (some (fn [xdg-dir]
                                (when (str/starts-with? path xdg-dir)
                                  xdg-dir))
                              dirs)]
      (-> path
          (str/replace-first matched "")
          (str/replace #"^/+|/+$" "")))))

(defn match-xdg-data-dir [path]
  (when-some [subdir (match-xdg-dir (System/getenv "XDG_DATA_DIRS") path)]
    [(vector 'system/xdg-data-dir subdir)]))

(defn match-xdg-config-dir [path]
  (when-some [subdir (match-xdg-dir (System/getenv "XDG_CONFIG_DIRS") path)]
    [(vector 'system/xdg-config-dir subdir)]))

(defn match-xdg-data-home [path]
  (when-some [subdir (match-xdg-dir (system/xdg-data-home-path) path)]
    [(vector 'system/xdg-data-home subdir)]))

(defn match-xdg-config-home [path]
  (when-some [subdir (match-xdg-dir (system/xdg-config-home-path) path)]
    [(vector 'system/xdg-config-home subdir)]))

(defn match-xdg-cache-home [path]
  (when-some [subdir (match-xdg-dir (system/xdg-cache-home-path) path)]
    [(vector 'system/xdg-cache-home subdir)]))

(defn match-xdg-state-home [path]
  (when-some [subdir (match-xdg-dir (system/xdg-state-home-path) path)]
    [(vector 'system/xdg-state-home subdir)]))

(def ^:private command-paths
  (->> (str/split (System/getenv "PATH") #":")
       (map #(str % "/"))))

(defn match-command [path]
  ;; hack: reaching for current global env variable
  ;; will need to add ability to pass options when creating matchers
  (when-some [matched-path (->> command-paths
                                (some (fn [p]
                                        (when (str/starts-with? path p)
                                          p))))]
    [['system/command (str/replace path matched-path "")]]))

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
   match-xdg-config-dir
   match-xdg-data-home
   match-xdg-config-home
   match-xdg-cache-home
   match-xdg-state-home
   match-command])

(defn match-path [path]
  (let [matches (->> static-matchers
                     (keep (fn [[k prefixes]]
                             (when-some [match (->> prefixes
                                                    (filter #(str/starts-with? path %))
                                                    first)]
                               [k match]))))
        matches (if (seq matches)
                  matches
                  (keep (fn [matcher] (matcher path)) matchers))]
    matches))

(defn- trace-matches [trace]
  (->> trace
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
              (update-in m [(vector 'system/bind-ro-try path)] (fnil conj []) path))))
        {})))

(defn trace->suggest [trace]
  (let [matches (trace-matches trace)
        {without-args false
         with-args true} (->> matches
                              (keys)
                              (group-by (fn [[_ & args]]
                                          (pos? (count args)))))]
    (concat
     ['-> '(base/base)]
     (->> without-args
          (sort)
          (walk/postwalk (fn [x] (if (vector? x) (seq x) x))))
     (->> with-args
          (group-by first)
          (sort-by key)
          (mapcat (fn [[k items]]
                    (let [path-tree (->> items
                                        ;; assumption: all args are paths
                                         (mapcat (fn [[_ & args]]
                                                   args))
                                         (reduce (fn [m path]
                                                   (let [segments (str/split path #"/")]
                                                     (update-in m segments merge {})))
                                                 {}))]
                      [(bind-autogen path-tree nil k)])))))))

(defn write-rules [writer rules]
  (let [result (list
                'defn 'profile ['_]
                rules)]
    (.write writer "#_:clj-kondo/ignore")
    (.write writer "\n")
    (pprint result writer)))

(defn generate-rules [_]
  (let [trace (with-open [rdr (io/reader *in*)]
                (doall (json/parsed-seq rdr true)))
        rules (trace->suggest trace)]
    (write-rules *out* rules)))

(declare cli-table)

(defn print-help [_]
  (println "Generate suggested rules from a trace

Usage: firewrap generate

Reads JSON trace from stdin, prints result to stdout.

Example: cat foo.trace | b3-strace-parser | firehelper generate > profile/foo.clj"))

(def cli-table
  [{:cmds ["generate"]
    :fn generate-rules
    :args->opts [:file]}
   {:cmds []
    :fn print-help}])

(defn -main [& args]
  (cli/dispatch cli-table args {}))

(comment
  (def trace-prefix "echo")
  (def trace-prefix "clojure")
  (def trace (read-trace (format "tmp/%s-strace" trace-prefix)))

  (def trace (read-trace "test/fixtures/echo-strace"))

  (tap> trace)

  (do
    (def rules (trace->suggest trace))
    (with-open [writer (io/writer (format "tmp/%s-bindings.clj" trace-prefix))]
      (write-rules writer rules)))

  (do
    (def rules (trace->suggest trace))
    (write-rules *out* rules)))

(comment
  (->> trace
       (trace->file-syscalls)
       (count))

  (->> trace
       (trace->file-syscalls))

  (->> trace
       (mapcat syscall->file-paths)
       (distinct)
       ; (count)
       (take 10))

  (->> trace
       (mapcat syscall->file-paths)
       (map match-path))

  (def matches (trace-matches trace))

  (-> trace
      (trace-matches)
      (update-vals #(update-vals % count)))

  (->> matches
       (reduce-kv
        (fn [m k v]
          (let [total (count (get static-matchers k))]
            (assoc m k
                   (when (pos? total)
                     (* 1.0 (/ (count v) total))))))
        {}))
       ; (sort-by first)))

  (->> trace
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
  (count trace)
  (first trace)

  (->> trace
       (map :syscall)
       (frequencies)
       (sort-by val))

  (->> trace
       (filter #(seq (syscall->file-paths %)))
       (take 10))

  (defn- extract-filepaths! [strace-filepath]
    (let [out-path (str strace-filepath "-paths")
          lines (read-trace strace-filepath)]
      (->> lines
           (trace->file-syscalls)
           (map pr-str)
           ; (distinct)
           ; (sort)
           (str/join "\n")
           (spit out-path))))

  (def tree
    (-> (->> trace
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

  (->> trace
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

  (do
    (defn- args-with-path? [args]
      (boolean (some arg->path args)))

    [(args-with-path? ["a" 1])
     (args-with-path? ["/etc/fstab" 1])])

  (defn- open-flags [trace]
    (->> (:args trace)
         (some #(and (map? %)
                     (= (:name %) "O_")
                     (:value %)))))

  ;; open flags - start of trying to distinguish RO vs RW access
  (->> trace
       (filter #(some? (:syscall %)))
       (filter #(args-with-path? (:args %)))
       (filter (comp #{"openat"} :syscall))
       (mapcat open-flags)
       (frequencies)))
