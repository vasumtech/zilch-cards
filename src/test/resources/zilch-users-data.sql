
INSERT INTO zilch_role (role_id, name) VALUES (1, 'ROLE_USER');
INSERT INTO zilch_role (role_id, name) VALUES (2, 'ROLE_ADMIN');

INSERT INTO zilch_user (user_id, user_name, password, creation_date_time, version) VALUES(1, 'zilchtestuser',
'$2a$10$G/3hULpEcDHGEY2ZqIYNA.ykZZpNJNP/x0Y6AfqfBjF7TZKT1wh42', CURRENT_TIMESTAMP ,1);

INSERT INTO zilch_user_roles(user_id, role_id) VALUES(1, 1);

