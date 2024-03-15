#!/bin/bash

# Counter for incrementing the alias
counter=1

# Loop through each certificate file and import it to the keystore with an incrementing alias
for cert_file in $certhostpath/*.cer
do
    # Generate the alias for the certificate
    alias="$keytoolalias$counter"
    echo -e "\033[32m"
    # Import the certificate to the keystore
    keytool -importcert -keystore "$keystorefile" -storepass changeit -alias "$alias" -file "$cert_file" -noprompt -v
    echo -e "\033[0m"
    # Increment the counter
    counter=$((counter+1))
        echo "Imported $cert_file to $keystorefile as $alias."
done

# Running Customapi jar file

java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar customapi.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/customapi.properties
