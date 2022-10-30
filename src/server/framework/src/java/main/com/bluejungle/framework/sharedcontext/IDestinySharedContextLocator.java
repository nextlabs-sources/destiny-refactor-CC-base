/*
 * Created on Dec 6, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.sharedcontext;

import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;

/**
 * This is the Destiny shared context locator interface. This interface is
 * implemented by the locator that can find the instance of the current Destiny
 * Shared context. This level of indirection is provided mostly to help unit
 * testing.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcc/IDestinySharedContextLocator.java#1 $:
 */

public interface IDestinySharedContextLocator {

    public static final String COMP_NAME = "DestinySharedContextLocator";

    /**
     * Returns the Destiny Shared context
     * 
     * @return the Destiny Shared context
     */
    IDestinySharedContext getSharedContext();
}