/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;

/**
 * This is the shared folder data interface
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/ISharedFolderData.java#1 $
 */

public interface ISharedFolderData {

    /**
     * Returns the shared folder cookie
     * 
     * @return the shared folder cookie
     */
    public ISharedFolderCookie getCookie();

    /**
     * Returns the shared folder aliases
     * 
     * @return the shared folder aliases
     */
    public ISharedFolderAlias[] getAliases();
}
