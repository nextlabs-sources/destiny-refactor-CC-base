/*
 * Created on Feb 12, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dabs.components.log;

import java.util.Map;

import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.framework.comp.IStartable;

/**
 * This is the interface for a log queue manager.  The implmentation classes
 * will manage the log queue and give the log writer a list of logs to write 
 * to the database
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/main/com/nextlabs/destiny/container/dabs/components/log/ILogQueueMgr.java#1 $
 */

public interface ILogQueueMgr extends IStartable{
            
    /**
     * <code>COMP_NAME</code> is the name of the ILogQueueMgr component. This
     * name is used to retrieve an instance of a ILogQueueMgr from the
     * ComponentManager
     */
    String COMP_NAME = "logQueueMgr";
    
    /**
     * Queues the group of logs, and assigns a log group id to the set of logs
     * 
     * @param logs
     * @return id assigned to the log group
     * @throws LogQueueException
     */
    Long queueLogs(BaseLogEntry[] logs) throws LogQueueException;
    
    /**
     * Dequeues the group of logs by the log group id
     * 
     * @param logGroupId
     * @return true if the group of logs is successfully dequeued
     *         false if the group of logs was not successfully dequeued
     * @throws LogQueueException
     */
    boolean dequeueLogs(Long... logGroupId) throws LogQueueException;

    /**
     * retrieve the next set of logs to write, this could be across multiple groups
     * 
     * @return an array of logs to write
     * @throws LogQueueException
     */
    Map<Long, BaseLogEntry[]> getLogsToWrite() throws LogQueueException;
    
    void addLogInsertTaskObserver(LogInsertTaskObserver logInsertTaskObserver);
    
    void removeLogInsertTaskObserver(LogInsertTaskObserver logInsertTaskObserver);
}
