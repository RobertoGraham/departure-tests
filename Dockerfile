FROM maven:3.6.3-openjdk-15-slim
LABEL org.opencontainers.image.source=https://github.com/robertograham/departure-tests
WORKDIR /src
COPY pom.xml .
RUN mvn -e -B dependency:go-offline
COPY src ./src
CMD [ "mvn", "-e", "-B", "test" ]