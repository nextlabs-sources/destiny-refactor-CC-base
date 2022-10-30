/*
 * Created on Dec 13, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * This mock object returns an instance of a dummy registration manager
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/tests/MockSharedContextLocator.java#1 $:
 */

public class MockSharedContextLocator implements IDestinySharedContextLocator {

    IDestinySharedContext sharedContext = new MockSharedContext();

    /**
     * @return the shared context
     * @see com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator#getSharedContext()
     */
    public IDestinySharedContext getSharedContext() {
        return this.sharedContext;
    }
}