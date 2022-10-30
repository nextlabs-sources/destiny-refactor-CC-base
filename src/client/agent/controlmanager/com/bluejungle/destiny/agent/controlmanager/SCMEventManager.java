/*
 * Created on Nov 2, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager;

import java.util.ArrayList;

/**
 * @author hfriedland
 */
public class SCMEventManager {
    static private SCMEventManager mngr = null;
    private ArrayList<ISCMEventListener> listeners;

    /**
     * Constructor
     */
    private SCMEventManager() {
        this.listeners = new ArrayList<ISCMEventListener>();
    }

    public static synchronized SCMEventManager getInstance() {
        // this is a singleton -- create if does not exist
        // otherwise return existing object
        if (mngr == null) {
            mngr = new SCMEventManager();
        }

        return mngr;
    }

    public synchronized void dispatchSCMEvent(int eventID) {
        SCMEvent event = new SCMEvent(eventID);

        for (ISCMEventListener listener : listeners) {
            listener.handleSCMEvent(event);
        }
    }

    public synchronized void addSCMEventListener(ISCMEventListener listener) {
        //Add new listener to vector of listeners
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void removeSCMEventListener(ISCMEventListener listener) {
        //Remove the specified listener from the vector of listeners
        listeners.remove(listener);
    }
}
