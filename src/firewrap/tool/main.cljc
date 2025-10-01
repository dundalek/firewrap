(ns firewrap.tool.main
  (:require
   [babashka.cli :as cli]
   [firewrap.tool.strace :as strace]))

(defn print-help [_]
  (println "Generate suggested rules from a trace

Usage: firehelper generate

Reads JSON trace from stdin, prints result to stdout.

Example: cat foo.trace | b3-strace-parser | firehelper generate > profile/foo.clj"))

(def cli-table
  [{:cmds ["generate"]
    :fn strace/generate-rules
    :args->opts [:file]}
   {:cmds []
    :fn print-help}])

(defn -main [& args]
  (cli/dispatch cli-table args {}))
