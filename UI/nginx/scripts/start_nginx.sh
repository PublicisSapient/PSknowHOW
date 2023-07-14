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
if [ -e /etc/ssl/certs/knowhow.key ]
then
    echo "SSL certificate copyied from Host or already present "
else
    openssl req -newkey rsa:4096 \
            -x509 \
            -sha256 \
            -days 3650 \
            -nodes \
            -out /etc/ssl/certs/knowhow.cer \
            -keyout /etc/ssl/certs/knowhow.key \
            -subj "/C=IN/ST=HR/L=ggn/O=Security/OU=IT Department/CN=${DNS_SSL}"
    echo "Self sign certificate created "
fi
API_HOST=${API_HOST:-api}
API_PORT=${API_PORT:-8080}

sed -i "s/API_HOST/${API_HOST}/g" ${CONF_LOG}/ui2.conf
sed -i "s/API_PORT/${API_PORT}/g" ${CONF_LOG}/ui2.conf
envsubst < /var/lib/nginx/ui2/assets/env.template.json > /var/lib/nginx/ui2/assets/env.json 
nginx -g "daemon off;"
/bin/sh
