FROM eclipse-temurin:21-jre-jammy
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} event-service.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /event-service.jar"]