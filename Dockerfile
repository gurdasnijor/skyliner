from java:8

ENV BUILD_DEPS="bash libpng-dev autoconf curl" \
    LEIN_ROOT=1 \
    PORT=8080 \
    DISPLAY=:0.0

EXPOSE 52286

# WORKDIR /build
COPY . /srv/Skyliner/

# COPY . .

WORKDIR /srv/Skyliner

# RUN cp -r bin ../

RUN \
  apt-get update --assume-yes && \
  apt-get install -o 'Dpkg::Options::=--force-confnew' -y --force-yes -q  $RUN_DEPS $BUILD_DEPS && \

  curl --silent https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /usr/local/bin/lein && \
  chmod +x /usr/local/bin/lein

  # lein ring uberjar && \
  # mkdir app && mv target/*-standalone.jar app/app.jar && \
  # rm -rf ~/.m2 ~/.lein

# WORKDIR /app