/*
 * Created on May 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.sharedfolder;

import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderData;
import com.bluejungle.framework.comp.IInitializable;

/**
 * This interface represents the component that acts as a sink for
 * ISharedFolderInformationDO objects that are passed between DCC components
 * (DMS and DABS).
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/sharedfolder/ISharedFolderInformationSink.java#1 $
 */

public interface ISharedFolderInformationSink extends IInitializable {

    public static final String COMP_NAME = "SharedFolderInformationSink";

    /**
     * This method should persist the shared folder data.
     * 
     * @param sharedFolderData
     */
    public void setUpdate(ISharedFolderData sharedFolderData);

    /**
     * This method should return the cookie corresponding to the last update
     * that was set.
     * 
     * @return cookie for last update
     */
    public ISharedFolderCookie getLastUpdateCookie();
}