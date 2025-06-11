Firewrap is a security tool that runs programs in isolated sandbox environments using Bubblewrap. Main principles:

**Security Philosophy:**
- **Principle of Least Privilege** - processes get minimal permissions needed
- **Secure by default** - everything forbidden unless explicitly allowed (Default Deny)
- **Lisp-based configuration** - full programming language for flexible, composable security rules

**Key Design Guidelines:**
- Use presets and profiles for reusable security policies
- Provide tooling to assist sandbox creation (trace analysis, static analysis)
- Support iterative workflow: record traces → map to abstractions → audit rules
- Pragmatic approach - allow starting with wider sandboxes, then tightening over time

The tool prioritizes security through programming language abstractions rather than simple configuration files, enabling better reasoning about sandbox security properties.

## Development

Run tests using `bb test:once`
