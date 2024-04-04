FROM psknowhow/postgres:13.11-alpine3.18

# Set a non-root user
ARG USER=knowhowuser
ARG UID=1000
ARG GID=1000

RUN apk --no-cache add shadow \
    && groupadd -g $GID $USER \
    && useradd -u $UID -g $GID -m -s /bin/bash $USER

ENV POSTGRES_USER="postgres"
ENV PGDATA="/var/lib/postgresql/data"

COPY ../database /docker-entrypoint-initdb.d/
RUN ls -ltr /docker-entrypoint-initdb.d/
# COPY dml/ /docker-entrypoint-initdb.d/dml/
# COPY main.sh /docker-entrypoint-initdb.d/

WORKDIR /docker-entrypoint-initdb.d

RUN chown -R $USER:$USER /docker-entrypoint-initdb.d/ \
    && chown -R $USER:$USER $PGDATA \
    && chmod +x /docker-entrypoint-initdb.d/main.sh

USER $USER:$GID

EXPOSE 5432
