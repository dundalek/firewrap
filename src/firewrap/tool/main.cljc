(ns firewrap.tool.main
  (:require
   #?@(:bb [[babashka.deps :as deps]])
   [babashka.cli :as cli]
   [firewrap.tool.inspect :as-alias inspect]
   [firewrap.tool.strace :as strace]))

(defn print-help [_]
  (println "Firehelper - Firewrap helper tools

Commands:
  generate - Generate suggested rules from a trace
    Reads JSON trace from stdin, prints result to stdout.

    Example: cat foo.trace | b3-strace-parser | firehelper generate > profile/foo.clj

  inspect - Inspect profile rules tree in Portal
    Accepts same options as firewrap

    Examples:
      firehelper inspect --profile claude
      firehelper inspect -bcn"))

(def cli-table
  [{:cmds ["generate"]
    :fn strace/generate-rules
    :args->opts [:file]}
   {:cmds []
    :fn print-help}])

(defn -main [& args]
  (if (and (= (first args) "inspect") (> (count args) 1))
    (do
      #?(:bb (deps/add-deps '{:deps {djblue/portal {:mvn/version "0.61.0"}}}))
      ((requiring-resolve `inspect/inspect-sandbox) {:args (cons "firewrap" (rest args))}))
    (cli/dispatch cli-table args {})))
