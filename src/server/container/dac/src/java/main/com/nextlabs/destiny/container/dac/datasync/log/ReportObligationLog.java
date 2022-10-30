/*
 * Created on Jun 15, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync.log;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.bluejungle.framework.datastore.hibernate.SQLHelper;
import com.nextlabs.destiny.container.shared.inquirymgr.SharedLib;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/nextlabs/destiny/container/dac/datasync/log/ReportObligationLog.java#1 $
 */

public class ReportObligationLog extends ReportLog {
    private static final int ID;
    private static final int REF_LOG_ID;
    private static final int NAME;
    private static final int ATTR_ONE;
    private static final int ATTR_TWO;
    private static final int ATTR_THREE;

    public static final String INSERT_LOG_QUERY;
    
    static {
        int i = 1;
        ID = i++;
        REF_LOG_ID = i++;
        NAME = i++;
        ATTR_ONE = i++;
        ATTR_TWO = i++;
        ATTR_THREE = i++;
        
        INSERT_LOG_QUERY = "insert into " + SharedLib.REPORT_PA_OBLIGATION_TABLE
                + " (id,ref_log_id,name,attr_one,attr_two,attr_three"
                + ") values " + SQLHelper.makeInList(i -1);
    }
    
    public long refLogId;
    public String name;
    public String attrOne;
    public String attrTwo;
    public String attrThree;

    public void setValue(PreparedStatement statement) throws SQLException {
        statement.setLong(    ID,           id);
        statement.setLong(    REF_LOG_ID,   refLogId);
        statement.setString(  NAME,         name);
        statement.setString(  ATTR_ONE,     attrOne);
        statement.setString(  ATTR_TWO,     attrTwo);
        statement.setString(  ATTR_THREE,   attrThree);
        statement.addBatch();
    }

}
