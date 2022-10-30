/*
 * Created on Feb 12, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dabs.components.log.filesystemimpl;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dabs.components.log.ILogWriter;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.log.PolicyActivityLogEntryV2;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.domain.log.TrackingLogEntry;
import com.bluejungle.domain.log.TrackingLogEntryV2;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.nextlabs.destiny.container.dabs.components.log.ILogQueueMgr;
import com.nextlabs.destiny.container.dabs.components.log.LogQueueException;
import com.nextlabs.domain.log.PolicyActivityLogEntryV3;
import com.nextlabs.domain.log.PolicyActivityLogEntryV4;
import com.nextlabs.domain.log.PolicyActivityLogEntryV5;
import com.nextlabs.domain.log.TrackingLogEntryV3;


/**
 * This is the log writer implementation specific to a file system.  It uses
 * the FileSystemLogQueueMgr to queue the logs onto the file system, and 
 * fires a TimerTask to write the queued logs into the database using 
 * the HibernateLogWriter
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/main/com/nextlabs/destiny/container/dabs/components/log/filesystemimpl/FileSystemBufferLogWriter.java#1 $
 */

public class FileSystemBufferLogWriter implements ILogWriter, ILogEnabled, IInitializable,
		IConfigurable {
	
    public static final PropertyKey<ILogQueueMgr> LOG_QUEUE_MGR_PARAM =
    new PropertyKey<ILogQueueMgr>("LogQueueMgr");
    
    protected Log log;
    
    private ILogQueueMgr logQueueMgr;
    private IConfiguration configuration;
    
    
    /**
     * @see com.bluejungle.destiny.container.dabs.components.log.ILogWriter#log(com.bluejungle.domain.log.PolicyActivityLogEntry[])
     */
    public void log(PolicyActivityLogEntry[] logEntries) throws DataSourceException {
        try {
            logQueueMgr.queueLogs(logEntries);
        } catch (LogQueueException e) {
            this.getLog().error("Queueing of the policy activity logs failed");
            throw new DataSourceException(e);
        }
    }

    
    /**
     * @see com.bluejungle.destiny.container.dabs.components.log.ILogWriter#log(com.bluejungle.domain.log.PolicyActivityLogEntryV2[])
     */
    public void log(PolicyActivityLogEntryV2[] logEntries) throws DataSourceException {
        try {
            logQueueMgr.queueLogs(logEntries);
        } catch (LogQueueException e) {
            this.getLog().error("Queueing of the policy activity logs (v2) failed");
            throw new DataSourceException(e);
        }
    }

    /*
     * @see com.bluejungle.destiny.container.dabs.components.log.ILogWriter#log(com.nextlabs.domain.log.PolicyActivityLogEntryV3[])
     */
    public void log(PolicyActivityLogEntryV3[] logEntries) throws DataSourceException {
        try {
            logQueueMgr.queueLogs(logEntries);
        } catch (LogQueueException e) {
            this.getLog().error("Queueing of the policy activity logs (v3) failed");
            throw new DataSourceException(e);
        }
    }

    /*
     * @see com.bluejungle.destiny.container.dabs.components.log.ILogWriter#log(com.nextlabs.domain.log.PolicyActivityLogEntryV4[])
     */
    public void log(PolicyActivityLogEntryV4[] logEntries) throws DataSourceException {
        try {
            logQueueMgr.queueLogs(logEntries);
        } catch (LogQueueException e) {
            this.getLog().error("Queueing of the policy activity logs (v4) failed");
            throw new DataSourceException(e);
        }
    }

    /*
     * @see com.bluejungle.destiny.container.dabs.components.log.ILogWriter#log(com.nextlabs.domain.log.PolicyActivityLogEntryV5[])
     */
    public void log(PolicyActivityLogEntryV5[] logEntries) throws DataSourceException {
        try {
            logQueueMgr.queueLogs(logEntries);
        } catch (LogQueueException e) {
            this.getLog().error("Queueing of the policy activity logs (v5) failed");
            throw new DataSourceException(e);
        }
    }
    
    /**
     * @see com.bluejungle.destiny.container.dabs.components.log.ILogWriter#log(com.bluejungle.domain.log.PolicyAssistantLogEntry[])
     */
    public void log(PolicyAssistantLogEntry[] logEntries) throws DataSourceException {
        try {
            logQueueMgr.queueLogs(logEntries);
        } catch (LogQueueException e) {
            this.getLog().error("Queueing of the policy assistant logs failed");
            throw new DataSourceException(e);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.dabs.components.log.ILogWriter#log(com.bluejungle.domain.log.TrackingLogEntry[])
     */
    public void log(TrackingLogEntry[] logEntries) throws DataSourceException {
        try {
            logQueueMgr.queueLogs(logEntries);
        } catch (LogQueueException e) {
            this.getLog().error("Queueing of the tracking activity logs failed");
            throw new DataSourceException(e);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.dabs.components.log.ILogWriter#log(com.bluejungle.domain.log.TrackingLogEntryV2[])
     */
    public void log(TrackingLogEntryV2[] logEntries) throws DataSourceException {
        try {
            logQueueMgr.queueLogs(logEntries);
        } catch (LogQueueException e) {
            this.getLog().error("Queueing of the tracking activity logs (v2) failed");
            throw new DataSourceException(e);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.dabs.components.log.ILogWriter#log(com.nextlabs.domain.log.TrackingLogEntryV3[])
     */
    public void log(TrackingLogEntryV3[] logEntries) throws DataSourceException {
        try {
            logQueueMgr.queueLogs(logEntries);
        } catch (LogQueueException e) {
            this.getLog().error("Queueing of the tracking activity logs (v3) failed");
            throw new DataSourceException(e);
        }
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }
    
    /**
     * 
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * 
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }
    
    /**
     * Initializes the file system buffering log writer
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        this.logQueueMgr = this.configuration.get(LOG_QUEUE_MGR_PARAM);
    }
}
