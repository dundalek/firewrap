(ns firewrap.tool.portlet
  (:require
   [clojure.java.io :as io]
   [firewrap.profile.claude :as claude]
   [firewrap.sandbox :as sb]
   [portal.api :as p]))

(defn load-viewers! []
  (p/eval-str (slurp (io/resource "firewrap/tool/portlet/viewers.cljs"))))

(comment
  (load-viewers!)
  (let [[tree-data _ctx] (sb/interpret-instrumenting [claude/wide])]
    (p/submit tree-data)))
