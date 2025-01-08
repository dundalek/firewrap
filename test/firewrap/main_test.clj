(ns firewrap.main-test
  (:require
   [clojure.test :refer [deftest is]]
   [firewrap.main :as main]))

(deftest parse-opts
  (is (= {:args []}
         (main/parse-opts [])))
  (is (= {:args ["cmd"]}
         (main/parse-opts ["cmd"])))
  (is (= {:args ["cmd" "--arg"]}
         (main/parse-opts ["cmd" "--arg"])))

  (is (= {:preset "net" :args ["cmd"]}
         (main/parse-opts ["--net" "cmd"])))
  (is (= {:preset "net" :args ["cmd" "--arg"]}
         (main/parse-opts ["--net" "cmd" "--arg"])))

  (is (= {:preset "home" :args ["cmd"] :preset-args ["cmd"]}
         (main/parse-opts ["--home" "cmd"])))
  (is (= {:preset "home" :args ["cmd"] :preset-args ["dir"]}
         (main/parse-opts ["--home" "dir" "cmd"])))
  (is (= {:preset "home" :args ["--arg"] :preset-args ["cmd"]}
         (main/parse-opts ["--home" "cmd" "--arg"])))

  (is (= {:preset "home" :args ["cmd"] :preset-args ["cmd"]}
         (main/parse-opts ["--home" "--" "cmd"])))
  (is (= {:preset "home" :args ["cmd" "--arg"] :preset-args ["cmd"]}
         (main/parse-opts ["--home" "--" "cmd" "--arg"])))

  (is (= {:preset "homenet" :args ["cmd" "--arg"] :preset-args ["cmd"]}
         (main/parse-opts ["--homenet" "--" "cmd" "--arg"]))))

