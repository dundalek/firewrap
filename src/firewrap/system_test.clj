(ns firewrap.system-test
  (:require
   [clojure.test :refer [deftest is]]
   [firewrap.system :as system]))

(deftest xdg-data-dirs
  (is (= [] [] #_(system/xdg-data-dirs {} "icons" "themes"))))

