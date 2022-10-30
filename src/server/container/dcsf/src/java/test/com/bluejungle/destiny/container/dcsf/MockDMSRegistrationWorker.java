/*
 * Created on Dec 8, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dcsf.IDMSRegistrationWorker;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.threading.ITask;

/**
 * Mock object for the DMS registration worker
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/tests/MockDMSRegistrationWorker.java#1 $:
 */

public class MockDMSRegistrationWorker implements IDMSRegistrationWorker {

    private static List taskList = new ArrayList();
    private static Integer initCount = new Integer(0);
    private static Integer configCount = new Integer(0);

    /**
     * Constructor
     *  
     */
    public MockDMSRegistrationWorker() {
        super();
    }

    /**
     * @param taskDefinition
     * @see com.bluejungle.framework.threading.IWorker#doWork(com.bluejungle.framework.threading.ITask)
     */
    public synchronized void doWork(ITask taskDefinition) {
        synchronized (MockDMSRegistrationWorker.taskList) {
            MockDMSRegistrationWorker.taskList.add(taskDefinition);
        }
    }

    /**
     * @param conf
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration conf) {
        synchronized (MockDMSRegistrationWorker.configCount) {
            int currentCount = MockDMSRegistrationWorker.configCount.intValue();
            currentCount++;
            MockDMSRegistrationWorker.configCount = new Integer(currentCount);
        }
    }

    /**
     * @return the configuration
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return null;
    }

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        synchronized (MockDMSRegistrationWorker.initCount) {
            int currentCount = MockDMSRegistrationWorker.initCount.intValue();
            currentCount++;
            MockDMSRegistrationWorker.initCount = new Integer(currentCount);
        }
    }

    /**
     * Returns the requested task lists
     * 
     * @return the requested task lists
     */
    public static List getTaskList() {
        return MockDMSRegistrationWorker.taskList;
    }

    /**
     * Returns the number of inits
     * 
     * @return the number of inits
     */
    public static Integer getInitCounts() {
        return MockDMSRegistrationWorker.initCount;
    }

    /**
     * Returns the number of config
     * 
     * @return the number of config
     */
    public static Integer getConfigCounts() {
        return MockDMSRegistrationWorker.configCount;
    }

    /**
     * Sets the new log object
     * 
     * @param log
     *            new log to set
     */
    public void setLog(Log log) {
    }

    /**
     * Returns the log object
     * @return the log
     */
    public Log getLog() {
        return null;
    }
}