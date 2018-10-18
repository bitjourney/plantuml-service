#!/bin/sh -xe

BASE_TAG=bitjourney/plantuml-service
TAG=${BASE_TAG}:1.3.3

docker --version
docker login
docker build -t ${TAG} .
docker push ${TAG}
