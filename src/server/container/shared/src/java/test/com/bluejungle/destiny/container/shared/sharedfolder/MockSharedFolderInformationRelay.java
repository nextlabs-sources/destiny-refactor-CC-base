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
 * This is a mock implementation of the shared folder information relay that is
 * for testing puprposes only.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/sharedfolder/MockSharedFolderInformationRelay.java#1 $
 */

public class MockSharedFolderInformationRelay implements ISharedFolderInformationRelay {

    private ISharedFolderCookie latestCookie;
    private ISharedFolderData latestUpdate;

    /**
     * Constructor
     *  
     */
    public MockSharedFolderInformationRelay() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationSource#getSharedFolderInformationUpdateSince(com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie)
     */
    public ISharedFolderData getSharedFolderInformationUpdateSince(ISharedFolderCookie cookie) {
        this.latestCookie = cookie;
        return null;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationSink#setUpdate(com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformation)
     */
    public void setUpdate(ISharedFolderData data) {
        this.latestUpdate = data;
    }

    /**
     * For testing purposes. Accessor method
     * 
     * @return
     */
    public ISharedFolderData getLastUpdate() {
        return this.latestUpdate;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationSink#getLastUpdateCookie()
     */
    public ISharedFolderCookie getLastUpdateCookie() {
        return this.latestCookie;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
    }
}