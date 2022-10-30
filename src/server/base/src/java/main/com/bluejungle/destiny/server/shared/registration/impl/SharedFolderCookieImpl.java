/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration.impl;

import java.util.Calendar;

import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;

/**
 * This is the shared folder cookie implementation class.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/impl/SharedFolderCookieImpl.java#1 $
 */

public class SharedFolderCookieImpl implements ISharedFolderCookie {

    private Calendar timestamp;

    /**
     * Constructor
     * 
     * @param time
     *            cookie timestamp
     */
    public SharedFolderCookieImpl(Calendar time) {
        super();
        timestamp = time;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie#getTimestamp()
     */
    public Calendar getTimestamp() {
        return this.timestamp;
    }

}
