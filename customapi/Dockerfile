# Use a base image
FROM amazoncorretto:17-al2023-jdk

# Set a non-root user
ARG USER=knowhowuser
ARG UID=1000
ARG GID=1000

RUN yum install -y shadow-utils \
    && ln -sf /bin/bash /bin/sh \ 
    && groupadd -g $GID $USER \
    && useradd -u $UID -g $GID -m -s /bin/bash $USER

# Set the environment variables
ENV CONFIG_LOCATION="/app/properties/customapi.properties" \
    certhostpath="/app/certs/" \
    keytoolalias="myknowhow" \
    JAVA_OPTS="" \
    keystorefile="/usr/lib/jvm/java-1.8.0-amazon-corretto/jre/lib/security/cacerts"

# Set the working directory
WORKDIR /app

# Copy your application files to the container
ARG JAR_FILE
ADD ${JAR_FILE} /app/customapi.jar

COPY src/main/resources/application.properties /app/properties/customapi.properties
COPY start_combined_collector.sh /app/start_combined_collector.sh

# Change ownership to the non-root user
RUN chown -R $USER:$USER /app

# Give execute permissions to the script
RUN chmod +x /app/start_combined_collector.sh

# Expose the port
EXPOSE 8080

# Switch to the non-root user
USER $USER:$GID

# Specify the command to run your application
CMD ["sh", "start_combined_collector.sh"]
