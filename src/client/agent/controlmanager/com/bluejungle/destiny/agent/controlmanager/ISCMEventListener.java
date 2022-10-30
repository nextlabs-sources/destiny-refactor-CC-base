/*
 * Created on Nov 2, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager;

/**
 * @author hfriedland
 */
public interface ISCMEventListener {

    public void handleSCMEvent(SCMEvent event);
}
