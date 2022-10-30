/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;

import java.util.Calendar;

/**
 * This is the shared folder data cookie interface. It contains the information
 * regarding shared folder used during the heartbeat.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/ISharedFolderCookie.java#1 $
 */

public interface ISharedFolderCookie {

    /**
     * Returns the cookie timestamp
     * 
     * @return the cookie timestamp
     */
    public Calendar getTimestamp();
}
