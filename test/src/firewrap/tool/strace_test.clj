(ns firewrap.tool.strace-test
  (:require
   [clojure.test :refer [deftest is]]
   [firewrap.tool.strace :as strace]
   [firewrap.sandbox :as sb]
   [firewrap.preset.oldsystem :as oldsystem]
   [snap.core :as snap]))

(def test-ctx
  {::sb/envs-system {"XDG_RUNTIME_DIR" "/run/user/1000"}})

(deftest bwrap->paths
  (is (= #{"/etc/machine-id" "/var/lib/dbus/machine-id" "/run/user/1000/bus"}
         (strace/bwrap->paths (sb/ctx->args (oldsystem/dbus-unrestricted test-ctx))))))

(deftest match-xdg-runtime-dir
  (is (= '[[system/xdg-runtime-dir "at-spi/bus_1"]]
         (strace/match-xdg-runtime-dir "/run/user/1000/at-spi/bus_1")))
  (is (= nil (strace/match-xdg-runtime-dir "/dev/null"))))

(deftest match-path
  (is (= '[[[system/at-spi] "/run/user/1000/at-spi/bus_1"]]
         (strace/match-path "/run/user/1000/at-spi/bus_1")))
  (is (= '[[[system/dev-null] "/dev/null"]]
         (strace/match-path "/dev/null"))))

(deftest tracing
  (let [trace (strace/read-trace "test/fixtures/echo-strace")]
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

    (snap/match-snapshot ::echo-suggest (strace/trace->suggest trace))))
