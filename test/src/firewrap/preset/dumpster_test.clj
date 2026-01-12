(ns firewrap.preset.dumpster-test
  (:require
   [clojure.test :refer [deftest is]]
   [firewrap.preset.dumpster :as dumpster]
   [firewrap.sandbox :as sb]))

(deftest path->appname
  (is (= "foo" (dumpster/path->appname "/usr/bin/foo")))
  (is (= "foobar" (dumpster/path->appname "FooBar")))
  (is (= "foobar" (dumpster/path->appname "/usr/bin/FooBar"))))

(deftest bind-cwd-rw-refuses-home
  (let [home (System/getenv "HOME")
        ctx (-> {}
                (sb/*populate-env!*)
                (assoc ::sb/system-cwd home))
        result (dumpster/bind-cwd-rw ctx {})
        comments (sb/get-comments result)
        args (sb/ctx->args result)]
    (is (= 1 (count comments)))
    (is (= :warning (:level (first comments))))
    (is (= "Refusing to bind home directory with --cwd. Use --cwd-home to explicitly allow binding home."
           (:message (first comments))))
    (is (nil? (some #(and (string? %) (= home %)) args)))))

(deftest bind-cwd-rw-allows-home-with-flag
  (let [home (System/getenv "HOME")
        ctx (-> {}
                (sb/*populate-env!*)
                (assoc ::sb/system-cwd home))
        result (dumpster/bind-cwd-rw ctx {:allow-home? true})
        args (sb/ctx->args result)]
    (is (some #(and (string? %) (= home %)) args))))

(deftest bind-cwd-rw-allows-non-home
  (let [ctx (-> {}
                (sb/*populate-env!*)
                (assoc ::sb/system-cwd "/tmp"))
        result (dumpster/bind-cwd-rw ctx {})
        args (sb/ctx->args result)]
    (is (some #(and (string? %) (= "/tmp" %)) args))))
