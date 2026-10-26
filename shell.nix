{ pkgs ? import <nixpkgs> {} }:

let
  myPackages = with pkgs; [
    pre-commit

  ];

in
pkgs.mkShell {
  packages = myPackages;

  shellHook = ''
    exec fish
  '';
}
