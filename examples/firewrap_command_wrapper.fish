# Firewrap commandline sandbox wrapper is a hook function for fish shell which prefixes commands to run in a firewrap sandbox.
#
# Use by moving this file to ~/.config/fish/functions and adding following to ~/.config/fish/config.fish to bind to return key:
#
#  bind \r firewrap_command_wrapper
#
# Environment Variables:
#  FIREWRAP_ARGS
#      Custom firewrap arguments to use instead of defaults
#      If not set, defaults to "-bc" (base + current dir) or "-b" (base only in $HOME)
#      For example:
#         set -gx FIREWRAP_ARGS "--profile foo"
#         export FIREWRAP_ARGS="--profile foo"
#      Using set -gx in fish has the advantage that it is detected as a builtin, so it won't be prefixed with fw
#  DISABLE_FIREWRAP_COMMAND_WRAPPER
#      Ability start shell with disabled wrapper with DISABLE_FIREWRAP_COMMAND_WRAPPER=1 fish

function firewrap_command_wrapper
    set -l cmd (commandline)
    set -l first_word (string split -m 1 " " $cmd)[1]

    # First conditions to disable sandbox wrapping
    if set -q DISABLE_FIREWRAP_COMMAND_WRAPPER
        # Disable wrapper for commands that start with DISABLE_FIREWRAP_COMMAND_WRAPPER= so that we can start non-sandboxed shell
        or string match -q "DISABLE_FIREWRAP_COMMAND_WRAPPER=*" $cmd
        # If commands already specifies firewrap sandbox, just use pass the original command
        or string match -q "fw *" $cmd; or string match -q "firewrap *" $cmd
        # Detect fish builtins and disable sandbox, so that `cd` and others work
        or builtin -q $first_word
        # Don't prefix when command is empty or only whitespace
        or test -z "$cmd"
        # zoxide
        or string match -q "z *" $cmd
        # using nofw passthrough helper script, so that commands without sandbox are available in history
        or string match -q "nofw *" $cmd
        # no point trying to sandbox if we want to run command as root
        or string match -q "sudo *" $cmd

        commandline -r "$cmd"

        # `nf fish` Shorthand to start unsandboxed subshell
    else if string match -q "nf fish" $cmd
        commandline -r "DISABLE_FIREWRAP_COMMAND_WRAPPER=1 fish"

        # Turn of sandbox sandbox by prepending `nf`
    else if string match -q "nf *" $cmd
        set -l stripped_cmd (string replace -r "^nf\s+" "" $cmd)
        commandline -r "$stripped_cmd"

        # By default sandbox commands run without network, prepend `fwnet` for network
    else if string match -q "fwnet *" $cmd
        set -l stripped_cmd (string replace -r "^fwnet\s+" "" $cmd)
        commandline -r "fw -bcn -- $stripped_cmd"

        # Running in home directory could accidentaly leak it since it is default, use strictes sandbox just with base without sharing current directory
    else if test (pwd) = $HOME
        set -l fw_args (if set -q FIREWRAP_ARGS; echo $FIREWRAP_ARGS; else; echo "-b"; end)
        commandline -r "fw $fw_args -- $cmd"

        # Default prepend sandbox with base and current directory access
    else
        set -l fw_args (if set -q FIREWRAP_ARGS; echo $FIREWRAP_ARGS; else; echo "-bc"; end)
        commandline -r "fw $fw_args -- $cmd"
    end

    commandline -f execute
end
