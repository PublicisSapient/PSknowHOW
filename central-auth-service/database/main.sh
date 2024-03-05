#!/bin/bash

DIR_PATH="/docker-entrypoint-initdb.d/"

until pg_isready -h localhost -U $POSTGRES_USER
do
  echo "Waiting for postgres to start accepting connection "
  sleep 2;
done

echo "############ Create database and schema if not exist `date` ###########"
psql -v ON_ERROR_STOP=0 -h localhost --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -f $DIR_PATH"ddl/create_db_ddl.sql"

echo "############ Create index if not exist `date` ###########"
psql -v ON_ERROR_STOP=0 -h localhost --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -f $DIR_PATH"ddl/create_index_ddl.sql"

echo "############ Create table if not exist `date` ###########"
psql -v ON_ERROR_STOP=0 -h localhost --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -f $DIR_PATH"ddl/create_table_ddl.sql"

echo "############ insert master data `date` ###########"
psql -v ON_ERROR_STOP=0 -h localhost --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -f $DIR_PATH"dml/insert_master_data.sql"
