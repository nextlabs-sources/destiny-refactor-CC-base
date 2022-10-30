/*
 * Created on Dec 6, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.sharedcontext;

import com.bluejungle.destiny.server.shared.context.DestinySharedContextFactory;
import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.exceptions.FactoryInitException;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.ILogEnabled;

import org.apache.commons.logging.Log;

/**
 * This is the implementation class for the shared content locator. 
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcc/DestinySharedContextLocatorImpl.java#1 $:
 */

public class DestinySharedContextLocatorImpl implements IDestinySharedContextLocator, ILogEnabled, IDisposable {

    protected static Log log;

    /**
     * Returns an instance of the current event manager. The current event
     * manager is in the shared JNDI context
     * 
     * @return an instance of the shared context
     */
    public IDestinySharedContext getSharedContext() {
        IDestinySharedContext sharedContext = null;
        try {
            sharedContext = DestinySharedContextFactory.getInstance().getSharedContext();
        } catch (FactoryInitException exception) {
            // FIX ME - Should throw to clients
            log.error("Unable to retrieve shared context : " + exception.getMessage());
        }
        return (sharedContext);
    }

    public void dispose() {
        DestinySharedContextFactory.getInstance().destroySharedContext();
    }

    /**
     * Sets the log object
     * 
     * @param logObj
     *            log object
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log logObj) {
        log = logObj;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return log;
    }
}
