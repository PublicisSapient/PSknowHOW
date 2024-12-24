FROM mongo:5.0.18

LABEL version "VERSION"
LABEL description "Docker image of dashboard mongo database"
LABEL keywords "infra-devops_mongodb"
LABEL team "infra-devops"
LABEL type "mongodb/VERSION"

ENV AUTH="yes" \
    JOURNALING="yes" \
    DATA_DIR="/data/db" \
    LOG_DIR="/data/logs" \
    MONGODB_HOME="/data" \
    STORAGE_ENGINE="wiredTiger" \
    MONGODB_PACKAGE_NAME="mongodb mongodb-tools" \
    CRONTAB_DIR="/bin/bbsuid" \
    CONF="/etc/"

# Set the appropriate permissions for MongoDB directories and scripts
RUN mkdir -p /data/db /data/logs /docker-entrypoint-initdb.d && \
    chown -R mongodb:mongodb /data /docker-entrypoint-initdb.d && \
    chmod 777 /data/logs && \
    touch /data/logs/create_user.log && \
    chmod 777 /data/logs/create_user.log

# Copy initialization scripts
COPY scripts/* /docker-entrypoint-initdb.d/

# Install necessary packages
RUN apt update && apt install -y \
    netcat \
    cron \
    dos2unix && \
    chmod +x /docker-entrypoint-initdb.d/mongodb_start.sh /docker-entrypoint-initdb.d/create_db_user.sh && \
    dos2unix /docker-entrypoint-initdb.d/mongodb_start.sh /docker-entrypoint-initdb.d/create_db_user.sh && \
    rm -rf /var/cache/apk/*

# Expose MongoDB port
EXPOSE 27017

# Set the working directory
WORKDIR /docker-entrypoint-initdb.d/

# Switch to the non-root user
USER mongodb

# Start the MongoDB process with the script as the non-root user
CMD ["./mongodb_start.sh"]
