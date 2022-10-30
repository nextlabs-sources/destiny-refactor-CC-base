CREATE OR REPLACE FUNCTION UNARCHIVE_TRACKING_LOGS () RETURNS VOID AS '
DECLARE
 BeginDate  DATE := DATE \'2009-06-12\' ;  
 EndDate DATE := DATE \'2009-06-12\'; 
 RecordsLeft INTEGER := 0;
BEGIN
    IF BeginDate IS NULL THEN
    
        RAISE NOTICE ''Incorrect start date provided'';
        RETURN;
    END IF;
    
    IF EndDate IS  NULL 
    THEN
        RAISE NOTICE ''Incorrect end date provided'';
        RETURN ;
    END IF;
    
    EndDate := EndDate::timestamp + cast(''1 days'' as interval);
    
    RecordsLeft := COUNT(*) FROM ARCHIVE_TRACKING_ACTIVITY_LOG
            WHERE time >= BeginDate AND time <= EndDate;
    WHILE (RecordsLeft > 0) LOOP    
       BEGIN 
        RAISE NOTICE ''Record Count is in this batch %.'',RecordsLeft;

        INSERT INTO REPORT_TRACKING_ACTIVITY_LOG 
            SELECT  * FROM ARCHIVE_TRACKING_ACTIVITY_LOG 
            WHERE time >= BeginDate AND time <= EndDate order by id LIMIT 500;
    
            
            
             INSERT INTO REPORT_TRACKING_CUSTOM_ATTR
                 SELECT id, tracking_log_id, attribute_name, attribute_value 
                 FROM ARCHIVE_TRACKING_CUSTOM_ATTR
                 WHERE tracking_log_id in (
                    SELECT id from ARCHIVE_TRACKING_ACTIVITY_LOG 
                    WHERE time >= BeginDate AND time <= EndDate order by id LIMIT 500
                 );
    
            
            DELETE from ARCHIVE_TRACKING_CUSTOM_ATTR
            WHERE tracking_log_id in (
                    SELECT id FROM ARCHIVE_TRACKING_ACTIVITY_LOG
                    WHERE time >= BeginDate AND time <= EndDate order by id LIMIT 500
            );
            
                    
            DELETE from ARCHIVE_TRACKING_ACTIVITY_LOG 
            WHERE id in (
                    SELECT  id FROM ARCHIVE_TRACKING_ACTIVITY_LOG
                    WHERE time >= BeginDate AND time <= EndDate order by id LIMIT 500
            );
            
           
       END;
        RecordsLeft :=  COUNT(*) FROM ARCHIVE_TRACKING_ACTIVITY_LOG WHERE time >= BeginDate AND time <= EndDate;    
     END LOOP;
    DELETE from REPORT_INTERNAL WHERE property = \'TRACKING_ACTIVITY_LOG_MIN_TIME\';
    RETURN;
END;
' language 'plpgsql';
SELECT UNARCHIVE_TRACKING_LOGS();
