FROM amazoncorretto:17

# Create a non-root user
ARG USER=marriott
ARG UID=10000
ARG GID=10000

# Set the working directory
WORKDIR /app

# Set the ownership of the working directory to the non-root user
RUN ln -sf /bin/bash /bin/sh \
    && yum install -y shadow-utils \
    && groupadd -g $GID $USER \
    && useradd -u $UID -g $GID -m -s /bin/bash $USER \
    && yum clean all -y
    #&& apt-get update && apt upgrade libc-bin -y

# Set environment variables for volumes
ENV APP_DIR="/app" \
    PROPERTIES_DIR="/app/properties" \
    CONFIG_LOCATION="/app/properties/azure.properties" \
    keytoolalias="myknowhow" \
    keystorefile="/usr/lib/jvm/java-17-amazon-corretto/lib/security/cacerts" \
    JAVA_OPTS=""

# Create the volumes
VOLUME $PROPERTIES_DIR

# Set the JAR file variable
ARG JAR_FILE
ADD ${JAR_FILE} $APP_DIR/azure.jar

# Copy application.properties file
ADD src/main/resources/application.properties $PROPERTIES_DIR/azure.properties

# Expose port
EXPOSE 50017

# Set permissions for the JAR file
RUN chown -R $USER:$USER $APP_DIR \
    && chmod 766 $keystorefile
RUN chgrp -R 0 /app && \
    chmod -R g=u /app && \
    chmod -R u+x /app

# Switch to the non-root user
USER $USER

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar azure.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/azure.properties" ]
