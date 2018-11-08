# https://hub.docker.com/_/openjdk/
FROM openjdk:8

ARG PLANTUML_SERVICE_VERSION
ARG PLANTUML_SERVICE_JAR_URL="https://github.com/bitjourney/plantuml-service/releases/download/v${PLANTUML_SERVICE_VERSION}/plantuml-service.jar"
ARG PLANTUML_SERVICE_BIN_DIR="/home/app/plantuml-service/bin"
ARG PLANTUML_SERVICE_PATH="${PLANTUML_SERVICE_BIN_DIR}/plantuml-service.jar"

RUN echo "${PLANTUML_SERVICE_VERSION?:--build-arg PLANTUML_SERVICE_VERSION=version is mandatory}"

USER root

RUN useradd --create-home app \
  && apt-get update -qq \
  && apt-get upgrade -y \
  && apt-get install -y graphviz fonts-takao curl \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/* \
  && mkdir -p ${PLANTUML_SERVICE_BIN_DIR} \
  && curl -L ${PLANTUML_SERVICE_JAR_URL} -o ${PLANTUML_SERVICE_PATH} \
  && chown -R app ${PLANTUML_SERVICE_BIN_DIR}

USER app

ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "/home/app/plantuml-service/bin/plantuml-service.jar"]
