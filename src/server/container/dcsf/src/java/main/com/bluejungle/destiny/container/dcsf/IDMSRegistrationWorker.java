/*
 * Created on Dec 2, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.threading.IWorker;

/**
 * This interface is implemented by the DMS registration worker object
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/src/java/main/com/bluejungle/destiny/container/dcsf/IDMSRegistrationWorker.java#1 $:
 */

public interface IDMSRegistrationWorker extends IWorker, IConfigurable, IDisposable, IInitializable, ILogEnabled {

    public static final String COMP_NAME = "DMSRegWorker";
    public static final String SLEEP_TIME_CONFIG_PARAM = "sleepTime";
}
