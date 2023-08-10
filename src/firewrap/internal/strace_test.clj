(ns firewrap.internal.strace-test
  (:require
   [clojure.test :refer [deftest is]]
   [firewrap.internal.strace :as strace]
   [firewrap.system :as system]))

(deftest bwrap->paths
  (is (= #{"/etc/machine-id" "/var/lib/dbus/machine-id" "/run/user/1000/bus"}
         (strace/bwrap->paths (system/dbus-unrestricted {})))))
