/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration.impl;

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.server.shared.registration.ISharedFolderAlias;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderAliasesAlias;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/impl/SharedFolderAliasImpl.java#1 $
 */

public class SharedFolderAliasImpl implements ISharedFolderAlias {

    private List aliases = new ArrayList();

    /**
     * Constructor
     * 
     * @param name
     *            name of the alias
     */
    public SharedFolderAliasImpl() {
        super();
    }

    /**
     * Adds a new alias
     * 
     * @param newAlias
     *            new alias to add
     */
    public void addAlias(ISharedFolderAliasesAlias newAlias) {
        this.aliases.add(newAlias);
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.ISharedFolderAliasesAlias#getAliases()
     */
    public ISharedFolderAliasesAlias[] getAliases() {
        int size = this.aliases.size();
        ISharedFolderAliasesAlias[] result = new ISharedFolderAliasesAlias[size];
        for (int i = 0; i < size; i++) {
            result[i] = (ISharedFolderAliasesAlias) this.aliases.get(i);
        }
        return result;
    }
}
