# Use a base image
FROM amazoncorretto:17

# Create a non-root user
ARG USER=knowhowuser
ARG UID=1000
ARG GID=1000

RUN ln -sf /bin/bash /bin/sh \
    && yum install -y shadow-utils \
    && groupadd -g $GID $USER \
    && useradd -u $UID -g $GID -m -s /bin/bash $USER \
    && yum clean all -y

# Set environment variables for volumes
ENV APP_DIR="/app" \
    PROPERTIES_DIR="/app/properties" \
    keytoolalias="myknowhow" \
    certhostpath="/app/certs/" \
    keystorefile="/usr/lib/jvm/java-17-amazon-corretto/lib/security/cacerts" \
    JAVA_OPTS=""

# Define environment variables to control which jars to run (can be set during container run)
ENV RUN_ZEPHYR=true \
    RUN_JIRATEST=true

# Set the working directory
WORKDIR $APP_DIR

# Create the volumes
VOLUME $PROPERTIES_DIR

# Set the JAR file variables
ARG ZEPHYR_JAR_FILE=processors/jira-zephyr-scale/target/zephyr-processor.jar
ARG JIRA_TEST_JAR_FILE=processors/jira-xray-zephyr-squad/target/jiratest-processor.jar

# Set the properties file names
ARG ZEPHYR_PROPERTIES_FILE_NAME=zephyr.properties
ARG JIRATEST_PROPERTIES_FILE_NAME=jiratest.properties

# Copy JAR files
ADD ${ZEPHYR_JAR_FILE} $APP_DIR/zephyr.jar
ADD ${JIRA_TEST_JAR_FILE} $APP_DIR/jiratest.jar


# Expose ports
EXPOSE 50001
EXPOSE 50020

# Copy startup script
ADD processors/test-management/start_combined_collector.sh $APP_DIR/start_combined_collector.sh

# Set the ownership of the working directory to the non-root user
RUN chown -R $USER:$USER $APP_DIR \
    && chmod +x $APP_DIR/start_combined_collector.sh \
    && chmod 766 $keystorefile

# Switch to the non-root user
USER $USER:$GID

# Command to run the application
CMD ["sh", "start_combined_collector.sh"]