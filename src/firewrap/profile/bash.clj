(ns firewrap.profile.bash
  (:require
   [firewrap.preset.base :as base]
   [firewrap.preset.oldsystem :as system]
   [firewrap.sandbox :as sb]))

(defn profile [_]
  (sb/$-> (base/base)
      (system/libs)
      ; (system/bind-ro-try "/dev/tty")
      (system/command "bash")))
