(ns firewrap.main-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [are deftest is]]
   [firewrap.main :as main]))

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
     :opts {:profile "cmd"}}

    "firewrap -bcn -- cmd --arg"
    {:args ["cmd" "--arg"]
     :opts {:base true :cwd true :net true}}

    "firewrap -bn -- cmd -c"
    {:args ["cmd" "-c"]
     :opts {:base true :net true}}

    "firewrap --base 4 -- cmd"
    {:args ["cmd"]
     :opts {:base 4}}

    "firewrap -b4 -- cmd"
    {:args ["cmd"]
     :opts {:base 4}}

    "firewrap -b4n -- cmd -b4"
    {:args ["cmd" "-b4"]
     :opts {:base 4 :net true}}

    "firewrap -cb5 -- cmd -b5"
    {:args ["cmd" "-b5"]
     :opts {:base 5 :cwd true}}

    "firewrap --env-pass VAR1 --env-pass VAR2 -- cmd"
    {:args ["cmd"]
     :opts {:env-pass ["VAR1" "VAR2"]}}

    "firewrap --env-pass TEST_VAR -- echo test"
    {:args ["echo" "test"]
     :opts {:env-pass ["TEST_VAR"]}}

    "firewrap --env-unset BADVAR -- cmd"
    {:args ["cmd"]
     :opts {:env-vars [[:unsetenv "BADVAR"]]}}

    "firewrap --env-set MYVAR value -- cmd"
    {:args ["cmd"]
     :opts {:env-vars [[:setenv "MYVAR" "value"]]}}

    "firewrap --env-set VAR1 val1 --env-unset VAR2 -- cmd"
    {:args ["cmd"]
     :opts {:env-vars [[:setenv "VAR1" "val1"] [:unsetenv "VAR2"]]}}

    "firewrap --cwd-home -- cmd"
    {:args ["cmd"]
     :opts {:cwd-home true}}

    "firewrap --microvm --shell -- bash"
    {:args ["bash"]
     :opts {:microvm true :shell true}}

    "firewrap --microvm --shell /path/to/project -- bash"
    {:args ["bash"]
     :opts {:microvm true :shell "/path/to/project"}}))

(deftest preprocess-short-options
  (is (= ["--base" "4" "cmd"] (main/preprocess-short-options ["-b4" "cmd"])))
  (is (= ["-cn" "--base" "4" "cmd"] (main/preprocess-short-options ["-b4cn" "cmd"])))
  (is (= ["-cn" "--base" "5" "cmd"] (main/preprocess-short-options ["-cnb5" "cmd"]))))
