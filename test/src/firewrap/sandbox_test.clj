(ns firewrap.sandbox-test
  (:require
   [clojure.test :refer [deftest is]]
   [firewrap.sandbox :as sb]))

(deftest env
  (let [ctx {::sb/envs-system {"FOO" "123"}}]
    (is (= ["--unsetenv" "FOO"]
           (-> ctx
               (sb/ctx->args))))

    (is (= []
           (-> ctx
               (sb/env-pass-many ["FOO" "BAR"])
               (sb/ctx->args))))

    (is (= ["--setenv" "FOO" "456"]
           (-> ctx
               (sb/env-set "FOO" "456")
               (sb/ctx->args))))))
