/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration.impl;

import com.bluejungle.destiny.server.shared.registration.ISharedFolderAliasesAlias;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/impl/SharedFolderAliasesAliasImpl.java#1 $
 */

public class SharedFolderAliasesAliasImpl implements ISharedFolderAliasesAlias {

    private String name;

    /**
     * Constructor
     * 
     * @param name
     *            alias name
     */
    public SharedFolderAliasesAliasImpl(String name) {
        super();
        this.name = name;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.ISharedFolderAlias#getName()
     */
    public String getName() {
        return this.name;
    }

}
