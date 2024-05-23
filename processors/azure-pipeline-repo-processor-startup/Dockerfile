# Use a base image
FROM amazoncorretto:17

# Create a non-root user
ARG USER=marriott
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
    CERTS_DIR="/app/certs" \
    keytoolalias="myknowhow" \
    keystorefile="/usr/lib/jvm/java-17-amazon-corretto/lib/security/cacerts"

# Create the volumes
VOLUME $PROPERTIES_DIR

# Set the JAR file variables
ARG AZUREPIPELINE_JAR_FILE=processors/azure-pipeline/target/azurepipeline-processor.jar
ARG AZUREREPO_JAR_FILE=processors/azure-repo/target/azurerepo-processor.jar

# Set the properties file names
ARG AZUREPIPELINE_PROPERTIES_FILE_NAME=azurepipeline.properties
ARG AZUREREPO_PROPERTIES_FILE_NAME=azurerepo.properties

# Copy JAR files
ADD ${AZUREPIPELINE_JAR_FILE} $APP_DIR/azurepipeline.jar
ADD ${AZUREREPO_JAR_FILE} $APP_DIR/azurerepo.jar

# Set environment variables
ENV JAVA_OPTS=""

# Expose ports
EXPOSE 50015
EXPOSE 50016

# Copy startup script
ADD processors/azure-pipeline-repo-processor-startup/start_combined_collector.sh $APP_DIR/start_combined_collector.sh

# Set permissions for the startup script
RUN chown -R $USER:$USER /app \
    && chmod +x $APP_DIR/start_combined_collector.sh \
    && chmod 766 $keystorefile
RUN chgrp -R 0 /app && \
    chmod -R g=u /app && \
    chmod -R u+x /app

# Switch to the non-root user
USER $USER

# Command to run the application
CMD ["sh", "start_combined_collector.sh"]
