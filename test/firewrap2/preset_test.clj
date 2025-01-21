(ns firewrap2.preset-test
  (:require
   [clojure.test :refer [deftest]]
   [firewrap2.bwrap :as bwrap]
   [firewrap2.main :as main]
   [firewrap2.preset.env :as env]
   [firewrap2.profile.godmode :as godmode]
   [firewrap2.profile.windsurf :as windsurf]
   [snap.core :as snap]))

(def test-env
  (-> (select-keys (System/getenv) env/allowed)
      (assoc "HOME" "/home/user")))

(def env-ctx {::bwrap/envs-system test-env
              ::bwrap/system-cwd "/home/me/projects/firewrap"})

(defn test-main [& args]
  (binding [main/*exec-fn* (fn [& args] args)
            bwrap/*populate-env!* (constantly env-ctx)
            bwrap/*run-effects!* (fn [_])]
    (apply main/main args)))

(deftest presets
  (binding [bwrap/*populate-env!* (constantly env-ctx)]
    (snap/match-snapshot ::godmode (main/unwrap-raw (bwrap/ctx->args (godmode/profile "/path/to/GodMode.AppImage"))))
    (snap/match-snapshot ::windsurf (main/unwrap-raw (bwrap/ctx->args (windsurf/profile))))
    (snap/match-snapshot ::windsurf-cwd (test-main "windsurf" "--cwd" "--" "."))
    (snap/match-snapshot ::ferdium (test-main "ferdium"))))

(deftest base
  (snap/match-snapshot ::base-bc (test-main "firewrap" "-bc" "--" "date"))
  (snap/match-snapshot ::base-bhn (test-main "firewrap" "-bhn" "--" "date"))
  (snap/match-snapshot ::base-home-arg (test-main "firewrap" "-b" "--home" "customhome" "--" "date")))
