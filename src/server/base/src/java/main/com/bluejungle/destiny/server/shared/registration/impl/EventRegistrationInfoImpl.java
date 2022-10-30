/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration.impl;

import java.net.URL;

import com.bluejungle.destiny.server.shared.registration.IEventRegistrationInfo;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/impl/EventRegistrationInfoImpl.java#1 $
 */

public class EventRegistrationInfoImpl implements IEventRegistrationInfo {

    final private String name;
    final private URL callbackURL;
    private boolean active;

    /**
     * Constructor
     */
    public EventRegistrationInfoImpl(String name, URL callbackURL, boolean active) {
        super();
        this.active = active;
        this.callbackURL = callbackURL;
        this.active = active;
        this.name = name;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IEventRegistrationInfo#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IEventRegistrationInfo#getCallbackURL()
     */
    public URL getCallbackURL() {
        return this.callbackURL;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IEventRegistrationInfo#isActive()
     */
    public boolean isActive() {
        return this.active;
    }

}
