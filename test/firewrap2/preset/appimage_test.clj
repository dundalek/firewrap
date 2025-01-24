(ns firewrap2.preset.appimage-test
  (:require
   [clojure.test :refer [deftest is]]
   [firewrap2.preset.appimage :as appimage]))

(deftest appimage-command?
  (is (= true (appimage/appimage-command? "x.AppImage")))
  (is (= true (appimage/appimage-command? "x.appimage")))
  (is (= false (appimage/appimage-command? "x"))))
