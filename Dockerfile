#
# Build stage
FROM docker.io/library/maven:3-openjdk-17 as build
COPY . /source

RUN mvn  -q -f /source/pom.xml clean package spring-boot:repackage

#
# Package stage
#
COPY --from=build /source/target/*.jar /app.jar

EXPOSE 9020
ENTRYPOINT ["java", "-jar", "/app.jar"]
# Expose Java debug port
#EXPOSE 9021
# Run in debug mode
#ENTRYPOINT ["java","-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9021","-jar","/app.jar"]
