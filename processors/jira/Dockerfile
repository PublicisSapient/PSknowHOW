FROM openjdk:8-jre-slim-stretch

# There are environment variables with periods in the names so change bash as default
RUN ln -sf /bin/bash /bin/sh 
#&& apt-get update && apt upgrade libc-bin -y

VOLUME /tmp
VOLUME /app
VOLUME /app/properties
VOLUME /app/offline_data

ENV CONFIG_LOCATION="/app/properties/jira.properties"
ENV JAVA_OPTS=""
ARG JAR_FILE
ADD ${JAR_FILE} /app/jira.jar
ADD src/main/resources/application.properties /app/properties/jira.properties

EXPOSE 50008
WORKDIR /app
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar jira.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/jira.properties" ]
