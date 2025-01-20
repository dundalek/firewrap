(ns firewrap2.preset-test
  (:require
   [clojure.test :refer [deftest]]
   [firewrap2.bwrap :as bwrap]
   [firewrap2.main :as main]
   [firewrap2.preset.env :as env]
   [firewrap2.profile.godmode :as godmode]
   [firewrap2.profile.windsurf :as windsurf]
   [snap.core :as snap]))

(defn test-main [& args]
  (binding [main/*exec-fn* (fn [& args] args)
            bwrap/*system-getenv* (constantly (select-keys (System/getenv) env/allowed))]
    (apply main/main args)))

(deftest presets
  (binding [bwrap/*system-getenv* (constantly (select-keys (System/getenv) env/allowed))]
    (snap/match-snapshot ::godmode (main/unwrap-raw (bwrap/ctx->args (godmode/profile "/path/to/GodMode.AppImage"))))
    (snap/match-snapshot ::windsurf (main/unwrap-raw (bwrap/ctx->args (windsurf/profile))))
    (snap/match-snapshot ::windsurf-cwd (test-main "windsurf" "--cwd" "--" "."))))

(deftest base
  (snap/match-snapshot ::base-bc (test-main "firewrap" "-bc" "--" "date"))
  (snap/match-snapshot ::base-bhn (test-main "firewrap" "-bhn" "--" "date"))
  (snap/match-snapshot ::base-home-arg (test-main "firewrap" "-b" "--home" "customhome" "--" "date")))
