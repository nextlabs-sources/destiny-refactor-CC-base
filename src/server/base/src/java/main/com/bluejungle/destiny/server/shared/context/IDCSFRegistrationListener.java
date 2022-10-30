/*
 * Created on Dec 1, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.context;

import com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent;

/**
 * This interface is implemented by all the shared context module interested in
 * receiving a notification when the DCSF web application has registered.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/context/IDCSFRegistrationListener.java#1 $:
 */

public interface IDCSFRegistrationListener {

    /**
     * This function is called when the DCSF web application is registered with
     * the shared context. Subscribers to this event can access the DCSF web
     * application container interface.
     * 
     * @param dcsfContainer
     *            DCSF web application container object
     */
    public void onDCSFRegistered(IRegisteredDCSFComponent dcsfContainer);

    /**
     * This function is called whrn the DCSF web application unregisters with
     * the shared context. This could happen if the web application is shutdown
     * for some reason, but this is not a very likely scenario.
     * 
     * @param dcsfContainer
     *            DCSF web application container object
     */
    public void onDCSFUnRegistered(IRegisteredDCSFComponent dcsfContainer);
}