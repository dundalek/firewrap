(ns firewrap.preset-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is]]
   [firewrap.main :as main]
   [firewrap.preset.base :as base]
   [firewrap.preset.dumpster :as dumpster]
   [firewrap.preset.oldprofiles :as oldprofiles]
   [firewrap.preset.oldsystem :as system]
   [firewrap.profile :as profile]
   [firewrap.profile.claude :as claude]
   [firewrap.profile.godmode :as godmode]
   [firewrap.profile.windsurf :as windsurf]
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

(defn test-raw-profile [profile-fn]
  (binding [sb/*populate-env!* (constantly env-ctx)]
    (main/unwrap-raw (sb/ctx->args (sb/interpret-hiccup (profile-fn))))))

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

(deftest profiles
  (snap/match-snapshot ::godmode (test-raw-profile #(godmode/profile "/path/to/GodMode.AppImage")))
  (snap/match-snapshot ::windsurf (test-raw-profile #(windsurf/profile nil)))
  (snap/match-snapshot ::claude-wide (test-raw-profile #(claude/wide nil)))
  (snap/match-snapshot ::claude-wide (test-raw-profile (fn [] [[claude/wide]])))

  (snap/match-snapshot ::ferdium (test-main "ferdium"))
  (snap/match-snapshot ::ferdium-absolute (test-main "/some/path/ferdium"))
  (snap/match-snapshot ::bash (test-main "bash"))
  (snap/match-snapshot ::cursor (test-main "cursor"))
  (snap/match-snapshot ::date (test-main "date"))
  (snap/match-snapshot ::echo (test-main "echo"))

  (with-redefs [profile/resolve (fn [name]
                                  (assert (= name "windsurf"))
                                  (partial windsurf/profile-with-options {:windsurf-dir "/tmp/windsurf-dir"}))]
    (snap/match-snapshot ::windsurf-cwd (test-main "windsurf" "--cwd" "--" ".")))

  (with-redefs [env-ctx (assoc-in env-ctx [::sb/envs-system "JAVA_HOME"] "/some/path/to/jdk")]
    (snap/match-snapshot ::clojure (test-main "clojure"))))

(deftest oldprofiles
  (snap/match-snapshot ::chatall (test-raw-profile oldprofiles/chatall))
  (snap/match-snapshot ::cheese (test-raw-profile #(oldprofiles/cheese {:executable "/usr/bin/cheese"})))
  (snap/match-snapshot ::gedit (test-raw-profile #(oldprofiles/gedit {:executable "gedit"})))
  (snap/match-snapshot ::gnome-calculator (test-raw-profile #(oldprofiles/gnome-calculator {:executable "gnome-calculator"})))
  (snap/match-snapshot ::notify-send (test-raw-profile #(oldprofiles/notify-send {:executable "notify-send"})))
  (snap/match-snapshot ::xdg-open (test-raw-profile oldprofiles/xdg-open)))

(deftest help
  (let [help-text (with-out-str (main/print-help))]
    (is (str/includes? help-text "Usage: firewrap"))
    (is (= help-text (with-out-str (test-main "firewrap"))))
    (is (= help-text (with-out-str (test-main "firewrap" "--help"))))
    (is (= help-text (with-out-str (test-main "firewrap" "--help" "--"))))
    (is (= help-text (with-out-str (test-main "firewrap" "--help" "--" "date"))))))

(deftest main-dry-run-preview
  (is (= "Firewrap sandbox:
  bwrap --unshare-all --die-with-parent --new-session
  --dev /dev
  --ro-bind /etc /etc
  --proc /proc
  --tmpfs /tmp
  --ro-bind /usr /usr
  --symlink usr/bin /bin
  --symlink usr/sbin /sbin
  --symlink usr/lib /lib
  --symlink usr/lib64 /lib64
  --ro-bind-try /lib32 /lib32
  --tmpfs /home/user
  echo hello world
"
         (with-out-str (binding [*err* *out*]
                         (test-main "firewrap" "-b" "--dry-run" "--" "echo" "hello" "world"))))))

(defn- my-preset-comp [ctx]
  (-> ctx
      (system/libs)))

(defn- my-preset-hiccup [_ctx]
  [system/libs])

(defn- my-preset-threaded-hiccup [ctx]
  [[ctx]
   [system/libs]])

(deftest hiccup
  (binding [sb/*populate-env!* (constantly env-ctx)]
    (let [example-echo (-> (base/base)
                           (system/libs)
                           (system/command "echo"))
          example-libs (-> (base/base)
                           (system/libs))]
      (is (= example-echo
             (sb/interpret-hiccup [[base/base]
                                   [system/libs]
                                   [system/command "echo"]])))

      (is (= example-echo
             (sb/interpret-hiccup (sb/$-> (base/base)
                                          (system/libs)
                                          (system/command "echo")))))

      (is (= example-libs
             (sb/interpret-hiccup (base/base) [[my-preset-hiccup]])))
      (is (= example-libs
             (sb/interpret-hiccup (base/base) [[my-preset-comp]])))
      (is (= example-libs
             (sb/interpret-hiccup (base/base) [[my-preset-threaded-hiccup]]))))))
