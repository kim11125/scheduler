FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-Dfile.encoding=UTF-8", "-Dserver.servlet.encoding.force=true", "-jar", "app.jar"]
