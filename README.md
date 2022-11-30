# pokemon-backend-service

Pokemon backend service is a Java 11 / Gradle / Spring Boot (version 2.7.6) application that provides pokemon data
through REST API.

## Getting started

Please install Docker to run pokemon-backend-service with Docker.

* [Docker Installation Documentation](https://docs.docker.com/get-docker/)

Please install Java 11 and Gradle to compile, build, test, and run pokemon-backend-service locally.

* [Java 11 Installation Documentation](https://docs.oracle.com/en/java/javase/11/install/overview-jdk-installation.html)
* [Gradle Installation Documentation](https://gradle.org/install/)

## How to run

Please execute the following commands from the project root directory to run the pokemon backend service.

### Using Docker

```
docker-compose up
```

The above command creates and start containers created from the Dockerfile that contains commands to assemble the docker
image.

## How to test

Please execute the following command from the project root directory to run pokemon backend service tests.

```
gradle test -i
```

## API documentation

Pokemon backend service API documentation is available in the following URL.

```
http://localhost:8080/swagger-ui/index.html#/
```

## How to test REST API with postman

Please locate to postman directory and import following postman collections stored under this directory.

* local.postman_environment.json - Environment variable
* pokemon-backend-service.postman_collection.json - Pokemon backend service REST API collections

Please choose local environment to test REST API provided by pokemon-backend-service after importing postman
collections.