#!/bin/sh
cd "$(dirname "$0")" || exit 1

JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"

if ! command -v "$JAVA" >/dev/null 2>&1; then
    echo "Java was not found. Please install Java 25 (for example from https://adoptium.net)."
    exit 1
fi

exec "$JAVA" -Xms256M -Xmx1G -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -jar potatocloud.jar
