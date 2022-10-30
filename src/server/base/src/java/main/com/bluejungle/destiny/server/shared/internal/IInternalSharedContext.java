/*
 * Created on Jan 17, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.internal;

import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.exceptions.FactoryInitException;

/**
 * This is the initialization API for the shared context. This is a private
 * interface.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/internal/IInternalSharedContext.java#2 $
 */

public interface IInternalSharedContext extends IDestinySharedContext {

    /**
     * Initializes the shared context
     * 
     * @param eventMgr
     *            event manager class name
     * @param regMgr
     *            registration manager class name
     * @throws FactoryInitException
     *             if init fails
     */
    void init(String eventMgr, String regMgr) throws FactoryInitException;

    /**
     * Destroy the shared context
     */
    void destroy();
}
