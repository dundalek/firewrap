(ns firewrap.microvm-test
  (:require
   [babashka.fs :as fs]
   [clojure.string :as str]
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
    (is (= "[ 1\n2\n3 ]" (microvm/clj->nix [1 2 3])))
    (is (= "[ \"a\"\n\"b\" ]" (microvm/clj->nix ["a" "b"]))))

  (testing "maps with kebab->camel conversion"
    (is (= "{ foo = \"bar\";\n }" (microvm/clj->nix {:foo "bar"})))
    (is (= "{ fooBar = 42;\n }" (microvm/clj->nix {:foo-bar 42}))))

  (testing "nested structures"
    (is (= "{ host = { address = \"127.0.0.1\";\n port = 8080;\n };\n }"
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

(deftest run-microvm-test
  (testing "creates temp flake and calls nix run on it"
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
                  :extra-packages []}]
      (binding [microvm/*exec-fn* mock-exec]
        (microvm/run-microvm config {:dry-run false
                                     :socket-dir "/tmp/claude/microvm-test-test-id"}))
      (is (= 1 (count @exec-calls)))
      (let [[cmd run-arg temp-dir] (first @exec-calls)]
        (is (= "nix" cmd))
        (is (= "run" run-arg))
        (is (str/includes? temp-dir "firewrap-microvm-"))))))

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

(deftest config->vm-config-test
  (testing "transforms config into VM config format"
    (let [config {:user-name "testuser"
                  :user-home "/home/testuser"
                  :user-uid 1000
                  :user-gid 1000
                  :socket-dir "/tmp/sockets"
                  :virtiofs-shares [{:source "/src" :target "/mnt/src" :read-only true}]
                  :chdir "/mnt/src"
                  :environment-variables {"FOO" "bar"}
                  :network-enabled true
                  :forward-ports ["8080:3000"]
                  :extra-packages ["git"]}
          result (microvm/config->vm-config config)]
      (is (= "testuser" (:user-name result)))
      (is (= "/home/testuser" (:user-home result)))
      (is (= 1000 (:user-uid result)))
      (is (= 1000 (:user-gid result)))
      (is (= "/tmp/sockets" (:socket-dir result)))
      (is (= ["git"] (:extra-packages result)))
      (is (= true (:network-enabled result)))
      (is (= [3000] (:firewall-ports result)))
      (is (= 1 (count (:virtiofs-shares result))))
      (is (str/includes? (:profile-content result) "export FOO='bar'"))
      (is (str/includes? (:profile-content result) "cd '/mnt/src'")))))

(deftest generate-args-nix-test
  (testing "generates valid Nix attrset"
    (let [config {:user-name "testuser"
                  :user-home "/home/testuser"
                  :user-uid 1000
                  :user-gid 1000
                  :socket-dir "/tmp/sockets"
                  :virtiofs-shares []
                  :environment-variables {}
                  :network-enabled true
                  :forward-ports []
                  :extra-packages []}
          result (microvm/generate-args-nix config)]
      (is (str/includes? result "userName = \"testuser\""))
      (is (str/includes? result "userHome = \"/home/testuser\""))
      (is (str/includes? result "userUid = 1000"))
      (is (str/includes? result "networkEnabled = true"))
      (is (str/includes? result "Generated by firewrap")))))

(deftest generate-flake-nix-test
  (testing "generates simple flake.nix entry point"
    (let [result (microvm/generate-flake-nix)]
      (is (str/includes? result "description = \"NixOS in MicroVMs\""))
      (is (str/includes? result "inputs.microvm"))
      (is (str/includes? result "import ./microvm-args.nix"))
      (is (str/includes? result "import ./microvm-static.nix"))
      (is (str/includes? result "mkMicroVM args")))))

(deftest generate-static-nix-test
  (testing "returns static template with mkMicroVM builder"
    (let [result (microvm/generate-static-nix)]
      (is (str/includes? result "{ nixpkgs, microvm }"))
      (is (str/includes? result "userName,"))
      (is (str/includes? result "socketDir,"))
      (is (str/includes? result "nixpkgs.lib.nixosSystem"))
      (is (str/includes? result "microvm.nixosModules.microvm"))
      (is (str/includes? result "USER CUSTOMIZATION")))))

(deftest export-flake-test
  (testing "exports flake files to directory"
    (let [temp-dir (str (fs/create-temp-dir {:prefix "microvm-export-test"}))
          config {:user-name "testuser"
                  :user-home "/home/testuser"
                  :user-uid 1000
                  :user-gid 1000
                  :virtiofs-shares []
                  :environment-variables {}
                  :network-enabled true}]
      (try
        (microvm/export-flake config {:export-dir temp-dir
                                      :socket-dir "/tmp/sockets"
                                      :packages []
                                      :forward-ports []})
        (is (fs/exists? (fs/path temp-dir "flake.nix")))
        (is (fs/exists? (fs/path temp-dir "microvm-args.nix")))
        (is (fs/exists? (fs/path temp-dir "microvm-static.nix")))
        (is (fs/exists? (fs/path temp-dir "flake.lock")))
        (is (str/includes? (slurp (str (fs/path temp-dir "flake.nix"))) "import ./microvm-args.nix"))
        (is (str/includes? (slurp (str (fs/path temp-dir "microvm-args.nix"))) "userName = \"testuser\""))
        (finally
          (fs/delete-tree temp-dir)))))

  (testing "preserves existing static file"
    (let [temp-dir (str (fs/create-temp-dir {:prefix "microvm-export-test"}))
          static-path (fs/path temp-dir "microvm-static.nix")
          custom-content "{ mem = 4096; }"
          config {:user-name "testuser"
                  :user-home "/home/testuser"
                  :user-uid 1000
                  :user-gid 1000
                  :virtiofs-shares []
                  :environment-variables {}
                  :network-enabled true}]
      (try
        (fs/create-dirs temp-dir)
        (spit (str static-path) custom-content)
        (microvm/export-flake config {:export-dir temp-dir
                                      :socket-dir "/tmp/sockets"
                                      :packages []
                                      :forward-ports []})
        (is (= custom-content (slurp (str static-path))))
        (finally
          (fs/delete-tree temp-dir))))))
