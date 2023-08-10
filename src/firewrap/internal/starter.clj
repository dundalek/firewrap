(ns firewrap.internal.starter
  (:require
   [firewrap.system :as system]))

;; Starter templates that can help constructing a new sandbox profile.
;; Starting with wide permissions and then locking things down further with more granular rules.

;; First no fs restrictions, just unsharing namespaces to verify if an app still works
(def step1-minimal-restrictions
  (-> (system/base)
      (system/network)
      (system/bind-dev "/")))

;; For debugging when --unshare-all causes problems to determine the namespaces that cause issues by toggling individually
(def unshare-all
  ["--unshare-user-try"
   "--unshare-ipc"
   "--unshare-pid"
   "--unshare-net"
   "--unshare-uts"
   "--unshare-cgroup-try"])

(def lazy-sandbox
  (->
   (system/base)
   (system/network)
   (system/bind-ro "/")
   (system/isolated-home "my-app")
   (system/tmp)))

;; Then trying selective restrictions and trial-and-error next to define the sandbox
(def step2-toplevel-dirs
  (->
   (system/base)
   (system/libs)
   ; (system/rw-bind "/home")
   (system/isolated-home "my-app")
   (system/bind-dev "/dev")
   (system/bind-ro "/etc")
   (system/bind-dev "/proc")
   (system/bind-dev "/run")
   (system/bind-dev "/sys")
   (system/bind-ro "/usr")
   ; (system/bind-ro "/usr/bin/my-app")
   ; (system/bind-ro "/usr/bin")
   ; (system/bind-ro "/usr/share")
   (system/bind-dev "/var")
   (system/tmp)))
