FROM azul/zulu-openjdk-alpine:17.0.1-17.30.15-jre-headless
LABEL org.opencontainers.image.source=https://github.com/robertograham/departure-tests
WORKDIR /src
COPY . .
RUN ./gradlew -v
CMD ./gradlew test
