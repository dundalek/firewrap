(ns firewrap2.preset-test
  (:require
   [clojure.test :refer [deftest]]
   [firewrap2.bwrap :as bwrap]
   [firewrap2.main :as main]
   [firewrap2.profile.godmode :as godmode]
   [firewrap2.profile.windsurf :as windsurf]
   [snap.core :as snap]))

(deftest foo
  (snap/match-snapshot ::godmode (main/unwrap-raw (bwrap/ctx->args (godmode/profile "/path/to/GodMode.AppImage"))))
  (snap/match-snapshot ::windsurf (main/unwrap-raw (bwrap/ctx->args (windsurf/profile))))
  #_(is (= {} (bwrap/ctx->args (godmode/preset "/path/to/GodMode.AppImage")))))
