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
ENV RUN_JENKINS=true \
    RUN_BAMBOO=true \
    RUN_GITLAB=true \
    RUN_ZEPHYR=true \
    RUN_SONAR=true \
    RUN_BITBUCKET=true \
    RUN_TEAMCITY=true \
    RUN_GITHUB=true \
    RUN_GITHUBACTION=true \
    RUN_JIRATEST=true \
    RUN_ARGOCD=true

# Set the working directory
WORKDIR $APP_DIR

# Create the volumes
VOLUME $PROPERTIES_DIR

# Set the JAR file variables
ARG ZEPHYR_JAR_FILE=processors/jira-zephyr-scale/target/zephyr-processor.jar
ARG JIRA_TEST_JAR_FILE=processors/jira-xray-zephyr-squad/target/jiratest-processor.jar
ARG BITBUCKET_JAR_FILE=processors/bitbucket/target/bitbucket-processor.jar
ARG JENKINS_JAR_FILE=processors/jenkins/target/jenkins-processor.jar
ARG SONAR_JAR_FILE=processors/sonar/target/sonar-processor.jar
ARG BAMBOO_JAR_FILE=processors/bamboo/target/bamboo-processor.jar
ARG TEAMCITY_JAR_FILE=processors/teamcity/target/teamcity-processor.jar
ARG GITLAB_JAR_FILE=processors/gitlab/target/gitlab-processor.jar
ARG GITHUB_JAR_FILE=processors/github/target/github-processor.jar
ARG GITHUBACTION_JAR_FILE=processors/github-action/target/githubaction-processor.jar
ARG ARGOCD_JAR_FILE=processors/argocd/target/argocd-processor.jar

# Set the properties file names
ARG JENKINS_PROPERTIES_FILE_NAME=jenkins.properties
ARG BAMBOO_PROPERTIES_FILE_NAME=bamboo.properties
ARG BITBUCKET_PROPERTIES_FILE_NAME=bitbucket.properties
ARG SONAR_PROPERTIES_FILE_NAME=sonar.properties
ARG ZEPHYR_PROPERTIES_FILE_NAME=zephyr.properties
ARG JIRATEST_PROPERTIES_FILE_NAME=jiratest.properties
ARG TEAMCITY_PROPERTIES_FILE_NAME=teamcity.properties
ARG GITLAB_PROPERTIES_FILE_NAME=gitlab.properties
ARG GITHUB_PROPERTIES_FILE_NAME=github.properties
ARG GITHUBACTION_PROPERTIES_FILE_NAME=githubaction.properties
ARG ARGOCD_PROPERTIES_FILE_NAME=argocd.properties

# Copy JAR files
ADD ${ZEPHYR_JAR_FILE} $APP_DIR/zephyr.jar
ADD ${JIRA_TEST_JAR_FILE} $APP_DIR/jiratest.jar
ADD ${JENKINS_JAR_FILE} $APP_DIR/jenkins.jar
ADD ${SONAR_JAR_FILE} $APP_DIR/sonar.jar
ADD ${BAMBOO_JAR_FILE} $APP_DIR/bamboo.jar
ADD ${BITBUCKET_JAR_FILE} $APP_DIR/bitbucket.jar
ADD ${TEAMCITY_JAR_FILE} $APP_DIR/teamcity.jar
ADD ${GITLAB_JAR_FILE} $APP_DIR/gitlab.jar
ADD ${GITHUB_JAR_FILE} $APP_DIR/github.jar
ADD ${GITHUBACTION_JAR_FILE} $APP_DIR/githubaction.jar
ADD ${ARGOCD_JAR_FILE} $APP_DIR/argocd.jar

# Expose ports
EXPOSE 50001
EXPOSE 50002
EXPOSE 50003
EXPOSE 50007
EXPOSE 50011
EXPOSE 50012
EXPOSE 50014
EXPOSE 50019
EXPOSE 50020
EXPOSE 50022
EXPOSE 50023

# Copy startup script
ADD processors/devops-processor-startup/start_combined_collector.sh $APP_DIR/start_combined_collector.sh

# Set the ownership of the working directory to the non-root user

RUN chown -R $USER:$USER $APP_DIR \
    && chmod +x $APP_DIR/start_combined_collector.sh \
    && chmod 766 $keystorefile

# Switch to the non-root user
USER $USER:$GID

# Command to run the application
CMD ["sh", "start_combined_collector.sh"]