# --- !Ups

create table my_table
(
    STRING_VALUE    VARCHAR(50),
    DATE_VALUE      DATE,
    CHAR_VALUE      CHAR,
    NUMBER_VALUE	NUMBER(15,3),
    TIMESTAMP_VALUE TIMESTAMP(6) default CURRENT_TIMESTAMP
);
GRANT SELECT, INSERT, UPDATE, DELETE ON my_table TO PUBLIC;

insert into app values('MY_TESTING', 'testing desc',  '{  "isAllowedRestDownload" : true, "isAllowedRestUpload": true}');
insert into tab values('MY_TESTING', 'MY_TABLE', 'public');

insert into col values('MY_TESTING', 'MY_TABLE', 'STRING_VALUE',   'N', 'Y', 'S', null);
insert into col values('MY_TESTING', 'MY_TABLE', 'DATE_VALUE',     'N', 'Y', 'D', null);
insert into col values('MY_TESTING', 'MY_TABLE', 'CHAR_VALUE',     'N', 'Y', 'S', null);
insert into col values('MY_TESTING', 'MY_TABLE', 'NUMBER_VALUE',   'N', 'Y', 'N', null);
insert into col values('MY_TESTING', 'MY_TABLE', 'TIMESTAMP_VALUE','N', 'Y', 'D', null);

insert into my_table values ('testing string value 1', TO_DATE('2023-01-15 00:00:00', 'YYYY-MM-DD HH:MI:SS'), 'Y', 1234.56789, CURRENT_TIMESTAMP);
insert into my_table values ('another string value 2', null, null, 1, null);
insert into my_table values (null, null, null, null, null);

commit ;

# --- !Downs

delete from app where APP_NAME = 'MY_TESTING';
delete from tab where APP_NAME = 'MY_TESTING';
delete from col where APP_NAME = 'MY_TESTING';

DROP TABLE my_table;
commit;