# --- !Ups

create table app
(
    APP_NAME            VARCHAR(20),
    APP_DESC            VARCHAR(200),
    SETTINGS            VARCHAR(4000) default NULL
);
insert into app values('MAIN','desc', '{  "isAllowedRestDownload" : true, "isAllowedRestUpload": false }');
GRANT SELECT, INSERT, UPDATE, DELETE ON app TO PUBLIC;


create table tab
(
    APP_NAME            VARCHAR(20),
    TBL_NAME            VARCHAR(61),
    TBL_SCHEMA          VARCHAR(50)
);
GRANT SELECT, INSERT, UPDATE, DELETE ON tab TO PUBLIC;

insert into tab values('MAIN', 'TAB', 'public');
insert into tab values('MAIN', 'APP', 'public');
insert into tab values('MAIN', 'COL', 'public');


create table col
(
    APP_NAME                  VARCHAR(20),
    TBL_NAME                  VARCHAR(60),
    COL_NAME                  VARCHAR(30),
    COL_PK                    CHAR,
    COL_REQUIRED              CHAR,
    COL_DATATYPE              CHAR,
    COL_FORMAT                VARCHAR(50)
);
GRANT SELECT, INSERT, UPDATE, DELETE ON col TO PUBLIC;

insert into col values('MAIN', 'APP', 'APP_NAME', 'Y', 'Y', 'S', null);
insert into col values('MAIN', 'APP', 'SETTINGS', 'N', 'N', 'S', null);

insert into col values('MAIN', 'TAB', 'APP_NAME',   'Y', 'Y', 'S', null);
insert into col values('MAIN', 'TAB', 'TBL_NAME',   'Y', 'Y', 'S', null);
insert into col values('MAIN', 'TAB', 'TBL_SCHEMA', 'Y', 'N', 'S', null);


insert into col values('MAIN', 'COL', 'APP_NAME',        'Y', 'Y', 'S', null);
insert into col values('MAIN', 'COL', 'TBL_NAME',        'Y', 'Y', 'S', null);
insert into col values('MAIN', 'COL', 'COL_NAME',        'Y', 'Y', 'S', null);
insert into col values('MAIN', 'COL', 'COL_FORMAT',      'N', 'N', 'S', null);
insert into col values('MAIN', 'COL', 'COL_PK',          'N', 'Y', 'S', null);
insert into col values('MAIN', 'COL', 'COL_DATATYPE',    'N', 'Y', 'S', null);
insert into col values('MAIN', 'COL', 'COL_DEFAULT_VAL', 'N', 'N', 'S', null);

commit;

# --- !Downs

DROP TABLE app;
DROP TABLE tab;
DROP TABLE col;