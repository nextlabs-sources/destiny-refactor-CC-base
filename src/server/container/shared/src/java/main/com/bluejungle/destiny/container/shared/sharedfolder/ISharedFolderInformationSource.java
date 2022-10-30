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
 * This interface represents the component that acts as a source for
 * ISharedFolderInformationDO objects that are passed between DCC components
 * (DMS and DABS).
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/sharedfolder/ISharedFolderInformationSource.java#1 $
 */

public interface ISharedFolderInformationSource extends IInitializable {

    public static final String COMP_NAME = "SharedFolderInformationSource";

    /**
     * This method checks to see if the provided cookie is up-to-date and should
     * return data that is more up-to-date if not.
     * 
     * @param cookie
     * @return up-to-date shared folder data
     */
    public ISharedFolderData getSharedFolderInformationUpdateSince(ISharedFolderCookie cookie);
}