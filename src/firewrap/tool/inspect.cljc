(ns firewrap.tool.inspect
  (:require
   [clojure.java.io :as io]
   [firewrap.main :as main]
   [firewrap.sandbox :as sb]
   [firewrap.tool.portlet.viewers :as-alias viewers]
   [firewrap.tracer :as tracer]
   [portal.api :as p]
   [portal.viewer :as pv]))

(defn load-viewers! []
  (p/eval-str (slurp (io/resource "firewrap/tool/portlet/viewers.cljs"))))

(defn args->tree [args]
  (let [parsed (main/parse-args args)
        profile-fn (main/resolve-profile-fn parsed)
        [_ctx tree-data] (tracer/with-trace (profile-fn parsed))]
    tree-data))

(defn inspect-sandbox [{:keys [args]}]
  (p/open {:on-load (fn []
                      (load-viewers!)
                      (p/submit (pv/default
                                 (args->tree args)
                                 ::viewers/profile-tree)))}))

(comment
  (main/load-user-config)

  (do
    (require '[clojure.walk :as walk])

    (defn compact-tree [tree-data]
      (walk/postwalk
       (fn [x] (if (map? x)
                 (let [{:keys [form children]} x]
                   (cond-> {}
                     form (assoc :form form)
                     (seq children) (assoc :children children)))
                 x))
       tree-data))

    (defn test-interpret [f]
      (compact-tree
       (second (tracer/with-trace (f {}))))))

  (test-interpret firewrap.profile.claude/wide)
  (test-interpret firewrap.profile.date/profile)
  (test-interpret (firewrap.profile/resolve "date"))

  (test-interpret firewrap.preset.base/base4)
  (test-interpret firewrap.preset.base/base5)

  (compact-tree (args->tree ["firewrap" "--profile" "date" "--" "date"]))
  (compact-tree (args->tree ["firewrap" "--profile" "date"]))

  (compact-tree (args->tree ["firewrap" "--profile" "claude" "--" "claude"]))

  (compact-tree (args->tree ["firewrap" "-bcn" "--" "echo"]))
  (compact-tree (args->tree ["firewrap" "-b5cn" "--" "echo"]))

  (inspect-sandbox {:args ["firewrap" "-b5cn" "--" "echo"]})
  (inspect-sandbox {:args ["firewrap" "--profile" "claude" "--" "claude"]}))
