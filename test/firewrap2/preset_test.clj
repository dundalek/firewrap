(ns firewrap2.preset-test
  (:require
   [clojure.test :refer [deftest is]]
   [firewrap2.bwrap :as bwrap]
   [firewrap2.preset.godmode :as godmode]
   [snap.core :as snap]))

(deftest foo
  (snap/match-snapshot ::godmode (bwrap/ctx->args (godmode/preset "/path/to/GodMode.AppImage")))
  #_(is (= {} (bwrap/ctx->args (godmode/preset "/path/to/GodMode.AppImage")))))
