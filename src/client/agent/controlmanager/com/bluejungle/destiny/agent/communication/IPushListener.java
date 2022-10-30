/*
 * Created on Jan 18, 2005
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.destiny.agent.communication;

import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.IStartable;

/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/communication/IPushListener.java#1 $:
 */

public interface IPushListener extends IInitializable, IStartable {

    /**
     * @return port number on which the PushListener is listening. -1 if no port
     *         is set up
     */
    public abstract int getPort();
}