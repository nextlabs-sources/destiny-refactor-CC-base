/*
 * Created on Dec 13, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.compmgr;

import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl.ConfigurationInitException;

/**
 * A Mock Shared Context Locator to return the MockSharedContext
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/test/com/bluejungle/destiny/container/dms/components/compmgr/MockSharedContextLocator.java#1 $
 */

public class MockSharedContextLocator implements IDestinySharedContextLocator {
    
    IDestinySharedContext sharedContext;
    
    /**
     * Constructor
     * @throws ConfigurationInitException
     */
    public MockSharedContextLocator() throws ConfigurationInitException {
        super();
        this.sharedContext = new MockSharedContext();
    }
    
    /**
     * @return the shared context
     * @see com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator#getSharedContext()
     */
    public IDestinySharedContext getSharedContext() {
        return this.sharedContext;
    }
}