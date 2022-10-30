ALTER TABLE SUPER_APPLICATION_USER ADD INITLOGIN_DONE CHAR(1) DEFAULT 'N';
ALTER TABLE APPLICATION_USER ADD INITLOGIN_DONE CHAR(1) DEFAULT 'N';


ALTER TABLE pm_parameter_config ADD ( list_values_copy CLOB);
UPDATE pm_parameter_config SET list_values_copy=list_values;
COMMIT;
ALTER TABLE pm_parameter_config DROP COLUMN list_values;
ALTER TABLE pm_parameter_config RENAME COLUMN list_values_copy TO list_values;