package com.nextlabs.destiny.container.dac.datasync;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.nextlabs.destiny.container.dac.datasync.log.AttributeColumnMappingInfoWrapper;
import com.nextlabs.destiny.container.dac.datasync.log.ReportObligationLog;
import com.nextlabs.destiny.container.dac.datasync.log.ReportPolicyActivityLog;
import com.nextlabs.destiny.container.dac.datasync.log.ReportPolicyActivityLog.CustomAttribute;
import com.nextlabs.destiny.container.shared.inquirymgr.ReportDataHolderManager;
import com.nextlabs.destiny.container.shared.inquirymgr.SharedLib;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.AttributeColumnMappingDAOImpl;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.AttributeColumnMappingDO;

public class ArchiverTest extends BaseDatasyncTestCase {

    Session session;
    Connection connection;
    HashMapConfiguration config;
    Archiver archiver;
    
    final static int CUT_OFF_DAYS = 5;
    final static int LOGS_TO_KEEP = 50;
    final static int LOGS_TO_ARCHIVE = 5000;
    final static int TOTAL_LOGS = LOGS_TO_KEEP + LOGS_TO_ARCHIVE;
    final static int DEFAULT_EXTENDED_ATTR_COLUMNS = 50;
    
    static final ReportPolicyActivityLog REPORT_PAL_SAMPLE;
    
    static final AttributeColumnMappingInfoWrapper attrColumnMappingConfig = 
    		new AttributeColumnMappingInfoWrapper(DEFAULT_EXTENDED_ATTR_COLUMNS, "attr");
    
	/**
	 * This method fetches the attribute-column mapping records from database
	 * and constructs lookup structures
	 * @param attrColumnMapConfig
	 */
	private static void populateAttrColumnMap() {
		List<AttributeColumnMappingDO> mappingList = null;

		try {
			mappingList = AttributeColumnMappingDAOImpl.list();
		} catch (HibernateException e) {
			fail("Failed testArchivePolicyLogs: Error fetching mapping list");
		}

		if (mappingList == null || mappingList.isEmpty()) {			
			return;
		}

		Map<String, Map<String, AttributeColumnMappingDO>> map = attrColumnMappingConfig.getAttrColumnNameMapping();
		
		boolean[] columnsInUse = attrColumnMappingConfig.getColumnsInUse();
		
		for (AttributeColumnMappingDO mapping : mappingList) {
			
			if (!mapping.isDynamic())
			{
				continue;
			}
			
			String type = mapping.getAttributeType();
			
			if (map.get(type) == null)
			{
				map.put(type, new HashMap<String, AttributeColumnMappingDO>());
			}
			
			map.get(type).put(mapping.getAttributeName().toLowerCase(), mapping);

			String columnName = mapping.getColumnName();
			
			if (columnName == null || columnName.isEmpty())
			{
				continue;
			}
			
			try
			{
				int index = Integer.parseInt(columnName.replace(attrColumnMappingConfig.getAttributePrefix(), ""));
				columnsInUse[index] = true;
			}
			catch(Exception e)
			{
				System.err.println(e);
			}
		}
	}
    
    static{     
    	
    	populateAttrColumnMap();
    	
        REPORT_PAL_SAMPLE = new ReportPolicyActivityLog(attrColumnMappingConfig);
        REPORT_PAL_SAMPLE.month = 1238544000000L;
        REPORT_PAL_SAMPLE.day = 1238544000000L;

        REPORT_PAL_SAMPLE.hostId = 20;
        REPORT_PAL_SAMPLE.hostIp = "31.77.10.15";
        REPORT_PAL_SAMPLE.hostName = "host00020.demo.nextlabs.com";

        REPORT_PAL_SAMPLE.userId = 123;
        REPORT_PAL_SAMPLE.userName = "user_name";
        REPORT_PAL_SAMPLE.userSid = "u.sid";

        REPORT_PAL_SAMPLE.applicationId = 123;
        REPORT_PAL_SAMPLE.applicationName = "application_name";

        REPORT_PAL_SAMPLE.action = "on";
        REPORT_PAL_SAMPLE.policyId = 20;
        REPORT_PAL_SAMPLE.policyName = "t.name";
        REPORT_PAL_SAMPLE.policyFullname = "t.fullname";
        REPORT_PAL_SAMPLE.policyDecision = "D";
        REPORT_PAL_SAMPLE.decisionRequestId = -1;
        REPORT_PAL_SAMPLE.logLevel = 1;

        REPORT_PAL_SAMPLE.fromResourceName = "fromResourceName";
        REPORT_PAL_SAMPLE.fromResourcePrefix = "x";
        REPORT_PAL_SAMPLE.fromResourcePath = "y";
        REPORT_PAL_SAMPLE.fromResourceShortName = "z";

        REPORT_PAL_SAMPLE.fromResourceSize = 23L;
        REPORT_PAL_SAMPLE.fromResourceOwnerId = "fromResourceOwnerId";
        REPORT_PAL_SAMPLE.fromResourceCreatedDate = null;
        REPORT_PAL_SAMPLE.fromResourceModifiedDate = null;

        REPORT_PAL_SAMPLE.toResourceName = "toResourceName";
        
        CustomAttribute attr = new CustomAttribute();
        attr.attributeType = "attribute_type";
        attr.attributeName = "attribute_name";
        attr.attributeValue = "attribute_value";
        
        REPORT_PAL_SAMPLE.attrs = Collections.singletonList(attr);
    }
    
    static final ReportObligationLog REPORT_OL_SAMPLE;
    static{
        REPORT_OL_SAMPLE = new ReportObligationLog();
        REPORT_OL_SAMPLE.name = "name";
        REPORT_OL_SAMPLE.attrOne = "attr_one";
        REPORT_OL_SAMPLE.attrTwo = "attr_two";
        REPORT_OL_SAMPLE.attrThree = "attr_three";
    }
        
    public ArchiverTest(String testName) {
        super(testName);
    }
    
    public void setUp() throws Exception {
        super.setUp();
        HashMapConfiguration reportDataHolderonfig = new HashMapConfiguration();
        reportDataHolderonfig.setProperty(ReportDataHolderManager.DATA_SOURCE_PARAM, getActivityDataSource());
        ComponentManagerFactory.getComponentManager().getComponent(ReportDataHolderManager.class, 
                reportDataHolderonfig);
        
        session = getSession();
        connection = session.connection();
        setupData();
        archiver = new Archiver();
        archiver.setCutOffDays(CUT_OFF_DAYS);
        config = new HashMapConfiguration();
        
        config.setProperty(IDataSyncTask.DIALECT_CONFIG_PARAMETER, getDialect());
        config.setProperty(IDataSyncTask.TASK_UPDATE_PARAMETER, new IDataSyncTaskUpdate(){

            public void addFail(int size) throws IllegalStateException {
            }

            public void addSuccess(int size) throws IllegalStateException {
            }

            public boolean alive() {
                return true;
            }

            public long getUpdateInterval() {
                return Long.MAX_VALUE;
            }

            public void reset() {
            }

            public void setPrefix(String prefix) {
            }

            public void setTotalSize(int size) throws IllegalStateException {
            }
        });
    }
    
    private void setupData() throws Exception {
        dropData();
        createData();
    }
    
    private void insertData( int index,
            java.sql.Timestamp time,
            ReportPolicyActivityLog log,
            PreparedStatement reportPALInsertStatement,
            PreparedStatement reportPACAInsertStatement) throws SQLException {
        insertData( index,
                time,
                log,
                null,
                reportPALInsertStatement,
                reportPACAInsertStatement, 
                null);
    }
    
    private void insertData( int index,
            java.sql.Timestamp time,
            ReportPolicyActivityLog log,
            ReportObligationLog oLog,
            PreparedStatement reportPALInsertStatement,
            PreparedStatement reportPACAInsertStatement, 
            PreparedStatement reportOLInsertStatement) throws SQLException {
        log.id = index;
        log.time = time;
        log.setValue(reportPALInsertStatement);
        
        if(log.attrs != null){
            assertEquals(1, log.attrs.size());
            log.attrs.get(0).id = index;
        }
        log.setCustomAttributesValue(reportPACAInsertStatement);
        
        
        if (oLog != null) {
            oLog.id = index;
            oLog.refLogId = log.id;
            oLog.setValue(reportOLInsertStatement);
        }
    }
    
    private void createData() throws Exception {
        final java.sql.Timestamp pastTime = getTimestampBeforeDays(CUT_OFF_DAYS);
        final java.sql.Timestamp nowTime = getTimestampBeforeDays(0);
        
        PreparedStatement reportPALInsertStatement = connection.prepareStatement(
    	        REPORT_PAL_SAMPLE.getInsertQueryString());
    	PreparedStatement reportPACAInsertStatement = connection.prepareStatement(
    	        ReportPolicyActivityLog.INSERT_CUSTOM_ATTR_QUERY);
    	PreparedStatement reportOLInsertStatement = connection.prepareStatement(
    	        ReportObligationLog.INSERT_LOG_QUERY);
    	
    	try{
    	    
        	/** 
        	 * These should be archived and hence removed from the source tables
        	 */
        	for (int i = 0; i < LOGS_TO_ARCHIVE; i++) {
                insertData(i + 1, pastTime, REPORT_PAL_SAMPLE, REPORT_OL_SAMPLE,
                        reportPALInsertStatement, reportPACAInsertStatement,
                        reportOLInsertStatement);
            }
    	
        	/**
        	 *  These should NOT be archived and should remain in the source tables
        	 */
        	for (int i = LOGS_TO_ARCHIVE; i < TOTAL_LOGS; i++) {
                insertData(i + 1, nowTime, REPORT_PAL_SAMPLE, REPORT_OL_SAMPLE,
                        reportPALInsertStatement, reportPACAInsertStatement,
                        reportOLInsertStatement);
            }
        	
        	int[] result;
        	result = reportPALInsertStatement.executeBatch();
        	assertNotNull(result);
        	assertEquals(TOTAL_LOGS, result.length);
        	assertEquals(TOTAL_LOGS, sum(result));
        	
        	result = reportPACAInsertStatement.executeBatch();
        	assertNotNull(result);
            assertEquals(TOTAL_LOGS, result.length);
            assertEquals(TOTAL_LOGS, sum(result));
            
        	result = reportOLInsertStatement.executeBatch();
        	assertNotNull(result);
            assertEquals(TOTAL_LOGS, result.length);
            assertEquals(TOTAL_LOGS, sum(result));
            
        	connection.commit();
    	} catch (BatchUpdateException se) {
    	    System.out.println(se.getNextException());
    	    throw se;
    	}finally{
            reportPALInsertStatement.close();
            reportPACAInsertStatement.close();
            reportOLInsertStatement.close();
        }
    }

    private int sum(int[] result) {
        int i = 0;
        for (int r : result) {
            if (r == Statement.SUCCESS_NO_INFO) {
                i++;
            } else {
                i += r;
            }
        }
        return i;
    }

    private void dropData() throws Exception {
        Statement stmt = connection.createStatement();
        
    	// drop everything from the archive tables
        stmt.execute("delete from " + SharedLib.ARCHIVE_PA_CUST_ATTR_TABLE);
        stmt.execute("delete from " + SharedLib.ARCHIVE_PA_OBLIGATION_TABLE);
        stmt.execute("delete from " + SharedLib.ARCHIVE_PA_TABLE);
        
        stmt.execute("delete from " + SharedLib.REPORT_PA_CUST_ATTR_TABLE);
        stmt.execute("delete from " + SharedLib.REPORT_PA_OBLIGATION_TABLE);
        stmt.execute("delete from " + SharedLib.REPORT_PA_TABLE);
        
        stmt.execute("delete from " + SharedLib.REPORT_INTERNAL_TABLE);
        
        connection.commit();
        stmt.close();
    }
    
    private java.sql.Timestamp getTimestampBeforeDays(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        java.sql.Timestamp date = new java.sql.Timestamp(cal.getTimeInMillis());
        return date;
    }
    
    private Session getSession() throws HibernateException {
        return getActivityDataSource().getSession();
    }
    
    private void checkResults() throws Exception {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            
            // pa logs  - source
            ResultSet rs = stmt.executeQuery("select count(*) from " + SharedLib.REPORT_PA_TABLE);
            rs.next();
            int logRecordCount = rs.getInt(1);
            assertEquals("Source Policy Log Record Count", LOGS_TO_KEEP, logRecordCount);
            
            rs = stmt.executeQuery("select count(*) from " + SharedLib.REPORT_PA_CUST_ATTR_TABLE);
            rs.next();
            int caLogRecordCount = rs.getInt(1);
            assertEquals("Source Policy Cust Attr Log Record Count",
                    LOGS_TO_KEEP, caLogRecordCount);
            
            rs = stmt.executeQuery(
                "select count(*) from " + SharedLib.REPORT_PA_OBLIGATION_TABLE);
            rs.next();
            int obLogRecordCount = rs.getInt(1);
            assertEquals("Source Policy Obligation Log Record Count", 
                    LOGS_TO_KEEP, obLogRecordCount);
            
            // pa logs - archived
            rs = stmt.executeQuery(
                    "select count(*) from " + SharedLib.ARCHIVE_PA_TABLE);
            rs.next();
            logRecordCount = rs.getInt(1);
            assertEquals("Archived Policy Log Record Count", 
                    LOGS_TO_ARCHIVE, logRecordCount);

            rs = stmt.executeQuery(
               "select count(*) from " + SharedLib.ARCHIVE_PA_CUST_ATTR_TABLE);
            rs.next();
            caLogRecordCount = rs.getInt(1);
            assertEquals("Archived Policy Cust Attr Log Record Count", 
                    LOGS_TO_ARCHIVE, logRecordCount);

            rs = stmt.executeQuery(
               "select count(*) from " + SharedLib.ARCHIVE_PA_OBLIGATION_TABLE);
            rs.next();
            obLogRecordCount = rs.getInt(1);
            assertEquals("Archived Policy Obligation Log Record Count", 
                    LOGS_TO_ARCHIVE, logRecordCount);
            
            // ta logs - source
            rs = stmt.executeQuery(
                    "select count(*) from " + SharedLib.REPORT_TA_TABLE);
            rs.next();
            logRecordCount = rs.getInt(1);
            assertEquals("Source Tracking Log Record Count",
                    LOGS_TO_KEEP, logRecordCount );

            rs = stmt.executeQuery(
                "select count(*) from " + SharedLib.REPORT_TA_CUST_ATTR_TABLE);
            rs.next();
            caLogRecordCount = rs.getInt(1);
            assertEquals("Source Tracking Custom Attr Log Record Count",
                    LOGS_TO_KEEP, logRecordCount);

            
            rs = stmt.executeQuery(
                    "select count(*) from " + SharedLib.ARCHIVE_TA_TABLE);
            rs.next();
            logRecordCount = rs.getInt(1);
            assertEquals("Archived Tracking Log Record Count", 
                    LOGS_TO_ARCHIVE, logRecordCount);

            rs = stmt.executeQuery(
                "select count(*) from " + SharedLib.ARCHIVE_TA_CUST_ATTR_TABLE);
            rs.next();
            caLogRecordCount = rs.getInt(1);
            assertEquals("Archived Tracking Cust Attr Log Record Count", 
                    LOGS_TO_ARCHIVE, logRecordCount );
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (Exception ex) {
                fail("Could not close stmt - should we really fail here?");
            }
        }
    }
    public void tearDown() throws Exception {
        dropData();
        connection.close();
        session.close();
    }

    public void testGetMinPolicyActivityReportDate() throws Exception {
        Statement st = null;
        Timestamp obtainedDate = null;
        Timestamp actualMinDate = null;
        Timestamp minDateInReportInternal = null;
        
        try {
            obtainedDate =  SharedLib.getEarliestReportDate(true);
            Connection conn = session.connection();
            st = conn.createStatement();
            ResultSet rs = st.executeQuery(SharedLib.SELECT_PA_MIN_TIME_SQL);
             if (rs.next()) {
                 actualMinDate = rs.getTimestamp(1);
             } 
             rs = st.executeQuery("select value from " + 
                     SharedLib.REPORT_INTERNAL_TABLE +
                    " where property = '" + SharedLib.POLICY_ACTIVITY_LOG_MIN_TIME + 
                    "'");
             
            if (rs.next()) {
                String tstamp = rs.getString(1);
                minDateInReportInternal = Timestamp.valueOf(tstamp);
            }
            assertEquals("Obtaine date == actual Min Date" , true,
                          obtainedDate.equals(actualMinDate));
            assertEquals("Obtaine date == minDateInReportInternal" , true,
                    obtainedDate.equals(minDateInReportInternal));
        } finally {
            try {
                if (st != null) st.close();
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
            
        }
    }

    public void testGetMinTrackingActivityReportDate() throws Exception {
        Statement st = null;
        Timestamp obtainedDate = null;
        Timestamp actualMinDate = null;
        Timestamp minDateInReportInternal = null;
        
        try {
            Connection conn = session.connection();
            st = conn.createStatement();
            obtainedDate =  SharedLib.getEarliestReportDate(false);
            ResultSet rs = st.executeQuery(SharedLib.SELECT_TA_MIN_TIME_SQL);
             if (rs.next()) {
                 actualMinDate = rs.getTimestamp(1);
             } 
             rs = st.executeQuery("select value from " + 
                     SharedLib.REPORT_INTERNAL_TABLE +
                    " where property = '" + SharedLib.TRACKING_ACTIVITY_LOG_MIN_TIME + 
                    "'");
             
            if (rs.next()) {
                String tstamp = rs.getString(1);
                minDateInReportInternal = Timestamp.valueOf(tstamp);
            }
            assertEquals("Obtaine date == actual Min Date" , true,
                          obtainedDate.equals(actualMinDate));
            assertEquals("Obtaine date == minDateInReportInternal" , true,
                    obtainedDate.equals(minDateInReportInternal));
        } finally {
            try {
                if (st != null) st.close();
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
            
        }
    }

    public void testArchiveRun()  {
        try {
            archiver.run(session, 0, config);
            checkResults();
        } catch (Exception ex) {
            fail("Failed testArchivePolicyLogs");
        }
    }
}
