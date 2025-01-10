(ns firewrap.main
  (:require
   [babashka.fs :as fs]
   [babashka.process :as process]
   [clojure.string :as str]
   [firewrap.presets :as presets]
   [firewrap.profiles.chatall :as chatall]
   [firewrap.profiles.ferdium :as ferdium]
   [firewrap.profiles.godmode :as godmode]
   [firewrap.profiles.xdg-open :as xdg-open]
   [firewrap.system :as system]))

;; Workaround to write bwrap command as temporary script because process/exec
;; can't pass content via file descriptors.
(defn run-bwrap-sh-wrapper [{:keys [bwrap-args executable]}]
  (let [script (->> ["exec bwrap" bwrap-args executable]
                    (flatten)
                    (str/join " "))
        f (fs/file (fs/create-temp-file {:prefix "firewrap"}))]
    (spit f script)
    (println "Firewrap sandbox:" script)
    (process/exec "sh" f)))

(defn run-bwrap-exec [{:keys [bwrap-args executable]}]
  (let [params (flatten (concat ["bwrap"]
                                bwrap-args
                                (when executable [executable])))]
    (println "Firewrap sandbox:" params)
    (apply process/exec params)))

(defn needs-bwrap-sh-wrapper? [opts]
  (->> opts :bwrap-args
       flatten
       (some (fn [s] (when (string? s)
                       (str/includes? s "--ro-bind-data"))))))

(defn run-bwrap [opts]
  (if (needs-bwrap-sh-wrapper? opts)
    (run-bwrap-sh-wrapper opts)
    (run-bwrap-exec opts)))

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

(defn print-help []
  (println "Run program in sanbox")
  (println)
  (println "Usage: firewrap [<preset>] <command> [<args>]")
  (println)
  (println "Available presets:")
  (->> presets/presets
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
  (cond-> (presets/fw-homenet ["cursor"])
    (= (first args) "--cwd") (presets/bind-cwd-rw)
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
    (cond-> (presets/fw-homenet ["windsurf"])
      cwd? (presets/bind-cwd-rw)
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
  (let [[cmd & root-args] args
        appname (presets/path->appname cmd)]
    (case appname
      "chatall" (run-bwrap (chatall/profile
                            (system/glob-one (str (System/getenv "HOME") "/Applications/")
                                             "ChatALL-*.AppImage")))
      "godmode" (run-bwrap (godmode/profile "/home/me/bin/vendor/GodMode/release/build/GodMode-1.0.0-beta.9.AppImage"))

; "cheese" (run-bwrap (-> (cheese/profile {:executable "/usr/bin/cheese"})
       ;                         (system/add-bwrap-args cmd)))
       ;                          ; (with-strace cmd)))
      "ferdium" (run-bwrap (ferdium/profile
                            (system/glob-one (str (System/getenv "HOME") "/Applications/")
                                             "Ferdium-*.AppImage")))
      "cursor" (run-bwrap (cursor-profile
                           root-args
                           (system/glob-one (str (System/getenv "HOME") "/Applications/")
                                            "cursor-*.AppImage")))
      "windsurf" (run-bwrap (windsurf-profile root-args))
        ; "gedit" (run-bwrap (-> (gedit/profile {:executable "/usr/bin/gedit"})
        ;                         ; (system/add-bwrap-args cmd)
        ;                        (with-strace cmd)))
       ; "notify-send" (run-bwrap (-> (notify-send/profile {:executable cmd})
       ;                               ; (with-shell
       ;                              (system/add-bwrap-args cmd)
       ;                                ; (with-strace cmd)
       ;                              (system/add-bwrap-args args)))
      "xdg-open" (run-bwrap (-> (xdg-open/profile)
                                (system/add-bwrap-args cmd root-args)))

      ("firewrap" "frap" "fw")
      (let [{:keys [preset args preset-args]} (parse-opts root-args)]
        (if-some [preset-fn (get presets/preset-map preset)]
          (let [params (-> (preset-fn preset-args)
                           (system/add-bwrap-args args))]
            ; (println "Running preset" cmd params))
            (run-bwrap params))
          (print-help)
          #_(let [[cmd & args] root-args
                  appname (presets/path->appname cmd)]
              (if-let [profile (resolve-profile appname)]
                (run-bwrap
                 (-> (profile {:executable cmd})
                     (system/add-bwrap-args cmd args)))
                 ;; maybe just print it to stderr?
                (println "echo Cannot resolve profile" (system/escape-shell appname))))))

      (if-let [profile (resolve-profile appname)]
        (run-bwrap
         (-> (profile {:executable cmd})
             (system/add-bwrap-args cmd args)))
       ;; maybe just print it to stderr?
        (println "echo Cannot resolve profile" (system/escape-shell appname))))))
