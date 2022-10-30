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

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.comp.IConfiguration;
import com.nextlabs.destiny.container.dac.datasync.IDataSyncTask;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/nextlabs/destiny/container/dac/datasync/sync/SyncTask.java#1 $
 */

public class SyncTask implements IDataSyncTask{
    private static final Log LOG = LogFactory.getLog(SyncTask.class);
    private long startTime;
    private long timeout;
    
    public SyncType getType() {
        return SyncType.SYNC;
    }
    
    
    public void run(Session session, long timeout, IConfiguration config) {
        startTime = System.currentTimeMillis();
        this.timeout = timeout;

        try {
            Connection connection = session.connection();
            new PolicyActivityLogSyncTask().run(session, connection, config, this);
            new ObligationLogSyncTask().run(session, connection, config, this);
            connection.close();
        } catch (HibernateException e) {
            LOG.error("Hibernate Exception: " + e);
        } catch (SQLException e) {
            LOG.error("SQL Exception: " + e);
        }
    }
    
    /**
     * @return in seconds
     */
    protected int getRemainingTime(){
        long time = (timeout - (System.currentTimeMillis() - startTime)) / 1000;
        return time > Integer.MAX_VALUE 
                ? Integer.MAX_VALUE
                : (int)time;
    }
}
