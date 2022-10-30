/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;

import java.net.URL;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/IEventRegistrationInfo.java#1 $
 */

public interface IEventRegistrationInfo {

    /**
     * Returns the event name
     * 
     * @return the event name
     */
    public String getName();

    /**
     * Returns the event callback URL
     * 
     * @return the event callback URL
     */
    public URL getCallbackURL();

    /**
     * Returns if the event is active now
     * 
     * @return if the event is active now
     */
    public boolean isActive();
}
