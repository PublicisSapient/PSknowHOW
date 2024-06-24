SET search_path TO auth;
ALTER TABLE "users"
ALTER COLUMN last_unsuccessful_login_time TYPE TIMESTAMP USING last_unsuccessful_login_time::TIMESTAMP;