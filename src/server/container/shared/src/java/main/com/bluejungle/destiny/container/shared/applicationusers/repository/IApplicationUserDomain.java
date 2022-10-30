/*
 * Created on Jun 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository;

import java.util.Collection;
import java.util.SortedSet;

import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserCreationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserImportFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.IDomain;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser;

/**
 * This interface represents an application domain - i.e. a domain that exists
 * in the application user repository, containing imported application users.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/framework/src/java/main/com/bluejungle/framework/applicationusers/IApplicationUserDomain.java#1 $
 */

public interface IApplicationUserDomain extends IDomain {

    /**
     * Returns a collection of application users <IApplicationUser>in this
     * domain
     * 
     * @return collection of application users <IApplicationUser>
     */
    public SortedSet getApplicationUsers() throws ApplicationUserRepositoryAccessException;

    /**
     * Creates an application user from the provided external user.
     * 
     * @param user
     * @return an application user
     */
    public IApplicationUser importExternalUser(IExternalUser userToImport) throws ApplicationUserImportFailedException, UserAlreadyExistsException;

    /**
     * Creates a linked application user group from the provided external user
     * group
     * 
     * @param externalId
     * @param title
     * @throws GroupLinkAlreadyExistsException
     * @throws GroupAlreadyExistsException
     * @return linked user group
     */
    public ILinkedAccessGroup linkExternalGroup(byte[] externalId, String title) throws GroupAlreadyExistsException, GroupLinkAlreadyExistsException, ApplicationUserRepositoryAccessException;

    /**
     * Returns the linked group that links to the provided external id.
     * 
     * @param externalId
     * @return linked group
     * @throws GroupNotFoundException
     * @throws ApplicationUserRepositoryAccessException
     */
    public ILinkedAccessGroup getLinkedGroupByExternalId(byte[] externalId) throws GroupNotFoundException, ApplicationUserRepositoryAccessException;

    /**
     * Deletes an application user entry. Returns true if the user was found and
     * deleted.
     * 
     * @param userToDelete
     * @return
     */
    public void deleteApplicationUser(IApplicationUser userToDelete) throws ApplicationUserRepositoryAccessException, UserNotFoundException;

    /**
     * Sets the default role for the given application user
     * 
     * @param user
     * @param primaryGroup
     * @throws ApplicationUserRepositoryAccessException
     */
    public void setPrimaryAccessGroupForUser(IApplicationUser user, IAccessGroup primaryGroup) throws ApplicationUserRepositoryAccessException;

    /**
     * Sets the Access Control string that will be inherited by all objects
     * created by the users within this group.
     * 
     * @param group
     * @param acl
     * @throws
     */
    public void setApplicableAccessControlForGroup(IAccessGroup group, String acl) throws ApplicationUserRepositoryAccessException;

    /**
     * Retrieves an application user entry given it's login.
     * 
     * @param login
     * @return application user
     */
    public IApplicationUser getUser(String login) throws ApplicationUserRepositoryAccessException, UserNotFoundException;

    /**
     * Retrieves an access group entry with the given title
     * 
     * @param title
     * @return access group
     * @throws ApplicationUserRepositoryAccessException
     * @throws GroupNotFoundException
     */
    public IAccessGroup getAccessGroup(String title) throws ApplicationUserRepositoryAccessException, GroupNotFoundException;

    /**
     * Creates a new user.
     * 
     * @param login
     * @param fn
     * @param ln
     * @param displayName
     * @param password
     *            plaintext password for the user
     * @throws ApplicationUserRepositoryAccessException
     * @throws UserAlreadyExistsException
     *             if a user with a given login already exists in the repository
     */
    public IApplicationUser createNewUser(String login, String fn, String ln, String password) throws ApplicationUserCreationFailedException, UserAlreadyExistsException;

    /**
     * Updates first name, last name, and password of the given user. If any one
     * of those things is null, it is not updated. The update is done
     * atomically.
     * 
     * @param user
     *            user to update
     * @param password
     *            new password
     * @throws UserNotFoundException
     * @throws ApplicationUserRepositoryAccessException
     */
    public void updateUser(IApplicationUser user, String password) throws UserNotFoundException, ApplicationUserRepositoryAccessException;

    /**
     * Creates a new group with the given title and description
     * 
     * @param title
     * @param description
     * @return newly created access group
     * @throws ApplicationUserRepositoryAccessException
     * @throws GroupAlreadyExistsException
     */
    public IInternalAccessGroup createAccessGroup(String title, String description) throws ApplicationUserRepositoryAccessException, GroupAlreadyExistsException;

    /**
     * Deletes the given access group from the repository
     * 
     * @param groupToDelete
     * @throws ApplicationUserRepositoryAccessException
     */
    public void deleteAccessGroup(IAccessGroup groupToDelete) throws ApplicationUserRepositoryAccessException;

    /**
     * Updates the description of the given access group in the repository
     * 
     * @param groupToUpdate
     * @param description
     * @throws ApplicationUserRepositoryAccessException
     */
    public void setDescriptionForGroup(IAccessGroup groupToUpdate, String description) throws ApplicationUserRepositoryAccessException;

    /**
     * Updates the title of the given access group in the repository
     * 
     * @param groupToUpdate
     * @param title
     * @throws ApplicationUserRepositoryAccessException
     * @throws GroupAlreadyExistsException
     */
    public void setTitleForGroup(IAccessGroup groupToUpdate, String newTitle) throws ApplicationUserRepositoryAccessException, GroupAlreadyExistsException;

    /**
     * Add the collection of user entries into the given group.
     * 
     * @param group
     * @param users
     * @throws ApplicationUserRepositoryAccessException
     * @throws UserNotFoundException
     */
    public void addUsersToGroup(IInternalAccessGroup group, Collection<IApplicationUser> users) throws ApplicationUserRepositoryAccessException;

    /**
     * Remove the collection of user entries from the given group
     * 
     * @param group
     * @param users
     * @throws ApplicationUserRepositoryAccessException
     */
    public void removeUsersFromGroup(IInternalAccessGroup group, Collection users) throws ApplicationUserRepositoryAccessException;
}