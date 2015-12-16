DROP ALL OBJECTS;

CREATE TABLE ADDRESS (
    id                          int not null IDENTITY,
    street_name                 varchar(100),
    city                        varchar(100) not null,
    version                     int default 0,

    primary key (id)
);

CREATE TABLE ACCOUNT (
    id                          int not null IDENTITY,
    username                    varchar(100) not null,
    last_name                   varchar(255),
    birth_date                  timestamp,
    address_id                  int,
    constraint account_unique_1 unique (username),
    constraint account_fk_1 foreign key (address_id) references ADDRESS,
    primary key (id)
);

-- use negative id to avoid conflict with auto generated ids.
INSERT INTO ADDRESS (id, street_name, city) values(-1, 'Avenue des champs Elysées', 'Paris');
INSERT INTO ADDRESS (id, street_name, city) values(-2, 'Park avenue', 'New-York');
INSERT INTO ADDRESS (id, street_name, city) values(-3, 'Tochomae', 'Tokyo');
INSERT INTO ADDRESS (id, street_name, city) values(-4, 'California Street', 'San Francisco');

-- use negative id to avoid conflict with auto generated ids.
INSERT INTO ACCOUNT (id, username, last_name, birth_date, address_id) VALUES (-1, 'nico', 'Romanetti', {d '1956-12-08'}, -1);
INSERT INTO ACCOUNT (id, username, last_name, birth_date, address_id) VALUES (-2, 'flo',  'Ramimère', {d '1975-08-18'}, -2);
INSERT INTO ACCOUNT (id, username, last_name, birth_date, address_id) VALUES (-3, 'bibi', 'Sock', {d '1979-01-08'}, -3);
INSERT INTO ACCOUNT (id, username, last_name, birth_date, address_id) VALUES (-4, 'jlb',  'Boudart', {d '2012-10-30'}, -4);
INSERT INTO ACCOUNT (id, username, last_name, birth_date, address_id) VALUES (-5, 'mick',  'Jagger', {d '1943-10-30'}, -1);
INSERT INTO ACCOUNT (id, username, last_name, birth_date, address_id) VALUES (-6, 'keith',  'Richards', {d '1943-10-30'}, -2);
INSERT INTO ACCOUNT (id, username, last_name, birth_date, address_id) VALUES (-7, 'charlie',  'Watts', {d '1941-10-30'}, -3);
