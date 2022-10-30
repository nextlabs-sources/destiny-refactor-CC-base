/*
 * Created on May 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.sharedfolder;

import java.util.Calendar;

import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/sharedfolder/MockSharedFolderInformationCookie.java#1 $
 */

public class MockSharedFolderInformationCookie implements ISharedFolderCookie {

    /**
     * Constructor
     *  
     */
    public MockSharedFolderInformationCookie() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationCookie#getTimestamp()
     */
    public Calendar getTimestamp() {
        return null;
    }
}
