(ns firewrap2.preset.appimage
  (:require
   [firewrap2.bwrap :as bwrap]
   [firewrap2.preset.dumpster :as dumpster]))

(defn run [ctx appimage]
  ;; We want to avoid execuring the appimage binary outside of sandbox.
  ;; The issue is that mounting on which the appimage relies is not allowed inside the sandbox.
  ;; As a workaround we extract it inside the sandbox to tmpfs with --appimage-extract.

  ;; Might need to ro-bind bash/sh as well.
  (bwrap/add-raw-args
   ctx
   "--perms 0555 --ro-bind-data 9 /tmp/app.AppImage"
   "--perms 0555 --ro-bind-data 8 /tmp/run.sh"

   ; (ro-bind "/usr/bin/strace")

   "/tmp/run.sh"
   ;; TODO: should introduce some kind of ops for file descriptor inputs
   (str "9<" (dumpster/escape-shell appimage))
   (str
    "8<<END
#!/usr/bin/env sh
cd /tmp
./app.AppImage --appimage-extract"
     ; "\nstrace -f bash squashfs-root/AppRun"
     ;; run with bash in case script hardcodes #!/bin/bash
    "\nbash squashfs-root/AppRun"
     ; "\nbash"
    "\nEND")))

