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

###########################################################################
##### Utility     :: Mongodb_backup.sh                                 #####
##### Description :: this utility is used to setup the Mongodb Backup  #####
##### Author      :: DevOps Team                                       #####
############################################################################

RED=`tput setaf 1`
GREEN=`tput setaf 2`
RESET=`tput sgr0`
YELLOW=`tput setaf 3`
CYAN=`tput setaf 6`
BLUE=`tput setaf 4`
MAGENTA=`tput setaf 5`
BLANK=`echo`

MONGO_DATABASE="mongodb-backup"
BACKUPS_DIR="/var/backups/"

TIMESTAMP=`date +%H-%s-%d-%m-%Y`

ARCHIVE_NAME="$MONGO_DATABASE-$TIMESTAMP.tgz"

echo "Performing backup of MONGO_DATABASE"
echo "--------------------------------------------"

mkdir -p $BACKUPS_DIR
echo -e " ${GREEN}[\xE2\x9C\x94] Createing Backup Archive...${RESET}"
echo ""

mongo=`docker ps | grep mongodb | awk '{ print $1}'`
sudo docker exec -t $mongo mongodump --db kpidashboard --username devadmin --password admin@123 --out /tmp
echo " ${GREEN}[\xE2\x9C\x94] Creating Backup .....${RESET}"


sudo docker cp $mongo:/tmp/kpidashboard /var/backups
echo " ${GREEN}[\xE2\x9C\x94] Coping backup to $BACKUPS_DIR .....${RESET}"

tar -czf $BACKUPS_DIR/$MONGO_DATABASE-$TIMESTAMP.tar.gz  $BACKUPS_DIR/kpidashboard
echo "--------------------------------------------"
echo -e "${GREEN}[\xE2\x9C\x94] The Backup Archive has been Created as $BACKUPS_DIR/$MONGO_DATABASE-$TIMESTAMP.tar.gz ${RESET}"


find /var/backups/ -name "*.tar.gz" -type f -mtime +15 -exec rm -f {} \;
echo "--------------------------------------------"
echo -e "${GREEN}[\xE2\x9C\x94] Database backup complete!"
