FROM mongo:5.0.18

ENV MONGO_INITDB_ROOT_USERNAME="<your username>" \
    MONGO_INITDB_ROOT_PASSWORD="<your pass>"

ARG USER=knowhowuser
ARG UID=1000
ARG GID=1000

RUN groupadd -g $GID $USER \
    && useradd -u $UID -g $GID -m -s /bin/bash $USER

USER $USER:$GID

# COPY scripts/* /docker-entrypoint-initdb.d/

# RUN \
#     mkdir -p /data/logs && touch /data/logs/create_user.log && chmod 777 /data/logs/create_user.log && apt update &&  apt install netcat -y && apt-get install cron -y && \
#     \
#     apt-get install dos2unix && chmod +x /docker-entrypoint-initdb.d/mongodb_start.sh /docker-entrypoint-initdb.d/create_db_user.sh && dos2unix /docker-entrypoint-initdb.d/mongodb_start.sh && dos2unix /docker-entrypoint-initdb.d/create_db_user.sh && \
#     \
#     rm -rf /var/cache/apk/*

# EXPOSE 27017
# WORKDIR /docker-entrypoint-initdb.d/
# CMD ./mongodb_start.sh
