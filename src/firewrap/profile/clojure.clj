(ns firewrap.profile.clojure
  (:require
   [firewrap.preset.oldsystem :as system]
   [firewrap.profile.java :as java]))

(defn profile [_]
  (-> (java/profile _)

      (system/bind-ro-try "/nix")

      (system/bind-rw-try "/home/me/.m2")
      (system/bind-rw-try "/home/me/.gitlibs")

      (system/command "cat")
      (system/command "cksum")
      (system/command "cp")
      (system/command "cut")
      (system/command "mkdir")

      (system/command "clojure")))
