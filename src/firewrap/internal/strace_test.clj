(ns firewrap.internal.strace-test
  (:require
   [clojure.test :refer [deftest is]]
   [firewrap.internal.strace :as strace]
   [firewrap2.bwrap :as bwrap]
   [firewrap2.preset.oldsystem :as oldsystem]))

(deftest bwrap->paths
  (is (= #{"/etc/machine-id" "/var/lib/dbus/machine-id" "/run/user/1000/bus"}
         (strace/bwrap2->paths (bwrap/ctx->args (oldsystem/dbus-unrestricted {}))))))
