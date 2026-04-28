FROM gradle:9.1-jdk25 AS builder
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

RUN gradle --version

COPY api api
COPY common common
COPY connector connector
COPY core core
COPY modules modules
COPY node node
COPY platform-plugins platform-plugins
COPY server-plugins server-plugins

RUN gradle clean build --no-daemon

RUN JAR_NAME=$(ls /app/node/build/libs/potatocloud*.jar | head -n1) && \
    echo "Found JAR: $JAR_NAME" && \
    cp $JAR_NAME /app.jar

FROM eclipse-temurin:25-jdk AS jlink-base

RUN mkdir -p /opt/server/data

COPY --from=builder /app.jar /opt/server/server.jar
COPY --from=builder /app/platform-plugins/*/build/libs/potatocloud-plugin*.jar /opt/server/data

WORKDIR /opt/server

COPY .server /opt/server/

EXPOSE 25565
EXPOSE 5051

RUN printf '%s\n' '#!/bin/bash' \
'JAR_FILE=$(find /opt/server -name "server*.jar" | head -n1)' \
'exec java -jar "$JAR_FILE"' \
> /opt/server/start.sh && chmod +x /opt/server/start.sh

CMD ["/opt/server/start.sh"]