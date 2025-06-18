with import <nixpkgs> { };
mkShell {
buildInputs = [
babashka
bun
clojure
];
shellHook = ''
'';
}
