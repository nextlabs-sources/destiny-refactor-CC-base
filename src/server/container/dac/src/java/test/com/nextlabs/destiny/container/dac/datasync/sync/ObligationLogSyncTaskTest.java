/*
 * Created on Jun 17, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync.sync;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import net.sf.hibernate.HibernateException;

import com.nextlabs.destiny.container.shared.inquirymgr.SharedLib;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/nextlabs/destiny/container/dac/datasync/sync/ObligationLogSyncTaskTest.java#1 $
 */

public class ObligationLogSyncTaskTest extends BaseActivityLogSyncTaskTest<ObligationLogSyncTask> {
    
    public ObligationLogSyncTaskTest() {
        super(ObligationLogSyncTaskTest.class.getName(), SharedLib.PA_OBLIGATION_TABLE);
    }

    private class ObligationLogSyncTaskMock extends ObligationLogSyncTask{
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
       assertEquals(goodCount, ((ObligationLogSyncTaskMock)syncTaskBase).goodCount );
       assertEquals(badCount, ((ObligationLogSyncTaskMock)syncTaskBase).badCount );
    }

    @Override
    protected ObligationLogSyncTask createSyncTaskBase() {
        return new ObligationLogSyncTaskMock();
    }

    @Override
    protected void clearAllReportLog() throws HibernateException, SQLException {
        Connection c = session.connection();
        Statement statement = c.createStatement();
        try {
            statement.execute("delete from " + SharedLib.REPORT_PA_OBLIGATION_TABLE);
        } finally {
            statement.close();
        }
    }
}
