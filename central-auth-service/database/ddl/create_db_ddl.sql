--- Create database if not exist ---

SELECT 'CREATE DATABASE authNauth'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'authNauth')\gexec

\c authNauth


--- Create schema if not exist ---
CREATE SCHEMA IF NOT EXISTS auth;

SET search_path TO auth;
