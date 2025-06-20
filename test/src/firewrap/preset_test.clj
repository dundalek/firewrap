(ns firewrap.preset-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is]]
   [firewrap.main :as main]
   [firewrap.preset.base :as base]
   [firewrap.preset.dumpster :as dumpster]
   [firewrap.profile :as profile]
   [firewrap.profile.godmode :as godmode]
   [firewrap.profile.windsurf :as windsurf]
   [firewrap.preset.oldprofiles :as oldprofiles]
   [firewrap.sandbox :as sb]
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
    (with-redefs [profile/resolve (fn [name]
                                    (assert (= name "windsurf"))
                                    (partial windsurf/profile-with-options {:windsurf-dir "/tmp/windsurf-dir"}))]
      (snap/match-snapshot ::windsurf-cwd (test-main "windsurf" "--cwd" "--" ".")))
    (snap/match-snapshot ::ferdium (test-main "ferdium"))
    (snap/match-snapshot ::ferdium-absolute (test-main "/some/path/ferdium"))))

(deftest profiles
  (snap/match-snapshot ::bash (test-main "bash"))
  (snap/match-snapshot ::cursor (test-main "cursor"))
  (snap/match-snapshot ::date (test-main "date"))
  (snap/match-snapshot ::echo (test-main "echo"))
  (with-redefs [env-ctx (assoc-in env-ctx [::sb/envs-system "JAVA_HOME"] "/some/path/to/jdk")]
    (snap/match-snapshot ::clojure (test-main "clojure"))))

(deftest base
  (snap/match-snapshot ::no-base (test-main "firewrap" "date"))
  (snap/match-snapshot ::base-b (test-main "firewrap" "-b" "--" "date"))
  (snap/match-snapshot ::base5-b (test-main "firewrap" "-b5" "--" "date"))
  (snap/match-snapshot ::base5-bc (test-main "firewrap" "-b5c" "--" "date"))
  (snap/match-snapshot ::base5-bhn (test-main "firewrap" "-b5hn" "--" "date"))
  (snap/match-snapshot ::base5-home-arg (test-main "firewrap" "-b5" "--home" "customhome" "--" "date"))
  (with-redefs [base/bind-user-programs dumpster/bind-nix-profile
                base/bind-extra-system-programs  dumpster/bind-nix-root]
    (snap/match-snapshot ::base-b-nix (test-main "firewrap" "-b" "--" "date"))))

(deftest oldprofiles
  (binding [sb/*populate-env!* (constantly env-ctx)]
    (snap/match-snapshot ::chatall (main/unwrap-raw (sb/ctx->args (oldprofiles/chatall))))
    (snap/match-snapshot ::cheese (main/unwrap-raw (sb/ctx->args (oldprofiles/cheese {:executable "/usr/bin/cheese"}))))
    (snap/match-snapshot ::gedit (main/unwrap-raw (sb/ctx->args (oldprofiles/gedit {:executable "gedit"}))))
    (snap/match-snapshot ::gnome-calculator (main/unwrap-raw (sb/ctx->args (oldprofiles/gnome-calculator {:executable "gnome-calculator"}))))
    (snap/match-snapshot ::notify-send (main/unwrap-raw (sb/ctx->args (oldprofiles/notify-send {:executable "notify-send"}))))
    (snap/match-snapshot ::xdg-open (main/unwrap-raw (sb/ctx->args (oldprofiles/xdg-open))))))

(deftest help
  (let [help-text (with-out-str (main/print-help))]
    (is (str/includes? help-text "Usage: firewrap"))
    (is (= help-text (with-out-str (test-main "firewrap"))))
    (is (= help-text (with-out-str (test-main "firewrap" "--help"))))
    (is (= help-text (with-out-str (test-main "firewrap" "--help" "--"))))
    (is (= help-text (with-out-str (test-main "firewrap" "--help" "--" "date"))))))
