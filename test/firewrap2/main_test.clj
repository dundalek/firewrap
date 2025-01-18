(ns firewrap2.main-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [are deftest is]]
   [firewrap2.main :as main]))

(deftest path->appname
  (is (= "foo" (main/path->appname "/usr/bin/foo")))
  (is (= "foobar" (main/path->appname "FooBar")))
  (is (= "foobar" (main/path->appname "/usr/bin/FooBar"))))

(deftest parse-args
  (are [args expected] (= expected (main/parse-args (str/split args #"\s+")))
    "firewrap cmd"
    {:args ["cmd"]
     :opts {}}

    "firewrap cmd --arg"
    {:args ["cmd" "--arg"]
     :opts {}}

    "bin/cmd --arg"
    {:args ["bin/cmd" "--arg"]
     :opts {:profile "cmd"}}

    "cmd --arg"
    {:args ["cmd" "--arg"]
     :opts {:profile "cmd"}}

    "firewrap --profile aprofile -- cmd --arg"
    {:args ["cmd" "--arg"]
     :opts {:profile "aprofile"}}

    "cmd --profile aprofile -- --arg"
    {:args ["cmd" "--arg"]
     :opts {:profile "aprofile"}}

    ; ;; -- is mandatory, otherwise args are passed to command
    "firewrap --profile cmdarg cmd --arg"
    {:args ["--profile" "cmdarg" "cmd" "--arg"]
     :opts {}}

    "bin/cmd --profile cmdarg --arg"
    {:args ["bin/cmd" "--profile" "cmdarg" "--arg"]
     :opts {:profile "cmd"}}

    "bin/cmd --profile aprofile -- --arg"
    {:args ["bin/cmd" "--arg"]
     :opts {:profile "aprofile"}}

    ;; pass extra `--` if subcommand needs `--`
    "bin/cmd -- --arg -- some other"
    {:args ["bin/cmd" "--arg" "--" "some" "other"]
     :opts {:profile "cmd"}}))
