(ns firewrap.microvm-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [firewrap.microvm :as microvm]
   [firewrap.sandbox :as-alias sb]))

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
    vmConfig = flake.lib.mkMicroVM {
      chdir = null;
      environmentVariables = {  };
      extraPackages = [  ];
      forwardPorts = [  ];
      networkEnabled = true;
      socketDir = \"/tmp/claude/microvm-test-id\";
      userGid = 1000;
      userHome = \"/home/testuser\";
      userName = \"testuser\";
      userUid = 1000;
      virtiofsShares = [  ];
    };
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
