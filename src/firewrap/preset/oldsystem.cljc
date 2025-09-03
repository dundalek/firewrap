(ns firewrap.preset.oldsystem
  (:require
   [clojure.string :as str]
   [firewrap.sandbox :as sb]
   [firewrap.preset.dumpster :as dumpster]))

(defmacro nop [ctx & _]
  ctx)

(defmacro not-exists [ctx & _]
  ctx)

(def add-bwrap-args #'sb/add-raw-args)

(def bind-ro sb/bind-ro)
(def bind-ro-try sb/bind-ro-try)
(def bind-rw sb/bind-rw)
(def bind-rw-try sb/bind-rw-try)
(def bind-dev sb/bind-dev)
(def bind-dev-try sb/bind-dev-try)

;; might need helpers like this since ctx would be in wrong position using `->`
;; alternative that comes to mind is `(-> ctx ((partial reduce bind-ro-try) paths))`
;; which looks somewhat complicated
(defn bind-ro-try-many [ctx paths]
  (reduce bind-ro-try ctx paths))

(defn bind-rw-try-many [ctx paths]
  (reduce bind-rw-try ctx paths))

(def isolated-home dumpster/bind-isolated-home)

(defn- xdg-dirs [dirs-env subfolders]
  (let [dirs (some-> dirs-env
                     (str/split #":"))]
    (for [subfolder subfolders
          dir dirs]
      (str (str/replace dir #"\/+$" "")
           "/" subfolder))))

(defn xdg-data-dirs-path [ctx]
  (or (sb/getenv ctx "XDG_DATA_DIRS")
      "/usr/local/share/:/usr/share/"))

(defn xdg-data-dir-paths [ctx & subfolders]
  ;; e.g. $HOME/.local/share /usr/local/share /usr/share
  (xdg-dirs (xdg-data-dirs-path ctx) subfolders))

(comment
  (xdg-data-dir-paths "icons" "themes"))

(defn xdg-config-dirs-path [ctx]
  (or (sb/getenv ctx "XDG_CONFIG_DIRS")
      "/etc/xdg"))

(defn xdg-config-dir-paths [ctx & subfolders]
  (xdg-dirs (xdg-config-dirs-path ctx) subfolders))

(comment
  (xdg-config-dir-paths "gtk-3.0" "glib-2.0"))

(defn xdg-data-home-path [ctx]
  (or (sb/getenv ctx "XDG_DATA_HOME")
      (dumpster/home ctx ".local/share")))

(defn xdg-data-home-paths [ctx & subfolders]
  (xdg-dirs (xdg-data-home-path ctx) subfolders))

(defn xdg-config-home-path [ctx]
  (or (sb/getenv ctx "XDG_CONFIG_HOME")
      (dumpster/home ctx ".config")))

(defn xdg-config-home-paths [ctx & subfolders]
  (xdg-dirs (xdg-config-home-path ctx) subfolders))

(defn xdg-cache-home-path [ctx]
  (or (sb/getenv ctx "XDG_CACHE_HOME")
      (dumpster/home ctx ".cache")))

(defn xdg-cache-home-paths [ctx & subfolders]
  (xdg-dirs (xdg-cache-home-path ctx) subfolders))

(defn xdg-state-home-path [ctx]
  (or (sb/getenv ctx "XDG_STATE_HOME")
      (dumpster/home ctx ".local/state")))

(defn xdg-state-home-paths [ctx & subfolders]
  (xdg-dirs (xdg-state-home-path ctx) subfolders))

(defn xdg-data-dir [ctx & subfolders]
  (bind-ro-try-many ctx (apply xdg-data-dir-paths ctx subfolders)))

(defn xdg-data-home [ctx & subfolders]
  (bind-ro-try-many ctx (apply xdg-data-home-paths ctx subfolders)))

(defn xdg-cache-home [ctx & subfolders]
  (bind-ro-try-many ctx (apply xdg-cache-home-paths ctx subfolders)))

(defn xdg-config-home [ctx & subfolders]
  (bind-ro-try-many ctx (apply xdg-config-home-paths ctx subfolders)))

(defn xdg-state-home [ctx & subfolders]
  (bind-ro-try-many ctx (apply xdg-state-home-paths ctx subfolders)))

(defn xdg-runtime-dir-path [ctx]
  ;; fallback to "/run/user/$(id -u)" if missing?
  (sb/getenv ctx "XDG_RUNTIME_DIR"))

(defn xdg-runtime-dir [ctx subdir]
  (let [path (str (xdg-runtime-dir-path ctx) "/" subdir)]
    (-> ctx
        (bind-ro-try path))))

(defn fontconfig [ctx]
  (-> ctx
      (bind-ro-try (dumpster/home ctx ".config/fontconfig"))
      (bind-ro-try "/usr/share/fontconfig")))

(defn fontconfig-shared-cache [ctx]
  ;; bind so that apps can share fontconfig cache for better performance
  ;; but for better security one can avoid it
  (-> ctx
      ;; TODO read xdg cache dir
      (bind-rw-try (dumpster/home ctx ".cache/fontconfig"))
      (bind-ro-try "/var/cache/fontconfig")))

(defn fonts [ctx]
  (-> ctx
      (bind-ro-try "/etc/fonts")
      (bind-ro-try "/usr/share/fonts")
      (bind-ro-try "/usr/local/share/fonts")
      (bind-ro-try (dumpster/home ctx ".fonts"))
      (bind-ro-try (dumpster/home ctx ".local/share/fonts"))))

(defn icons [ctx]
  (-> ctx
      (bind-ro-try "/usr/share/icons")
      (bind-ro-try (dumpster/home ctx ".icons"))
      (bind-ro-try (dumpster/home ctx ".local/share/icons"))))

(defn locale [ctx]
  (-> ctx
      (bind-ro-try "/usr/share/locale")
      (bind-ro-try "/usr/share/locale-langpack")))

(defn themes [ctx]
  ;; include /usr/share/pixmaps?
  (-> ctx
      (bind-ro-try (dumpster/home ctx ".themes"))
      (bind-ro-try (dumpster/home ctx ".local/share/themes"))
      (bind-ro-try "/usr/share/themes")))

(defn dconf [ctx]
  ;; are there some security implications of apps sharing access to dconf?
  (-> ctx
      (bind-ro-try "/etc/dconf/profile/user")
      (bind-ro-try (dumpster/home ctx ".config/dconf/user"))
      ;; should it be more granular to enable user and profile explicitly?
      ;; is there a difference between user and profile?
      ; (bind-ro-try (str (xdg-runtime-dir-path ctx) "/dconf/user"))
      ; (bind-ro-try (str (xdg-runtime-dir-path ctx) "/dconf/profile"))
      (bind-rw-try (str (xdg-runtime-dir-path ctx) "/dconf"))
      (bind-ro-try-many (xdg-data-dir-paths ctx "dconf"))))
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

(defn libs [ctx]
  (-> ctx
      ;; is it safe to include ld.so.cache?
      (bind-ro-try "/etc/ld.so.cache")
      (bind-ro-try "/usr/lib")
      (bind-ro-try "/usr/lib64")
      (sb/symlink "usr/lib" "/lib")
      (sb/symlink "usr/lib64" "/lib64")))

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

(defn dev-pts [ctx]
  (-> ctx
      (bind-dev-try "/dev/pts")))

(defn dbus-bus-path [ctx]
  (or (some-> (sb/getenv ctx "DBUS_SESSION_BUS_ADDRESS")
              (str/replace #"^unix:path=" ""))
      (str (xdg-runtime-dir-path ctx) "/bus")))

(defn dbus-system-bus [ctx]
  (-> ctx
      (bind-ro-try "/run/dbus/system_bus_socket")
      (bind-ro-try "/var/run/dbus/system_bus_socket")))

(defn dbus-unrestricted [ctx]
  (-> ctx
      (bind-ro "/etc/machine-id")
      (bind-ro "/var/lib/dbus/machine-id")
      (bind-ro (dbus-bus-path ctx))))

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
      (bind-ro-try-many (xdg-data-dir-paths ctx "glib-2.0"))))

(defn gtk [ctx]
  ;; add X11 and Wayland
  (-> ctx
      (glib)
      (bind-ro-try "/etc/gtk-3.0")
      (bind-ro-try-many (xdg-config-dir-paths ctx "gtk-3.0"))
      (bind-ro-try-many (xdg-data-dir-paths ctx "gtk-3.0"))
      (xdg-config-home "gtk-3.0")))

(defn mime-cache [ctx]
  (-> ctx
      (bind-ro-try-many (xdg-data-dir-paths ctx "mime/mime.cache"))))

(defn command [ctx cmd]
  (let [paths (some-> (sb/getenv ctx "PATH")
                      (str/split #":"))]
    ;; Consider resolving and binding only existing binaries?
    (reduce (fn [ctx path]
              (bind-ro-try ctx (str path "/" cmd)))
            ctx
            paths)))

(defn timezone [ctx]
  (-> ctx
      (sb/bind-ro-try "/etc/localtime")
      (xdg-data-dir "zoneinfo")))
