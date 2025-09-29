(ns firewrap.sandbox-test
  (:require
   [clojure.test :refer [deftest is]]
   [firewrap.sandbox :as sb]
   [firewrap.tracer :as tracer]
   [snap.core :as snap]))

(defn- test-profile-simple [_]
  (sb/$-> {}
    (sb/unshare-all)
    (sb/new-session)))

(defn- test-preset [ctx arg]
  (sb/$-> ctx
    (sb/new-session)
    (sb/bind-ro arg arg)))

(defn- test-profile-nested [_]
  (sb/$-> {}
    (sb/unshare-all)
    (test-preset "/path")))

(deftest env
  (let [ctx {::sb/envs-system {"FOO" "123"}}]
    (is (= ["--unshare-all" "--unsetenv" "FOO"]
           (-> ctx
               (sb/ctx->args))))

    (is (= ["--unshare-all"]
           (-> ctx
               (sb/env-pass-many ["FOO" "BAR"])
               (sb/ctx->args))))

    (is (= ["--unshare-all" "--setenv" "FOO" "456"]
           (-> ctx
               (sb/env-set "FOO" "456")
               (sb/ctx->args))))))

(deftest instrumentation-tree-structure
  (let [[ctx node] (tracer/with-trace (test-profile-simple {}))]
    (snap/match-snapshot ::instrumentation-tree-structure
                         node)
    (is (= (test-profile-simple {}) ctx))))

(deftest instrumentation-tree-with-nested-calls
  (let [[ctx node] (tracer/with-trace (test-profile-nested {}))]
    (snap/match-snapshot ::instrumentation-tree-with-nested-calls
                         node)
    (is (= (test-profile-nested {}) ctx))))
