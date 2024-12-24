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
}
fg

/bin/bash