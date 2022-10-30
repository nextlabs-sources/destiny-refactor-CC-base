/*
 * Created on Jun 12, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync.sync;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.dabs.components.log.hibernateimpl.HibernateLogWriter;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.PolicyActivityInfo;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.log.ToResourceInformation;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.nextlabs.destiny.container.shared.inquirymgr.SharedLib;
import com.nextlabs.random.RandomString;

import net.sf.hibernate.HibernateException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/nextlabs/destiny/container/dac/datasync/sync/PolicyActivityLogSyncTaskTest.java#1 $
 */

public class PolicyActivityLogSyncTaskTest extends BaseActivityLogSyncTaskTest<PolicyActivityLogSyncTask> {
    private static final Log LOG = LogFactory.getLog(PolicyActivityLogSyncTaskTest.class);
    
    public PolicyActivityLogSyncTaskTest() {
        super(PolicyActivityLogSyncTaskTest.class.getName(), SharedLib.PA_TABLE);
    }

    private class PolicyActivityLogSyncTaskMock extends PolicyActivityLogSyncTask{
        int goodCount = 0;
        int badCount = 0;
        
        protected void logResult(int[][] results){
            int successRows = 0;
            if (results != null) {
                int[] updatedPALResults = results[getResultCheckIndex()];
                boolean isGood = true;
                for (int r : updatedPALResults) {
                    if (r <= 0 && r != Statement.SUCCESS_NO_INFO) {
                        isGood = false;
                        break;
                    }
                    if (isGood) {
                        successRows++;
                    }
                }
            } else {
                // SHOULD not happen...
                fail("missing results");
            }
            
            int failedRows = transform.size() - successRows;
            
            goodCount += successRows;
            badCount += failedRows;
        }
    }

    @Override
    protected void assertGoodAndBase(int goodCount, int badCount) {
       assertEquals(goodCount, ((PolicyActivityLogSyncTaskMock)syncTaskBase).goodCount );
       assertEquals(badCount, ((PolicyActivityLogSyncTaskMock)syncTaskBase).badCount );
    }

    @Override
    protected PolicyActivityLogSyncTask createSyncTaskBase() {
        return new PolicyActivityLogSyncTaskMock();
    }

    @Override
    protected void clearAllReportLog() throws HibernateException, SQLException {
        Connection c = session.connection();
        Statement statement = c.createStatement();
        try {
            LOG.info("start clearing " + SharedLib.REPORT_PA_CUST_ATTR_TABLE);
            statement.execute("delete from " + SharedLib.REPORT_PA_CUST_ATTR_TABLE);
        } finally {
            LOG.info("done clearing " + SharedLib.REPORT_PA_CUST_ATTR_TABLE);
            statement.close();
        }

        statement = c.createStatement();
        try {
            LOG.info("start clearing " + SharedLib.REPORT_PA_TABLE);
            statement.execute("delete from " + SharedLib.REPORT_PA_TABLE);
        } finally {
            LOG.info("done clearing " + SharedLib.REPORT_PA_TABLE);
            statement.close();
        }
    }
    
    public void testLongFromResourceName() throws HibernateException, DataSourceException{
        HibernateLogWriter writer = initHibernateLogWriter();
        
        Long maxId = (Long) session.createQuery("select max(p.id) from PolicyActivityLogDO p").uniqueResult();
        if (maxId == null) {
            maxId = 1L;
        }
        
        String longStr = RandomString.getRandomString(400, 400, RandomString.ALNUM);
        String template = "%s://%s/%s";
        
        PolicyActivityLogEntry[] entries = new PolicyActivityLogEntry[]{
                createLongFromResourceNamePAL(String.format(template, "sharepoint", longStr, "1.txt"), ++maxId),
                createLongFromResourceNamePAL(String.format(template, longStr, "path", "1.txt"), ++maxId),
                createLongFromResourceNamePAL(String.format(template, "sharepoint", "path", longStr), ++maxId),
        };
        writer.log(entries);
    }
    
    protected PolicyActivityLogEntry createLongFromResourceNamePAL(String name, long uid){
        long time = System.currentTimeMillis();
        PolicyActivityLogEntry entry = new PolicyActivityLogEntry(
                new PolicyActivityInfo(
                        new FromResourceInformation(
                                name, // name, 
                                1L,  //size, 
                                time, //createdDate, 
                                time, //modifiedDate, 
                                "ownerId" //ownerId)
                                ), //fromResourceInfo
                        new ToResourceInformation(
                                "" //name
                                ), //toResourceInfo,
                        "userName", //userName, 
                        -1, //userId, 
                        "hostName", //hostName, 
                        "1.1.1.1", //hostIP, 
                        -1, //hostId, 
                        "applicationName", //applicationName, 
                        -1, //applicationId, 
                        ActionEnumType.ACTION_OPEN, //action, 
                        PolicyDecisionEnumType.POLICY_DECISION_ALLOW, //policyDecision, 
                        -1, //decisionRequestId, 
                        time, //ts, 
                        1, //level, 
                        new DynamicAttributes() //customAttr
                        ), 
                -1, //policyId
                uid //uid
        );
        
        return entry;
    }
}
