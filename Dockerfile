# https://hub.docker.com/_/eclipse-temurin?tab=description&page=2&name=17
FROM eclipse-temurin:17.0.5_8-jdk-jammy AS builder

ADD . /work

WORKDIR /work

RUN ./gradlew build

FROM eclipse-temurin:17.0.5_8-jdk-jammy AS runner

ARG PLANTUML_SERVICE_BIN_DIR="/home/app/plantuml-service/bin"
ARG PLANTUML_SERVICE_PATH="${PLANTUML_SERVICE_BIN_DIR}/plantuml-service.jar"

USER root

RUN useradd --create-home app \
  && apt-get update -qq \
  && apt-get upgrade -y \
  && apt-get install -y graphviz fonts-takao \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/* \
  && mkdir -p ${PLANTUML_SERVICE_BIN_DIR} \
  && chown -R app ${PLANTUML_SERVICE_BIN_DIR}

COPY --from=builder "/work/build/libs/plantuml-service.jar" ${PLANTUML_SERVICE_BIN_DIR}

USER app

ENTRYPOINT ["/opt/java/openjdk/bin/java"]
CMD ["-jar", "/home/app/plantuml-service/bin/plantuml-service.jar"]
