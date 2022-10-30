DECLARE @BeginDate datetime , @EndDate datetime
SET @BeginDate = '4/7/2009'
SET @EndDate = '4/8/2009'
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

WHILE (SELECT COUNT(*) FROM ARCHIVE_TRACKING_ACTIVITY_LOG
		WHERE time >= @BeginDate AND time <= @EndDate) > 0

BEGIN
	BEGIN TRAN
		INSERT INTO REPORT_TRACKING_ACTIVITY_LOG 
		SELECT TOP 500 * FROM ARCHIVE_TRACKING_ACTIVITY_LOG 
        WHERE time >= @BeginDate AND time <= @EndDate order by id

		IF @@ERROR <> 0
		BEGIN
			ROLLBACK TRAN
			RAISERROR ('Error occured while UnArchiving tracking logs', 16, 1)			
		END

		 INSERT INTO REPORT_TRACKING_CUSTOM_ATTR
			 SELECT id, tracking_log_id, attribute_name, attribute_value 
			 FROM ARCHIVE_TRACKING_CUSTOM_ATTR
			 WHERE tracking_log_id in (
				SELECT TOP 500 id from ARCHIVE_TRACKING_ACTIVITY_LOG 
				WHERE time >= @BeginDate AND time <= @EndDate order by id
			 )

		IF @@ERROR <> 0
		BEGIN
			ROLLBACK TRAN
			RAISERROR ('Error occured while UnArchiving tracking custom attr logs', 16, 1)
		END
		
		DELETE from ARCHIVE_TRACKING_CUSTOM_ATTR
		WHERE tracking_log_id in (
				SELECT TOP 500 id FROM ARCHIVE_TRACKING_ACTIVITY_LOG
				WHERE time >= @BeginDate AND time <= @EndDate order by id
		)
		IF @@ERROR <> 0
		BEGIN
			ROLLBACK TRAN
			RAISERROR ('Error occured while Deleting archived tracking custom atr logs', 16, 1)		
		END

		DELETE from ARCHIVE_TRACKING_ACTIVITY_LOG 
		WHERE id in (
				SELECT TOP 500 id FROM ARCHIVE_TRACKING_ACTIVITY_LOG
				WHERE time >= @BeginDate AND time <= @EndDate order by id
		)
		IF @@ERROR <> 0
		BEGIN
			ROLLBACK TRAN
			RAISERROR ('Error occured while UnArchiving-deleting archived tracking logs', 16, 1)
		END
	    
		DELETE from REPORT_INTERNAL WHERE property = 'TRACKING_ACTIVITY_LOG_MIN_TIME'
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
