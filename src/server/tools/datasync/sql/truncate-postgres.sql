-- Truncates the data from the archive tables and the original OLTP tables 
-- Please run this as a part of off-line database maintenance after a sync is 
-- run and a backup is done
-- This deletes all synchronized data from the OLTP tables and truncates all
-- data from the archive tables

-- clean Original tables

delete from obligation_log where sync_done = true;
delete from policy_custom_attr where policy_log_id in
(select id from policy_activity_log where sync_done = true);
delete from policy_activity_log where sync_done = true;

delete from tracking_custom_attr where tracking_log_id in
(select id from tracking_activity_log where sync_done = true);
delete from tracking_activity_log where sync_done = true;

-- truncate archive tables

ALTER TABLE apa_log_attr DROP CONSTRAINT fkdee1c92f81e153a3;

truncate table archive_obligation_log;
truncate table apa_log_attr;
truncate table apa_log;

ALTER TABLE apa_log_attr
  ADD CONSTRAINT fkdee1c92f81e153a3 FOREIGN KEY (policy_log_id)
      REFERENCES apa_log (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE archive_tracking_custom_attr DROP CONSTRAINT fk69d663f4f4470a7e;

truncate table archive_tracking_custom_attr;
truncate table archive_tracking_activity_log;

ALTER TABLE archive_tracking_custom_attr
  ADD CONSTRAINT fk69d663f4f4470a7e FOREIGN KEY (tracking_log_id)
      REFERENCES archive_tracking_activity_log (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;