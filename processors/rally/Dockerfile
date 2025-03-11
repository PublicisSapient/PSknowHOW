# Use a base image
FROM amazoncorretto:17

# Create a non-root user
ARG USER=knowhowuser
ARG UID=1000
ARG GID=1000

# Set the working directory
WORKDIR /app

# Set the ownership of the working directory to the non-root user
RUN ln -sf /bin/bash /bin/sh \
    && yum install -y shadow-utils \
    && groupadd -g $GID $USER \
    && useradd -u $UID -g $GID -m -s /bin/bash $USER \
    && yum clean all -y

# Set environment variables for volumes

ENV APP_DIR="/app" \
    PROPERTIES_DIR="/app/properties" \
    CONFIG_LOCATION="/app/properties/rally.properties" \
    JAVA_OPTS="" \
    keytoolalias="myknowhow" \
    keystorefile="/usr/lib/jvm/java-17-amazon-corretto/lib/security/cacerts"

# Create the volumes
VOLUME $PROPERTIES_DIR

# Set the JAR file variable
ARG JAR_FILE
ADD ${JAR_FILE} $APP_DIR/rally.jar

# Copy application.properties file
ADD src/main/resources/application.properties $PROPERTIES_DIR/rally.properties

# Expose port
EXPOSE 50024

# Set permissions for the JAR file
RUN chown -R $USER:$USER /app \
    && chmod 766 $keystorefile

# Switch to the non-root user
USER $USER:$GID

# Entrypoint command
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar rally.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/rally.properties"]
