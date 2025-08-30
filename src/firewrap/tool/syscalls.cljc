(ns firewrap.tool.syscalls
  (:require
   [cheshire.core :as json]
   [clojure.string :as str]))

;; wget https://raw.githubusercontent.com/paolostivanin/syscalls-table-64bit/master/www/syscalls-x86_64.js

(comment
  (def syscalls
    (->> (-> (slurp "tmp/syscalls-x86_64.js")
             (json/parse-string)
             (get "aaData"))
         (filter #(str/starts-with? (second %) "sys_"))
         (map (fn [[id name :as call]]
                {:id id
                 :name (str/replace name #"^sys_" "")
                 :loc (get call 10)}))))

  (->> syscalls
       (map :loc)
       frequencies
       (sort-by key))

  (->> syscalls
       (group-by :loc)
       (sort-by key)
       (map (fn [[k v]]
              [k (->> v (map :name) sort vec)]))))
