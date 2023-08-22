FROM psknowhow/amazoncorretto:8.1

# There are environment variables with periods in the names so change bash as default
RUN ln -sf /bin/bash /bin/sh 
#&& apt-get update && apt upgrade libc-bin -y

VOLUME /tmp
VOLUME /app
VOLUME /app/properties
VOLUME /app/offline_data
VOLUME /app/certs

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

ADD ${ZEPHYR_JAR_FILE} /app/zephyr.jar
ADD ${JIRA_TEST_JAR_FILE} /app/jiratest.jar
ADD ${JENKINS_JAR_FILE} /app/jenkins.jar
ADD ${SONAR_JAR_FILE} /app/sonar.jar
ADD ${BAMBOO_JAR_FILE} /app/bamboo.jar
ADD ${BITBUCKET_JAR_FILE} /app/bitbucket.jar
ADD ${TEAMCITY_JAR_FILE} /app/teamcity.jar
ADD ${GITLAB_JAR_FILE} /app/gitlab.jar
ADD ${GITHUB_JAR_FILE} /app/github.jar
ADD ${GITHUBACTION_JAR_FILE} /app/githubaction.jar


WORKDIR /app
ENV JAVA_OPTS=""

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

ADD processors/devops-processor-startup/start_combined_collector.sh start_combined_collector.sh
RUN ["chmod", "+x", "/app/start_combined_collector.sh"]
CMD ["sh", "start_combined_collector.sh"]
