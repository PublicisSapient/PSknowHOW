#!/bin/bash

ENVIRONMENT=${ENVIRONMENT:-dev} # default to dev you can pass external var to change to prod

if [ "$ENVIRONMENT" = "prod" ]; then
   cp /tmp/nginx_prod.conf ${CONF_LOC}/nginx.conf
else
   cp /tmp/nginx_dev.conf ${CONF_LOC}/nginx.conf
fi

if [ -e $CERT_LOC/knowhow_ssl.key ] || [ "$ENVIRONMENT" = "prod" ]; then
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
    echo "your_password" > $CERT_LOC/knowhow_ssl_passphrase.txt

fi

# Check if the passphrase file exists
if [ ! -e $CERT_LOC/knowhow_ssl_passphrase.txt ]; then
    echo $(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 10) > $CERT_LOC/knowhow_ssl_passphrase.txt
    echo "Passphrase file created"
fi

# Recreate config file
rm -rf ./env-config.js
touch ./env-config.js

# Add assignment 
echo "window.env = {" >> ./env-config.js

# Read each line in .env file
# Each line represents key=value pairs
while read -r line || [[ -n "$line" ]];
do
  # Split env variables by character `=`
  if printf '%s\n' "$line" | grep -q -e '='; then
    varname=$(printf '%s\n' "$line" | sed -e 's/=.*//')
    varvalue=$(printf '%s\n' "$line" | sed -e 's/^[^=]*=//')
  fi

  # Read value of current variable if exists as Environment variable
  value=$(printf '%s\n' "${!varname}")
  # Otherwise use value from .env file
  [[ -z $value ]] && value=${varvalue}
  
  # Append configuration property to JS file
  echo "  $varname: \"$value\"," >> ./env-config.js
done < .env

echo "}" >> ./env-config.js

nginx -g "daemon off;"
/bin/sh