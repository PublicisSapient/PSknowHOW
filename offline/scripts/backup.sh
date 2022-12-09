#!/bin/bash
set -e
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
mongo=`sudo docker ps | grep mongodb | awk '{ print $1}'`
COMPOSE="/app/apps/"

echo " value $COMPOSE"

mkdir -p /var/backups/kpidashboard
echo -e " ${GREEN}[\xE2\x9C\x94] Taking db backup ...${RESET}"
sudo docker exec -t $mongo /bin/sh -c "rm -rf /tmp"

sudo docker exec -t $mongo /bin/sh -c "mongodump --db kpidashboard --username devadmin --password admin@123 --out /tmp"

echo "backup completed"
echo -e " ${GREEN}[\xE2\x9C\x94] Copying backed up dump to destination directory ...${RESET}"
sudo docker cp $mongo:/tmp/kpidashboard/ /var/backups/
# cp -r /var/backups/kpidashboard/* /var/backups/kpidashboard
sudo mkdir -p /app/backupsnew/
sudo docker cp $mongo:/tmp/kpidashboard/ /app/backupsnew/

echo -e " ${GREEN}[\xE2\x9C\x94] Shutting down containers ...${RESET}"
cd "${COMPOSE}/"
sudo docker-compose down
#sudo mv /app/apps/db_data/ /tmp/

