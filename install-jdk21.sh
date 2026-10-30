#!/usr/bin/env bash
set -euo pipefail

JDK_DIR="$HOME/.local/jdk"
mkdir -p "$JDK_DIR"

if [ -x "$JDK_DIR"/jdk-21*/bin/java ] || ls -d "$JDK_DIR"/jdk-21* >/dev/null 2>&1; then
  JAVA_HOME="$(ls -d "$JDK_DIR"/jdk-21* | head -1)"
  if [ -x "$JAVA_HOME/bin/java" ]; then
    echo "JDK already present at $JAVA_HOME"
    "$JAVA_HOME/bin/java" -version
    exit 0
  fi
fi

cd /tmp
curl -L --fail -o OpenJDK21.tar.gz "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.13%2B9/OpenJDK21U-jdk_x64_linux_hotspot_21.0.13_9.tar.gz"
tar -xzf OpenJDK21.tar.gz -C "$JDK_DIR"
rm -f OpenJDK21.tar.gz
JAVA_HOME="$(ls -d "$JDK_DIR"/jdk-21* | head -1)"
echo "Installed JDK at $JAVA_HOME"
"$JAVA_HOME/bin/java" -version
