#!/bin/sh
# Maven Wrapper startup script
SCRIPT_DIR="$( cd "$( dirname "$0" )" && pwd )"
MAVEN_HOME="${MAVEN_HOME:-$HOME/.m2/wrapper/dists/apache-maven-3.9.6}"
if [ ! -d "$MAVEN_HOME" ]; then
    echo "Maven wrapper not set up. Please run: mvn wrapper:wrapper"
    exit 1
fi
exec "$MAVEN_HOME/bin/mvn" "$@"
