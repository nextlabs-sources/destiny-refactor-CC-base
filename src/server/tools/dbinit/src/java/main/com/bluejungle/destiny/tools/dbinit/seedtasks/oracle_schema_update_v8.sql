alter table APPLICATION_USER add created_date date;
alter table APPLICATION_USER add last_updated_by number(19,0);
alter table APPLICATION_USER add last_updated date;
alter table APPLICATION_USER add created_by number(19,0);
alter table APPLICATION_USER add displayname varchar2(255);
alter table APPLICATION_USER add hide_splash number(1,0);
alter table APPLICATION_USER add last_logged_time number(19,0);
alter table APPLICATION_USER add auth_handler_id number(19,0);
alter table APPLICATION_USER add email varchar2(255);
alter table DEVELOPMENT_ENTITIES add extended_desc clob;
alter table SUPER_APPLICATION_USER add created_date date;
alter table SUPER_APPLICATION_USER add last_updated_by number(19,0);
alter table SUPER_APPLICATION_USER add last_updated date;
alter table SUPER_APPLICATION_USER add created_by number(19,0);
alter table SUPER_APPLICATION_USER add displayname varchar2(255);
alter table SUPER_APPLICATION_USER add hide_splash number(1,0);
alter table SUPER_APPLICATION_USER add last_logged_time number(19,0);
alter table SUPER_APPLICATION_USER add email varchar2(255);
create table APP_USER_PROPERTIES (id number(19,0) not null,created_date date,last_updated_by number(19,0),last_updated date,created_by number(19,0),version number(10,0) not null,data_type varchar2(255),prop_key varchar2(255),super_user_id number(19,0),user_id number(19,0),prop_value varchar2(255),primary key (id));
create table AUTH_HANDLER_REGISTRY (id number(19,0) not null,created_date date,last_updated_by number(19,0),last_updated date,created_by number(19,0),version number(10,0) not null,account_id varchar2(255),config_data_json clob,name varchar2(255),type varchar2(255),user_attrs_json clob,primary key (id));
create table AUDIT_LOGS (id number(19,0) not null,created_date date,last_updated_by number(19,0),last_updated date,created_by number(19,0),version number(10,0) not null,component varchar2(1000),hidden number(1,0),msgCode varchar2(255),msgParams varchar2(1000),primary key (id));
create table DEVELOPMENT_ENTITIES_TAGS (dev_entity_id number(19,0) not null,tag_id number(19,0) not null,primary key (dev_entity_id, tag_id));
create table OPERATOR_CONFIG (id number(19,0) not null,created_date date,last_updated_by number(19,0),last_updated date,created_by number(19,0),version number(10,0) not null,data_type varchar2(255),operator_key varchar2(255),label varchar2(255),primary key (id));
create table PM_ACTION_CONFIG (id number(19,0) not null,created_date date,last_updated_by number(19,0),last_updated date,created_by number(19,0),version number(10,0) not null,name varchar2(255),short_code varchar2(2),short_name varchar2(50),plcy_model_id number(19,0),primary key (id));
create table PM_ATTRIBUTE_CONFIG (id number(19,0) not null,created_date date,last_updated_by number(19,0),last_updated date,created_by number(19,0),version number(10,0) not null,data_type varchar2(255),name varchar2(255),reg_ex_pattern varchar2(255),short_name varchar2(50),policy_model_id number(19,0),plcy_model_id number(19,0),primary key (id));
create table PM_ATTRIB_CONFIG_OPER_CONFIG (attribute_id number(19,0) not null,operator_id number(19,0) not null,primary key (attribute_id, operator_id));
create table PM_OBLIGATION_CONFIG (id number(19,0) not null,created_date date,last_updated_by number(19,0),last_updated date,created_by number(19,0),version number(10,0) not null,name varchar2(255),run_at varchar2(255),short_name varchar2(50),plcy_model_id number(19,0),primary key (id));
create table PM_PARAMETER_CONFIG (id number(19,0) not null,created_date date,last_updated_by number(19,0),last_updated date,created_by number(19,0),version number(10,0) not null,default_value varchar2(1000),is_editable number(1,0),is_hidden number(1,0),list_values varchar2(1000),is_mandatory number(1,0),name varchar2(255),short_name varchar2(50),data_type varchar2(255),obligation_id number(19,0),primary key (id));
create table POLICY_MODEL (discriminator varchar2(31) not null,id number(19,0) not null,created_date date,last_updated_by number(19,0),last_updated date,created_by number(19,0),version number(10,0) not null,description varchar2(4000),name varchar2(264),short_name varchar2(50),status varchar2(255),type varchar2(255),primary key (id));
create table POLICY_MODEL_TAGS (plcy_model_id number(19,0) not null,tag_id number(19,0) not null,primary key (plcy_model_id, tag_id));
create table SAVED_SEARCH (id number(19,0) not null,created_date date,last_updated_by number(19,0),last_updated date,created_by number(19,0),version number(10,0) not null,criteria_json long,description varchar2(255),lowercase_name varchar2(255),name varchar2(255),shared_mode varchar2(255),status varchar2(255),type varchar2(255),primary key (id));
create table TAG_LABELS (id number(19,0) not null,created_date date,last_updated_by number(19,0),last_updated date,created_by number(19,0),version number(10,0) not null,hidden number(1,0),tag_key varchar2(255),label varchar2(255),status varchar2(255),type varchar2(255),primary key (id));
alter table APP_USER_PROPERTIES add constraint FK87A05FE9B53FEACB foreign key (super_user_id) references SUPER_APPLICATION_USER;
alter table APP_USER_PROPERTIES add constraint FK87A05FE9A759120A foreign key (user_id) references APPLICATION_USER;
alter table DEVELOPMENT_ENTITIES_TAGS add constraint FKDB1ED5B379C3E7F9 foreign key (dev_entity_id) references DEVELOPMENT_ENTITIES;
alter table DEVELOPMENT_ENTITIES_TAGS add constraint FKDB1ED5B3E3AB049A foreign key (tag_id) references TAG_LABELS;
alter table PM_ACTION_CONFIG add constraint FKEC9D56A94476BBF5 foreign key (plcy_model_id) references POLICY_MODEL;
alter table PM_ATTRIBUTE_CONFIG add constraint FK85828674476BBF5 foreign key (plcy_model_id) references POLICY_MODEL;
alter table PM_ATTRIBUTE_CONFIG add constraint FK8582867B27E9A35 foreign key (policy_model_id) references POLICY_MODEL;
alter table PM_ATTRIB_CONFIG_OPER_CONFIG add constraint FK2AB580A9FC12A4DC foreign key (operator_id) references OPERATOR_CONFIG;
alter table PM_ATTRIB_CONFIG_OPER_CONFIG add constraint FK2AB580A965AE0D5C foreign key (attribute_id) references PM_ATTRIBUTE_CONFIG;
alter table PM_OBLIGATION_CONFIG add constraint FK70C823614476BBF5 foreign key (plcy_model_id) references POLICY_MODEL;
alter table PM_PARAMETER_CONFIG add constraint FK79C446BAFD78E31C foreign key (obligation_id) references PM_OBLIGATION_CONFIG;
alter table POLICY_MODEL_TAGS add constraint FKDC8BC8BC4476BBF5 foreign key (plcy_model_id) references POLICY_MODEL;
alter table POLICY_MODEL_TAGS add constraint FKDC8BC8BCE3AB049A foreign key (tag_id) references TAG_LABELS;
update OPERATOR_CONFIG set LABEL = '=' where DATA_TYPE= 'MULTIVAL' and OPERATOR_KEY = 'equals_unordered';
alter table AUDIT_LOGS modify component varchar2(1000);
alter table AUDIT_LOGS modify msgParams varchar2(3000);