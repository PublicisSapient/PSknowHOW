FROM psknowhow/postgres:13.11-alpine3.18

ENV POSTGRES_USER="postgres"
ENV PGDATA="/var/lib/postgresql/data"

COPY ddl/ /docker-entrypoint-initdb.d/ddl/
COPY dml/ /docker-entrypoint-initdb.d/dml/
COPY main.sh /docker-entrypoint-initdb.d/

WORKDIR /docker-entrypoint-initdb.d

RUN chmod -R 777 /docker-entrypoint-initdb.d/

EXPOSE 5432
