/*
 * Created on May 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.sharedfolder;

import com.bluejungle.destiny.server.shared.registration.ISharedFolderAlias;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderData;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/sharedfolder/MockSharedFolderInformation.java#2 $
 */

public class MockSharedFolderInformation implements ISharedFolderData {

    private ISharedFolderCookie cookie;

    /**
     * Constructor
     */
    public MockSharedFolderInformation() {
        super();
        cookie = new MockSharedFolderInformationCookie();
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.ISharedFolderData#getCookie()
     */
    public ISharedFolderCookie getCookie() {
        return cookie;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.ISharedFolderData#getAliases()
     */
    public ISharedFolderAlias[] getAliases() {
        return null;
    }
}