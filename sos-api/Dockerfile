FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY gradlew.bat gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY src ./src
RUN chmod +x ./gradlew || true
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:17-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xms64m", "-Xmx384m", "-jar", "app.jar"]
