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

API_HOST=${API_HOST:-customapi}
API_PORT=${API_PORT:-8080}
# Determine the environment (dev or prod) based on an environment variable
ENVIRONMENT=${ENVIRONMENT:-dev} # default to dev you can pass external var to change to prod

if [ "$ENVIRONMENT" = "prod" ]; then
   cp /tmp/nginx_prod.conf ${CONF_LOC}/nginx_prod.conf
   sed -i "s/API_HOST/${API_HOST}/g" ${CONF_LOC}/nginx_prod.conf
   sed -i "s/API_PORT/${API_PORT}/g" ${CONF_LOC}/nginx_prod.conf
else
   cp /tmp/nginx_dev.conf ${CONF_LOC}/nginx_dev.conf
   sed -i "s/API_HOST/${API_HOST}/g" ${CONF_LOC}/nginx_dev.conf
   sed -i "s/API_PORT/${API_PORT}/g" ${CONF_LOC}/nginx_dev.conf
fi

if [ -e /etc/ssl/certs/knowhow_ssl.key ] || [ "$ENVIRONMENT" = "prod" ]; then
    echo "SSL certificate already exist in host or managed externally. "
else
    openssl req -newkey rsa:4096 \
            -x509 \
            -sha256 \
            -days 3650 \
            -nodes \
            -out /etc/ssl/certs/knowhow_ssl.cer \
            -keyout /etc/ssl/certs/knowhow_ssl.key \
            -subj "/C=IN/ST=HR/L=ggn/O=Security/OU=IT Department/CN=${DNS_SSL}"
    echo "Welcome@123" > /etc/ssl/certs/knowhow_ssl_passphrase.txt
    echo "Self-signed certificate created"
fi

envsubst < /var/lib/nginx/ui2/assets/env.template.json > /var/lib/nginx/ui2/assets/env.json 
nginx -g "daemon off;"
