FROM psknowhow/amazoncorretto:8

# There are environment variables with periods in the names so change bash as default
RUN ln -sf /bin/bash /bin/sh 
#&& apt-get update && apt upgrade libc-bin -y

ENV CONFIG_LOCATION="/app/properties/customapi.properties" certhostpath="/app/certs/" keytoolalias="myknowhow" JAVA_OPTS="" keystorefile="/usr/local/openjdk-8/lib/security/cacerts"

ARG JAR_FILE
ADD ${JAR_FILE} /app/customapi.jar

WORKDIR /app

COPY src/main/resources/application.properties /app/properties/customapi.properties
COPY start_combined_collector.sh /app/start_combined_collector.sh

RUN ["chmod", "+x", "/app/start_combined_collector.sh"]

EXPOSE 8080

CMD ["sh", "start_combined_collector.sh"]

