/*
 * Created on Sep 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl;

import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/core/defaultimpl/MockSharedContextLocatorImpl.java#1 $
 */

public class MockSharedContextLocatorImpl implements IDestinySharedContextLocator {

    private MockSharedContextImpl sharedCtx;

    /**
     * Constructor
     *  
     */
    public MockSharedContextLocatorImpl() {
        super();
        this.sharedCtx = new MockSharedContextImpl();
    }

    /**
     * @see com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator#getSharedContext()
     */
    public IDestinySharedContext getSharedContext() {
        return this.sharedCtx;
    }
}