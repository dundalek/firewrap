(ns firewrap.bwrap-test
  (:require
   [clojure.test :refer [deftest is]]
   [firewrap.bwrap :as bwrap]))

(deftest env
  (let [ctx {::bwrap/envs-system {"FOO" "123"}}]
    (is (= ["--unsetenv" "FOO"]
           (-> ctx
               (bwrap/ctx->args))))

    (is (= []
           (-> ctx
               (bwrap/env-pass-many ["FOO" "BAR"])
               (bwrap/ctx->args))))

    (is (= ["--setenv" "FOO" "456"]
           (-> ctx
               (bwrap/env-set "FOO" "456")
               (bwrap/ctx->args))))))
