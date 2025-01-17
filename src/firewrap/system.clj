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

(defn bind-ro
  ([ctx path]
   (bind-ro ctx path path))
  ([ctx source destination]
   (add-bwrap-args ctx ["--ro-bind" (escape-shell source) (escape-shell destination)])))

(defn bind-ro-try [ctx path]
  (add-bwrap-args ctx  ["--ro-bind-try" (escape-shell path) (escape-shell path)]))

;; might need helpers like this since ctx would be in wrong position using `->`
;; alternative that comes to mind is `(-> ctx ((partial reduce bind-ro-try) paths))`
;; which looks somewhat complicated
(defn bind-ro-try-many [ctx paths]
  (reduce bind-ro-try ctx paths))

(defn bind-rw
  ([ctx path]
   (bind-rw ctx path path))
  ([ctx source destination]
   (add-bwrap-args ctx ["--bind" (escape-shell source) (escape-shell destination)])))

(defn bind-rw-try
  ([ctx path]
   (bind-rw-try ctx path path))
  ([ctx source destination]
   (add-bwrap-args ctx ["--bind-try" (escape-shell source) (escape-shell destination)])))

(defn bind-dev
  ([ctx path]
   (bind-dev ctx path path))
  ([ctx source destination]
   (add-bwrap-args ctx ["--dev-bind" (escape-shell source) (escape-shell destination)])))

(defn bind-dev-try
  ([ctx path]
   (bind-dev-try ctx path path))
  ([ctx source destination]
   (add-bwrap-args ctx ["--dev-bind-try" (escape-shell source) (escape-shell destination)])))

(defn getenv [{:keys [getenv] :as _ctx} k]
  (assert (ifn? getenv))
  (getenv k))

(defn- xdg-dirs [dirs-env subfolders]
  (let [dirs (some-> dirs-env
                     (str/split #":"))]
    (for [subfolder subfolders
          dir dirs]
      (str (str/replace dir #"\/+$" "")
           "/" subfolder))))

(defn xdg-data-dirs-path []
  (or (System/getenv "XDG_DATA_DIRS")
      "/usr/local/share/:/usr/share/"))

(defn xdg-data-dir-paths [& subfolders]
  ;; e.g. $HOME/.local/share /usr/local/share /usr/share
  (xdg-dirs (xdg-data-dirs-path) subfolders))

(comment
  (xdg-data-dir-paths "icons" "themes"))

(defn xdg-config-dirs-path []
  (or (System/getenv "XDG_CONFIG_DIRS")
      "/etc/xdg"))

(defn xdg-config-dir-paths [& subfolders]
  (xdg-dirs (xdg-config-dirs-path) subfolders))

(comment
  (xdg-config-dir-paths "gtk-3.0" "glib-2.0"))

(defn xdg-data-home-path []
  (or (System/getenv "XDG_DATA_HOME")
      (str (System/getenv "HOME") "/.local/share")))

(defn xdg-data-home-paths [& subfolders]
  (xdg-dirs (xdg-data-home-path) subfolders))

(defn xdg-config-home-path []
  (or (System/getenv "XDG_CONFIG_HOME")
      (str (System/getenv "HOME") "/.config")))

(defn xdg-config-home-paths [& subfolders]
  (xdg-dirs (xdg-config-home-path) subfolders))

(defn xdg-cache-home-path []
  (or (System/getenv "XDG_CACHE_HOME")
      (str (System/getenv "HOME") "/.cache")))

(defn xdg-cache-home-paths [& subfolders]
  (xdg-dirs (xdg-cache-home-path) subfolders))

(defn xdg-state-home-path []
  (or (System/getenv "XDG_STATE_HOME")
      (str (System/getenv "HOME") "/.local/state")))

(defn xdg-state-home-paths [& subfolders]
  (xdg-dirs (xdg-state-home-path) subfolders))

(defn xdg-data-dir [ctx & subfolders]
  (bind-ro-try-many ctx (apply xdg-data-dir-paths subfolders)))

(defn xdg-data-home [ctx & subfolders]
  (bind-ro-try-many ctx (apply xdg-data-home-paths subfolders)))

(defn xdg-cache-home [ctx & subfolders]
  (bind-ro-try-many ctx (apply xdg-cache-home-paths subfolders)))

(defn xdg-config-home [ctx & subfolders]
  (bind-ro-try-many ctx (apply xdg-config-home-paths subfolders)))

(defn xdg-state-home [ctx & subfolders]
  (bind-ro-try-many ctx (apply xdg-state-home-paths subfolders)))

(defn base  []
  (add-bwrap-args
   {:getenvs (fn [] (System/getenv))
    :getenv (fn [k] (System/getenv k))}
   "--die-with-parent"
   "--unshare-all"
   ;; Create a new session to prevent using the TIOCSTI ioctl to push
   ;; characters into the terminal's input buffer, allowing an attacker to
   ;; escape the sandbox.
   ;; See https://github.com/containers/bubblewrap/issues/555
   "--new-session"))

(defn xdg-runtime-dir-path [ctx]
  ;; fallback to "/run/user/$(id -u)" if missing?
  (getenv ctx "XDG_RUNTIME_DIR"))

(defn xdg-runtime-dir [ctx subdir]
  (let [path (str (xdg-runtime-dir-path ctx) "/" subdir)]
    (-> ctx
        (bind-ro-try path))))

(defn network [ctx]
  (-> ctx
      (add-bwrap-args "--share-net")
      (bind-ro "/etc/resolv.conf")
      (bind-ro-try "/run/systemd/resolve")))

(defn fontconfig [ctx]
  (-> ctx
      (bind-ro-try (str (System/getenv "HOME") "/.config/fontconfig"))
      (bind-ro-try "/usr/share/fontconfig")))

(defn fontconfig-shared-cache [ctx]
  ;; bind so that apps can share fontconfig cache for better performance
  ;; but for better security one can avoid it
  (-> ctx
      ;; TODO read xdg cache dir
      (bind-rw-try (str (System/getenv "HOME") "/.cache/fontconfig"))
      (bind-ro-try "/var/cache/fontconfig")))

(defn fonts [ctx]
  (-> ctx
      (bind-ro-try "/etc/fonts")
      (bind-ro-try "/usr/share/fonts")
      (bind-ro-try "/usr/local/share/fonts")
      (bind-ro-try (str (System/getenv "HOME") "/.fonts"))
      (bind-ro-try (str (System/getenv "HOME") "/.local/share/fonts"))))

(defn icons [ctx]
  (-> ctx
      (bind-ro-try "/usr/share/icons")
      (bind-ro-try (str (System/getenv "HOME") "/.icons"))
      (bind-ro-try (str (System/getenv "HOME") "/.local/share/icons"))))

(defn locale [ctx]
  (-> ctx
      (bind-ro-try "/usr/share/locale")
      (bind-ro-try "/usr/share/locale-langpack")))

(defn themes [ctx]
  ;; include /usr/share/pixmaps?
  (-> ctx
      (bind-ro-try (str (System/getenv "HOME") "/.themes"))
      (bind-ro-try (str (System/getenv "HOME") "/.local/share/themes"))
      (bind-ro-try "/usr/share/themes")))

(defn dconf [ctx]
  ;; are there some security implications of apps sharing access to dconf?
  (-> ctx
      (bind-ro-try "/etc/dconf/profile/user")
      (bind-ro-try (str (System/getenv "HOME") "/.config/dconf/user"))
      ;; should it be more granular to enable user and profile explicitly?
      ;; is there a difference between user and profile?
      ; (bind-ro-try (str (xdg-runtime-dir-path ctx) "/dconf/user"))
      ; (bind-ro-try (str (xdg-runtime-dir-path ctx) "/dconf/profile"))
      (bind-rw-try (str (xdg-runtime-dir-path ctx) "/dconf"))
      (bind-ro-try-many (xdg-data-dir-paths "dconf"))))
      ; (bind-ro "/user/share/dconf/user")))

(defn at-spi [ctx]
  ;; should have higher-level wrapper `accessibility`?
  ;; Assistive Technology Service Provider Interface
  (-> ctx
      (xdg-runtime-dir "at-spi/bus_1")))

(defn gpu [ctx]
  (-> ctx
      (bind-dev-try "/dev/dri")
      (bind-ro-try "/sys/dev")
      (bind-ro-try "/sys/devices")))

(defn isolated-home [ctx appname]
  (let [HOME (System/getenv "HOME")
        source-path (str HOME "/sandboxes/" appname)]
    ;; Side-effect! Ideally make it pure and interpret side effects separately
    (fs/create-dirs source-path)
    (-> ctx
        (bind-rw (escape-shell source-path) (escape-shell HOME)))))

(defn libs [ctx]
  (-> ctx
      ;; is it safe to include ld.so.cache?
      (bind-ro-try "/etc/ld.so.cache")
      (bind-ro-try "/usr/lib")
      (bind-ro-try "/lib")
      (bind-ro-try "/lib64")))

(defn processes [ctx]
  ;; Should restrict /proc but how to whitelist individual pids?
  (add-bwrap-args ctx "--proc /proc"))
  ; (ro-bind "/proc/self")

(defn tmpfs [ctx path]
  (add-bwrap-args ctx ["--tmpfs" path]))

(defn tmp [ctx]
  (tmpfs ctx "/tmp"))
  ; (add-bwrap-args ctx "--tmpfs /tmp")))

(defn dev-urandom [ctx]
  (-> ctx
      (bind-ro-try "/dev/urandom")))

;; maybe have dev-null-ro and dev-null-rw?
(defn dev-null [ctx]
  (-> ctx
      (bind-dev-try "/dev/null")))

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
   ;; TODO: should introduce some kind of ops for file descriptor inputs
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
  ;; if missing could fallback to $XDG_RUNTIME_DIR/bus
  (-> (System/getenv "DBUS_SESSION_BUS_ADDRESS")
      (str/replace #"^unix:path=" "")))

(defn dbus-system-bus [ctx]
  (-> ctx
      (bind-ro-try "/run/dbus/system_bus_socket")
      (bind-ro-try "/var/run/dbus/system_bus_socket")))

(defn dbus-unrestricted [ctx]
  (-> ctx
      (bind-ro "/etc/machine-id")
      (bind-ro "/var/lib/dbus/machine-id")
      (bind-ro (dbus-bus-path))))

#_(defn dbus-see [])

(defn dbus-talk [ctx name]
  ;; todo filter with xdg-dbus-proxy
  ; (system/ro-bind "/tmp/my-dbus-proxy" (dbus-bus-path)
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
      (bind-ro "/usr/libexec/flatpak-xdg-utils/xdg-open" "/usr/bin/xdg-open")))

(defn x11 [ctx]
  ;; will need x11 proxying for better security
  (-> ctx
      (bind-ro-try "/tmp/.X11-unix/X1")))

(defn glib [ctx]
  (-> ctx
      (bind-ro-try-many (xdg-data-dir-paths "glib-2.0"))))

(defn gtk [ctx]
  ;; add X11 and Wayland
  (-> ctx
      (glib)
      (bind-ro-try "/etc/gtk-3.0")
      (bind-ro-try-many (xdg-config-dir-paths "gtk-3.0"))
      (bind-ro-try-many (xdg-data-dir-paths "gtk-3.0"))
      (xdg-config-home "gtk-3.0")))

(defn mime-cache [ctx]
  (-> ctx
      (bind-ro-try-many (xdg-data-dir-paths "mime/mime.cache"))))
