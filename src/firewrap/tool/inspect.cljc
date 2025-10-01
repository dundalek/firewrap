(ns firewrap.tool.inspect
  (:require
   [clojure.java.io :as io]
   [firewrap.main :as main]
   [firewrap.sandbox :as sb]
   [firewrap.tool.portlet.viewers :as-alias viewers]
   [portal.api :as p]
   [portal.viewer :as pv]))

(defn load-viewers! []
  (p/eval-str (slurp (io/resource "firewrap/tool/portlet/viewers.cljs"))))

(defn args->tree [args]
  (let [parsed (main/parse-args args)
        profile-fn (main/resolve-profile-fn parsed)
        data [profile-fn]
        [tree-data _ctx] (sb/interpret-instrumenting data)]
    tree-data))

(defn inspect-sandbox [{:keys [args]}]
  (main/load-user-config)
  (p/open {:on-load (fn []
                      (load-viewers!)
                      (p/submit (pv/default
                                 (args->tree args)
                                 ::viewers/profile-tree)))})
  @(promise))

(comment
  (main/load-user-config)

  (require '[clojure.walk :as walk])

  (defn compact-tree [tree-data]
    (walk/postwalk
     (fn [x] (if (map? x)
               (let [{:keys [symbol children]} x]
                 (cond-> {}
                   symbol (assoc :symbol symbol)
                   (seq children) (assoc :children children)))
               x))
     tree-data))

  (defn test-interpret [forms]
    (compact-tree
     (first (sb/interpret-instrumenting forms))))

  (test-interpret [firewrap.profile.claude/wide])
  (test-interpret [firewrap.profile.date/profile])
  (test-interpret [(firewrap.profile/resolve "date")])

  (compact-tree (args->tree ["firewrap" "--profile" "date" "--" "date"]))

  (compact-tree (args->tree ["firewrap" "--profile" "claude" "--" "claude"])))
