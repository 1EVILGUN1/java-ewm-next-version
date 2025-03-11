FROM eclipse-temurin:21-jre-jammy
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} stats-server.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /stats-server.jar"]