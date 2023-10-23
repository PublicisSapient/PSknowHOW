FROM psknowhow/amazoncorretto:8.1

# There are environment variables with periods in the names so change bash as default
RUN ln -sf /bin/bash /bin/sh 
#&& apt-get update && apt upgrade libc-bin -y

VOLUME /tmp
VOLUME /app
VOLUME /app/properties
VOLUME /app/offline_data

ENV CONFIG_LOCATION="/app/properties/azure.properties"
ENV JAVA_OPTS=""
ARG JAR_FILE
ADD ${JAR_FILE} /app/azure.jar
ADD src/main/resources/application.properties /app/properties/azure.properties

EXPOSE 50017
WORKDIR /app
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar azure.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/azure.properties" ]
