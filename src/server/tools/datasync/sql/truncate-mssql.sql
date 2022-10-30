-- Truncates the data from the archive tables and the original OLTP tables 
-- Please run this as a part of off-line database maintenance after a sync is 
-- run and a backup is done
-- This deletes all synchronized data from the OLTP tables and truncates all
-- data from the archive tables

use [database]

-- clean Original tables

--Set rowcount to 50000 to limit number of deletes per batch
SET rowcount 50000

--Declare variable for row count
DECLARE @rc int
SET @rc=50000

WHILE @rc=50000
 BEGIN
  BEGIN TRANSACTION
  DELETE FROM obligation_log WHERE sync_done = 1;

  --Get number of rows updated, process will continue until less than 50000
  SELECT @rc=@@rowcount
  COMMIT
 END

SET @rc=50000

WHILE @rc=50000
 BEGIN
  BEGIN TRANSACTION
  DELETE FROM policy_custom_attr
   WHERE policy_log_id IN
    (SELECT id FROM policy_activity_log WHERE sync_done = 1);
  
  SELECT @rc=@@rowcount
  COMMIT
 END

SET @rc=50000

WHILE @rc=50000
 BEGIN
  BEGIN TRANSACTION
  DELETE FROM policy_activity_log  WHERE sync_done = 1;;
  
  SELECT @rc=@@rowcount
  COMMIT
 END

SET @rc=50000

WHILE @rc=50000
 BEGIN
  BEGIN TRANSACTION
  DELETE FROM tracking_custom_attr
   WHERE tracking_log_id IN
    (SELECT id FROM tracking_activity_log WHERE sync_done = 1);
  
  SELECT @rc=@@rowcount
  COMMIT
 END

SET @rc=50000

WHILE @rc=50000
 BEGIN
  BEGIN TRANSACTION
  DELETE FROM tracking_activity_log WHERE  sync_done = 1;
  
  SELECT @RC=@@rowcount
  COMMIT
 End



-- truncate archive tables

ALTER TABLE apa_log_attr DROP CONSTRAINT [FKDEE1C92F81E153A3];

truncate table archive_obligation_log;
truncate table apa_log_attr;
truncate table apa_log;

ALTER TABLE apa_log_attr  
	WITH CHECK ADD CONSTRAINT [FKDEE1C92F81E153A3] 
	FOREIGN KEY([policy_log_id]) REFERENCES apa_log ([id]);

ALTER TABLE archive_tracking_custom_attr DROP CONSTRAINT [FK69D663F4F4470A7E];

truncate table archive_tracking_custom_attr;
truncate table archive_tracking_activity_log;

ALTER TABLE archive_tracking_custom_attr  
	WITH CHECK ADD CONSTRAINT [FK69D663F4F4470A7E] 
	FOREIGN KEY(tracking_log_id) REFERENCES archive_tracking_activity_log ([id]);
