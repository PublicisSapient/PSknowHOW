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
# Determine the PROTOCOL (http or https) based on an environment variable
PROTOCOL=${PROTOCOL:-http} # default to https you can pass external var to change to http

if [ "$PROTOCOL" = "http" ]; then
   cp /tmp/nginx_http.conf ${CONF_LOC}/nginx.conf
   sed -i "s/API_HOST/${API_HOST}/g" ${CONF_LOC}/nginx.conf
   sed -i "s/API_PORT/${API_PORT}/g" ${CONF_LOC}/nginx.conf
else
   cp /tmp/nginx_https.conf ${CONF_LOC}/nginx.conf
   sed -i "s/API_HOST/${API_HOST}/g" ${CONF_LOC}/nginx.conf
   sed -i "s/API_PORT/${API_PORT}/g" ${CONF_LOC}/nginx.conf
fi

if [ -e $CERT_LOC/knowhow_ssl.key ] || [ "$PROTOCOL" = "http" ]; then
    echo "SSL certificate already exist in host or managed externally. "
else
    openssl req -newkey rsa:4096 \
            -x509 \
            -sha256 \
            -days 3650 \
            -nodes \
            -out $CERT_LOC/knowhow_ssl.cer \
            -keyout $CERT_LOC/knowhow_ssl.key \
            -subj "/C=IN/ST=HR/L=ggn/O=Security/OU=IT Department/CN=${DNS_SSL}"
    echo "Self-signed certificate created"
fi
# Check if the passphrase file exists
if [ ! -e $CERT_LOC/knowhow_ssl_passphrase.txt ]; then
    echo $(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 10) > $CERT_LOC/knowhow_ssl_passphrase.txt
    echo "Passphrase file created"
fi

envsubst < /var/lib/nginx/ui2/assets/env.template.json > /var/lib/nginx/ui2/assets/env.json 
nginx -g "daemon off;"
