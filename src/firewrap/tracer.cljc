(ns firewrap.tracer)

(def ^:dynamic *trace* nil)

(defmacro with-trace [& body]
  `(binding [*trace* (atom [])]
     (let [result# (do ~@body)]
       [result# {:children @*trace*}])))

(defmacro span [ctx form]
  (let [{:keys [line column]} (meta form)
        location {:file *file*
                  :line line
                  :column column}]
    `(let [ctx# ~ctx
           nested-trace# (when *trace* (atom []))
           result# (binding [*trace* nested-trace#]
                     (-> ctx# ~form))]
       (when *trace*
         (swap! *trace* conj {:location ~location
                              :form '~form
                              :children @nested-trace#
                              :ctx-prev ctx#
                              :ctx-next result#}))
       result#)))

(defmacro span-> [ctx & forms]
  (reduce (fn [acc form]
            `(span ~acc ~form))
          ctx
          forms))
