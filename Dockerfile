FROM maven:3.6.3-openjdk-15-slim
WORKDIR /src
COPY pom.xml .
RUN mvn -e -B dependency:go-offline
COPY src ./src
CMD [ "mvn", "-e", "-B", "test" ]