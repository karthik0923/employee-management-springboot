#!/usr/bin/env bash
set -euo pipefail
export JAVA_HOME="$(ls -d "$HOME/.local/jdk"/jdk-21* | head -1)"
export PATH="$JAVA_HOME/bin:$PATH"
cd /home/karthik/workSpace/personal/springboot/employee-management
echo "Using JAVA_HOME=$JAVA_HOME"
java -version
./mvnw -q -DskipTests compile
echo "COMPILE_OK"
