(ns firewrap2.preset-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is]]
   [firewrap2.bwrap :as bwrap]
   [firewrap2.main :as main]
   [firewrap2.profile.godmode :as godmode]
   [firewrap2.profile.windsurf :as windsurf]
   [snap.core :as snap]))

(def test-env
  {"HOME" "/home/user"})

(def env-ctx {::bwrap/envs-system test-env
              ::bwrap/system-cwd "/home/user/somedir"})

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
  (snap/match-snapshot ::no-base (test-main "firewrap" "date"))
  (snap/match-snapshot ::base-b (test-main "firewrap" "-b" "--" "date"))
  (snap/match-snapshot ::base-bc (test-main "firewrap" "-bc" "--" "date"))
  (snap/match-snapshot ::base-bhn (test-main "firewrap" "-bhn" "--" "date"))
  (snap/match-snapshot ::base-home-arg (test-main "firewrap" "-b" "--home" "customhome" "--" "date")))

(deftest help
  (let [help-text (with-out-str (main/print-help))]
    (is (str/includes? help-text "Usage: firewrap"))
    (is (= help-text (with-out-str (test-main "firewrap"))))
    (is (= help-text (with-out-str (test-main "firewrap" "--help"))))
    (is (= help-text (with-out-str (test-main "firewrap" "--help" "--"))))
    (is (= help-text (with-out-str (test-main "firewrap" "--help" "--" "date"))))))
