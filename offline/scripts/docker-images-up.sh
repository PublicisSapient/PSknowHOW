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

RED=`tput setaf 1`
GREEN=`tput setaf 2`
RESET=`tput sgr0`
YELLOW=`tput setaf 3`
CYAN=`tput setaf 6`
BLANK=`echo`

TAR_DIR=$(dirname `readlink -f "$0"`)/
TAR_DIR1=/app/apps
COMPOSE_FILE=/app/apps/docker-compose.yaml
DOCKER_HOST_IP_ADDRESS=$(ip -o route get to 8.8.8.8 | sed -n 's/.*src \([0-9.]\+\).*/\1/p')

##############################################
##### function to load the docker images #####
##############################################
function extract_tar()
{
echo -e "${GREEN}[\xE2\x9C\x94] extract Docker Image from tar.gz files ${RESET}"
 find ${TAR_DIR} -type f -name "dockers.zip" | while read TAR; do
  unzip ${TAR} -c ${TAR_DIR1} | tar -xf -
    if [ $? -eq  0 ]; then
        echo -e "    ${GREEN}[\xE2\x9C\x94] extracted Docker image from ${TAR_DIR},${CYAN} ${RESET}"
        #rm -rf ${DOCKER_IMAGE}
    else
        echo -e "    ${RED}[\xE2\x9D\x8C] Failed to extract docker image from tar, please check that tar is present or not ${TAR_DIR} ${RESET}"
        exit 1
    fi
 done
}



##############################################
##### function to load the docker images #####
##############################################
function extract_zip()
{
echo -e "${GREEN}[\xE2\x9C\x94] extract Docker Image from zip files ${RESET}"
 find /KnowHOW6.0.0 -type f -name "dockers.zip" | while read zip; do
  unzip ${zip} -d ${TAR_DIR}
    if [ $? -eq  0 ]; then
        echo -e "    ${GREEN}[\xE2\x9C\x94] extracted Docker image from ${TAR_DIR},${CYAN} ${RESET}"
        #rm -rf ${DOCKER_IMAGE}
    else
        echo -e "    ${RED}[\xE2\x9D\x8C] Failed to extract docker image from zip, please check that zip is present or not ${TAR_DIR} ${RESET}"
        exit 1
    fi
 done
}



##############################################
##### function to load the docker images #####
##############################################
function load_docker_images()
{
echo -e "${GREEN}[\xE2\x9C\x94] Loading Docker Image from tar.gz files ${RESET}"
find ${TAR_DIR} -type f -name "*.zip" | while read DOCKER_IMAGE; do
 echo -e "\n    ${CYAN}[\xE2\x9C\x94] Loading ${DOCKER_IMAGE} as Docker Image ${RESET}"
docker load -i ${DOCKER_IMAGE}
echo "image ${DOCKER_IMAGE}"
#> /dev/null 2>&1
    if [ $? -eq  0 ]; then
        echo -e "    ${GREEN}[\xE2\x9C\x94] Docker image loaded from ${DOCKER_IMAGE},${CYAN} Removing the tar.gz for it ${RESET}"
        #rm -rf ${DOCKER_IMAGE}
    else
        echo -e "    ${RED}[\xE2\x9D\x8C] Failed to load docker image from tar ball, please check that tar is present or not ${DOCKER_IMAGE} ${RESET}"
        exit 1
    fi
done
}

##############################################
##### function to copy docker compose file #####
##############################################
function copy_docker_compose()
{
echo -e "${GREEN}[\xE2\x9C\x94] Coping docker-compose file ${RESET}"
cp ${TAR_DIR}/../docker-compose.yaml /app/apps/
sed -i "s/10.207.16.213/${DOCKER_HOST_IP_ADDRESS}/g" $COMPOSE_FILE
echo -e "${GREEN}[\xE2\x9C\x94] Changing IP in Docker compose file ${RESET}"

}

####################################################
##### Function to launch the tech stack #####
####################################################
function compose_up()
{
   #chown -R ${USER}:${GROUP} $PROJECT_NAME $PORTAINER_DIR
   docker-compose -f $COMPOSE_FILE up -d
   if [ $? -ne 0 ]; then
     echo -e "${RED}[\xE2\x9D\x8C] failed to launch docker stack for project ${PROJECT_NAME} ${RESET}"
     exit 1
   fi
   echo -e "${GREEN}[\xE2\x9C\x94] docker stack launched ${RESET}"
   echo "${CYAN}==> docker stack for project $PROJECT_NAME"
   echo "${YELLOW}"
   docker-compose -f $COMPOSE_FILE ps
   echo "${RESET}"
}

extract_zip
#extract_tar
load_docker_images

copy_docker_compose
compose_up
