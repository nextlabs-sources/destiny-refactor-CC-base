/*
 * Created on May 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.sharedfolder;

import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderData;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/sharedfolder/MockSharedFolderInformationSource.java#1 $
 */

public class MockSharedFolderInformationSource implements ISharedFolderInformationSource {

    /**
     * Constructor
     *  
     */
    public MockSharedFolderInformationSource() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationSource#getSharedFolderInformationUpdateSince(com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie)
     */
    public ISharedFolderData getSharedFolderInformationUpdateSince(ISharedFolderCookie cookie) {
        return null;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
    }

}
