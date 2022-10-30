DECLARE 
BeginDate DATE:= NULL -- DATE '2009-05-11' ;
EndDate DATE:= NULL -- DATE '2009-05-12' ;
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
    
    SELECT COUNT(*) INTO RowCount FROM APA_LOG 
        WHERE time >= BeginDate AND time <= EndDate;

    WHILE RowCount > 0 LOOP 

         INSERT INTO RPA_LOG 
            SELECT  * FROM (SELECT * FROM APA_LOG 
            WHERE time >= BeginDate AND time <= EndDate order by id) WHERE ROWNUM <= 500;
            
         INSERT INTO RPA_LOG_ATTR
             SELECT * 
               FROM APA_LOG_ATTR WHERE policy_log_id in 
                    (   SELECT id from (select id from APA_LOG 
                WHERE time >= BeginDate AND time <= EndDate order by id)
                          WHERE ROWNUM <=500
                 );
    
         INSERT INTO REPORT_OBLIGATION_LOG 
            SELECT * FROM ARCHIVE_OBLIGATION_LOG  WHERE ref_log_id in (
                select id from  (select id FROM APA_LOG 
                                 WHERE time >= BeginDate AND time <= EndDate 
                                 order by id) where ROWNUM <=500 
                            );        
            
         DELETE from APA_LOG_ATTR
	            WHERE policy_log_id in (
	                    SELECT id FROM (
	                        SELECT id FROM APA_LOG
	                        WHERE time >= BeginDate AND time <= EndDate order by id
	                                     ) WHERE ROWNUM <= 500
	
	            );
            
                    
         DELETE from ARCHIVE_OBLIGATION_LOG 
	             WHERE ref_log_id in (
	                SELECT id FROM (
	                   SELECT id FROM APA_LOG
	                   WHERE time >= BeginDate AND time <= EndDate order by id
	                ) WHERE ROWNUM <= 500
	             );
	                
         DELETE from APA_LOG 
            WHERE id in (
                    SELECT id FROM (SELECT id FROM APA_LOG
                    WHERE time >= BeginDate AND time <= EndDate order by id)
                        WHERE ROWNUM <= 500
            );                      
           
       COMMIT;
       SELECT COUNT(*) INTO RowCount FROM APA_LOG 
        WHERE time >= BeginDate AND time <= EndDate;
     END LOOP;
     DELETE from REPORT_INTERNAL WHERE property = 'POLICY_ACTIVITY_LOG_MIN_TIME';
END;
