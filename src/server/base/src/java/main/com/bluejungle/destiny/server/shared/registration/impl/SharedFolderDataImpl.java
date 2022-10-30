/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bluejungle.destiny.server.shared.registration.ISharedFolderAlias;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderData;

/**
 * This is the implementation class for the shared folder data
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/impl/SharedFolderDataImpl.java#1 $
 */

public class SharedFolderDataImpl implements ISharedFolderData {

    private ISharedFolderCookie cookie;
    private List aliasList = new ArrayList();

    /**
     * Constructor
     */
    public SharedFolderDataImpl() {
        super();
    }

    /**
     * Add a shared folder alias
     * 
     * @param alias
     *            alias to add
     */
    public void addSharedFolderAlias(ISharedFolderAlias alias) {
        this.aliasList.add(alias);
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.ISharedFolderData#getCookie()
     */
    public ISharedFolderCookie getCookie() {
        return this.cookie;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.ISharedFolderData#getAliases()
     */
    public ISharedFolderAlias[] getAliases() {
        int size = this.aliasList.size();
        ISharedFolderAlias[] result = new ISharedFolderAlias[size];
        Iterator it = this.aliasList.iterator();
        for (int i = 0; i < size; i++) {
            result[i] = (ISharedFolderAlias) this.aliasList.get(i);
        }
        return result;
    }

    /**
     * Sets the cookie
     * 
     * @param cookie
     *            cookie to be set
     */
    public void setCookie(ISharedFolderCookie cookie) {
        this.cookie = cookie;
    }

}
