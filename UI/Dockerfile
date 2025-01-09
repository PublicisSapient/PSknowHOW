# Use a base image
FROM psknowhow/nginx:1.22.1-alpine-slim

# Create a non-root user
ARG USER=knowhowuser
ARG UID=1000
ARG GID=1000

RUN apk add openssl --no-cache \
    && apk add curl --no-cache \
    && addgroup -g $GID $USER \
    && adduser -u $UID -G $USER -s /bin/sh -D $USER

# Set environment variables
ENV PID_LOC="/run/nginx" \
    CONF_LOC="/etc/nginx" \
    HTML_LOC="/var/lib/nginx/" \
    UI2_LOC="/var/lib/nginx/ui2" \
    START_SCRIPT_LOC="/etc/init.d" \
    UI2_ASSETS_ARCHIVE="ui2.tar" \
    ERRORPAGE_ASSETS_ARCHIVE="ErrorPage.tar" \
    ASSETS_ARCHIVE="*.tar" \
    CERT_LOC="/etc/ssl/certs" \
    ENVIRONMENT="dev"

# Create necessary directories
RUN mkdir -p ${PID_LOC} ${UI2_LOC}
RUN rm -f ${CONF_LOC}/nginx.conf ${CONF_LOC}/conf.d/default.conf ${HTML_LOC}index.html

# Copy files
COPY nginx/files/nginx-dev.conf /tmp/nginx_dev.conf
COPY nginx/files/nginx-prod.conf /tmp/nginx_prod.conf
COPY nginx/files/${ASSETS_ARCHIVE} ${HTML_LOC}
COPY nginx/scripts/start_nginx.sh ${START_SCRIPT_LOC}/start_nginx.sh
COPY nginx/files/certs/* ${CERT_LOC}/

# Extract assets
RUN tar xvf ${HTML_LOC}${UI2_ASSETS_ARCHIVE} -C ${UI2_LOC} && tar xvf ${HTML_LOC}${ERRORPAGE_ASSETS_ARCHIVE} -C ${UI2_LOC} \
    && chmod +x ${START_SCRIPT_LOC}/start_nginx.sh && rm -f ${HTML_LOC}${ASSETS_ARCHIVE}

# granting permission's
RUN chown -R $USER:$USER ${CONF_LOC} \
    && chown -R $USER:$USER ${CERT_LOC} \
    && find /var -path /var/run/secrets -prune -o -exec chown $USER:$USER {} + \
    && find /run -path /run/secrets -prune -o -exec chown -R $USER:$USER {} + \
    && apk add --no-cache libcap \
    && setcap 'cap_net_bind_service=+ep' /usr/sbin/nginx

# Expose ports
EXPOSE 80 443

# Switch to the non-root user
USER $USER:$GID

# Entrypoint command
ENTRYPOINT ["/etc/init.d/start_nginx.sh"]
