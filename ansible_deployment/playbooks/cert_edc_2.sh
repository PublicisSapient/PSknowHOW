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

# This is LDAP certificate for EDC server . "$1" is a external veriable passed from jenkins
sudo docker cp /app/apps/certs/jssecacerts.cer customapi:/app &&
sudo docker exec -i customapi /bin/sh -c "keytool -importcert -alias $1-1 -keystore /usr/local/openjdk-8/lib/security/cacerts -storepass changeit -file jssecacerts.cer"
