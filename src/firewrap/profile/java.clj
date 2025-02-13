(ns firewrap.profile.java
  (:require
   [firewrap.preset.base :as base]
   [firewrap.preset.oldsystem :as system]
   [firewrap.sandbox :as sb]))

;; Create example file:
;;   echo 'void main() { println("Hello world"); }' > hello.java
;; RUn with JDK 23+:
;;   firewrap --profile java -c -- java --enable-preview hello.java

(defn profile [_]
  ;; use --cwd option to run classes from current directory
  (->
   (base/base)
   (system/libs)

   (sb/env-pass "PATH")

   ; (system/bind-ro-try "/nix")

   (system/command "java")
   ;; on Ubuntu /usr/bin/java symlinks to /etc/alternatives/java which then symlinks to /usr/lib/jvm/...
   (sb/bind-ro-try "/etc/alternatives/java")))
