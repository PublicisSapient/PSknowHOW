# Use a base image
FROM amazoncorretto:17

# Create a non-root user
ARG USER=marriott
ARG UID=1000
ARG GID=1000

RUN ln -sf /bin/bash /bin/sh \
    && yum install -y shadow-utils \
    && groupadd -g $GID $USER \
    && useradd -u $UID -g $GID -m -s /bin/bash $USER \
    && yum clean all -y

# Set the working directory
WORKDIR /app

# Set environment variables for volumes
ENV APP_DIR="/app" \
    PROPERTIES_DIR="/app/properties" \
    CONFIG_LOCATION="/app/properties/notification-consumer.properties" \
    JAVA_OPTS=""

# Create the volumes
VOLUME $PROPERTIES_DIR

# Set the JAR file variable
ARG NOTIFICATION_CONSUMER_JAR_FILE
ADD ${NOTIFICATION_CONSUMER_JAR_FILE} $APP_DIR/notification-consumer.jar

# Copy application.properties file
ADD kafka-consumers/notification-consumer/src/main/resources/application.properties $PROPERTIES_DIR/notification-consumer.properties

# Expose port
EXPOSE 50020

# Set permissions for the JAR file
RUN chown -R $USER:$USER $APP_DIR
RUN chgrp -R 0 /app && \
    chmod -R g=u /app && \
    chmod -R u+x /app

# Switch to the non-root user
USER $USER

# Entrypoint command
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar notification-consumer.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/notification-consumer.properties"]
