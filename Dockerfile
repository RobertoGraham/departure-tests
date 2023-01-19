FROM azul/zulu-openjdk-alpine:19.0.2-19.32.13-jre-headless
LABEL org.opencontainers.image.source=https://github.com/robertograham/departure-tests
WORKDIR /src
COPY . .
RUN ./gradlew -v
CMD ./gradlew test
