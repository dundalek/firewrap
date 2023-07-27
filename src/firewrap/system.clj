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

(defn ro-bind
  ([path]
   (ro-bind path path))
  ([source destination]
   (str "--ro-bind " (escape-shell source) " " (escape-shell destination))))

(defn ro-bind-try [path]
  (str "--ro-bind-try " (escape-shell path) " " (escape-shell path)))

(defn rw-bind [path]
  (str "--bind " (escape-shell path) " " (escape-shell path)))

(defn dev-bind [path]
  (str "--dev-bind " (escape-shell path) " " (escape-shell path)))

(defn base  []
  ["--die-with-parent"
   "--unshare-all"
   ;; Create a new session to prevent using the TIOCSTI ioctl to push
   ;; characters into the terminal's input buffer, allowing an attacker to
   ;; escape the sandbox.
   ;; See https://github.com/containers/bubblewrap/issues/555
   "--new-session"])

(defn system-network []
  ["--share-net"
   (ro-bind "/etc/resolv.conf")
   (ro-bind-try "/run/systemd/resolve")])

(defn fonts []
  [(ro-bind "/etc/fonts")
   (ro-bind "/usr/share/fonts")])

(defn dconf []
  [(ro-bind "/etc/dconf")
   (ro-bind "/run/user/1000/dconf")
   (ro-bind "/usr/local/share/dconf")
   (ro-bind "/user/share/dconf")])

(defn gpu []
  ["--dev-bind-try /dev/dri /dev/dri"
   "--ro-bind-try /sys/dev /sys/dev"
   "--ro-bind-try /sys/devices /sys/devices"])

(defn isolated-home [appname]
  (let [HOME (System/getenv "HOME")
        source-path (str HOME "/sandboxes/" appname)]
    ;; Side-effect! Ideally make it pure and interpret side effects separately
    (fs/create-dirs source-path)
    ["--bind" (escape-shell source-path) (escape-shell HOME)]))

(defn libs []
  ["--ro-bind-try /usr/lib /usr/lib"
   "--ro-bind-try /lib /lib"
   "--ro-bind-try /lib64 /lib64"])

(defn processes []
  ;; Should restrict /proc but how to whitelist individual pids?
  ["--proc /proc"])
  ; (ro-bind "/proc/self")

(defn tmp []
  ["--tmpfs /tmp"])

(defn run-appimage [appimage]
  ["--perms 0555 --ro-bind-data 9 /app.AppImage"
   "--perms 0555 --ro-bind-data 8 /run.sh"

   ; (ro-bind "/usr/bin/strace")

   "/run.sh"
   "9<" (escape-shell appimage)
   "8<<END
#!/usr/bin/env sh
cd /tmp
/app.AppImage --appimage-extract"
   ; "\nstrace -f bash squashfs-root/AppRun"
   ;; run with bash in case script hardcodes #!/bin/bash
   "\nbash squashfs-root/AppRun"
   ; "\nbash"
   "\nEND"])

(defn dbus-bus-path []
  (-> (System/getenv "DBUS_SESSION_BUS_ADDRESS")
      (str/replace #"^unix:path=" "")))

(defn dbus-unrestricted []
  ;; todo filter with xdg-dbus-proxy
  [(ro-bind "/etc/machine-id")
   (ro-bind "/var/lib/dbus/machine-id")
   ;; Bind also /run/dbus/system_bus_socket ?
   (ro-bind (dbus-bus-path))])

(defn xdg-open []
  ;; Needs xdg-flatpak-utils
  ;; sudo apt install flatpak-xdg-utils
  ;; Opens via portal, ideally should ask user
  [(dbus-unrestricted)
   ;; org.freedesktop.portal.OpenURI
  ; https://github.com/flatpak/flatpak-xdg-utils/blob/main/src/xdg-open.c
   (ro-bind "/usr/libexec/flatpak-xdg-utils/xdg-open" "/usr/bin/xdg-open")])
