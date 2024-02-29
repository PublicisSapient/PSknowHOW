FROM amazoncorretto:17

# There are environment variables with periods in the names so change bash as default
RUN ln -sf /bin/bash /bin/sh 
#&& apt-get update && apt upgrade libc-bin -y

# Create a non-root user
ARG USER=knowhowuser
ARG UID=1000
ARG GID=1000

RUN yum install -y shadow-utils \
    && groupadd -g $GID $USER \
    && useradd -u $UID -g $GID -m -s /bin/bash $USER
	
# Set the working directory
WORKDIR /app

# Set the ownership of the working directory to the non-root user
RUN chown -R $USER:$USER /app

# Set environment variables for volumes
ENV TMP_DIR="/tmp"
ENV APP_DIR="/app"
ENV PROPERTIES_DIR="/app/properties"
ENV OFFLINE_DATA_DIR="/app/offline_data"

# Create the volumes
VOLUME $TMP_DIR
VOLUME $APP_DIR
VOLUME $PROPERTIES_DIR
VOLUME $OFFLINE_DATA_DIR

# Set environment variables
ENV CONFIG_LOCATION="/app/properties/azure.properties"
ENV JAVA_OPTS=""

# Set the JAR file variable
ARG JAR_FILE
ADD ${JAR_FILE} $APP_DIR/azure.jar

# Copy application.properties file
ADD src/main/resources/application.properties $PROPERTIES_DIR/azure.properties

# Expose port
EXPOSE 50017

# Set permissions for the JAR file
RUN chown $USER:$USER $APP_DIR/azure.jar

# Switch to the non-root user
USER $USER:$GID

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar azure.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/azure.properties" ]