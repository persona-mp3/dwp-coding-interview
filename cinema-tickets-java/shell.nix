{ pkgs ? import <nixpkgs> {} }:

let
  # 1. Define your package list as a reusable variable
  myPackages = with pkgs; [
    pre-commit
    google-java-format
  ];

  # 2. Extract the package names and join them into a comma-separated string
  packageNames = pkgs.lib.strings.concatMapStringsSep ", " (p: p.name or p.pname) myPackages;
in
pkgs.mkShell {
  packages = myPackages;

  shellHook = ''
    # 3. Interpolate the clean string directly into your message
    echo "nix shell started in $PWD with following packages: ${packageNames}"

    exec fish
  '';
}
