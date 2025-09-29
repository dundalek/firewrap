(ns src.firewrap.tracer-test
  (:require
   [clojure.test :refer [deftest is]]
   [firewrap.sandbox :as sb]
   [firewrap.tracer :refer [with-trace span->]]))

(deftest tracing
  (let [preset (fn [ctx]
                 (span-> ctx
                         (sb/bind-ro "/a")
                         (sb/bind-ro "/b")))
        [result trace] (with-trace
                         (span-> {}
                                 (preset)
                                 (sb/bind-ro "/c")))]
    (is (= (-> {}
               (sb/bind-ro "/a")
               (sb/bind-ro "/b")
               (sb/bind-ro "/c"))
           result))

    (is (= '{:children [{:children [{:children [],
                                     :ctx-next {:firewrap.sandbox/args [["--ro-bind" "/a" "/a"]]},
                                     :ctx-prev {},
                                     :form (sb/bind-ro "/a"),
                                     :location {:column 26, :file "src/firewrap/tracer_test.clj", :line 10}}
                                    {:children [],
                                     :ctx-next {:firewrap.sandbox/args [["--ro-bind" "/a" "/a"]
                                                                        ["--ro-bind" "/b" "/b"]]},
                                     :ctx-prev {:firewrap.sandbox/args [["--ro-bind" "/a" "/a"]]},
                                     :form (sb/bind-ro "/b"),
                                     :location {:column 26, :file "src/firewrap/tracer_test.clj", :line 11}}],
                         :ctx-next {:firewrap.sandbox/args [["--ro-bind" "/a" "/a"] ["--ro-bind" "/b" "/b"]]},
                         :ctx-prev {},
                         :form (preset),
                         :location {:column 34, :file "src/firewrap/tracer_test.clj", :line 14}}
                        {:children [],
                         :ctx-next {:firewrap.sandbox/args [["--ro-bind" "/a" "/a"]
                                                            ["--ro-bind" "/b" "/b"]
                                                            ["--ro-bind" "/c" "/c"]]},
                         :ctx-prev {:firewrap.sandbox/args [["--ro-bind" "/a" "/a"] ["--ro-bind" "/b" "/b"]]},
                         :form (sb/bind-ro "/c"),
                         :location {:column 34, :file "src/firewrap/tracer_test.clj", :line 15}}]}
           trace))))

(deftest tracing-without-parens
  (let [preset (fn [ctx]
                 (span-> ctx
                         (sb/bind-ro "/a")
                         (sb/bind-ro "/b")))
        [result trace] (with-trace
                         (span-> {}
                                 preset
                                 (sb/bind-ro "/c")))]
    (is (= (-> {}
               (sb/bind-ro "/a")
               (sb/bind-ro "/b")
               (sb/bind-ro "/c"))
           result))

    (is (= '{:children [{:children [{:children [],
                                     :ctx-next {:firewrap.sandbox/args [["--ro-bind" "/a" "/a"]]},
                                     :ctx-prev {},
                                     :form (sb/bind-ro "/a"),
                                     :location {:column 26, :file "src/firewrap/tracer_test.clj", :line 49}}
                                    {:children [],
                                     :ctx-next {:firewrap.sandbox/args [["--ro-bind" "/a" "/a"]
                                                                        ["--ro-bind" "/b" "/b"]]},
                                     :ctx-prev {:firewrap.sandbox/args [["--ro-bind" "/a" "/a"]]},
                                     :form (sb/bind-ro "/b"),
                                     :location {:column 26, :file "src/firewrap/tracer_test.clj", :line 50}}],
                         :ctx-next {:firewrap.sandbox/args [["--ro-bind" "/a" "/a"] ["--ro-bind" "/b" "/b"]]},
                         :ctx-prev {},
                         :form preset,
                         :location {:column nil, :file "src/firewrap/tracer_test.clj", :line nil}}
                        {:children [],
                         :ctx-next {:firewrap.sandbox/args [["--ro-bind" "/a" "/a"]
                                                            ["--ro-bind" "/b" "/b"]
                                                            ["--ro-bind" "/c" "/c"]]},
                         :ctx-prev {:firewrap.sandbox/args [["--ro-bind" "/a" "/a"] ["--ro-bind" "/b" "/b"]]},
                         :form (sb/bind-ro "/c"),
                         :location {:column 34, :file "src/firewrap/tracer_test.clj", :line 54}}]}
           trace))))
