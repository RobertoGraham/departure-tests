FROM azul/zulu-openjdk-alpine:18.0.2-jre-headless
LABEL org.opencontainers.image.source=https://github.com/robertograham/departure-tests
WORKDIR /src
COPY . .
RUN ./gradlew -v
CMD ./gradlew test
