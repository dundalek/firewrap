(ns firewrap.main
  (:require
   [babashka.fs :as fs]
   [babashka.process :as process]
   [clojure.string :as str]
   [firewrap.profiles.chatall :as chatall]
   [firewrap.profiles.ferdium :as ferdium]
   [firewrap.profiles.godmode :as godmode]
   [firewrap.profiles.xdg-open :as xdg-open]
   [firewrap.system :as system]))

;; Workaround to write bwrap command as temporary script because process/exec
;; can't pass content via file descriptors.
(defn run-bwrap-sh [{:keys [bwrap-args executable]}]
  (let [script (->> ["exec bwrap" bwrap-args executable]
                    (flatten)
                    (str/join " "))
        f (fs/file (fs/create-temp-file {:prefix "firewrap"}))]
    (spit f script)
    (println "Firewrap sandbox:" script)
    (process/exec "sh" f)))

(defn run-bwrap [{:keys [bwrap-args executable]}]
  (let [params (flatten (concat ["bwrap"]
                                bwrap-args
                                (when executable [executable])))]
    (println "Firewrap sandbox:" params)
    (apply process/exec params)))

(defn with-strace [ctx cmd]
  (-> ctx
      (system/bind-ro "/usr/bin/strace")
      (system/add-bwrap-args "strace -s 1024 -f" cmd)))

(def shell "/usr/bin/bash")

;; printing stats inside sandbox with:
;; for f in * /usr/*; do echo -en "$f\t"; find $f | wc -l; done
(defn with-shell [ctx]
  (-> ctx
      (system/bind-ro shell)
      (system/bind-ro "/usr/bin/ls")
      (system/bind-ro "/usr/bin/find")
      (system/bind-ro "/usr/bin/wc")
      (system/add-bwrap-args shell)))

(defn resolve-profile [appname]
  (try
    (requiring-resolve (symbol (str "firewrap.profiles." appname) "profile"))
    (catch Exception _)))

(defn path->appname [path]
  (some-> (re-find #"([^/]+)$" path)
          second
          (str/lower-case)))

(defn bind-cwd-rw [ctx]
  (let [cwd (str (fs/absolutize (fs/cwd)))]
    (-> ctx
        (system/bind-rw cwd)
        ;; Make sure cwd is set to current dir when we bind cwd
        (system/add-bwrap-args "--chdir" cwd))))

(defn bind-system-programs [ctx]
  (-> ctx
      ;; Make it tighter instead of dev binding /
      ;; bins and libs
      (system/bind-dev "/")
      (system/bind-ro "/nix"))) ; by default user can write to nix (daemonless setup), maliciuos actor could rewrite some binary there? therefore rebind as ro

(defn bind-user-programs [ctx]
  (system/bind-ro ctx (str (System/getenv "HOME") "/.nix-profile/bin")))

(defn isolated-home-with-user-programs [ctx appname]
  (-> ctx
      (system/isolated-home appname)
      (bind-user-programs))) ; need to rebind nix-profile again over home

;; presets

(defn fw-small-no-tmpfs [_]
  (-> (system/base)
      (bind-system-programs)
      (system/tmpfs (System/getenv "HOME"))
      (bind-user-programs)))

(defn fw-small [_]
  (-> (fw-small-no-tmpfs _)
      (system/tmp)))

(defn fw-net [_]
  (-> (fw-small nil)
      (system/network)))

(defn fw-home [[cmd]]
  (let [appname (path->appname cmd)]
    (-> (fw-small nil)
        (isolated-home-with-user-programs appname))))

(defn fw-homenet [args]
  (-> (fw-home args)
      (system/network)))

(defn fw-homecwdnet [args]
  (-> (fw-home args)
      (bind-cwd-rw)
      (system/network)))

(defn fw-tmphome [_]
  (let [sandbox (str "tmp-" (-> (str (java.time.LocalDateTime/now))
                                (str/replace #"[^\w-]" "-")))]
    (-> (fw-small nil)
        (isolated-home-with-user-programs sandbox))))

(defn fw-tmphomenet [args]
  (-> (fw-tmphome args)
      (system/network)))

(defn fw-tmphomecwdnet [args]
  (-> (fw-tmphome args)
      (bind-cwd-rw)
      (system/network)))

(defn fw-cwd [_]
  (-> (fw-small nil)
      (bind-cwd-rw)))

(defn fw-cwdnet [_]
  (-> (fw-cwd nil)
      (system/network)))

(defn fw-godmodedev [_]
  (->
   ;; WARNING: overly broad
   (fw-small-no-tmpfs nil) ; with tmpfs seems can't connect to X server
   (isolated-home-with-user-programs "godmode")
   (bind-cwd-rw)
   (system/network)))

(def presets
  [["small" fw-small "small profile with temporary home"]
   ["cwd" fw-cwd "\tsmall including current working directory"]
   ["home" fw-home "isolated home based on app name"]
   ["net" fw-net "\tsmall with network"]
   ["cwdnet" fw-cwdnet "small with network and current working directory"]
   ["homenet" fw-homenet "isolated home and network"]
   ["homecwdnet" fw-homecwdnet "isolated home with network and current working directory"]
   ["tmphome" fw-tmphome "newly created isolated home"]
   ["tmphomenet" fw-tmphomenet "newly created isolated home and network"]
   ["tmphomecwdnet" fw-tmphomecwdnet "newly created isolated home with network and current working directory"]

   ["c" fw-cwd "alias for cwd"]
   ["h" fw-home "alias for home"]
   ["n" fw-net "alias for net"]
   ["t" fw-tmphome "alias for tmphome"]
   ; ["ch"]
   ["chn" fw-homecwdnet "cwd + home + net"]
   ["cnt" fw-tmphomecwdnet "cwd + net + tmphome"]
   ["hn" fw-homenet "home + net"]
   ["nt" fw-tmphomenet "net + tmphome"]

   ["godmodedev" fw-godmodedev ""]])

(def preset-map (into {}
                      (map (fn [[name f]]
                             [name f]))
                      presets))

(defn print-help []
  (println "Run program in sanbox")
  (println)
  (println "Usage: firewrap [<preset>] <command> [<args>]")
  (println)
  (println "Available presets:")
  (->> presets
       (run! (fn [[name _ desc]]
               (println (str "  --" name "\t" desc))))))

(defn vscode-nvim [ctx]
  (-> ctx
      ;; Applications because using nvim via app image
      (system/bind-ro (str (System/getenv "HOME") "/Applications"))
      (system/bind-ro (str (System/getenv "HOME") "/.config/nvim"))
      ;; where plugins are stored
      (system/bind-ro (str (System/getenv "HOME") "/.local/share/nvim"))
      ;; local plugins
      (system/bind-ro (str (System/getenv "HOME") "/code/parpar.nvim"))
      (system/bind-ro (str (System/getenv "HOME") "/projects/keysensei/keysensei.nvim"))
      ;; RW
      (system/bind-rw (str (System/getenv "HOME") "/.local/share/nvim/lazy/nvim-treesitter/parser"))
      (system/bind-rw (str (System/getenv "HOME") "/.config/nvim/lazy-lock.json"))
      (system/bind-rw (str (System/getenv "HOME") "/.local/share/nvim/lazy/lazy.nvim/doc/tags"))))

(defn cursor-profile [args appimage]
  (cond-> (fw-homenet ["cursor"])
    (= (first args) "--cwd") (bind-cwd-rw)
    ;; bind nvim so that parinfer works using neovim plugin
    :always (-> (vscode-nvim))
    :always (system/run-appimage appimage)))
    ; :always (system/add-bwrap-args shell)))

(defn windsurf-profile [args]
  (let [windsurf-dir (system/glob-one (str (System/getenv "HOME") "/bin/vendor/")
                                      "Windsurf-linux-x64-*/Windsurf")
        windsurf-bin (str windsurf-dir "/windsurf")
        cwd? (= (first args) "--cwd")
        args (cond-> args
               cwd? rest)]
    (cond-> (fw-homenet ["windsurf"])
      cwd? (bind-cwd-rw)
      ;; bind nvim so that parinfer works using neovim plugin
      :always (-> (vscode-nvim)
                  (system/bind-rw windsurf-dir)
                  (system/add-bwrap-args windsurf-bin
                                         (cond->> args
                                           ;; when restricting to cwd, opening a different folder will load it in existing instance so files will not be visible
                                           ;; or specify different --user-data-dir ?
                                           cwd? (cons "--new-window")))))))

(defn parse-opts [args]
  (if-not (some-> (first args) (str/starts-with? "--"))
    {:args args}
    (let [preset (subs (first args) 2)]
      (if-not (#{"home" "homenet" "homecwdnet" "h" "hn" "chn"} preset)
        {:preset preset
         :args (rest args)}
        (cond
          (<= (count args) 2)
          {:preset preset
           :preset-args [(second args)]
           :args (rest args)}

          (= (second args) "--")
          {:preset preset
           :preset-args [(first (rest (rest args)))]
           :args (rest (rest args))}

          :else
          {:preset preset
           :preset-args [(second args)]
           :args (rest (rest args))})))))

(defn main [& args]
  (let [[cmd & args] args
        appname (path->appname cmd)]
    (case appname
      "chatall" (run-bwrap-sh (chatall/profile
                               (system/glob-one (str (System/getenv "HOME") "/Applications/")
                                                "ChatALL-*.AppImage")))
      "godmode" #_(run-bwrap-sh (godmode/profile
                                 (system/glob-one (str (System/getenv "HOME") "/Applications/")
                                                  "GodMode-*.AppImage")))
      (run-bwrap-sh (godmode/profile "/home/me/bin/vendor/GodMode/release/build/GodMode-1.0.0-beta.9.AppImage"))

; "cheese" (run-bwrap (-> (cheese/profile {:executable "/usr/bin/cheese"})
       ;                         (system/add-bwrap-args cmd)))
       ;                          ; (with-strace cmd)))
      "ferdium" (run-bwrap-sh (ferdium/profile
                               (system/glob-one (str (System/getenv "HOME") "/Applications/")
                                                "Ferdium-*.AppImage")))
      "cursor" (run-bwrap-sh (cursor-profile
                              args
                              (system/glob-one (str (System/getenv "HOME") "/Applications/")
                                               "cursor-*.AppImage")))
      "windsurf" (run-bwrap (windsurf-profile args))
        ; "gedit" (run-bwrap (-> (gedit/profile {:executable "/usr/bin/gedit"})
        ;                         ; (system/add-bwrap-args cmd)
        ;                        (with-strace cmd)))
       ; "notify-send" (run-bwrap (-> (notify-send/profile {:executable cmd})
       ;                               ; (with-shell
       ;                              (system/add-bwrap-args cmd)
       ;                                ; (with-strace cmd)
       ;                              (system/add-bwrap-args args)))
      "xdg-open" (run-bwrap (-> (xdg-open/profile)
                                (system/add-bwrap-args cmd args)))

      ("firewrap" "frap" "fw")
      (let [{:keys [preset args preset-args]} (parse-opts args)]
        (if-some [preset-fn (get preset-map preset)]
          (let [params (-> (preset-fn preset-args)
                           (system/add-bwrap-args args))]
            ; (println "Running preset" cmd params))
            (run-bwrap params))
          (print-help)))

      (if-let [profile (resolve-profile appname)]
        (run-bwrap
         (-> (profile {:executable cmd})
             (system/add-bwrap-args cmd args)))
       ;; maybe just print it to stderr?
        (println "echo Cannot resolve profile" (system/escape-shell appname))))))
