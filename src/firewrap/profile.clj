(ns firewrap.profile
  (:refer-clojure :exclude [resolve]))

(defonce ^:private !registry (atom {}))

(defn resolve-builtin-profile [appname]
  (try
    (requiring-resolve (symbol (str "firewrap.profile." appname) "profile"))
    (catch Exception _)))

(defn resolve [name]
  (or (get @!registry name)
      (resolve-builtin-profile name)))

(defn register! [name profile]
  (swap! !registry assoc name profile))
