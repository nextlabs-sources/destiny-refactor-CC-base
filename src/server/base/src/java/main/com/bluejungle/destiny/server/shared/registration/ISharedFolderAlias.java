/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;

/**
 * This is the shared folder alias interface.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/ISharedFolderAlias.java#1 $
 */

public interface ISharedFolderAlias {

    /**
     * Returns the list of aliases
     * 
     * @return the list of aliases
     */
    public ISharedFolderAliasesAlias[] getAliases();
}
