CREATE TABLE card_company (
    company_id INTEGER PRIMARY KEY,
    company_name VARCHAR(30) NOT NULL
);

CREATE TABLE CARD (
    user_id BIGINT PRIMARY KEY,
    company_id INTEGER NOT NULL,
    card_number VARCHAR(256) NOT NULL,
    valid_from date NOT NULL,
    valid_upto date NOT NULL,
    title VARCHAR(5) NOT NULL,
    name_on_card VARCHAR(50) NOT NULL,
    pin VARCHAR(256) NOT NULL,
    creation_date_time timestamp NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT company_id_fk FOREIGN KEY (company_id) REFERENCES card_company (company_id) ON DELETE CASCADE ON UPDATE CASCADE
);