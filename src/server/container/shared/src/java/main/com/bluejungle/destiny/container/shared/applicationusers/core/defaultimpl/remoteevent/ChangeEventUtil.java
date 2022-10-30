/*
 * Created on Sep 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.remoteevent;

import java.util.Properties;

import com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.ApplicationUserManagerImpl;
import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.impl.DCCServerEventImpl;

/**
 * This class wraps around the Axis event object.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/core/defaultimpl/remoteevent/axis/ChangeEventWrapper.java#1 $
 */

public class ChangeEventUtil {

    public static final String EVENT_NAME = ApplicationUserManagerImpl.class.getName() + ":" + "ChangeNotification";
    public static final String ACTION_PROP_NAME = "A";
    public static final String ID_PROP_NAME = "I";

    /**
     * Creates a custom event for dispatch from the event attributes provided
     * 
     * @param action
     * @param id
     * @return
     */
    public static IDCCServerEvent createEvent(ActionTypeEnumType action, long id) {
        IDCCServerEvent result = new DCCServerEventImpl(EVENT_NAME);
        final Properties props = result.getProperties();
        props.setProperty(ACTION_PROP_NAME, action.getName());
        props.setProperty(ID_PROP_NAME, String.valueOf(id));
        return result;
    }
}