(ns firewrap2.preset.dumpster-test
  (:require
   [clojure.test :refer [deftest is]]
   [firewrap2.preset.dumpster :as dumpster]))

(deftest path->appname
  (is (= "foo" (dumpster/path->appname "/usr/bin/foo")))
  (is (= "foobar" (dumpster/path->appname "FooBar")))
  (is (= "foobar" (dumpster/path->appname "/usr/bin/FooBar"))))
