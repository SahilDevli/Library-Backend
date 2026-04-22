FROM eclipse-temurin:21-jdk

WORKDIR /App

COPY target/LibraryApp.jar app.jar

# Container's port
EXPOSE 8082

# What to run
CMD ["java", "-jar", "app.jar"]