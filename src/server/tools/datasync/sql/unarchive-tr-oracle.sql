DECLARE 
BeginDate DATE:= NULL; --DATE '2009-05-13' ;
EndDate DATE:= NULL; --DATE '2009-05-14' ;
RowCount INTEGER := 0;

BEGIN
    IF BeginDate IS NULL THEN
    
        raise_application_error(-20000,'Incorrect start date provided');
        RETURN;
    END IF;
    
    IF EndDate IS  NULL 
    THEN
        raise_application_error(-20000,'Incorrect end date provided');
        RETURN ;
    END IF;
    
    EndDate := EndDate + 1;
    
    SELECT COUNT(*) INTO RowCount FROM ARCHIVE_TRACKING_ACTIVITY_LOG 
        WHERE time >= BeginDate AND time <= EndDate;

    WHILE RowCount > 0 LOOP 

        INSERT INTO REPORT_TRACKING_ACTIVITY_LOG 
            SELECT  * FROM (SELECT * FROM ARCHIVE_TRACKING_ACTIVITY_LOG 
            WHERE time >= BeginDate AND time <= EndDate order by id) WHERE ROWNUM <= 500;
    
            
            
             INSERT INTO REPORT_TRACKING_CUSTOM_ATTR
                 SELECT id, tracking_log_id, attribute_name, attribute_value 
                   FROM ARCHIVE_TRACKING_CUSTOM_ATTR WHERE tracking_log_id in 
                        (   SELECT id from (select id from ARCHIVE_TRACKING_ACTIVITY_LOG 
                    WHERE time >= BeginDate AND time <= EndDate order by id)
                              WHERE ROWNUM <=500
                     );
    
            
            DELETE from ARCHIVE_TRACKING_CUSTOM_ATTR
            WHERE tracking_log_id in (
                    SELECT id FROM (
                        SELECT id FROM ARCHIVE_TRACKING_ACTIVITY_LOG
                        WHERE time >= BeginDate AND time <= EndDate order by id
                                     ) WHERE ROWNUM <= 500

            );
            
                    
            DELETE from ARCHIVE_TRACKING_ACTIVITY_LOG 
            WHERE id in (
                    SELECT id FROM (SELECT id FROM ARCHIVE_TRACKING_ACTIVITY_LOG
                    WHERE time >= BeginDate AND time <= EndDate order by id)
                        WHERE ROWNUM <= 500
            );
            
       COMMIT;
       SELECT COUNT(*) INTO RowCount FROM ARCHIVE_TRACKING_ACTIVITY_LOG 
            WHERE time >= BeginDate AND time <= EndDate;
     END LOOP;
     DELETE from REPORT_INTERNAL WHERE property = 'TRACKING_ACTIVITY_LOG_MIN_TIME';
     COMMIT;
END;
