(ns firewrap.sandbox-test
  (:require
   [clojure.test :refer [deftest is]]
   [firewrap.sandbox :as sb]
   [snap.core :as snap]))

(defn- test-profile-simple [_]
  (sb/$->
    (sb/unshare-all)
    (sb/new-session)))

(defn- test-preset [ctx arg]
  (sb/$->
    (sb/new-session)
    (sb/bind-ro ctx arg arg)))

(defn- test-profile-nested [_]
  (sb/$->
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
  (let [profile [test-profile-simple]
        [node ctx] (sb/interpret-instrumenting profile)]
    (snap/match-snapshot ::instrumentation-tree-structure
                         (:children node))
    (is (= (sb/interpret-hiccup profile) ctx))))

(deftest instrumentation-tree-with-nested-calls
  (let [profile [test-profile-nested]
        [node ctx] (sb/interpret-instrumenting profile)]
    (snap/match-snapshot ::instrumentation-tree-with-nested-calls
                         (:children node))
    (is (= (sb/interpret-hiccup profile) ctx))))
