CREATE OR REPLACE FUNCTION UNARCHIVE_POLICY_LOGS () RETURNS VOID AS '
DECLARE
 BeginDate  DATE := DATE \'2009-03-31\' ;  
 EndDate DATE := DATE \'2009-04-01\'; 
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
	
	RecordsLeft := COUNT(*) FROM APA_LOG
			WHERE time >= BeginDate AND time <= EndDate;
	
	WHILE (RecordsLeft > 0) LOOP
	   BEGIN 
		RAISE NOTICE ''Record Count is in this batch %.'',RecordsLeft;

		INSERT INTO RPA_LOG 
		SELECT  * FROM APA_LOG 
		WHERE time >= BeginDate AND time <= EndDate order by id LIMIT 500;	
			
		INSERT INTO  RPA_LOG_ATTR
			SELECT * 
			FROM APA_LOG_ATTR
			WHERE policy_log_id in (
				SELECT id from APA_LOG 
				WHERE time >= BeginDate AND time <= EndDate order by id LIMIT 500
				 );
	
		INSERT INTO REPORT_OBLIGATION_LOG 
		SELECT  * FROM ARCHIVE_OBLIGATION_LOG 
		WHERE ref_log_id in (
			select id from APA_LOG 
			WHERE time >= BeginDate AND time <= EndDate order by id LIMIT 500);
			
		DELETE from APA_LOG_ATTR
		WHERE policy_log_id in (
			SELECT id FROM APA_LOG
			WHERE time >= BeginDate AND time <= EndDate order by id LIMIT 500
			);
			
	        
		DELETE from ARCHIVE_OBLIGATION_LOG 
		WHERE ref_log_id in (
				SELECT id FROM APA_LOG
				WHERE time >= BeginDate AND time <= EndDate order by id
				LIMIT 500);

		DELETE from APA_LOG 
			WHERE id in (
				SELECT  id FROM APA_LOG
				WHERE time >= BeginDate AND time <= EndDate order by id LIMIT 500
			);
			
		   
	   END;
	   RecordsLeft :=  COUNT(*) FROM APA_LOG
			WHERE time >= BeginDate AND time <= EndDate;
	 END LOOP;
     DELETE from REPORT_INTERNAL WHERE property = \'POLICY_ACTIVITY_LOG_MIN_TIME\';
	RETURN;
END;
' language 'plpgsql';
SELECT UNARCHIVE_POLICY_LOGS();
