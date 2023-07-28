(ns firewrap.internal.starter
  (:require
   [firewrap.system :as system]))

;; Starter templates that can help constructing a new sandbox profile.
;; Starting with wide permissions and then locking things down further with more granular rules.

;; First no fs restrictions, just unsharing namespaces to verify if an app still works
(def step1-minimal-restrictions
  [(system/base)
   (system/system-network)
   (system/dev-bind "/")])

;; For debugging when --unshare-all causes problems to determine the namespaces that cause issues by toggling individually
(def unshare-all
  ["--unshare-user-try"
   "--unshare-ipc"
   "--unshare-pid"
   "--unshare-net"
   "--unshare-uts"
   "--unshare-cgroup-try"])

(def lazy-sandbox
  [(system/base)
   (system/network)
   (system/bind-ro "/")
   (system/isolated-home "my-app")
   (system/tmp)])

;; Then trying selective restrictions and trial-and-error next to define the sandbox
(def step2-toplevel-dirs
  [(system/base)
   (system/libs)
   ; (system/rw-bind "/home")
   (system/isolated-home "my-app")
   (system/dev-bind "/dev")
   (system/bind-ro "/etc")
   (system/dev-bind "/proc")
   (system/dev-bind "/run")
   (system/dev-bind "/sys")
   (system/bind-ro "/usr")
   ; (system/bind-ro "/usr/bin/my-app")
   ; (system/bind-ro "/usr/bin")
   ; (system/bind-ro "/usr/share")
   (system/dev-bind "/var")
   (system/tmp)])
