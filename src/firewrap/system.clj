(ns firewrap.system
  (:require
   [babashka.fs :as fs]
   [clojure.string :as str]))

(defn escape-shell [s]
  ;; TODO
  s)

(defn glob-one [root pattern]
  (-> (fs/glob root pattern)
      (first)
      (str)))

(defn add-bwrap-args [ctx & args]
  (update ctx :bwrap-args (fnil into []) args))

(defn ro-bind
  ([ctx path]
   (ro-bind ctx path path))
  ([ctx source destination]
   (add-bwrap-args ctx "--ro-bind" (escape-shell source) (escape-shell destination))))

(defn ro-bind-try [ctx path]
  (add-bwrap-args ctx  "--ro-bind-try" (escape-shell path) (escape-shell path)))

(defn rw-bind
  ([ctx path]
   (rw-bind ctx path path))
  ([ctx source destination]
   (add-bwrap-args ctx "--bind" (escape-shell source) (escape-shell destination))))

(defn dev-bind
  ([ctx path]
   (dev-bind ctx path path))
  ([ctx source destination]
   (add-bwrap-args ctx "--dev-bind" (escape-shell source) (escape-shell destination))))

(defn bind-dev-try
  ([ctx path]
   (bind-dev-try ctx path path))
  ([ctx source destination]
   (add-bwrap-args ctx "--dev-bind-try" (escape-shell source) (escape-shell destination))))

(defn base  []
  (add-bwrap-args
   {}
   "--die-with-parent"
   "--unshare-all"
   ;; Create a new session to prevent using the TIOCSTI ioctl to push
   ;; characters into the terminal's input buffer, allowing an attacker to
   ;; escape the sandbox.
   ;; See https://github.com/containers/bubblewrap/issues/555
   "--new-session"))

(defn network [ctx]
  (-> ctx
      (add-bwrap-args "--share-net")
      (ro-bind "/etc/resolv.conf")
      (ro-bind-try "/run/systemd/resolve")))

(defn fonts [ctx]
  (-> ctx
      (ro-bind "/etc/fonts")
      (ro-bind "/usr/share/fonts")
      (ro-bind-try (str (System/getenv "HOME") "/.fonts"))
      (ro-bind-try (str (System/getenv "HOME") "/.local/share/fonts"))))

(defn dconf [ctx]
  (-> ctx
      (ro-bind "/etc/dconf")
      (ro-bind "/run/user/1000/dconf")
      (ro-bind "/usr/local/share/dconf")
      (ro-bind "/user/share/dconf")))

(defn gpu [ctx]
  (-> ctx
      (bind-dev-try "/dev/dri")
      (ro-bind-try "/sys/dev")
      (ro-bind-try "/sys/devices")))

(defn isolated-home [ctx appname]
  (let [HOME (System/getenv "HOME")
        source-path (str HOME "/sandboxes/" appname)]
    ;; Side-effect! Ideally make it pure and interpret side effects separately
    (fs/create-dirs source-path)
    (-> ctx
        (rw-bind (escape-shell source-path) (escape-shell HOME)))))

(defn libs [ctx]
  (-> ctx
      (ro-bind-try "/etc/ld.so.cache")
      (ro-bind-try "/usr/lib")
      (ro-bind-try "/lib")
      (ro-bind-try "/lib64")))

(defn processes [ctx]
  ;; Should restrict /proc but how to whitelist individual pids?
  (add-bwrap-args ctx "--proc /proc"))
  ; (ro-bind "/proc/self")

(defn tmp [ctx]
  (add-bwrap-args ctx "--tmpfs /tmp"))

(defn run-appimage [ctx appimage]
  ;; We want to avoid execuring the appimage binary outside of sandbox.
  ;; The issue is that mounting on which the appimage relies is not allowed inside the sandbox.
  ;; As a workaround we extract it inside the sandbox to tmpfs with --appimage-extract.

  ;; Might need to ro-bind bash/sh as well.
  (add-bwrap-args
   ctx
   "--perms 0555 --ro-bind-data 9 /tmp/app.AppImage"
   "--perms 0555 --ro-bind-data 8 /tmp/run.sh"

   ; (ro-bind "/usr/bin/strace")

   "/tmp/run.sh"
   "9<" (escape-shell appimage)
   "8<<END
#!/usr/bin/env sh
cd /tmp
./app.AppImage --appimage-extract"
   ; "\nstrace -f bash squashfs-root/AppRun"
   ;; run with bash in case script hardcodes #!/bin/bash
   "\nbash squashfs-root/AppRun"
   ; "\nbash"
   "\nEND"))

(defn dbus-bus-path []
  (-> (System/getenv "DBUS_SESSION_BUS_ADDRESS")
      (str/replace #"^unix:path=" "")))

(defn dbus-unrestricted [ctx]
  (-> ctx
      (ro-bind "/etc/machine-id")
      (ro-bind "/var/lib/dbus/machine-id")
   ;; Bind also /run/dbus/system_bus_socket ?
      (ro-bind (dbus-bus-path))))

#_(defn dbus-see [])

(defn dbus-talk [ctx name]
  ;; todo filter with xdg-dbus-proxy
  ; (system/ro-bind "/tmp/my-dbus-proxy" "/run/user/1000/bus")])
  ;; hook it with --filter and --talk
  (-> ctx
      (dbus-unrestricted)))

#_(defn dbus-own [])

(defn xdg-open [ctx]
  ;; Needs xdg-flatpak-utils
  ;; sudo apt install flatpak-xdg-utils
  ;; Opens via portal, ideally should ask user
  (-> ctx
      (dbus-unrestricted)
      ;; org.freedesktop.portal.OpenURI
      ; https://github.com/flatpak/flatpak-xdg-utils/blob/main/src/xdg-open.c
      (ro-bind "/usr/libexec/flatpak-xdg-utils/xdg-open" "/usr/bin/xdg-open")))
