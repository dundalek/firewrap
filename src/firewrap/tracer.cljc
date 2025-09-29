(ns firewrap.tracer)

(def ^:dynamic *trace* nil)

(defmacro with-trace [& body]
  `(binding [*trace* (atom [])]
     (let [result# (do ~@body)]
       [result# {:children @*trace*}])))

(defn thread-with-trace* [init forms]
  (reduce (fn [acc form]
            (let [nested-trace (atom [])
                  result (binding [*trace* nested-trace]
                           (form acc))
                  children @nested-trace]
              (when *trace*
                (swap! *trace* conj (assoc (meta form)
                                           :children children
                                           :ctx-prev acc
                                           :ctx-next result)))
              result))
          init
          forms))

(defmacro span-> [ctx & forms]
  `(thread-with-trace* ~ctx ~(mapv (fn [form]
                                     (let [{:keys [line column]} (meta form)
                                           location {:file *file*
                                                     :line line
                                                     :column column}]
                                       `(with-meta (fn [x#] (-> x# ~form)) {:location ~location
                                                                            :form '~form})))
                                   forms)))
