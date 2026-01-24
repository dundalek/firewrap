(ns firewrap.profile.windsurf
  (:require
   [firewrap.preset.base :as base]
   [firewrap.preset.vscode :as vscode]
   [firewrap.sandbox :as sb]))

(defn profile [parsed]
  (sb/$-> (base/base5)
    (base/configurable parsed {:home "windsurf" :net true})
    (vscode/vscode-nvim)))

;; distributed as tarball
(defn profile-with-options [{:keys [windsurf-dir]} {:keys [args opts] :as parsed}]
  (let [windsurf-bin (str windsurf-dir "/windsurf")
        windsurf-args (concat [windsurf-bin]
                               ;; when restricting to cwd, opening a different folder will load it in existing instance so files will not be visible
                               ;; or specify different --user-data-dir ?
                              (when (:cwd opts) ["--new-window"])
                              (rest args))]
    (sb/$-> (profile parsed)
      (sb/bind-ro windsurf-dir)
      (sb/set-cmd-args windsurf-args))))
