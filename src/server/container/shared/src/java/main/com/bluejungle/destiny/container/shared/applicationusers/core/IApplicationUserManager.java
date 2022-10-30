/*
 * Created on Jun 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomain;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroupLinkData;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser;
import com.bluejungle.destiny.container.shared.applicationusers.external.IGroupSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ApplicationUserRepositoryAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAuthenticationDomain;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IInternalAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ILinkedAccessGroup;
import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;

/**
 * Top level application-user management system interface
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/framework/src/java/main/com/bluejungle/framework/applicationusers/IApplicationUserManager.java#1 $
 */

public interface IApplicationUserManager extends ISingleDomainAuthenticationManager, IDestinyEventListener {

    long RESERVED_SUPER_USER_ID = 0;
    String SUPER_USER_USERNAME = "Administrator";
    
    /**
     * Initializes the application user manager with a configuration interface
     * 
     * @param configuration
     * @throws InvalidConfigurationException
     * @throws InitializationException
     */
    void initialize(IApplicationUserConfigurationDO configuration) throws InvalidConfigurationException, InitializationException;

    /**
     * Adds the given object as an observer of changes such as deletion of user
     * and group entries. The range of supported notifications can be determined
     * from the observer interface <IUserManagementObserver>
     * 
     * @param observer
     */
    void addListener(IUserManagementObserver observer);

    /**
     * Returns the authentication mode being used
     * 
     * @return authentcation mode
     */
    AuthenticationModeEnumType getAuthenticationMode();

    /**
     * Returns a collection of all the existing application users
     * <IApplicationUser>
     * 
     * @param array
     *            of user search specs
     * @param maxResultsToReturn -
     *            indicates max results to return
     * @throws UserManagementAccessException
     * @return set of all existing application users
     */
    SortedSet<IApplicationUser> getApplicationUsers(IUserSearchSpec[] searchSpecs, int maxResultsToReturn) throws UserManagementAccessException;

    /**
     * Returns a collection of all the available domain names
     * 
     * @throws UserManagementAccessException
     * @return domain names
     */
    Collection<String> getDomainNames() throws UserManagementAccessException;

    /**
     * Returns the name of the default local domain name
     * 
     * @return default local domain name
     */
    String getDefaultLocalDomainName();

    /**
     * Returns an <IAuthenticationDomain>instance with the given name against
     * which users can be authenticated.
     * 
     * @throws UserManagementAccessException
     * @throws DomainNotFoundException
     * @return the authentication domain instance
     */
    IAuthenticationDomain getAuthenticationDomain(String domainName) throws DomainNotFoundException, UserManagementAccessException;

    /**
     * Returns a set of the available external domains <IExternalDomain>, if
     * applicable. This method will return an empty set if using local
     * authentication.
     * 
     * @throws UserManagementAccessException
     * @return set of external domains, if applicable
     */
    Set<IExternalDomain> getExternalDomains() throws UserManagementAccessException;

    /**
     * Returns a collection of external users - in essence this gets all
     * external domains and iterates over the users in all those domains. The
     * returned list is sorted.
     * 
     * @param search specs
     * @param maxResultsToReturn -
     *            indicates max results to return
     * @throws UserManagementAccessException
     * @return all external users
     */
    SortedSet<IExternalUser> getExternalUsers(IUserSearchSpec[] searchSpecs, int maxResultsToReturn) throws UserManagementAccessException;

    /**
     * Returns an <IApplicationUserDomain>instance with the given name against
     * which users can be authenticated.
     * 
     * @throws UserManagementAccessException
     * @throws DomainNotFoundException
     * @return the application domain instance
     */
    IApplicationUserDomain getApplicationUserDomain(String domainName) throws DomainNotFoundException, UserManagementAccessException;

    /**
     * Returns the super user entry
     * 
     * @return
     * @throws UserManagementAccessException 
     */
    IApplicationUser getSuperUser() throws UserManagementAccessException;

    /**
     * Returns the application user entry with the given destiny id
     * 
     * @param id
     * @return application user
     * @throws UserManagementAccessException
     * @throws UserNotFoundException
     */
    IApplicationUser getApplicationUser(long id) throws UserManagementAccessException, UserNotFoundException;

    /**
     * Returns the application user with the given unique name
     * 
     * @param uniqueName
     * @return application user
     * @throws UserNotFoundException
     * @throws ApplicationUserRepositoryAccessException
     */
    IApplicationUser getApplicationUser(String uniqueName) throws UserNotFoundException, UserManagementAccessException;

    /**
     * Creates an application user from the provided external user.
     * 
     * @param user
     * @throws ApplicationUserImportFailedException
     * @throws UserAlreadyExistsException
     * @return an application user
     */
    IApplicationUser importExternalUser(IExternalUser userToImport) throws ApplicationUserImportFailedException, UserAlreadyExistsException;

    /**
     * Creates an application user from the provided user with specified
     * password
     * 
     * @param login
     * @param fn
     * @param ln
     * @param displayName
     * @param password
     * @throws ApplicationUserCreationFailedException
     * @throws UserAlreadyExistsException
     */
    IApplicationUser createUser(String login, String fn, String ln, String password) throws ApplicationUserCreationFailedException, UserAlreadyExistsException;

    /**
     * Deletes the application user with given destiny id
     * 
     * @param id
     * @throws ApplicationUserDeleteFailedException
     * @throws UserNotFoundException
     */
    void deleteApplicationUser(long id) throws ApplicationUserDeleteFailedException, UserNotFoundException;

    /**
     * Sets the primary access group setting for given user with given destiny
     * id
     * 
     * @param userId
     * @param groupId
     * @throws ApplicationUserModificationFailedException
     * @throws UserNotFoundException
     * @throws GroupNotFoundException
     */
    void setPrimaryAccessGroupForUser(long userId, Long groupId) throws ApplicationUserModificationFailedException, UserNotFoundException, GroupNotFoundException;

    /**
     * Returns the primary access group for the provided user id
     * 
     * @param userId
     * @return primary access group for user
     * @throws UserNotFoundException
     */
    IAccessGroup getPrimaryAccessGroupForUser(long userId) throws UserNotFoundException, UserManagementAccessException;

    /**
     * Returns a collection of the access groups <IAccessGroup>currently in the
     * system
     * 
     * @return collection of access groups
     * @throws UserManagementAccessException
     */
    SortedSet<IAccessGroup> getAccessGroups(IGroupSearchSpec[] searchSpecs, int pageSize) throws UserManagementAccessException;

    /**
     * Returns the access group with the given destiny id
     * 
     * @param groupId
     * @return access group
     * @throws UserManagementAccessException
     * @throws GroupNotFoundException
     */
    IAccessGroup getAccessGroup(long groupId) throws UserManagementAccessException, GroupNotFoundException;

    /**
     * Returns a collection of application users <IApplicationUser>that are
     * members of the access group with give id
     * 
     * @param groupId
     * @return collection of application users
     * @throws UserManagementAccessException
     * @throws GroupNotFoundException
     */
    SortedSet<IApplicationUser> getUsersInAccessGroup(long groupId) throws UserManagementAccessException, GroupNotFoundException;

    /**
     * Returns a collection of access groups <IAccessGroup>that have the given
     * user as a member. This collection includes both internal and linked
     * groups.
     * 
     * @param userId
     * @return
     * @throws UserManagementAccessException
     * @throws UserNotFoundException
     */
    Set<IAccessGroup> getAccessGroupsContainingUser(long userId) throws UserManagementAccessException, UserNotFoundException;

    /**
     * Add an array of user ids as members to an access group with given destiny
     * id. The given group id can only be an internal group, not a linked group.
     * If a linked group is provided, an IllegalArgumentException is thrown.
     * 
     * @param groupId
     * @param userIds
     * @throws AccessGroupModificationFailedException
     * @throws UserNotFoundException
     * @throws GroupNotFoundException
     */
    void addUsersToAccessGroup(long groupId, long[] userIds) throws AccessGroupModificationFailedException, GroupNotFoundException, UserNotFoundException;

    /**
     * Remove the given array of users from the access group with given destiny
     * id. The given group id can only be an internal group, not a linked group.
     * If a linked group is provided, an IllegalArgumentException is thrown.
     * 
     * @param groupId
     * @param userIds
     * @throws AccessGroupModificationFailedException
     * @throws UserNotFoundException
     * @throws GroupNotFoundException
     */
    void removeUsersFromAccessGroup(long groupId, long[] userIds) throws AccessGroupModificationFailedException, UserNotFoundException, GroupNotFoundException;

    /**
     * Sets the default access control assignment for the group with given
     * destiny id.
     * 
     * @param groupId
     * @param accessControlAssignment
     * @throws AccessGroupModificationFailedException
     * @throws GroupNotFoundException
     */
    void setDefaultAccessControlAssignmentForGroup(long groupId, String accessControlAssignment) throws AccessGroupModificationFailedException, GroupNotFoundException;

    /**
     * Creates an internal access group with the given title.
     * 
     * @param title
     * @param description
     * @throws AccessGroupCreationFailedException
     * @throws GroupAlreadyExistsException
     */
    IInternalAccessGroup createAccessGroup(String title, String description) throws AccessGroupCreationFailedException, GroupAlreadyExistsException;

    /**
     * Creates linked access groups with the given link data. This method should
     * follow a title renaming scheme to make sure that a given linked group
     * does not violate the title-uniqueness and can always be linked as long as
     * it doesn't refer to an external group that has already been linked
     * before. It is for this reason that it doesn't throw a
     * <GroupAlreadyExistsException>
     * 
     * @param links
     * @return array of equivalently indexed linked access groups
     * @throws AccessGroupCreationFailedException
     */
    ILinkedAccessGroup[] linkExternalAccessGroups(IExternalGroupLinkData[] links) throws AccessGroupCreationFailedException;

    /**
     * Updates a group with the given data
     * 
     * @param groupId
     * @param title
     * @param description
     * @throws AccessGroupModificationFailedException
     * @throws GroupNotFoundException
     * @throws GroupAlreadyExistsException
     */
    void updateGroup(long groupId, String title, String description) throws AccessGroupModificationFailedException, GroupNotFoundException, GroupAlreadyExistsException;

    /**
     * Delets a group with the given destiny id.
     * 
     * @param groupId
     * @throws AccessGroupDeletionFailedException
     */
    void deleteGroup(long groupId) throws AccessGroupDeletionFailedException, GroupNotFoundException;

    /**
     * Returns a collection of external groups <IExternalGroup>satisfying the
     * given search spec
     * 
     * @param searchSpecs
     * @param pageSize
     * @return
     * @throws UserManagementAccessException
     */
    SortedSet<IExternalGroup> getExternalGroups(IGroupSearchSpec[] searchSpecs, int pageSize) throws UserManagementAccessException;

    /**
     * Updates first name, last name, and password of the given user. If either
     * one of those things is null, it is not updated. The update is done
     * atomically.
     * 
     * @param user
     *            user with new attribute values
     * @param password
     *            new password
     * @throws UserNotFoundException
     *             if specified user cannot be found in the repository
     */
    void updateApplicationUser(IApplicationUser user, String password) throws UserNotFoundException, UserManagementAccessException;

    /**
     * Determine if the password can be changed on the provided user
     * 
     * @param authenticatedUser
     * @return true if the password can be changed, false otherwise
     */
    boolean canChangePassword(IApplicationUser applicationUser);

    /**
     * Determine if the specified application user is locally authenticated
     * 
     * @param applicationUser
     *            the user to test
     * @return true if locally authenticated; false otherwise
     */
    boolean isLocallyAuthenticated(IApplicationUser applicationUser);
}