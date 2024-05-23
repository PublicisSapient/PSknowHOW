#!/bin/bash

ls -ltr /app
pwd

# Running Customapi jar file

java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar customapi.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/customapi.properties
