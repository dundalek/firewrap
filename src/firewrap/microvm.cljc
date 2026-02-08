(ns firewrap.microvm
  (:require
   [babashka.fs :as fs]
   [babashka.process :as process]
   [clojure.string :as str]
   [firewrap.sandbox :as-alias sb]))

(def ^:dynamic *exec-fn* process/exec)

(defn parse-port-spec
  "Parse Docker-style port specs: 'port', 'hostPort:guestPort', or 'hostAddr:hostPort:guestPort'.
   Returns nested map matching microvm.nix forwardPorts format."
  [spec]
  (let [parts (str/split spec #":")
        [host-address host-port guest-port]
        (case (count parts)
          1 ["127.0.0.1" (parse-long (first parts)) (parse-long (first parts))]
          2 ["127.0.0.1" (parse-long (first parts)) (parse-long (second parts))]
          3 [(first parts) (parse-long (second parts)) (parse-long (nth parts 2))]
          (throw (ex-info (str "Invalid port spec '" spec "': expected 'port', 'hostPort:guestPort', or 'hostAddr:hostPort:guestPort'")
                          {:spec spec})))]
    {:from "host"
     :host {:address host-address :port host-port}
     :guest {:port guest-port}}))

(defn mk-virtiofs-share
  "Generate a VirtioFS share config with socket path.
   idx is the share index, socket-dir is the directory for sockets.
   Returns a complete share config for Nix."
  [socket-dir idx {:keys [source target read-only]}]
  (let [tag (str "share-" idx)]
    {:proto "virtiofs"
     :tag tag
     :source source
     :mount-point target
     :socket (str socket-dir "/virtiofs-" tag ".sock")
     :read-only (boolean read-only)}))

(defn- escape-shell-arg
  "Escape a value for safe use in shell single quotes."
  [s]
  (str "'" (str/replace s "'" "'\\''") "'"))

(defn generate-profile-content
  "Generate .profile content with environment exports and optional chdir.
   Returns a string suitable for writing to ~/.profile."
  [environment-variables chdir]
  (let [exports (map (fn [[k v]] (str "export " k "=" (escape-shell-arg v)))
                     environment-variables)
        chdir-cmd (when chdir (str "cd " (escape-shell-arg chdir)))]
    (str/join "\n" (filter some? (concat exports [chdir-cmd])))))

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

(defn- kebab->camel
  "Convert kebab-case keyword to camelCase string."
  [k]
  (let [s (name k)
        parts (str/split s #"-")]
    (apply str (first parts) (map str/capitalize (rest parts)))))

(defn clj->nix
  "Convert Clojure data to Nix expression string.
   Maps become attrsets, vectors become lists, keywords are camelCased."
  [x]
  (cond
    (nil? x) "null"
    (boolean? x) (if x "true" "false")
    (number? x) (str x)
    (string? x) (str "\""
                     (-> x
                         (str/replace "\\" "\\\\")
                         (str/replace "\"" "\\\"")
                         (str/replace "${" "\\${"))
                     "\"")
    (keyword? x) (clj->nix (name x))
    (map? x) (str "{ "
                  (str/join " " (for [[k v] x]
                                  (str (kebab->camel k) " = " (clj->nix v) ";")))
                  " }")
    (sequential? x) (str "[ " (str/join " " (map clj->nix x)) " ]")
    :else (throw (ex-info "Cannot convert to Nix" {:value x :type (type x)}))))

(defn config->nix-expr
  "Generate Nix expression from microvm configuration map."
  [config flake-path]
  (let [{:keys [user-name user-home user-uid user-gid
                socket-dir virtiofs-shares chdir
                environment-variables network-enabled forward-ports extra-packages]} config
        parsed-ports (mapv parse-port-spec forward-ports)
        shares-with-sockets (vec (map-indexed (fn [idx share] (mk-virtiofs-share socket-dir idx share))
                                              virtiofs-shares))
        profile-content (generate-profile-content environment-variables chdir)
        firewall-ports (mapv #(get-in % [:guest :port]) parsed-ports)
        vm-config (sorted-map
                   :extra-packages (vec extra-packages)
                   :firewall-ports firewall-ports
                   :forward-ports parsed-ports
                   :network-enabled network-enabled
                   :profile-content profile-content
                   :socket-dir socket-dir
                   :user-gid user-gid
                   :user-home user-home
                   :user-name user-name
                   :user-uid user-uid
                   :virtiofs-shares shares-with-sockets)]
    (str "
  let
    flake = builtins.getFlake " (clj->nix flake-path) ";
    pkgs = flake.inputs.nixpkgs.legacyPackages.x86_64-linux;
    vmConfig = flake.lib.mkMicroVM " (clj->nix vm-config) ";
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

    (when (and (seq forward-ports) (not (:network-enabled config)))
      (println "[warning] Port forwarding specified but network is not enabled. Use --net to enable networking."))

    (println "Nix expression:" nix-expr)
    (println)

    (when-not dry-run
      (fs/create-dirs socket-dir)
      (try
        (*exec-fn* "nix" "run" "--impure" "--expr" nix-expr)
        (finally
          (println (str "Cleaning up socket directory: " socket-dir))
          (fs/delete-tree socket-dir))))))
