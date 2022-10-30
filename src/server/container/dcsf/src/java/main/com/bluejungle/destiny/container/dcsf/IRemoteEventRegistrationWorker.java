/*
 * Created on Dec 2, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.util.Set;

import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;

/**
 * This is the event registration worker interface
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dcsf/src/java/main/com/bluejungle/destiny/container/dcsf/IRemoteEventRegistrationWorker.java#1 $:
 */

public interface IRemoteEventRegistrationWorker extends IConfigurable, IDisposable, IInitializable, ILogEnabled {

    String COMP_NAME = "RemoteEventRegWorker";
    
    void processRequests (Set<String> registration, Set<String> unRegistration);    
}