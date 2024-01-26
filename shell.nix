with (import <nixpkgs> {});
mkShell {
  buildInputs = [
    jdk11
  ];
}