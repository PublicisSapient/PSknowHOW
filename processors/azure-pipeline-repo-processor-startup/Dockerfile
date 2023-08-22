FROM psknowhow/amazoncorretto:8.1

# There are environment variables with periods in the names so change bash as default 
RUN ln -sf /bin/bash /bin/sh 
#&& apt-get update && apt upgrade libc-bin -y

VOLUME /tmp
VOLUME /app
VOLUME /app/properties
VOLUME /app/offline_data
VOLUME /app/certs

ARG AZUREPIPELINE_JAR_FILE=processors/azure-pipeline/target/azurepipeline-processor.jar
ARG AZUREREPO_JAR_FILE=processors/azure-repo/target/azurerepo-processor.jar


ARG AZUREPIPELINE_PROPERTIES_FILE_NAME=azurepipeline.properties
ARG AZUREREPO_PROPERTIES_FILE_NAME=azurerepo.properties

ADD ${AZUREPIPELINE_JAR_FILE} /app/azurepipeline.jar
ADD ${AZUREREPO_JAR_FILE} /app/azurerepo.jar


WORKDIR /app
ENV JAVA_OPTS=""

EXPOSE 50015
EXPOSE 50016


ADD processors/azure-pipeline-repo-processor-startup/start_combined_collector.sh start_combined_collector.sh
RUN ["chmod", "+x", "/app/start_combined_collector.sh"]
RUN ls -lrt
CMD ["sh", "start_combined_collector.sh"]
