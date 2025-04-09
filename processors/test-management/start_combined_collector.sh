#!/bin/bash
################################################################################
# Copyright 2014 CapitalOne, LLC.
# Further development Copyright 2022 Sapient Corporation.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
################################################################################

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

# Conditionally run the JAR files based on environment variables

if [ "$RUN_ZEPHYR" == "true" ]; then
    java -jar zephyr.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/zephyr.properties &
    echo "Running Zephyr processor..."
fi

if [ "$RUN_JIRATEST" == "true" ]; then
    java -jar jiratest.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/jiratest.properties &
    echo "Running Jira Test processor..."
fi

wait