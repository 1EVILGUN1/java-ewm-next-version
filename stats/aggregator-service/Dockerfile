FROM eclipse-temurin:21-jre-jammy
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} aggregator-service.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /aggregator-service.jar"]