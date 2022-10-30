package com.bluejungle.destiny.container.dabs.components.sharedfolder.defaultimpl;

/*
 * Created on May 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

import com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationRelay;
import com.bluejungle.destiny.container.shared.sharedfolder.defaultimpl.SharedFolderInformationCookieImpl;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderData;
import com.bluejungle.destiny.server.shared.registration.impl.SharedFolderDataImpl;

/**
 * This class acts as a relay (located on DABS) to pass the shared folder
 * information from DMS to the Agents.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dabs/src/java/main/com/bluejungle/destiny/container/dabs/components/sharedfolder/defaultimpl/DABSSharedFolderInformationRelayImpl.java#3 $
 */

public class DABSSharedFolderInformationRelayImpl implements ISharedFolderInformationRelay {

    ISharedFolderData data = null;

    /**
     * Constructor
     *  
     */
    public DABSSharedFolderInformationRelayImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationSource#getSharedFolderInformationUpdateSince(com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie)
     */
    public ISharedFolderData getSharedFolderInformationUpdateSince(ISharedFolderCookie cookie) {
        if (cookie == null) {
            cookie = new SharedFolderInformationCookieImpl();
        }
        if (data == null) {
            return null;
        } else if (data.getCookie().getTimestamp().equals(cookie.getTimestamp())) {
            // Return the same cookie but no data to indicate that nothing has changed
            SharedFolderDataImpl unchangedData = new SharedFolderDataImpl();
            unchangedData.setCookie(data.getCookie());

            return unchangedData;
        }
        return data;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationSink#setUpdate(com.bluejungle.destiny.server.shared.registration.ISharedFolderData)
     */
    public void setUpdate(ISharedFolderData sharedFolderData) {
        if (data == null || sharedFolderData == null || !data.getCookie().getTimestamp().equals(sharedFolderData.getCookie().getTimestamp())) {
            data = sharedFolderData;
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationSink#getLastUpdateCookie()
     */
    public ISharedFolderCookie getLastUpdateCookie() {
        return data != null ? data.getCookie() : new SharedFolderInformationCookieImpl();
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
    }
}
