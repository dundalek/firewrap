(ns firewrap.preset-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is]]
   [firewrap.sandbox :as sb]
   [firewrap.main :as main]
   [firewrap.profile.godmode :as godmode]
   [firewrap.profile.windsurf :as windsurf]
   [snap.core :as snap]))

(def test-env
  {"HOME" "/home/user"})

(def env-ctx {::sb/envs-system test-env
              ::sb/system-cwd "/home/user/somedir"})

(defn test-main [& args]
  (binding [main/*exec-fn* (fn [& args] args)
            sb/*populate-env!* (constantly env-ctx)
            sb/*run-effects!* (fn [_])]
    (apply main/main args)))

(deftest presets
  (binding [sb/*populate-env!* (constantly env-ctx)]
    (snap/match-snapshot ::godmode (main/unwrap-raw (sb/ctx->args (godmode/profile "/path/to/GodMode.AppImage"))))
    (snap/match-snapshot ::windsurf (main/unwrap-raw (sb/ctx->args (windsurf/profile nil))))
    (snap/match-snapshot ::windsurf-cwd (test-main "windsurf" "--cwd" "--" "."))
    (snap/match-snapshot ::ferdium (test-main "ferdium"))
    (snap/match-snapshot ::ferdium-absolute (test-main "/some/path/ferdium"))))

(deftest base
  (snap/match-snapshot ::no-base (test-main "firewrap" "date"))
  (snap/match-snapshot ::base-b (test-main "firewrap" "-b" "--" "date"))
  (snap/match-snapshot ::base5-b (test-main "firewrap" "-b5" "--" "date"))
  (snap/match-snapshot ::base5-bc (test-main "firewrap" "-b5c" "--" "date"))
  (snap/match-snapshot ::base5-bhn (test-main "firewrap" "-b5hn" "--" "date"))
  (snap/match-snapshot ::base5-home-arg (test-main "firewrap" "-b5" "--home" "customhome" "--" "date")))

(deftest help
  (let [help-text (with-out-str (main/print-help))]
    (is (str/includes? help-text "Usage: firewrap"))
    (is (= help-text (with-out-str (test-main "firewrap"))))
    (is (= help-text (with-out-str (test-main "firewrap" "--help"))))
    (is (= help-text (with-out-str (test-main "firewrap" "--help" "--"))))
    (is (= help-text (with-out-str (test-main "firewrap" "--help" "--" "date"))))))
