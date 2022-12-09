#!/bin/bash
keytool -noprompt -importcert -alias  LDAPSP_2021_DER.cer -keystore /usr/local/openjdk-8/lib/security/cacerts -storepass changeit -file certs/LDAPSP_2021_DER.cer
keytool -noprompt -importcert -alias LDAPSP_2021_BASE64.com -keystore /usr/local/openjdk-8/lib/security/cacerts -storepass changeit -file certs/LDAPSP_2021_BASE64.cer
java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar customapi.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/customapi.properties
