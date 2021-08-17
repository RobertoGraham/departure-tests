FROM maven:3.8.2-openjdk-16-slim
LABEL org.opencontainers.image.source=https://github.com/robertograham/departure-tests
WORKDIR /src
COPY pom.xml .
RUN mvn -e -B dependency:go-offline
COPY src ./src
CMD [ "mvn", "-e", "-B", "test" ]