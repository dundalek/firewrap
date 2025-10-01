(ns firewrap.tool.portlet
  (:require
   [clojure.java.io :as io]
   [firewrap.profile.claude :as claude]
   [firewrap.sandbox :as sb]
   [firewrap.tool.portlet.viewers :as-alias viewers]
   [portal.api :as p]
   [portal.viewer :as pv]))

(defn load-viewers! []
  (p/eval-str (slurp (io/resource "firewrap/tool/portlet/viewers.cljs"))))

(comment
  (load-viewers!)

  (let [[tree-data _ctx] (sb/interpret-instrumenting [claude/wide])]
    (p/submit (pv/default tree-data ::viewers/profile-tree)))

  (let [[tree-data _ctx] (sb/interpret-instrumenting [claude/wide])]
    (p/submit tree-data)))
