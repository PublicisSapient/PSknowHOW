#!/bin/sh

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

#nonjira=`sudo docker ps | grep nonjira-processor | awk '{ print $1}'`

sudo docker cp /app/apps/certs/knowhowinstaller.cer nonjira-processor:/app/certs &&
sudo docker exec -t nonjira-processor /bin/sh -c "keytool -noprompt -importcert -alias $1 -keystore /usr/local/openjdk-8/lib/security/cacerts -storepass changeit -file certs/knowhowinstaller.cer" &&

# Adding certificate for login through PS AD
#customapi=`sudo docker ps | grep customapi | awk '{ print $1}'`

sudo docker cp /app/apps/certs/$2.cer customapi:/app/certs &&
sudo docker exec -t customapi /bin/sh -c "keytool -noprompt -importcert -alias $2 -keystore /usr/local/openjdk-8/lib/security/cacerts -storepass changeit -file certs/$2.cer" &&
sudo docker restart customapi ui
