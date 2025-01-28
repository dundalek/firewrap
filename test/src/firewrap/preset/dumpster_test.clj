(ns firewrap.preset.dumpster-test
  (:require
   [clojure.test :refer [deftest is]]
   [firewrap.preset.dumpster :as dumpster]))

(deftest path->appname
  (is (= "foo" (dumpster/path->appname "/usr/bin/foo")))
  (is (= "foobar" (dumpster/path->appname "FooBar")))
  (is (= "foobar" (dumpster/path->appname "/usr/bin/FooBar"))))
