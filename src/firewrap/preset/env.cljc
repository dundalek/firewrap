(ns firewrap.preset.env
  (:require
   [clojure.set :as set]))

(def terminal
  #{"COLORTERM"
    "TERMINAL"
    "TERMINFO"
    "TERM_PROGRAM"
    "TERM_PROGRAM_VERSION"
    "TERM"
    "SHLVL"

    "GHOSTTY_BIN_DIR"
    "GHOSTTY_RESOURCES_DIR"
    "GHOSTTY_SHELL_INTEGRATION_NO_SUDO"})

(def programming-languages
  #{"BUN_INSTALL"
    "DEPS_CLJ_TOOLS_DIR"
    "GOPATH"
    "JAVA_HOME"})

(def locales
  #{"PAPERSIZE"
    "LANG"
    "LANGUAGE"
    "LC_ADDRESS"
    "LC_IDENTIFICATION"
    "LC_MEASUREMENT"
    "LC_MONETARY"
    "LC_NAME"
    "LC_NUMERIC"
    "LC_PAPER"
    "LC_TELEPHONE"
    "LC_TIME"})

(def desktop-environment
  #{"DISPLAY"
    "WINDOWPATH"
    "XAUTHORITY"
    "XMODIFIERS"

    "DESKTOP_SESSION"
    "GDMSESSION"
    "GNOME_DESKTOP_SESSION_ID"
    "GNOME_SHELL_SESSION_MODE"
    "MANAGERPID"
    "SESSION_MANAGER"

    "GTK_IM_MODULE"
    "GTK_MODULES"

    "QT_ACCESSIBILITY"
    "QT_IM_MODULE"

    "LIBGL_DRIVERS_PATH"
    "LIBVA_DRIVERS_PATH"
    "__EGL_VENDOR_LIBRARY_FILENAMES"

    "XDG_CONFIG_DIRS"
    "XDG_CURRENT_DESKTOP"
    "XDG_DATA_DIRS"
    "XDG_MENU_PREFIX"
    "XDG_RUNTIME_DIR"
    "XDG_SESSION_CLASS"
    "XDG_SESSION_DESKTOP"
    "XDG_SESSION_TYPE"

    "DBUS_SESSION_BUS_ADDRESS"})

(def systemd
  #{"SYSTEMD_EXEC_PID"
    "JOURNAL_STREAM"
    "INVOCATION_ID"})

(def gpg-ssh
  #{"GPG_AGENT_INFO"
    "SSH_AGENT_LAUNCHER"
    "SSH_AUTH_SOCK"})

(def allowed
  (set/union
   #{"NIX_PROFILES"
     "NIX_SSL_CERT_FILE"
     "LOCALE_ARCHIVE"

     "LOGNAME"
     "USER"
     "USERNAME"

     "EDITOR"
     "SHELL"

     "_"
     "HOME"
     "PWD"
     "PATH"
     "LD_LIBRARY_PATH"}

   programming-languages
   locales

   terminal
   desktop-environment

   systemd
   #_gpg-ssh))
