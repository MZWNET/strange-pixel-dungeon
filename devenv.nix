{pkgs, ...}: {
  languages.java = {
    enable = true;
    jdk.package = pkgs.openjdk17;
    gradle.enable = true;
    gradle.package = pkgs.gradle_9;
    maven.enable = true;
    lsp.enable = true;
  };

  enterShell = ''
    java -version
    gradle --version | head -n 5
  '';

  enterTest = ''
    java -version
    gradle --version
    mvn --version
    command -v jdtls
  '';
}
