(ns firewrap.tool.portlet.viewers
  (:require
   [reagent.core :as r]
   [portal.ui.api :as pui]
   [portal.ui.inspector :as ins]
   [portal.viewer :as pv]))

(defn profile-tree? [value]
  "Predicate to detect profile tree data structure (just the tree, not the full tuple)"
  (and (map? value)
       (contains? value :children)
       (vector? (:children value))))

(defn count-children [node]
  "Count total child nodes recursively"
  (let [direct-count (count (:children node []))
        nested-count (reduce + (map count-children (:children node [])))]
    (+ direct-count nested-count)))

(defn tree-node-view [{:keys [node selected-node on-select expanded-nodes on-toggle level]}]
  (let [has-children? (seq (:children node))
        node-id (str (:symbol node) "-" (hash node))
        is-expanded? (contains? @expanded-nodes node-id)
        is-selected? (= @selected-node node)
        children-count (count-children node)
        indent-style {:padding-left (str (* level 20) "px")}]
    [:div
     [:div.tree-node
      {:style (merge indent-style
                     {:cursor "pointer"
                      :padding "4px 8px"
                      :background-color (if is-selected? "#e3f2fd" "transparent")
                      :border-radius "4px"
                      :margin "1px 0"
                      :border-left (if (> level 0) "1px solid #ccc" "none")})
       :on-click #(do
                    (when has-children?
                      (on-toggle node-id))
                    (on-select node))}
      [:div {:style {:display "flex" :align-items "center" :gap "8px"}}
       (when has-children?
         [:span {:style {:width "12px" :text-align "center"}}
          (if is-expanded? "▼" "▶")])
       [:span {:style {:font-weight "bold"}}
        (str (:symbol node))]
       (when (> children-count 0)
         [:span {:style {:color "#666" :font-size "0.9em"}}
          (str "(" children-count " children)")])]
      (when (:location node)
        [:div {:style {:font-size "0.8em" :color "#888" :margin-top "2px"}}
         (str (:file (:location node)) ":" (:line (:location node)))])]
     (when (and has-children? is-expanded?)
       [:div {:style {:margin-left "20px"}}
        (for [child (:children node)]
          ^{:key (str (:symbol child) "-" (hash child))}
          [tree-node-view {:node child
                           :selected-node selected-node
                           :on-select on-select
                           :expanded-nodes expanded-nodes
                           :on-toggle on-toggle
                           :level (inc level)}])])]))

(defn details-panel [{:keys [selected-node]}]
  [:div {:style {:padding "16px" :border-left "1px solid #ddd"}}
   [:h3 {:style {:margin-top "0"}} "Node Details"]

   (if selected-node
     [:div
      [:div {:style {:margin-bottom "16px"}}
       [:h4 "Symbol"]
       [:div {:style {:font-family "monospace" :background-color "#f5f5f5" :padding "8px" :border-radius "4px"}}
        (str (:symbol selected-node))]]

      (when (:location selected-node)
        [:div {:style {:margin-bottom "16px"}}
         [:h4 "Location"]
         [:div {:style {:font-family "monospace" :font-size "0.9em"}}
          [:div (str "File: " (:file (:location selected-node)))]
          [:div (str "Line: " (:line (:location selected-node)))]
          [:div (str "Column: " (:column (:location selected-node)))]]])

      [:div {:style {:margin-bottom "16px"}}
       [:h4 "Children Count"]
       [:div (str (count (:children selected-node [])) " direct children")]]

      (when (seq (:children selected-node))
        [:div {:style {:margin-bottom "16px"}}
         [:h4 "Direct Children"]
         [:div {:style {:font-family "monospace" :font-size "0.9em"}}
          (for [[idx child] (map-indexed vector (:children selected-node))]
            ^{:key idx}
            [:div {:style {:padding "2px 0" :border-bottom "1px solid #eee"}}
             (str (:symbol child))])]])]

     [:div {:style {:color "#888" :font-style "italic"}}
      "Select a node to view its details"])])

(defn profile-tree-viewer [tree-data]
  (let [selected-node (r/atom nil)
        expanded-nodes (r/atom #{})]
    (fn [tree-data]
      [:div {:style {:display "flex" :height "600px" :border "1px solid #ddd" :border-radius "4px"}}
       [:div {:style {:flex "1" :overflow "auto" :padding "16px"}}
        [:h3 {:style {:margin-top "0"}} "Profile Tree"]
        [:div {:style {:font-size "0.9em" :color "#666" :margin-bottom "12px"}}
         "Click on nodes to explore the preset call hierarchy"]
        (if (:children tree-data)
          [tree-node-view {:node tree-data
                           :selected-node selected-node
                           :on-select #(reset! selected-node %)
                           :expanded-nodes expanded-nodes
                           :on-toggle #(swap! expanded-nodes (fn [nodes]
                                                               (if (contains? nodes %)
                                                                 (disj nodes %)
                                                                 (conj nodes %))))
                           :level 0}]
          [:div {:style {:color "#888" :font-style "italic"}}
           "No tree structure available"])]
       [:div {:style {:flex "1" :overflow "auto"}}
        [details-panel {:selected-node @selected-node}]]])))

(defn register-viewers! []
  "Register the profile tree viewer with Portal"
  (pui/register-viewer!
   {:name ::profile-tree
    :predicate profile-tree?
    :component profile-tree-viewer
    :doc "Visualizes instrumented profile tree data with expandable tree view and node details"}))

(register-viewers!)
