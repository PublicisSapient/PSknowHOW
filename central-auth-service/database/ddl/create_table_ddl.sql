---
--- Create Tables START
---
SET search_path TO auth;

CREATE TABLE IF NOT EXISTS users (
	id serial PRIMARY KEY,
	username VARCHAR(250) UNIQUE NOT NULL,
	saml_email VARCHAR(250),
	password VARCHAR(250),
	first_name VARCHAR(100) NOT NULL,
	last_name VARCHAR(100) NOT NULL,
	display_name VARCHAR(250) NOT NULL,
	email VARCHAR(250) NOT NULL,
	approved BOOLEAN DEFAULT FALSE,
	user_verified BOOLEAN DEFAULT FALSE,
	auth_type VARCHAR(100),
	last_unsuccessful_login_time DATE,
	failed_login_attempt_count INTEGER DEFAULT 0,
	created_date DATE NOT NULL,
	modified_date DATE
);

CREATE TABLE IF NOT EXISTS resource (
	id serial PRIMARY KEY,
	name VARCHAR(50) NOT NULL,
	created_by INTEGER NOT NULL REFERENCES users (id),
	created_date DATE NOT NULL,
	modified_by	 INTEGER REFERENCES users (id),
	modified_date DATE
);

CREATE TABLE IF NOT EXISTS role (
	id serial PRIMARY KEY,
	name VARCHAR(250) NOT NULL,
	description VARCHAR(250),
	root_user BOOLEAN DEFAULT FALSE,
    default_role BOOLEAN DEFAULT FALSE,
	resource_id INTEGER NOT NULL REFERENCES resource (id),
	created_by INTEGER REFERENCES users (id),
	created_date DATE NOT NULL,
	modified_by	 INTEGER REFERENCES users (id),
	modified_date DATE
);

CREATE TABLE IF NOT EXISTS user_token (
	id serial PRIMARY KEY,
	username VARCHAR(50) NOT NULL,
	token VARCHAR,
	expiry_date VARCHAR(100)
);


CREATE TABLE IF NOT EXISTS user_role (
	id serial PRIMARY KEY,
	role_id INTEGER REFERENCES role (id),
	username VARCHAR(50) NOT NULL,
	created_by INTEGER NOT NULL REFERENCES users (id),
	created_date DATE NOT NULL,
	modified_by	 INTEGER REFERENCES users (id),
	modified_date DATE
);

-- Create the 'global_config' table
CREATE TABLE global_config (
    id serial PRIMARY KEY,
    env VARCHAR(255) UNIQUE NOT NULL,
    email_host VARCHAR(255) NOT NULL,
    email_port INT NOT NULL,
    from_email VARCHAR(255) NOT NULL
);

CREATE TABLE forgot_password_token (
    id SERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    expiry_date DATE NOT NULL,
    username VARCHAR(255) NOT NULL
);

CREATE TABLE user_verification_token (
    id SERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP,
    username VARCHAR(255),
    email VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS api_key (
	id serial PRIMARY KEY,
	resource_id INTEGER NOT NULL REFERENCES resource (id),
	key VARCHAR NOT NULL,
	expiry_date DATE,
	created_by INTEGER NOT NULL REFERENCES users (id),
    created_date DATE NOT NULL,
    modified_by	 INTEGER REFERENCES users (id),
    modified_date DATE
);

---
--- END
---


