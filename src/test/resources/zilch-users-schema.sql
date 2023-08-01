CREATE TABLE zilch_user (
    user_id BIGINT PRIMARY KEY,
    user_name VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(256) NOT NULL,
    creation_date_time TIMESTAMP,
    version BIGINT NOT NULL
);

CREATE TABLE zilch_role (
  role_id INTEGER PRIMARY KEY,
  name VARCHAR(20) UNIQUE NOT NULL
);

CREATE TABLE zilch_user_roles (
  user_id BIGINT NOT NULL,
  role_id INTEGER NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT ROLE_FK FOREIGN KEY (role_id) REFERENCES zilch_role (role_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT USER_FK FOREIGN KEY (user_id) REFERENCES zilch_user (user_id) ON DELETE CASCADE ON UPDATE CASCADE
);