FROM azul/zulu-openjdk:17.0.5-17.38.21
LABEL org.opencontainers.image.source=https://github.com/robertograham/departure-tests
WORKDIR /src
COPY . .
RUN ./gradlew -v
CMD ./gradlew test
