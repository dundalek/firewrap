(ns firewrap2.bwrap-test
  (:require
   [clojure.test :refer [deftest is]]
   [firewrap2.bwrap :as bwrap]))

(deftest foo
  (binding [bwrap/*system-getenv* (constantly {"FOO" "123"})]
    (is (= ["--unsetenv" "FOO"]
           (-> (bwrap/populate-envs! {})
               (bwrap/ctx->args))))

    (is (= []
           (-> (bwrap/populate-envs! {})
               (bwrap/env-pass-many ["FOO" "BAR"])
               (bwrap/ctx->args))))

    (is (= ["--setenv" "FOO" "456"]
           (-> (bwrap/populate-envs! {})
               (bwrap/env-set "FOO" "456")
               (bwrap/ctx->args))))))
