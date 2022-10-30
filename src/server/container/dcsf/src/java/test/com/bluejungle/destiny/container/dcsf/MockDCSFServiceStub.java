/*
 * Created on Dec 9, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.framework.types.BadArgumentFault;
import com.bluejungle.destiny.services.dcsf.DCSFServiceIF;
import com.bluejungle.destiny.services.dcsf.types.DestinyEvent;

/**
 * This dummy web service is used to spy the calls from the event registration
 * manager
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/tests/MockDCSFServiceStub.java#1 $:
 */

public class MockDCSFServiceStub implements DCSFServiceIF {

    private static final List EVENT_NOTIFICATION_LIST = new ArrayList();

    /**
     * @param event
     *            event to be notified
     * @throws BadArgumentException
     * @throws RemoteException
     * @see com.bluejungle.destiny.services.dcsf.DCSFServiceIF#notifyEvent(com.bluejungle.destiny.services.dcsf.types.DestinyEvent)
     */
    public void notifyEvent(DestinyEvent event) throws RemoteException {
        synchronized (EVENT_NOTIFICATION_LIST) {
            EVENT_NOTIFICATION_LIST.add(event);
        }
    }

    /**
     * Returns the event List. The caller should acquire a lock before looking
     * at the object.
     * 
     * @return the event List.
     */
    public static List getEventNotificationList() {
        return EVENT_NOTIFICATION_LIST;
    }
}
