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
set -m

MONGODB_CMD="mongod --bind_ip 0.0.0.0 --logpath ${LOG_DIR}/mongodb.log "

if [ "${AUTH}" == "yes" ]
then
    CMD="${MONGODB_CMD} --auth"
fi
 
if [ "${JOURNALING}" == "no" ]
then
    CMD="${MONGODB_CMD} --nojournal"
fi
 
$CMD &
 
if [ ! -f ${DATA_DIR}/.mongodb_password_set ]
then
    . /docker-entrypoint-initdb.d/create_db_user.sh
fi

function running_js()
{
	
	COUNTER=0
	while !(nc -z localhost 27017) && [[ $COUNTER -lt 300 ]] ; do
	    sleep 5s
	    let COUNTER+=5
	    echo "Waiting for mongo to initialize... ($COUNTER seconds so far)" 
	done
	
	echo "############ Inserting KPI Master on Date `date` ###########"
	mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/kpi_master_insert.js
	
	echo "########### User defined Kpi category  ###########"
	mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/kpi_category_insert.js
	
	echo "########### kpi category mapping with kpi master  ###########"
	mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/kpi_category_mapping_insert.js

	echo "########### superadmin_setup `date` ###########"
	mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/superadmin_setup.js

	echo "###########  create_permissions `date` ###########"
	mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/create_permissions.js

	echo "###########  create_roles `date` ###########"
	mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/create_roles.js

	echo "###########  creating authorization policy rules `date` ###########"
	mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/insert_action_policy_rules.js

	echo "########### metadata_identifier `date` ###########"
	mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/metadata_identifier.js

	echo "########### creating hierarchy `date` ###########"
	mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/insert_hierarchy_levels.js

	echo "########### creating hierarchy default suggestions`date` ###########"
	mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/insert_hierarchy_level_suggestions.js

	echo "########### creating additional filter`date` ###########"
	mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/insert_additional_filter_categories.js

	echo "###########  default_estimation_mapping `date` ###########"
	mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/default_estimation_mapping.js


	echo "###########  creating indexes for db collections ###########"
	mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/dbCollection_indexes.js


	echo "########### Email server Sapecloud details script ###########"
	mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/email_server_details_global_config.js

	echo "########## Zephyr Cloud Base Url ############"
	mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/zephyr_cloud_details.js

  echo "########## insert processors to show on run processor screen ############"
	mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/insert_processors.js

	echo "########## insert kpi_fieldmapping to show kpiwise field mapping############"
  mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/kpi_fieldmapping_insert.js
}

function cron_service()
{
echo "########### Starting the Cron Service at Date `date` ###########"
cron
CRON_CHECK=$(cat /etc/crontab | grep '/docker-entrypoint-initdb.d/archive.js' )
if [ -z "$CRON_CHECK" ]; then
    chmod 700 /etc/crontab
    echo -e " 0 0 1,15 * * mongo localhost:27017/${MONGODB_APPLICATION_DATABASE} --username=${MONGODB_APPLICATION_USER} --password=${MONGODB_APPLICATION_PASS} < /docker-entrypoint-initdb.d/archive.js  >> /data/logs/purge_cron_service.log \n " >> /etc/crontab
    
fi
}

cron_service >> /data/logs/purge_cron_service.log
running_js >> /data/logs/master_data.log


fg

/bin/bash

