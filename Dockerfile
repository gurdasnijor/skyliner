from alpine:edge

ENV BUILD_DEPS="bash build-base libpng-dev zlib-dev autoconf automake libtool nasm curl" \
    RUN_DEPS="openssh-client openjdk8-jre" \
    LEIN_ROOT=1

WORKDIR /build
COPY . .

RUN \
  apk update && \
  apk add $RUN_DEPS $BUILD_DEPS && \

  curl --silent https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /usr/local/bin/lein && \
  chmod +x /usr/local/bin/lein && \

  lein package && \
  mkdir /app && mv target/uberjar/*-standalone.jar /app/app.jar && \
  rm -rf /build ~/.m2 ~/.lein && \

  apk del $BUILD_DEPS && \
  rm -rf /tmp/* /var/cache/*

WORKDIR /app
ENTRYPOINT ["java", "-jar", "app.jar"]
