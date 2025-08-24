# Firewrap commandline sandbox wrapper is a hook function for fish shell which prefixes commands to run in a firewrap sandbox.
#
# Use by moving this file to ~/.config/fish/functions and adding following to ~/.config/fish/config.fish to bind to return key:
#
#  bind \r firewrap_command_wrapper
#
# Environment Variables:
#  FIREWRAP_ARGS
#      Custom firewrap arguments to use. If not set or empty, wrapper is disabled.
#      If set, overrides defaults of "-bc" (base + current dir) or "-b" (base only in $HOME)
#      For example:
#         set -gx FIREWRAP_ARGS "--profile foo"
#         export FIREWRAP_ARGS="--profile foo"
#      Using set -gx in fish has the advantage that it is detected as a builtin, so it won't be prefixed with fw
#      To disable wrapper: set -e FIREWRAP_ARGS

function firewrap_command_wrapper
    set -l cmd (commandline)
    set -l first_word (string split -m 1 " " $cmd)[1]

    # First conditions to disable sandbox wrapping
    if not set -q FIREWRAP_ARGS; or test -z "$FIREWRAP_ARGS"
        # Wrapper disabled when FIREWRAP_ARGS is not set or empty
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

        # Turn of sandbox sandbox by prepending `nf`
    else if string match -q "nf *" $cmd
        set -l stripped_cmd (string replace -r "^nf\s+" "" $cmd)
        commandline -r "$stripped_cmd"

        # Running in home directory could accidentaly leak it since it is default, use strictes sandbox just with base without sharing current directory
    else if test (pwd) = $HOME
        commandline -r "fw -b -- $cmd"

        # Default prepend sandbox with base and current directory access
    else
        commandline -r "fw $FIREWRAP_ARGS -- $cmd"
    end

    commandline -f execute
end
