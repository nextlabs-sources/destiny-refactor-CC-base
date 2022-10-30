/*
 * Created on Dec 3, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.threading.IWorker;

/**
 * This interface is implemented by the remote event dispatch worker object
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dcsf/src/java/main/com/bluejungle/destiny/container/dcsf/IRemoteEventDispatchWorker.java#1 $:
 */

public interface IRemoteEventDispatchWorker extends IWorker, IDisposable, IInitializable, ILogEnabled {
    
    public static final String COMP_NAME = "RemoteEventDispatchWorker";

}