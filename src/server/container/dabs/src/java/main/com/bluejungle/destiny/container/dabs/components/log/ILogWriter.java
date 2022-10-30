/*
 * Created on Jan 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs.components.log;

import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.log.PolicyActivityLogEntryV2;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.domain.log.TrackingLogEntry;
import com.bluejungle.domain.log.TrackingLogEntryV2;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.nextlabs.domain.log.PolicyActivityLogEntryV3;
import com.nextlabs.domain.log.PolicyActivityLogEntryV4;
import com.nextlabs.domain.log.PolicyActivityLogEntryV5;
import com.nextlabs.domain.log.TrackingLogEntryV3;

/**
 * An instance of ILogWriter is responsible for persisting Destiny analytical
 * logs
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dabs/com/bluejungle/destiny/container/dabs/components/log/ILogWriter.java#4 $
 */
public interface ILogWriter {

    /**
     * <code>COMP_NAME</code> is the name of the ILogWriter component. This
     * name is used to retrieve an instance of a ILogWriter from the
     * ComponentManager
     */
    public static final String COMP_NAME = "logWriter";
    
    
    public void log(PolicyActivityLogEntry[] logEntries) throws DataSourceException;

    public void log(PolicyActivityLogEntryV2[] logEntries) throws DataSourceException;

    public void log(PolicyActivityLogEntryV3[] logEntries) throws DataSourceException;

    public void log(PolicyActivityLogEntryV4[] logEntries) throws DataSourceException;
    
    public void log(PolicyActivityLogEntryV5[] logEntries) throws DataSourceException;
    
    public void log(PolicyAssistantLogEntry[] logEntries) throws DataSourceException;

    public void log(TrackingLogEntry[] logEntries) throws DataSourceException;

    public void log(TrackingLogEntryV2[] logEntries) throws DataSourceException;

    public void log(TrackingLogEntryV3[] logEntries) throws DataSourceException;

}
