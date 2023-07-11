FROM mongo:5.0.18

ENV AUTH="yes" \
    JOURNALING="yes" \
    DATA_DIR="/data/db" \
    LOG_DIR="/data/logs" \
    MONGODB_HOME="/data" \
    STORAGE_ENGINE="wiredTiger" \
    MONGODB_PACKAGE_NAME="mongodb mongodb-tools" \
    CRONTAB_DIR="/bin/bbsuid" \
    CONF="/etc/"

COPY scripts/* /docker-entrypoint-initdb.d/

RUN \
    mkdir -p /data/logs && touch /data/logs/create_user.log && chmod 777 /data/logs/create_user.log && apt update &&  apt install netcat -y && apt-get install cron -y && \
    \
    apt-get install dos2unix && chmod +x /docker-entrypoint-initdb.d/mongodb_start.sh /docker-entrypoint-initdb.d/create_db_user.sh && dos2unix /docker-entrypoint-initdb.d/mongodb_start.sh && dos2unix /docker-entrypoint-initdb.d/create_db_user.sh && \
    \
    rm -rf /var/cache/apk/*

EXPOSE 27017
WORKDIR /docker-entrypoint-initdb.d/
CMD ./mongodb_start.sh
