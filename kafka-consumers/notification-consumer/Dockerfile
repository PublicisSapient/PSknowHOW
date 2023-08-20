FROM psknowhow/amazoncorretto:8.1

#There are environment variables with periods in the names so change bash as default
RUN ln -sf /bin/bash /bin/sh 
#&& apt-get update && apt upgrade libc-bin -y

VOLUME /tmp
VOLUME /app
VOLUME /app/properties
VOLUME /app/offline_data

ENV CONFIG_LOCATION="/app/properties/notification-consumer.properties"
ENV JAVA_OPTS=""
ARG NOTIFICATION_CONSUMER_JAR_FILE
ADD ${NOTIFICATION_CONSUMER_JAR_FILE} /app/notification-consumer.jar
ADD kafka-consumers/notification-consumer/src/main/resources/application.properties /app/properties/notification-consumer.properties

EXPOSE 50020
WORKDIR /app
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar notification-consumer.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/notification-consumer.properties" ]
