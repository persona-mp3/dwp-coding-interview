{ pkgs ? import <nixpkgs> {} }:

let
  myPackages = with pkgs; [
    pre-commit
    google-java-format
  ];
  
in
pkgs.mkShell {
  packages = myPackages;

  shellHook = ''
    exec fish
  '';
}

