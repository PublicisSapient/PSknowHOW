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

#start_combined_collector.sh
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

java -jar zephyr.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/zephyr.properties &
java -jar jenkins.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/jenkins.properties &
java -jar sonar.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/sonar.properties &
java -jar bamboo.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/bamboo.properties &
java -jar bitbucket.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/bitbucket.properties  &
java -jar teamcity.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/teamcity.properties &
java -jar gitlab.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/gitlab.properties &
java -jar github.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/github.properties &
java -jar githubaction.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/githubaction.properties &
java -jar jiratest.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/jiratest.properties &
java -jar argocd.jar --spring.config.location=classpath:/BOOT-INF/classes/application.properties --spring.config.additional-location=optional:file:/app/properties/argocd.properties
