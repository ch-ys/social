# Docker 镜像构建
FROM maven:3.5-jdk-8-alpine as builder


# Copy local code to the container image.
WORKDIR /app
COPY /yupao-backend-0.0.1-SNAPSHOT.jar /app/yupao-backend-0.0.1-SNAPSHOT.jar

# Build a release artifact.
# RUN mvn package -DskipTests

# Run the web service on container startup.
CMD ["redis-server"]
CMD ["java","-jar","/app/yupao-backend-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]