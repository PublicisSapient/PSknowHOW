#!/bin/sh
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
sudo docker stop customapi jira-processor nonjira-processor azure-processor nonazureboards-processor
sleep 20
sudo docker exec -it $mongo mongo localhost:27017/kpidashboard --username admin --password reset@123 --authenticationDatabase "admin" --eval "printjson (db.dropDatabase())"
echo "Copying dump to container...[Do not interupt the running process]"
sleep 100
sudo docker exec -t $mongo /bin/bash -c "rm -rf /tmp/kpidashboard/"
sudo docker cp /app/backupsnew/kpidashboard/ $mongo:/tmp
echo "restoring DB......"
sudo docker exec -t $mongo /bin/bash -c "mongorestore --port 27017 --username devadmin --password admin@123 --db kpidashboard /tmp/kpidashboard"


echo "db restore completed"
cd /app/apps
sudo docker-compose restart
mv /KnowHOW6.0.0/scripts/offline-dbbackup.sh /tmp/
( crontab -u root -l; ) | crontab -u root -
( crontab -u root -l; echo "0 23 * * 1-7 /tmp/offline-dbbackup.sh" ) | crontab -u root -

