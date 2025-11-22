#!/usr/bin/env bb
;; Run MicroVM with project directory and optional packages
;; -*- clojure -*-
;; vim: set filetype=clojure:
#_{:clj-kondo/ignore [:namespace-name-mismatch]}
(ns script
  (:require
   [babashka.fs :as fs]
   [babashka.process :refer [shell process]]
   [clojure.string :as str]))

(defn parse-args [args]
  (loop [args args
         result {:extra-packages []
                 :publish-ports []
                 :project-dir nil}]
    (if (empty? args)
      result
      (let [[arg & rest-args] args]
        (case arg
          "--publish"
          (recur (rest rest-args)
                 (update result :publish-ports conj (first rest-args)))

          ("-p" "--packages")
          (loop [remaining rest-args
                 packages []]
            (if (empty? remaining)
              (assoc result
                     :extra-packages (into (:extra-packages result) packages)
                     :project-dir (:project-dir result))
              (let [item (first remaining)]
                (cond
                  (str/starts-with? item "-")
                  (recur remaining
                         (assoc result :extra-packages (into (:extra-packages result) packages)))

                  (str/includes? item "/")
                  (assoc result
                         :extra-packages (into (:extra-packages result) packages)
                         :project-dir item)

                  :else
                  (recur (rest remaining)
                         (conj packages item))))))

          (recur rest-args
                 (assoc result :project-dir arg)))))))

(defn get-user-info []
  {:user-name (or (System/getenv "USER")
                  (str/trim (:out (shell {:out :string} "whoami"))))
   :user-home (System/getenv "HOME")
   :user-uid (str/trim (:out (shell {:out :string} "id" "-u")))
   :user-gid (str/trim (:out (shell {:out :string} "id" "-g")))})

(defn generate-instance-id []
  (let [timestamp (quot (System/currentTimeMillis) 1000)
        random-bytes (byte-array 4)
        _ (.nextBytes (java.security.SecureRandom.) random-bytes)
        random-hex (apply str (map #(format "%02x" (bit-and % 0xff)) random-bytes))]
    (str timestamp "-" random-hex)))

(defn nix-str [s]
  (str "\""
       (-> s
           (str/replace "\\" "\\\\")
           (str/replace "\"" "\\\"")
           (str/replace "${" "\\${"))
       "\""))

(defn to-nix-list [items]
  (str "[ " (str/join " " (map nix-str items)) " ]"))

(defn -main [& args]
  (let [parsed (parse-args args)
        user-info (get-user-info)
        project-dir (-> (or (:project-dir parsed)
                            (System/getProperty "user.dir"))
                        fs/canonicalize
                        str)
        claude-config-dir (str (:user-home user-info) "/.claude")
        script-dir (-> *file*
                       fs/canonicalize
                       fs/parent
                       str)
        instance-id (generate-instance-id)
        socket-dir (str "/tmp/microvm-" instance-id)
        extra-packages (:extra-packages parsed)
        publish-ports (:publish-ports parsed)]

    (fs/create-dirs socket-dir)

    (println "Starting MicroVM with configuration:")
    (println (str "  Project Directory: " project-dir))
    (println (str "  User: " (:user-name user-info)
                  " (UID: " (:user-uid user-info)
                  ", GID: " (:user-gid user-info) ")"))
    (println (str "  Home: " (:user-home user-info)))
    (println (str "  Claude Config: " claude-config-dir))
    (println (str "  Socket Directory: " socket-dir))
    (println (str "  Instance ID: " instance-id))
    (when (seq extra-packages)
      (println (str "  Extra Packages: " (str/join " " extra-packages))))
    (when (seq publish-ports)
      (println (str "  Published Ports: " (str/join " " publish-ports))))
    (println "")

    (let [nix-expr (str "
  let
    flake = builtins.getFlake " (nix-str (str "path:" script-dir)) ";
    pkgs = flake.inputs.nixpkgs.legacyPackages.x86_64-linux;
    extraPackagesList = " (to-nix-list extra-packages) ";
    forwardPortsList = " (to-nix-list publish-ports) ";
    vmConfig = flake.lib.mkMicroVM {
      projectDir = " (nix-str project-dir) ";
      userName = " (nix-str (:user-name user-info)) ";
      userHome = " (nix-str (:user-home user-info)) ";
      userUid = " (:user-uid user-info) ";
      userGid = " (:user-gid user-info) ";
      claudeConfigDir = " (nix-str claude-config-dir) ";
      socketDir = " (nix-str socket-dir) ";
      extraPackages = extraPackagesList;
      forwardPorts = forwardPortsList;
    };
  in
    vmConfig.wrappedRunner
")]
      (try
        (let [result (shell {:continue true} "nix" "run" "--impure" "--expr" nix-expr)]
          (System/exit (:exit result)))
        (finally
          (println (str "Cleaning up socket directory: " socket-dir))
          (fs/delete-tree socket-dir))))))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
