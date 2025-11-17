# Use a Java 25 builder image to build the .jar file
FROM eclipse-temurin:25-jdk-jammy AS builder

# Set the working directory
WORKDIR /app

# Copy the maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline

# copy the rest of the source code and build the application
COPY src src
RUN ./mvnw clean package -DskipTests

# Create the final image
FROM eclipse-temurin:25-jre-jammy

# set the working directory
WORKDIR /app

# Copy  the built .jar lile from the "builder"" stage
COPY --from=builder /app/target/*.jar app.jar

# expose the port from application.properties
EXPOSE 8080

# command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]