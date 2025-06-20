(ns firewrap.tool.strace-test
  (:require
   [babashka.fs :as fs]
   [babashka.process :refer [shell]]
   [clojure.test :refer [deftest is testing]]
   [firewrap.preset.oldsystem :as oldsystem]
   [firewrap.sandbox :as sb]
   [firewrap.tool.strace :as strace]
   [snap.core :as snap]))

(defn- create-trace [& cmd]
  (let [tmp-file (fs/create-temp-file)]
    (apply shell "bin/strace-helper" "-o" tmp-file cmd)
    (strace/read-json-trace (str tmp-file ".jsonl"))))

(defn- filter-paths [matches-path? trace]
  (into []
        (filter (fn [item] (some matches-path? (strace/syscall->file-paths item))))
        trace))

(def test-ctx
  {::sb/envs-system {"XDG_RUNTIME_DIR" "/run/user/1000"
                     "PATH" "/usr/bin:/bin:/usr/sbin:/sbin"}})

(deftest bwrap->paths
  (is (= #{"/etc/machine-id" "/var/lib/dbus/machine-id" "/run/user/1000/bus"}
         (strace/bwrap->paths (sb/ctx->args (oldsystem/dbus-unrestricted test-ctx))))))

(deftest match-xdg-runtime-dir
  (is (= '[[system/xdg-runtime-dir "at-spi/bus_1"]]
         (strace/match-xdg-runtime-dir test-ctx "/run/user/1000/at-spi/bus_1")))
  (is (= nil (strace/match-xdg-runtime-dir test-ctx "/dev/null"))))

(deftest match-path
  (let [matchers (strace/make-matchers test-ctx)]
    (is (= '[[[system/at-spi] "/run/user/1000/at-spi/bus_1"]]
           (strace/match-path matchers "/run/user/1000/at-spi/bus_1")))
    (is (= '[[[system/dev-null] "/dev/null"]]
           (strace/match-path matchers "/dev/null")))))

(deftest syscall->file-paths
  (is (= ["/dev/null"]
         (strace/syscall->file-paths
          {:syscall "openat",
           :args
           [["AT_FDCWD"]
            "/dev/null"
            {:name "O_", :value ["WRONLY" "O_CREAT" "O_TRUNC"]}
            666],
           :result 3,
           :timing nil,
           :pid 1760627,
           :type "SYSCALL"})))

  (testing "file path without slash is not detected
    just recording current behavior, this is wrong due to naive path detection implementation and should be fixed"
    (is (= []
           #_["README.md"]
           (strace/syscall->file-paths
            {:syscall "openat",
             :args [["AT_FDCWD"] "README.md" {:name "O_", :value ["RDONLY"]}],
             :result 3,
             :timing nil,
             :pid 1760628,
             :type "SYSCALL"})))))

(deftest tracing
  (let [trace (strace/read-json-trace "test/fixtures/echo-strace.jsonl")]
    (is (= 40 (count trace)))

    (is (= [{:args [["AT_FDCWD"] "/etc/ld.so.cache" {:name "O_", :value ["RDONLY" "O_CLOEXEC"]}],
             :pid 620239,
             :result 3,
             :syscall "openat",
             :timing nil,
             :type "SYSCALL"}
            {:args [["AT_FDCWD"]
                    "/lib/x86_64-linux-gnu/libc.so.6"
                    {:name "O_", :value ["RDONLY" "O_CLOEXEC"]}],
             :pid 620239,
             :result 3,
             :syscall "openat",
             :timing nil,
             :type "SYSCALL"}
            {:args [["AT_FDCWD"]
                    "/usr/lib/locale/locale-archive"
                    {:name "O_", :value ["RDONLY" "O_CLOEXEC"]}],
             :pid 620239,
             :result 3,
             :syscall "openat",
             :timing nil,
             :type "SYSCALL"}]
           (->> trace (filter (comp #{"openat"} :syscall)))))

    (is (= [["/usr/bin/echo" "execve" "0"]
            ["/etc/ld.so.preload" "access" "-1 ENOENT (No such file or directory)"]
            ["/etc/ld.so.cache" "openat"]
            ["/lib/x86_64-linux-gnu/libc.so.6" "openat"]
            ["/usr/lib/locale/locale-archive" "openat"]]
           (strace/trace->file-syscalls trace)))

    (snap/match-snapshot ::echo-suggest (strace/trace->suggest (strace/make-matchers test-ctx) trace))))

(deftest not-exists
  (is (= '(->
           (base/base)
           (->
            (system/nop system/bind-ro-try ".")
            (-> (system/not-exists system/bind-ro-try "./NON_EXISTING_FILE")
                (system/bind-ro-try "./README.md"))))

         (strace/trace->suggest
          (strace/make-matchers test-ctx)
          #_(->> (create-trace "sh" "-c" "/usr/bin/cat ./README.md > /dev/null; /usr/bin/cat ./NON_EXISTING_FILE")
                 (filter-paths #{"./README.md" "./NON_EXISTING_FILE"}))
          [{:syscall "openat",
            :args [["AT_FDCWD"] "./README.md" {:name "O_", :value ["RDONLY"]}],
            :result 3,
            :timing nil,
            :pid 71928,
            :type "SYSCALL"}
           {:syscall "openat",
            :args
            [["AT_FDCWD"] "./NON_EXISTING_FILE" {:name "O_", :value ["RDONLY"]}],
            :result "-1 ENOENT (No such file or directory)",
            :timing nil,
            :pid 71929,
            :type "SYSCALL"}]))))

