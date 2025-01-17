# MathJax Plugin for COnfluence

## Pre-requisites

### Maven

Using Chocolatey:

```powershell
choco install maven
```

### Java 17

```powershell
choco install adoptopenjdk17
```

## Building

### Confluence Plugin

```powershell
cd mathjax
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.8.7-hotspot\"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
atlas-package.bat
```

### MathJax Server

```powershell
cd mathjax-server
rustup target add x86_64-unknown-linux-gnu
cargo build --release --target x86_64-unknown-linux-gnu
```
