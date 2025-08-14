--liquibase formatted sql

--changeset admin:1
CREATE TABLE users_credentials (
                       user_id SERIAL PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255),
                       refresh_token VARCHAR(1024),
                       role VARCHAR(255) NOT NULL DEFAULT 'USER'


);
--rollback DROP TABLE users;