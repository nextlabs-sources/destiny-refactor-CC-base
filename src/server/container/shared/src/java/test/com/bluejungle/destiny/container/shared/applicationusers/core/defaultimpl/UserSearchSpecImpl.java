/*
 * Created on Sep 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl;

import com.bluejungle.destiny.container.shared.applicationusers.core.IUserSearchSpec;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/core/defaultimpl/UserSearchSpecImpl.java#1 $
 */

public class UserSearchSpecImpl implements IUserSearchSpec {

    private String searchValue;

    /**
     * Constructor
     * 
     * @param searchValue
     */
    public UserSearchSpecImpl(String searchValue) {
        this.searchValue = searchValue;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUserSearchSpec#getLastNameStartsWith()
     */
    public String getLastNameStartsWith() {
        return this.searchValue;
    }
}