(ns firewrap.microvm-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [firewrap.microvm :as microvm]
   [firewrap.sandbox :as-alias sb]))

(deftest clj->nix-test
  (testing "primitives"
    (is (= "null" (microvm/clj->nix nil)))
    (is (= "true" (microvm/clj->nix true)))
    (is (= "false" (microvm/clj->nix false)))
    (is (= "42" (microvm/clj->nix 42)))
    (is (= "3.14" (microvm/clj->nix 3.14))))

  (testing "strings with escaping"
    (is (= "\"hello\"" (microvm/clj->nix "hello")))
    (is (= "\"with\\\"quotes\"" (microvm/clj->nix "with\"quotes")))
    (is (= "\"with\\\\backslash\"" (microvm/clj->nix "with\\backslash")))
    (is (= "\"\\${interpolation}\"" (microvm/clj->nix "${interpolation}"))))

  (testing "lists"
    (is (= "[  ]" (microvm/clj->nix [])))
    (is (= "[ 1 2 3 ]" (microvm/clj->nix [1 2 3])))
    (is (= "[ \"a\" \"b\" ]" (microvm/clj->nix ["a" "b"]))))

  (testing "maps with kebab->camel conversion"
    (is (= "{ foo = \"bar\"; }" (microvm/clj->nix {:foo "bar"})))
    (is (= "{ fooBar = 42; }" (microvm/clj->nix {:foo-bar 42}))))

  (testing "nested structures"
    (is (= "{ host = { address = \"127.0.0.1\"; port = 8080; }; }"
           (microvm/clj->nix (sorted-map :host (sorted-map :address "127.0.0.1" :port 8080)))))))

(deftest parse-port-spec-test
  (testing "single port (same for host and guest)"
    (is (= {:from "host"
            :host {:address "127.0.0.1" :port 8080}
            :guest {:port 8080}}
           (microvm/parse-port-spec "8080"))))

  (testing "hostPort:guestPort"
    (is (= {:from "host"
            :host {:address "127.0.0.1" :port 8080}
            :guest {:port 3000}}
           (microvm/parse-port-spec "8080:3000"))))

  (testing "hostAddr:hostPort:guestPort"
    (is (= {:from "host"
            :host {:address "0.0.0.0" :port 8080}
            :guest {:port 3000}}
           (microvm/parse-port-spec "0.0.0.0:8080:3000"))))

  (testing "invalid port spec throws"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Invalid port spec"
                          (microvm/parse-port-spec "a:b:c:d")))))

(deftest mk-virtiofs-share-test
  (testing "generates complete share config with socket path"
    (is (= {:proto "virtiofs"
            :tag "share-0"
            :source "/home/user/project"
            :mount-point "/mnt/project"
            :socket "/tmp/sockets/virtiofs-share-0.sock"
            :read-only false}
           (microvm/mk-virtiofs-share "/tmp/sockets" 0
                                       {:source "/home/user/project"
                                        :target "/mnt/project"
                                        :read-only false}))))

  (testing "handles read-only shares"
    (is (= {:proto "virtiofs"
            :tag "share-1"
            :source "/etc/config"
            :mount-point "/mnt/config"
            :socket "/tmp/sockets/virtiofs-share-1.sock"
            :read-only true}
           (microvm/mk-virtiofs-share "/tmp/sockets" 1
                                       {:source "/etc/config"
                                        :target "/mnt/config"
                                        :read-only true})))))

(deftest generate-profile-content-test
  (testing "generates environment exports"
    (is (= "export FOO='bar'\nexport BAZ='qux'"
           (microvm/generate-profile-content {"FOO" "bar" "BAZ" "qux"} nil))))

  (testing "generates chdir command"
    (is (= "cd '/home/user/project'"
           (microvm/generate-profile-content {} "/home/user/project"))))

  (testing "generates both exports and chdir"
    (is (= "export PATH='/usr/bin'\ncd '/home/user'"
           (microvm/generate-profile-content {"PATH" "/usr/bin"} "/home/user"))))

  (testing "escapes single quotes in values"
    (is (= "export MSG='it'\\''s working'"
           (microvm/generate-profile-content {"MSG" "it's working"} nil))))

  (testing "empty when no vars and no chdir"
    (is (= ""
           (microvm/generate-profile-content {} nil)))))

(deftest run-microvm
  (testing "calls exec-fn with correct nix arguments"
    (let [exec-calls (atom [])
          mock-exec (fn [& args] (swap! exec-calls conj (vec args)))
          socket-dir "/tmp/claude/microvm-test-id"
          config {:user-name "testuser"
                  :user-home "/home/testuser"
                  :user-uid 1000
                  :user-gid 1000
                  :socket-dir socket-dir
                  :virtiofs-shares []
                  :environment-variables {}
                  :network-enabled true
                  :forward-ports []
                  :extra-packages []}
          expected-nix-expr "
  let
    flake = builtins.getFlake \"/tmp/test-project/experiments/microvm\";
    pkgs = flake.inputs.nixpkgs.legacyPackages.x86_64-linux;
    vmConfig = flake.lib.mkMicroVM { extraPackages = [  ]; firewallPorts = [  ]; forwardPorts = [  ]; networkEnabled = true; profileContent = \"\"; socketDir = \"/tmp/claude/microvm-test-id\"; userGid = 1000; userHome = \"/home/testuser\"; userName = \"testuser\"; userUid = 1000; virtiofsShares = [  ]; };
  in
    vmConfig.wrappedRunner
"]
      (binding [microvm/*exec-fn* mock-exec]
        (microvm/run-microvm config {:dry-run false
                                     :flake-path "/tmp/test-project/experiments/microvm"
                                     :socket-dir "/tmp/claude/microvm-test-test-id"}))
      (is (= [["nix" "run" "--impure" "--expr" expected-nix-expr]] @exec-calls)))))

(deftest ctx->microvm-config-network
  (testing "enables network when net namespace is shared"
    (let [ctx {::sb/args []
               ::sb/shared-namespaces #{"net"}}
          result (microvm/ctx->microvm-config ctx)]
      (is (true? (get-in result [:config :network-enabled])))))

  (testing "disables network when net namespace is not shared"
    (let [ctx {::sb/args []
               ::sb/shared-namespaces #{}}
          result (microvm/ctx->microvm-config ctx)]
      (is (false? (get-in result [:config :network-enabled])))))

  (testing "disables network when other namespaces are shared but not net"
    (let [ctx {::sb/args []
               ::sb/shared-namespaces #{"ipc" "pid"}}
          result (microvm/ctx->microvm-config ctx)]
      (is (false? (get-in result [:config :network-enabled]))))))
