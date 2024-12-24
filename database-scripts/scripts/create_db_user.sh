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

 
# Wait for MongoDB to start
echo "========================================================================" >> ${LOG_DIR}/create_user.log 2>&1
STATUS_COUNT=1
while [ "${STATUS_COUNT}" -ne 0 ]
do
  echo "=> Waiting MongoDB service startup..." >> ${LOG_DIR}/create_user.log 2>&1
  sleep 5
  mongo admin --eval "help" > /dev/null 2>&1
  STATUS_COUNT=$?
done
 
# Create the admin user
echo "=> Creating admin user with a password in MongoDB" >> ${LOG_DIR}/create_user.log 2>&1
mongo admin --eval "db.createUser({user: '${MONGODB_ADMIN_USER}', pwd: '${MONGODB_ADMIN_PASS}', roles:[{role:'root',db:'admin'}]});"
if [ $? -eq  0 ]
then
    echo "=> admin user has been created in MongodDB" >> ${LOG_DIR}/create_user.log 2>&1
else
    echo "=> admin user is not created in MongoDB" >> ${LOG_DIR}/create_user.log 2>&1
    exit 1
fi

sleep 10
 
if [ "${MONGODB_APPLICATION_DATABASE}" != "admin" ]; then
    echo "=> Creating an ${MONGODB_APPLICATION_DATABASE} user with a password in MongoDB" >> ${LOG_DIR}/create_user.log 2>&1
    mongo admin -u ${MONGODB_ADMIN_USER} -p ${MONGODB_ADMIN_PASS} << EOF
use ${MONGODB_APPLICATION_DATABASE}
db.createUser({user: '${MONGODB_APPLICATION_USER}', pwd: '${MONGODB_APPLICATION_PASS}', roles:[{role:'readWrite', db:'${MONGODB_APPLICATION_DATABASE}'}]})
EOF
  if [ $? -eq  0 ]
  then
      echo "=> ${MONGODB_APPLICATION_DATABASE} user has been created in MongoDB" >> ${LOG_DIR}/create_user.log 2>&1
  else
      echo "=> ${MONGODB_APPLICATION_DATABASE} user is not created in MongoDB" >> ${LOG_DIR}/create_user.log 2>&1
      exit 1
  fi
fi

sleep 5

 
touch ${DATA_DIR}/.mongodb_password_set
echo "========================================================================" >> ${LOG_DIR}/create_user.log 2>&1