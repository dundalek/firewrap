(ns firewrap2.tool.strace-test
  (:require
   [clojure.test :refer [deftest is]]
   [firewrap2.tool.strace :as strace]
   [firewrap2.bwrap :as bwrap]
   [firewrap2.preset.oldsystem :as oldsystem]))

(deftest bwrap->paths
  (is (= #{"/etc/machine-id" "/var/lib/dbus/machine-id" "/run/user/1000/bus"}
         (strace/bwrap2->paths (bwrap/ctx->args (oldsystem/dbus-unrestricted {}))))))

(deftest match-xdg-runtime-dir
  (is (= '[[system/xdg-runtime-dir "at-spi/bus_1"]]
         (strace/match-xdg-runtime-dir "/run/user/1000/at-spi/bus_1")))
  (is (= nil (strace/match-xdg-runtime-dir "/dev/null"))))

(deftest match-path
  (is (= '[[[system/at-spi] "/run/user/1000/at-spi/bus_1"]]
         (strace/match-path "/run/user/1000/at-spi/bus_1")))
  (is (= '[[[system/dev-null] "/dev/null"]]
         (strace/match-path "/dev/null"))))

(deftest read-trace
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
           (->> trace (filter (comp #{"openat"} :syscall)))))))
