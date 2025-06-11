(ns kaocha.plugin.failure-summary
  (:require
   [kaocha.plugin :as plugin]
   [kaocha.result :as result]))

(defn collect-failed-tests
  "Recursively traverse test results and collect failed test names"
  [test-result]
  (cond
    ;; If this is a test that failed and has no nested tests (leaf test)
    (and (result/failed? test-result)
         (empty? (:kaocha.result/tests test-result)))
    [(str (:kaocha.testable/id test-result))]

    ;; If this has nested tests, recursively collect from children
    (:kaocha.result/tests test-result)
    (mapcat collect-failed-tests (:kaocha.result/tests test-result))

    ;; Otherwise return empty
    :else
    []))

(plugin/defplugin kaocha.plugin/failure-summary
  (post-summary [summary]
    (when-let [test-results (:kaocha.result/tests summary)]
      (let [failed-tests (mapcat collect-failed-tests test-results)]
        (when (seq failed-tests)
          (println)
          (println "❌ Failed tests:")
          (println)
          (doseq [test-name failed-tests]
            (println (str "  " test-name)))
          (println))))
    summary))
