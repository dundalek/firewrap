(ns firewrap.preset.appimage
  (:require
   [firewrap.sandbox :as sb]))

(defn appimage-command? [s]
  (some? (re-find #"(?i)\.appimage$" s)))

(defn run [ctx appimage & args]
  ;; We want to avoid execuring the appimage binary outside of sandbox.
  ;; The issue is that mounting on which the appimage relies is not allowed inside the sandbox.
  ;; As a workaround we extract it inside the sandbox to tmpfs with --appimage-extract.
  ;; Note: Might need to ro-bind bash/sh as well.
  (-> ctx
      (sb/bind-data-ro {:perms "0555" :fd 9 :path "/tmp/app.AppImage"
                        :file appimage})
      (sb/bind-data-ro {:perms "0555" :fd 8 :path "/tmp/run.sh"
                        :content (str
                                  "#!/usr/bin/env sh\n"
                                  "cd /tmp\n"
                                  "./app.AppImage --appimage-extract\n"
                                     ; "strace -f bash squashfs-root/AppRun\n"
                                     ;; run with bash in case script hardcodes #!/bin/bash
                                     ; "bash\n"
                                  "bash squashfs-root/AppRun\n")})
      #_(sb/set-cmd-args ["bash"])
      (sb/set-cmd-args (into ["/tmp/run.sh"] args))))
