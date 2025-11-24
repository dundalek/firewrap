(ns firewrap.microvm
  (:require
   [babashka.fs :as fs]
   [babashka.process :as process]
   [clojure.string :as str]
   [firewrap.sandbox :as-alias sb]))

(def ^:dynamic *exec-fn* process/exec)

(defn- get-user-info []
  {:user-name (System/getenv "USER")
   :user-home (System/getenv "HOME")
   :user-uid (Integer/parseInt (str/trim (:out (process/shell {:out :string} "id" "-u"))))
   :user-gid (Integer/parseInt (str/trim (:out (process/shell {:out :string} "id" "-g"))))})

(defn ctx->microvm-config [ctx]
  (let [used-keys [::sb/args ::sb/shared-namespaces]
        {::sb/keys [args shared-namespaces]} (select-keys ctx used-keys)
        remaining-ctx (apply dissoc ctx used-keys)
        _ (println "args" args)
        network-enabled (contains? shared-namespaces "net")
        config
        (reduce
         (fn [m arg]
           ;; Relies on args being vector of vectors
           ;; Might need to add code to normalize args
           (if (and (seq arg) (string? (first arg)))
             (case (first arg)
               "--ro-bind" (let [[_ src dest] arg]
                             (update m :virtiofs-shares conj {:source src :target dest :read-only true}))
               "--bind" (let [[_ src dest] arg]
                          (update m :virtiofs-shares conj {:source src :target dest :read-only false}))
               "--chdir" (let [[_ path] arg]
                           (assoc m :chdir path))
               ("--die-with-parent" "--new-session" "--dev" "--proc")
               (do
                 (println "Does not apply to microvm, ignoring: " arg)
                 m)
               (do
                 (println "Unhandled arg:" arg)
                 m))
             (do
               (println "Error processing arg:" arg)
               m)))
         {:virtiofs-shares []
          :network-enabled network-enabled}
         args)]
    (println "Unprocessed context keys" (keys remaining-ctx))
    {:config config
     :warnings []}))

(defn- to-nix-str [s]
  (str "\""
       (some-> s
               (str/replace "\\" "\\\\")
               (str/replace "\"" "\\\"")
               (str/replace "${" "\\${"))
       "\""))

(defn- to-nix-list [items]
  (str "[ " (str/join " " items) " ]"))

(defn- to-nix-bool [b]
  (if b "true" "false"))

(defn- to-nix-list-of-strs [items]
  (to-nix-list (map to-nix-str items)))

(defn- env->nix-attrs [env-map]
  (str "{ "
       (str/join " "
                 (for [[k v] env-map]
                   (str (to-nix-str k) " = " (to-nix-str v) ";")))
       " }"))

(defn- share->nix-attrs [{:keys [source target read-only]}]
  (str "{ source = " (to-nix-str source) "; "
       "target = " (to-nix-str target) "; "
       "readOnly = " (to-nix-bool read-only) "; }"))

(defn config->nix-expr
  "Generate Nix expression from microvm configuration map."
  [config flake-path]
  (let [{:keys [user-name user-home user-uid user-gid
                socket-dir virtiofs-shares chdir
                environment-variables network-enabled forward-ports extra-packages]} config]
    (str "
  let
    flake = builtins.getFlake " (to-nix-str flake-path) ";
    pkgs = flake.inputs.nixpkgs.legacyPackages.x86_64-linux;
    vmConfig = flake.lib.mkMicroVM {
      chdir = " (if chdir (to-nix-str chdir) "null") ";
      environmentVariables = " (env->nix-attrs environment-variables) ";
      extraPackages = " (to-nix-list-of-strs extra-packages) ";
      forwardPorts = " (to-nix-list-of-strs forward-ports) ";
      networkEnabled = " (to-nix-bool network-enabled) ";
      socketDir = " (to-nix-str socket-dir) ";
      userGid = " user-gid ";
      userHome = " (to-nix-str user-home) ";
      userName = " (to-nix-str user-name) ";
      userUid = " user-uid ";
      virtiofsShares = " (to-nix-list (map share->nix-attrs virtiofs-shares)) ";
    };
  in
    vmConfig.wrappedRunner
")))

(defn run-microvm [config {:keys [dry-run flake-path socket-dir packages forward-ports]}]
  (let [config (merge (get-user-info)
                      {:socket-dir socket-dir
                       :extra-packages packages
                       :forward-ports forward-ports}
                      config)
        nix-expr (config->nix-expr config flake-path)]

    (println "Nix expression:" nix-expr)
    (println)

    (when-not dry-run
      (fs/create-dirs socket-dir)
      (try
        (*exec-fn* "nix" "run" "--impure" "--expr" nix-expr)
        (finally
          (println (str "Cleaning up socket directory: " socket-dir))
          (fs/delete-tree socket-dir))))))
