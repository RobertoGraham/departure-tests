FROM azul/zulu-openjdk:19.0.1-19.30.11-jre-headless
LABEL org.opencontainers.image.source=https://github.com/robertograham/departure-tests
WORKDIR /src
COPY . .
RUN ./gradlew -v
CMD ./gradlew test
