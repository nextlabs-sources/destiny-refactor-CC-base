/*
 * Created on Apr 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.tools;

import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/test/com/bluejungle/pf/tools/MockSharedContextLocator.java#1 $
 */

public class MockSharedContextLocator implements IDestinySharedContextLocator {

    /*
     * Private variables:
     */
    private MockSharedContext mockSharedContext; 
    
    /**
     * Constructor
     *  
     */
    public MockSharedContextLocator() {
        super();
        this.mockSharedContext = new MockSharedContext();
    }

    /**
     * @see com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator#getSharedContext()
     */
    public IDestinySharedContext getSharedContext() {
        return this.mockSharedContext;
    }

}