FROM amazoncorretto:17

ENV JAVA_OPTS="" \
    SPRING_CONFIG_LOCATION="/app/properties/authnauth.properties"
ARG JAR_FILE
ADD ${JAR_FILE} /app/authnauth.jar

WORKDIR /app

EXPOSE 8787

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar authnauth.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/authnauth.properties" ]