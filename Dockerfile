FROM ubuntu:20.04

WORKDIR /app
COPY ./bin ./bin

RUN apt-get update -qq \
  && apt-get upgrade -y \
  && apt-get install -y openjdk-8-jdk graphviz fonts-takao \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

RUN java -version
RUN ls -al

CMD ["/app/bin/plantuml-service", "3000"]
