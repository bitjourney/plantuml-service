# https://hub.docker.com/_/eclipse-temurin?tab=description&page=2&name=17
FROM eclipse-temurin:17.0.5_8-jdk-jammy


WORKDIR /app
COPY ./bin ./bin

RUN apt-get update -qq \
  && apt-get upgrade -y \
  && apt-get install -y openjdk-8-jdk graphviz fonts-takao \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

RUN java -version
RUN ls -al

ENTRYPOINT ["/opt/java/openjdk/bin/java"]
CMD ["-jar", "/home/app/plantuml-service/bin/plantuml-service.jar"]
