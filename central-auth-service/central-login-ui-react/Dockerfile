FROM psknowhow/nginx:1.22.1-alpine-slim

ARG REACT_APP_DNS

ENV CONF_LOC="/etc/nginx" \
    # PID_LOC="/run/nginx" \
    HTML_LOC="/var/lib/nginx/" \
    # UI2_LOC="/var/lib/nginx/ui2" \
    # START_SCRIPT_LOC="/etc/init.d" \
    UI2_ASSETS_ARCHIVE="authui.tar" \
    REACT_APP_DNS=${DNS_NAME} \
    # ASSETS_ARCHIVE="*.tar" \
    cert_loc="/etc/ssl/certs"

RUN rm -rf ${CONF_LOC}/nginx.conf ${CONF_LOC}/conf.d/default.conf ${HTML_LOC}index.html
RUN mkdir -p ${PID_LOC}  ${UI2_LOC} /var/www/authui

COPY nginx/nginx_dev.conf /tmp/nginx_dev.conf
COPY nginx/nginx_prod.conf /tmp/nginx_prod.conf
COPY ${UI2_ASSETS_ARCHIVE} ${HTML_LOC}
# COPY nginx/start_nginx.sh ${START_SCRIPT_LOC}/start_nginx.sh
COPY certs/* ${cert_loc}/
# COPY src/assets /var/www/authui/

RUN tar xvf ${HTML_LOC}${UI2_ASSETS_ARCHIVE} -C /var/www/authui
# RUN chmod +x ${START_SCRIPT_LOC}/start_nginx.sh
# RUN rm -f ${HTML_LOC}${ASSETS_ARCHIVE}

EXPOSE 80 443

# Copy .env file and shell script to container
COPY env.sh /var/www/authui/build/
COPY .env /var/www/authui/build/


WORKDIR /var/www/authui/build
# Add bash
RUN apk add --no-cache bash

# Make our shell script executable
RUN chmod +x env.sh

CMD ["./env.sh"]