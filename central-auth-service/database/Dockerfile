FROM psknowhow/postgres:13.11-alpine3.18

# Set a non-root user
ARG USER=knowhowuser
ARG UID=1000
ARG GID=1000

RUN ln -sf /bin/bash /bin/sh \ 
    && groupadd -g $GID $USER \
    && useradd -u $UID -g $GID -m -s /bin/bash $USER

ENV POSTGRES_USER="postgres"
ENV PGDATA="/var/lib/postgresql/data"

COPY * /docker-entrypoint-initdb.d/
# COPY dml/ /docker-entrypoint-initdb.d/dml/
# COPY main.sh /docker-entrypoint-initdb.d/

WORKDIR /docker-entrypoint-initdb.d

RUN chown -R $USER:$USER /docker-entrypoint-initdb.d/ \
    && chmod +x main.sh

USER $USER:$GID

EXPOSE 5432
