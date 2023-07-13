FROM psknowhow/nginx:1.22.1-alpine-slim

ENV PID_LOC="/run/nginx" \
    CONF_LOG="/etc/nginx/conf.d" \
    HTML_LOC="/var/lib/nginx/" \
    UI2_LOC="/var/lib/nginx/ui2" \
    START_SCRIPT_LOC="/etc/init.d" \
    UI2_ASSETS_ARCHIVE="ui2.tar" \
    ERRORPAGE_ASSETS_ARCHIVE="ErrorPage.tar" \
    ASSETS_ARCHIVE="*.tar" \
    CERT_LOC="/etc/ssl/certs"

RUN mkdir -p ${PID_LOC}  ${UI2_LOC} && rm -f ${CONF_LOG}/default.conf ${HTML_LOC}index.html && apk add openssl

COPY nginx/files/ui2.conf ${CONF_LOG}/ui2.conf
COPY nginx/files/${ASSETS_ARCHIVE} ${HTML_LOC}
COPY nginx/scripts/start_nginx.sh ${START_SCRIPT_LOC}/start_nginx.sh
COPY nginx/files/certs/* ${CERT_LOC}/

RUN tar xvf ${HTML_LOC}${UI2_ASSETS_ARCHIVE} -C ${UI2_LOC} && tar xvf ${HTML_LOC}${ERRORPAGE_ASSETS_ARCHIVE} -C ${UI2_LOC}
RUN chmod +x ${START_SCRIPT_LOC}/start_nginx.sh && rm -f ${HTML_LOC}${ASSETS_ARCHIVE}
EXPOSE 80 443

ENTRYPOINT ["/etc/init.d/start_nginx.sh"]
