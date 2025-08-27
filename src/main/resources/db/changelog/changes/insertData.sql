--changeset admin:2
INSERT INTO users_credentials (email, password, role) VALUES ('ivan@example.com', '$2a$10$SOhC3hUoEbyzYg2t90QEE.UWa/pnN/anlNDfpwF5Oqh4wsI1FwEO6','USER');
INSERT INTO users_credentials (email, password, role) VALUES ('maria@example.com', '$2a$10$SOhC3hUoEbyzYg2t90QEE.UWa/pnN/anlNDfpwF5Oqh4wsI1FwEO6','USER');
INSERT INTO users_credentials (email, password, role) VALUES ('oleg@example.com', '$2a$10$SOhC3hUoEbyzYg2t90QEE.UWa/pnN/anlNDfpwF5Oqh4wsI1FwEO6','USER');
INSERT INTO users_credentials (email, password, role) VALUES ('elena@example.com', '$2a$10$SOhC3hUoEbyzYg2t90QEE.UWa/pnN/anlNDfpwF5Oqh4wsI1FwEO6','USER');
INSERT INTO users_credentials (email, password, role) VALUES ('sergey@example.com', '$2a$10$SOhC3hUoEbyzYg2t90QEE.UWa/pnN/anlNDfpwF5Oqh4wsI1FwEO6','USER');

--rollback DELETE FROM users WHERE email IS NOT NULL;