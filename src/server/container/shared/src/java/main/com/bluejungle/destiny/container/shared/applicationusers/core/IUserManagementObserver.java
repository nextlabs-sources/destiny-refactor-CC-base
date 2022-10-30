/*
 * Created on Sep 13, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core;


/**
 * This interface must be implemented by any object that wants to observe user
 * management changes such as deletion/addition of user/group entries. As more
 * change notifications are supported, this interface will grow larger.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/core/IUserManagementObserver.java#1 $
 */

public interface IUserManagementObserver {

    /**
     * This method will be invoked after the user has been deleted from the
     * repository. The implementation should assume that this user cannot be
     * queried from the repository any more. It is thus expected that whatever
     * cleanup is necessary is done using the user id. No exception is
     * advertised by this method, so all excpetions must be handled, or logged.
     * Since the user has been deleted already, this is the only time that this
     * method will be called to cleanup external references.
     * 
     * @param deletedUserId
     */
    public void onUserDelete(long deletedUserId);

    /**
     * This method will be invoked after the group has been deleted from the
     * repository. The implementation should assume that this group cannot be
     * queried from the repository any more. It is thus expected that whatever
     * cleanup is necessary is done using the group id. No exception is
     * advertised by this method, so all excpetions must be handled, or logged.
     * Since the group has been deleted already, this is the only time that this
     * method will be called to cleanup external references.
     * 
     * @param deletedGroupId
     */
    public void onGroupDelete(long deletedGroupId);
}