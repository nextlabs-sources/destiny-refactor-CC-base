/*
 * Created on Sep 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl;

import com.bluejungle.destiny.container.shared.applicationusers.core.IUserManagementObserver;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/core/defaultimpl/MockUserManagementObserverImpl.java#1 $
 */

public class MockUserManagementObserverImpl implements IUserManagementObserver {

    public static final int USER_DELETE = 0;
    public static final int GROUP_DELETE = 1;

    private int lastOperation;
    private long lastChangedId;

    /**
     * Constructor
     *  
     */
    public MockUserManagementObserverImpl() {
        super();
    }

    public int getLastOperation() {
        return this.lastOperation;
    }

    public long getLastChangedId() {
        return this.lastChangedId;
    }

    public void reset() {
        this.lastChangedId = -1;
        this.lastOperation = -1;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUserManagementObserver#onUserDelete(long)
     */
    public void onUserDelete(long deletedUserId) {
        this.lastOperation = USER_DELETE;
        this.lastChangedId = deletedUserId;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUserManagementObserver#onGroupDelete(long)
     */
    public void onGroupDelete(long deletedGroupId) {
        this.lastOperation = GROUP_DELETE;
        this.lastChangedId = deletedGroupId;
    }
}