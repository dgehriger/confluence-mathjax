# MathJax Plugin for Confluence 9.2

## Pre-requisites

### Maven

Using Chocolatey:

```powershell
choco install maven
```

### Java 20

```powershell
choco install openjdk --version=20.0.0
```

### Atlassian SDK

See instructions at [Atlassian SDK](https://developer.atlassian.com/server/framework/atlassian-sdk/install-the-atlassian-sdk-on-a-windows-system/).

## Building

### Confluence Plugin

```powershell
cd mathjax
$env:JAVA_HOME = "$env:PROGRAMFILES\OpenJDK\jdk-20.0.1\"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
javac -version
atlas-package.bat
```

### MathJax Server

```powershell
cd mathjax-server
rustup target add x86_64-unknown-linux-gnu
cargo build --release --target x86_64-unknown-linux-gnu
```
