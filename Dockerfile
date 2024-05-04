FROM amazoncorretto:22-al2023-jdk as build

WORKDIR /app

COPY mvnw mvnw.cmd ./
COPY .mvn .mvn/
COPY checkstyle.xml ./
COPY pom.xml ./
COPY src ./src

RUN chmod +x mvnw
RUN ./mvnw install -DskipTests

RUN cp /app/target/*.jar /app/app.jar

EXPOSE 8443
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=dev", "/app/app.jar"]
