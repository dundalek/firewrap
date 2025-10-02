(ns firewrap.preset-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
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

(def default-args ["bwrap" "--unshare-all" "--die-with-parent" "--new-session"])

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

(deftest profile-windsurf
  (snap/match-snapshot ::windsurf (test-raw-profile #(windsurf/profile nil))))

(deftest profile-windsurf-with-options
  (with-redefs [profile/resolve (fn [name]
                                  (assert (= name "windsurf"))
                                  (partial windsurf/profile-with-options {:windsurf-dir "/tmp/windsurf-dir"}))]
    (snap/match-snapshot ::windsurf-cwd (test-main "windsurf" "--cwd" "--" "."))))

(deftest profiles
  (snap/match-snapshot ::godmode (test-raw-profile #(godmode/profile "/path/to/GodMode.AppImage")))
  (snap/match-snapshot ::claude-wide (test-raw-profile #(claude/wide nil)))
  (snap/match-snapshot ::claude-wide (test-raw-profile (fn [] [[claude/wide]])))

  (snap/match-snapshot ::ferdium (test-main "ferdium"))
  (snap/match-snapshot ::ferdium-absolute (test-main "/some/path/ferdium"))
  (snap/match-snapshot ::bash (test-main "bash"))
  (snap/match-snapshot ::cursor (test-main "cursor"))
  (snap/match-snapshot ::date (test-main "date"))
  (snap/match-snapshot ::echo (test-main "echo"))

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
         (with-out-str (binding [*err* *out*
                                 main/*interactive* false]
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

(deftest bind-arguments
  (with-redefs [env-ctx (assoc env-ctx ::sb/envs-system {})]
    (is (= (concat default-args ["--ro-bind" "/tmp" "/tmp"
                                 "date"])
           (test-main "firewrap" "--bind-ro" "/tmp" "--" "date")))
    (testing "order of bind arguments is significant"
      (is (= (concat default-args ["--ro-bind" "/a" "/a"
                                   "--bind" "/b" "/b"
                                   "--ro-bind" "/c" "/c"
                                   "date"])
             (test-main "firewrap" "--bind-ro" "/a" "--bind-rw" "/b" "--bind-ro" "/c" "--" "date"))))

    (is (= (concat default-args ["--ro-bind" "/tmp" "/tmp"
                                 "date"])
           (test-main "firewrap" "--bind-ro" "/tmp" "--" "date")))

    (is (= (concat default-args ["--ro-bind" "/etc" "/config"
                                 "date"])
           (test-main "firewrap" "--bind-ro" "/etc:/config" "--" "date")))

    (is (= (concat default-args ["--ro-bind" "/tmp" "/tmp"
                                 "--ro-bind" "/var" "/var"
                                 "date"])
           (test-main "firewrap" "--bind-ro" "/tmp" "--bind-ro" "/var:/var" "--" "date")))

    (is (= (concat default-args ["--ro-bind" "/tmp" "/tmp"
                                 "--bind" "/home/user/work" "/home/user/work"
                                 "--dev-bind" "/dev/null" "/dev/null"
                                 "date"])
           (test-main "firewrap" "--bind-ro" "/tmp" "--bind-rw" "/home/user/work" "--bind-dev" "/dev/null" "--" "date")))

    (is (= (concat default-args ["--ro-bind" "/usr/local/bin" "/usr/bin"
                                 "--ro-bind" "/opt/app" "/opt/app"
                                 "date"])
           (test-main "firewrap" "--bind-ro" "/usr/local/bin:/usr/bin" "--bind-ro" "/opt/app" "--" "date")))))

(deftest env-pass-arguments
  (with-redefs [env-ctx (assoc env-ctx ::sb/envs-system {"TEST_VAR1" "value1" "TEST_VAR2" "value2" "OTHER_VAR" "other"})]
    (is (= (concat default-args ["--unsetenv" "OTHER_VAR"
                                 "date"])
           (test-main "firewrap" "--env-pass" "TEST_VAR1" "--env-pass" "TEST_VAR2" "--" "date")))))

(deftest env-set-arguments
  (with-redefs [env-ctx (assoc env-ctx ::sb/envs-system {})]
    (is (= (concat default-args ["--setenv" "MYVAR" "myvalue"
                                 "date"])
           (test-main "firewrap" "--env-set" "MYVAR" "myvalue" "--" "date")))

    (is (= (concat default-args ["--setenv" "VAR1" "value1" "--setenv" "VAR2" "value2"
                                 "date"])
           (test-main "firewrap" "--env-set" "VAR1" "value1" "--env-set" "VAR2" "value2" "--" "date")))))

(deftest env-unset-arguments
  (with-redefs [env-ctx (assoc env-ctx ::sb/envs-system {"VAR1" "existing"})]
    (is (= (concat default-args ["--unsetenv" "VAR1"
                                 "date"])
           (test-main "firewrap" "--env-unset" "VAR1" "--" "date")))))

(deftest mixed-env-set-env-unset-arguments
  (with-redefs [env-ctx (assoc env-ctx ::sb/envs-system {"UNSET" "value"})]
    (is (= (concat default-args ["--unsetenv" "UNSET" "--setenv" "KEEP" "value"
                                 "date"])
           (test-main "firewrap" "--env-set" "KEEP" "value" "--env-unset" "UNSET" "--" "date")))))
