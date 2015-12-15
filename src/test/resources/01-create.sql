DROP ALL OBJECTS;

CREATE TABLE ACCOUNT (
    id                          char(36) not null,
    username                    varchar(100) not null,
    birth_date                  timestamp,
    constraint account_unique_1 unique (username),
    primary key (id)
);

INSERT INTO ACCOUNT (id, username, birth_date) VALUES ('000000000000000000000000000000000001', 'nico',{d '1956-12-08'});
INSERT INTO ACCOUNT (id, username, birth_date) VALUES ('000000000000000000000000000000000002', 'flo',{d '1975-08-18'});
INSERT INTO ACCOUNT (id, username, birth_date) VALUES ('000000000000000000000000000000000003', 'bibi',{d '1979-01-08'});
INSERT INTO ACCOUNT (id, username, birth_date) VALUES ('000000000000000000000000000000000004', 'jlb',{d '2012-10-30'});
