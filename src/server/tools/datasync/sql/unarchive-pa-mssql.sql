
DECLARE @BeginDate datetime , @EndDate datetime
SET @BeginDate = '4/3/2009'
SET @EndDate = '4/7/2009'
SET NOCOUNT ON

IF @BeginDate IS NULL 
BEGIN
	RAISERROR ('Incorrect start date provided', 16, 1)
END

IF @EndDate IS NULL 
BEGIN
	RAISERROR ('Incorrect end date provided', 16, 1)
END

SET @EndDate = DATEADD(day, 1, @EndDate)

WHILE (SELECT COUNT(*) FROM APA_LOG
		WHERE time >= @BeginDate AND time <= @EndDate) > 0

BEGIN

	BEGIN TRAN
		
		INSERT INTO RPA_LOG
		SELECT TOP 500 * 
		FROM APA_LOG
		WHERE time >= @BeginDate AND time <= @EndDate ORDER BY id

		IF @@ERROR <> 0
		BEGIN
			ROLLBACK TRAN
			RAISERROR ('Error occured while  UnArchiving policy logs', 16, 1)
		END

		INSERT INTO RPA_LOG_ATTR
		SELECT *
		FROM APA_LOG_ATTR
		WHERE policy_log_id IN
		(
			SELECT TOP 500 id FROM APA_LOG
			where time >= @BeginDate AND time <= @EndDate order by id
		)

		IF @@ERROR <> 0
		BEGIN
			ROLLBACK TRAN
			RAISERROR ('Error occured while UnArchiving policy custom attribute logs', 16, 1)
		END

		INSERT INTO REPORT_OBLIGATION_LOG 
	    SELECT  * FROM ARCHIVE_OBLIGATION_LOG 
        WHERE ref_log_id in (
			select top 500 id from APA_LOG 
			WHERE time >= @BeginDate AND time <= @EndDate order by id)

		IF @@ERROR <> 0
		BEGIN
			ROLLBACK TRAN
			RAISERROR ('Error occured while UnArchiving obligation logs', 16, 1)			
		END

		
		DELETE from APA_LOG_ATTR 
		WHERE policy_log_id in (
				SELECT TOP 500 id FROM APA_LOG
				WHERE time >= @BeginDate AND time <= @EndDate order by id
		)
		IF @@ERROR <> 0
		BEGIN
			ROLLBACK TRAN
			RAISERROR ('Error occured while Deleting archived policy custom atr logs', 16, 1)
		END

		DELETE from ARCHIVE_OBLIGATION_LOG 
		WHERE ref_log_id in (
				SELECT TOP 500 id FROM APA_LOG
				WHERE time >= @BeginDate AND time <= @EndDate order by id
		)
		IF @@ERROR <> 0
		BEGIN
			ROLLBACK TRAN
			RAISERROR ('Error occured while Deleting archived policy custom atr logs', 16, 1)
		END

		DELETE from APA_LOG 
		WHERE id in (
				SELECT TOP 500 id FROM APA_LOG
				WHERE time >= @BeginDate AND time <= @EndDate order by id
		)
		IF @@ERROR <> 0
		BEGIN
			ROLLBACK TRAN
			RAISERROR ('Error occured while deleting archived policy logs', 16, 1)
		END
		
		DELETE from REPORT_INTERNAL WHERE property = 'POLICY_ACTIVITY_LOG_MIN_TIME'
		IF @@ERROR <> 0
        BEGIN
            ROLLBACK TRAN
            RAISERROR ('Error occured while deleting minimum time entry from internal table', 16, 1)
        END
        
	if @@TRANCOUNT > 0
	BEGIN 
	   COMMIT TRAN
	END
END

