/*
 * Created on Jun 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl;

import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator;
import com.bluejungle.destiny.container.shared.applicationusers.core.AccessGroupCreationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.AccessGroupDeletionFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.AccessGroupModificationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserCreationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserDeleteFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserImportFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserModificationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationModeEnumType;
import com.bluejungle.destiny.container.shared.applicationusers.core.DomainAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.DomainNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupComparatorImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager;
import com.bluejungle.destiny.container.shared.applicationusers.core.IUserManagementObserver;
import com.bluejungle.destiny.container.shared.applicationusers.core.IUserSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.core.InitializationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserComparatorImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserManagementAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.remoteevent.ActionTypeEnumType;
import com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.remoteevent.ChangeEventUtil;
import com.bluejungle.destiny.container.shared.applicationusers.external.ExternalDomainManagerImpl;
import com.bluejungle.destiny.container.shared.applicationusers.external.ExternalUserAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomain;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomainManager;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroupLinkData;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser;
import com.bluejungle.destiny.container.shared.applicationusers.external.IGroupSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ApplicationUserRepositoryAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.GroupLinkAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAuthenticatedUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAuthenticationDomain;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IInternalAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ILinkedAccessGroup;
import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IExternalDomainConfigurationDO;
import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventManager;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This implementation is aware of whether we're using local or remote
 * authentication.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/core/defaultimpl/ApplicationUserManagerImpl.java#1 $
 */

public class ApplicationUserManagerImpl implements IApplicationUserManager {

    private static final Log LOG = LogFactory.getLog(ApplicationUserManagerImpl.class);
    private static final int MAX_EXTERNAL_GROUP_LINK_ATTEMPTS = 5;
    
    /*
     * Private variables:
     */
    private IApplicationUserConfigurationDO configuration;
    private AuthenticationModeEnumType authenticationMode;
    private IApplicationUserRepository userRepository;
    private IExternalDomainManager externalDomainManager;
    private IHibernateRepository dataSource;
    private Set<IUserManagementObserver> observers = new HashSet<IUserManagementObserver>();
    private IDestinyEventManager eventManager;

    /**
     * Constructor
     */
    public ApplicationUserManagerImpl(IHibernateRepository dataSource, IDestinyEventManager eventManager) {
        this.dataSource = dataSource;
        this.eventManager = eventManager;
        if (this.dataSource == null) {
            throw new NullPointerException("data source is null");
        }
        if (this.eventManager == null) {
            throw new NullPointerException("event manager is null");
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#initialize(com.bluejungle.framework.applicationusers.IApplicationUserManagerConfiguration)
     */
    public void initialize(IApplicationUserConfigurationDO configuration) throws InvalidConfigurationException, InitializationException {
        this.configuration = configuration;
        this.authenticationMode = AuthenticationModeEnumType.getByName(this.configuration.getAuthenticationMode());

        // Initialize the application user repository:
        try {
            // Initialize the external domains, if they exist:
            this.externalDomainManager = new ExternalDomainManagerImpl();
            if ((this.configuration.getExternalDomainConfiguration() != null)) {
                Set<IExternalDomainConfigurationDO> configs = new HashSet<IExternalDomainConfigurationDO>();
                configs.add(this.configuration.getExternalDomainConfiguration());
                this.externalDomainManager.initialize(configs);
            } else {
                if (LOG.isInfoEnabled()) {
                    LOG.info("No external domain configuration was provided. No access will be available to external users and groups via the application.");
                }
            }

            String userRepositoryName = this.configuration.getUserRepositoryConfiguration().getProviderClassName();
            this.userRepository = (IApplicationUserRepository) Class.forName(userRepositoryName).newInstance();
            this.userRepository.initialize(this.configuration.getUserRepositoryConfiguration().getProperties(), this.dataSource, this.externalDomainManager);
        } catch (IllegalAccessException e) {
            throw new InitializationException(e);
        } catch (ClassNotFoundException e) {
            throw new InitializationException(e);
        } catch (InstantiationException e) {
            throw new InitializationException(e);
        } catch (InitializationException e) {
            throw new InitializationException(e);
        } catch (InvalidConfigurationException e) {
            throw new InvalidConfigurationException(e);
        }

        // Register for remote changes:
        registerForUserMgmtChangeEvents();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#getAuthenticationMode()
     */
    public AuthenticationModeEnumType getAuthenticationMode() {
        return this.authenticationMode;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#getDefaultLocalDomainName()
     */
    public String getDefaultLocalDomainName() {
        return this.userRepository.getDefaultAdminDomainName();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#getDomainNames()
     */
    public Collection<String> getDomainNames() throws UserManagementAccessException {
        try {
            return this.userRepository.getAllDomainNames();
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new UserManagementAccessException(e);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#getApplicationUsers()
     */
    public SortedSet<IApplicationUser> getApplicationUsers(IUserSearchSpec[] searchSpecs, int pageSize) throws UserManagementAccessException {
        SortedSet<IApplicationUser> usersToReturn = new TreeSet<IApplicationUser>(new UserComparatorImpl());
        try {
            SortedSet<IApplicationUser> usersReturned = this.userRepository.getApplicationUsers(searchSpecs);
            if (pageSize > 0) {
                int i = 0;

                // We return (pageSize+1) elements if available, so that caller
                // can determine that there are "more" records to be shown past
                // the pageSize.
                for (Iterator<IApplicationUser> iter = usersReturned.iterator(); (iter.hasNext() && i < pageSize + 1); i++) {
                    usersToReturn.add(iter.next());
                }
            } else {
                usersToReturn = usersReturned;
            }
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new UserManagementAccessException(e);
        }

        return usersToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#getAuthenticationDomain()
     */
    public IAuthenticationDomain getAuthenticationDomain(String domainName) throws DomainNotFoundException, UserManagementAccessException {
        IAuthenticationDomain domain = null;

        // If this is the local domain, we always authenticate locally:
        try {
            if (this.userRepository.getDefaultAdminDomainName().equals(domainName)) {
                domain = this.userRepository.getAuthenticationDomain(domainName);
            } else {
                // We check if we're using local/remote authentication:
                if (getAuthenticationMode() == AuthenticationModeEnumType.LOCAL) {
                    domain = this.userRepository.getAuthenticationDomain(domainName);
                } else {
                    IAuthenticator externalAuthenticator = this.externalDomainManager.getExternalDomain(domainName).getAuthenticator();
                    domain = this.userRepository.getAuthenticationDomain(domainName, externalAuthenticator);
                }
            }
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new UserManagementAccessException(e);
        }
        return domain;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#getExternalDomains()
     */
    public Set<IExternalDomain> getExternalDomains() throws UserManagementAccessException {
        Set<IExternalDomain> domains = new HashSet<IExternalDomain>();
        Set<String> domainNames = this.externalDomainManager.getAllDomainNames();
        for (String name : domainNames) {
            try {
                IExternalDomain domain = this.externalDomainManager.getExternalDomain(name);
                domains.add(domain);
            } catch (DomainNotFoundException ignore) {
                LOG.warn("Reference to external domain named: '" + name + "' could not be obtained even though it was returned as a valid external domain");
            }
        }
        return domains;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#getExternalUsers(com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUserSearchSpec)
     */
    public SortedSet<IExternalUser> getExternalUsers(IUserSearchSpec[] searchSpecs, int pageSize) throws UserManagementAccessException {
        SortedSet<IExternalUser> externalUsers = new TreeSet<IExternalUser>(new UserComparatorImpl());
        try {
            Set<IExternalDomain> externalDomains = getExternalDomains();
            for (IExternalDomain externalDomain : externalDomains) {

                // Note: The code below to return pageSize elements ASSUMES,
                // as is the case currently, that there is only ONE external
                // domain supported (i.e. that this loop will be entered only
                // once). If that changes, the code below would need
                // to be revisited as we would be returning [nDomains *
                // (pageSize)] users in that case.

                int usersToRetrieve = pageSize <= 0 ? 0 : pageSize;
                Collection<IExternalUser> usersToReturn = externalDomain.getUserAccessProvider().getExternalUsers(searchSpecs, usersToRetrieve);
                externalUsers.addAll(usersToReturn);
            }
        } catch (ExternalUserAccessException e) {
            throw new UserManagementAccessException(e);
        }
        return externalUsers;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#getApplicationUserDomain(java.lang.String)
     */
    public IApplicationUserDomain getApplicationUserDomain(String domainName) throws DomainNotFoundException, UserManagementAccessException {
        IApplicationUserDomain domain = null;
        try {
            domain = this.userRepository.getApplicationUserDomain(domainName);
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new UserManagementAccessException(e);
        }
        return domain;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#importExternalUser(com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser)
     */
    public IApplicationUser importExternalUser(IExternalUser userToImport) throws ApplicationUserImportFailedException, UserAlreadyExistsException {
        String userDomainName = userToImport.getDomainName();
        IApplicationUser importedUser = null;
        try {
            IApplicationUserDomain userDomain = null;
            try {
                userDomain = this.userRepository.getApplicationUserDomain(userDomainName);
            } catch (DomainNotFoundException ignore) {
            }

            if (userDomain != null) {
                importedUser = userDomain.importExternalUser(userToImport);
            } else {
                // Create an app user domain
                try {
                    userDomain = this.userRepository.createDomain(userDomainName);
                } catch (DomainAlreadyExistsException ignore) {
                    userDomain = this.userRepository.getApplicationUserDomain(userDomainName);
                }

                // Create an auth domain and add it to the local auth domain
                // list - the authenticator used by this auth domain depends on
                // whether we're using local/remote authentication.
                IAuthenticationDomain authDomain;
                if (getAuthenticationMode() == AuthenticationModeEnumType.LOCAL) {
                    // For local auth, we need to user the local authenticator:
                    authDomain = this.userRepository.getAuthenticationDomain(userDomainName);
                } else {
                    // For remote auth, we need to use the external
                    // authenticator:
                    IExternalDomain externalDomain = this.externalDomainManager.getExternalDomain(userDomainName);
                    if (externalDomain == null) {
                        throw new IllegalStateException("No external domain" + userToImport.getDomainName() + "' exists for the user being imported: '" + userToImport.getUniqueName() + "'");
                    }
                    authDomain = this.userRepository.getAuthenticationDomain(userDomainName, externalDomain.getAuthenticator());
                }
                importedUser = userDomain.importExternalUser(userToImport);
            }
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new ApplicationUserImportFailedException(e);
        } catch (DomainNotFoundException e) {
            throw new ApplicationUserImportFailedException(e);
        }
        return importedUser;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#deleteApplicationUser(long)
     */
    public void deleteApplicationUser(long id) throws ApplicationUserDeleteFailedException, UserNotFoundException {
        if (RESERVED_SUPER_USER_ID == id) {
            throw new UnsupportedOperationException("cannot delete super user");
        }

        try {
            // Retrieve the user with the given id:
            IApplicationUser userToDelete = this.userRepository.getUser(id);
            String userDomainName = userToDelete.getDomainName();
            IApplicationUserDomain userDomain = this.userRepository.getApplicationUserDomain(userDomainName);

            // Cleanup internal groups that contain the given user:
            Map<String, IApplicationUserDomain> groupDomains = new HashMap<String, IApplicationUserDomain>(); // Cache for group domains:
            IApplicationUserDomain groupDomain;
            Collection<IInternalAccessGroup> groupsImpacted = this.userRepository.getInternalGroupsContainingUser(userToDelete);
            for (IInternalAccessGroup groupToUpdate : groupsImpacted) {
                String groupDomainName = groupToUpdate.getDomainName();
                if (groupDomains.get(groupDomainName) == null) {
                    groupDomain = this.userRepository.getApplicationUserDomain(groupDomainName);
                    groupDomains.put(groupDomainName, groupDomain);
                } else {
                    groupDomain = groupDomains.get(groupDomainName);
                }
                groupDomain.removeUsersFromGroup(groupToUpdate, Collections.singleton(userToDelete));
            }

            // NOTE: We aren't doing anythign for external groups since they are
            // managed elsewhere adn we don't update them internally. During
            // exploration of those groups, we would automatically figure out
            // that a given member doesn't exist in the internal repository.

            // Delete the user physically:
            userDomain.deleteApplicationUser(userToDelete);
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new ApplicationUserDeleteFailedException(e);
        } catch (DomainNotFoundException e) {
            throw new ApplicationUserDeleteFailedException(e);
        }

        // Fire an event to the observers:
        fireUserMgntChangeEvent(ActionTypeEnumType.USER_DELETE, id);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#setPrimaryAccessGroupForUser(com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser,
     *      IApplicationUserGroup)
     */
    public void setPrimaryAccessGroupForUser(long userId, Long groupId) throws UserNotFoundException, GroupNotFoundException, ApplicationUserModificationFailedException {
        if (RESERVED_SUPER_USER_ID == userId) {
            throw new UnsupportedOperationException("cannot set primary access group for Super User");
        }

        try {
            // Retrieve the user and the group with the corresponding ids:
            IApplicationUser user = getApplicationUser(userId);
            IAccessGroup group = null;

            if (groupId != null) {
                group = getAccessGroup(groupId.longValue());
            }
            String userDomainName = user.getDomainName();
            IApplicationUserDomain userDomain = this.userRepository.getApplicationUserDomain(userDomainName);
            userDomain.setPrimaryAccessGroupForUser(user, group);
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new ApplicationUserModificationFailedException(e);
        } catch (DomainNotFoundException e) {
            throw new ApplicationUserModificationFailedException(e);
        } catch (UserManagementAccessException e) {
            throw new ApplicationUserModificationFailedException(e);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#createUser(com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser,
     *      java.lang.String)
     */
    public IApplicationUser createUser(String login, String fn, String ln, String password) throws ApplicationUserCreationFailedException, UserAlreadyExistsException {
        // We prevent creation of any entry with the same login as the super
        // user:
        if (SUPER_USER_USERNAME.equals(login)) {
            throw new UserAlreadyExistsException("Login: '" + login + "' is reserved. Please use another login.");
        }

        String userDomainName = this.userRepository.getDefaultAdminDomainName();
        try {
            IApplicationUserDomain userDomain = this.userRepository.getApplicationUserDomain(userDomainName);

            return userDomain.createNewUser(login, fn, ln, password);
        } catch (DomainNotFoundException e) {
            throw new ApplicationUserCreationFailedException(e);
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new ApplicationUserCreationFailedException(e);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.ISingleDomainAuthenticationManager#authenticateUser(java.lang.String,
     *      java.lang.String)
     */
    public IAuthenticatedUser authenticateUser(String username, String password) throws AuthenticationFailedException, UserManagementAccessException {
        IAuthenticatedUser authenticatedUser = null;
        String defaultLocalDomain = getDefaultLocalDomainName();
        boolean isAuthenticated = false;

        try {
            // Super-user has special authentication irrespective of
            // authentication mode:
            if (username.equals(SUPER_USER_USERNAME)) {
                LOG.trace(username + " is a super-user. Performing special authentication...");
                authenticatedUser = this.userRepository.authenticateSuperUser(password);
                isAuthenticated = true;
                LOG.trace("Password for '" + username + "' was succesfully authenticated. Granting access to application...");
            } else {
                // We might have 1-2 domains here, so we need to set a
                // sequence for authentication - until the domain qualifier
                // is available from the UI:
                IAuthenticationDomain domainToAuthenticateAgainst = null;

                // 1. If we're using Local/Hybrid auth, we try authenticating
                // locally:
                if (getAuthenticationMode() != AuthenticationModeEnumType.REMOTE) {
                    domainToAuthenticateAgainst = getAuthenticationDomain(defaultLocalDomain);
                    try {
                        LOG.trace("Attempting authentication for '" + username + " against default local domain ...");
                        authenticatedUser = domainToAuthenticateAgainst.authenticateUser(username, password);
                        LOG.trace("Authentication for '" + username + "' passed on default local domain ...");
                        isAuthenticated = true;
                    } catch (AuthenticationFailedException e) {
                        isAuthenticated = false;
                        LOG.trace(username + " was not authenticated against default local domain. Need to try remaining domains, if any...");
                    }
                }

                // 2. For all auth modes, we go through the remaining domains if
                // no auth has been succesfully completed yet:
                if (isAuthenticated == false) {
                    Collection<String> domainNames = getDomainNames();
                    Iterator<String> iter = domainNames.iterator();
                    while ((isAuthenticated == false) && (iter.hasNext())) {
                        String name = iter.next();
                        if (!name.equals(defaultLocalDomain)) {
                            try {
                                domainToAuthenticateAgainst = getAuthenticationDomain(name);
                                LOG.trace("Attempting authentication for '" + username + "' against domain: '" + name + "'");
                                authenticatedUser = domainToAuthenticateAgainst.authenticateUser(username, password);
                                LOG.trace("Password for '" + username + "' was succesfully authenticated.");
                                isAuthenticated = true;
                                break;
                            } catch (AuthenticationFailedException e) {
                                LOG.trace("Authentication for '" + username + "' failed against domain: '" + name + "'");
                                isAuthenticated = false;
                            } catch (DomainNotFoundException e) {
                                // Fix for bug 3402, there are cases one of domains is invalid
                                // we have to look other domains, so, ignoring domain not found expection 
                                continue;
                            }
                            
                        }
                    }

                    // If authentication fails against all domains, throw an
                    // exception signalling auth failure:
                    if (isAuthenticated == false) {
                        throw new AuthenticationFailedException("failed to authenticate user: '" + username + "'");
                    }
                }
            }
        } catch (DomainNotFoundException e) {
            throw new UserManagementAccessException(e);
        }

        return authenticatedUser;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#addUsersToAccessGroup(long,
     *      long[])
     */
    public void addUsersToAccessGroup(long groupId, long[] userIds) throws GroupNotFoundException, UserNotFoundException, AccessGroupModificationFailedException {
        if (userIds == null) {
            throw new NullPointerException("no user ids specified");
        }

        try {
            // Check if the group is of appropriate type:
            IAccessGroup group = getAccessGroup(groupId);
            if (!(group instanceof IInternalAccessGroup)) {
                throw new IllegalArgumentException("group with id:'" + groupId + "' is not an internal group and so cannot be modified");
            }

            // Convert long[] to Long[]:
            Long[] userIdsAsObjects = new Long[userIds.length];
            for (int i = 0; i < userIds.length; i++) {
                userIdsAsObjects[i] = new Long(userIds[i]);
            }

            // Obtain the domain for this group:
            String domainName = group.getDomainName();
            IApplicationUserDomain domain = this.userRepository.getApplicationUserDomain(domainName);

            // Do the membership operation:
            Map<Long, IApplicationUser> idToUserMap = this.userRepository.getUsersInBulk(userIdsAsObjects);
            Collection<IApplicationUser> users = idToUserMap.values();
            domain.addUsersToGroup((IInternalAccessGroup) group, users);

            // Check if any users were not found (i.e. left out) and throw an
            // exception if that happens:
            for (int i = 0; i < userIdsAsObjects.length; i++) {
                Long id = userIdsAsObjects[i];
                if (idToUserMap.get(id) == null) {
                    throw new UserNotFoundException("one or more users could not be found for addition as members to the group: '" + group.getTitle() + "' in domain: '" + group.getDomainName() + "'");
                }
            }
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new AccessGroupModificationFailedException(e);
        } catch (UserManagementAccessException e) {
            throw new AccessGroupModificationFailedException(e);
        } catch (DomainNotFoundException e) {
            throw new AccessGroupModificationFailedException(e);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#createAccessGroup(com.bluejungle.destiny.container.shared.applicationusers.core.ICustomAccessGroupData)
     */
    public IInternalAccessGroup createAccessGroup(String title, String description) throws AccessGroupCreationFailedException, GroupAlreadyExistsException {
        if (title == null) {
            throw new NullPointerException("title is null");
        }

        // We create an internal group in the "local" domain only:
        IInternalAccessGroup group = null;
        try {
            IApplicationUserDomain localDomain = this.userRepository.getApplicationUserDomain(getDefaultLocalDomainName());
            group = localDomain.createAccessGroup(title, description);
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new AccessGroupCreationFailedException(e);
        } catch (DomainNotFoundException e) {
            throw new AccessGroupCreationFailedException(e);
        }
        return group;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#deleteGroup(long)
     */
    public void deleteGroup(long groupId) throws AccessGroupDeletionFailedException, GroupNotFoundException {
        try {
            IAccessGroup groupToDelete = getAccessGroup(groupId);
            String domainName = groupToDelete.getDomainName();
            IApplicationUserDomain domain = this.userRepository.getApplicationUserDomain(domainName);

            // First remove all internal references to the group:
            Collection<IApplicationUser> usersImpacted = this.userRepository.getUsersWithPrimaryAccessGroup(groupToDelete);
            for (IApplicationUser user : usersImpacted) {
                // 1. Cleanup user entries that refer to this group as the
                // primary access group:
                String userDomainName = user.getDomainName();
                IApplicationUserDomain userDomain = this.userRepository.getApplicationUserDomain(userDomainName);
                userDomain.setPrimaryAccessGroupForUser(user, null);
            }

            // Then remove the group:
            domain.deleteAccessGroup(groupToDelete);
        } catch (UserManagementAccessException e) {
            throw new AccessGroupDeletionFailedException(e);
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new AccessGroupDeletionFailedException(e);
        } catch (DomainNotFoundException e) {
            throw new AccessGroupDeletionFailedException(e);
        }

        // Fire an event to the observers:
        fireUserMgntChangeEvent(ActionTypeEnumType.GROUP_DELETE, groupId);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#getAccessGroup(long)
     */
    public IAccessGroup getAccessGroup(long groupId) throws UserManagementAccessException, GroupNotFoundException {
        IAccessGroup group = null;
        try {
            group = this.userRepository.getAccessGroup(groupId);
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new UserManagementAccessException(e);
        }
        return group;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#getAccessGroups()
     */
    public SortedSet<IAccessGroup> getAccessGroups(IGroupSearchSpec[] searchSpecs, int pageSize) throws UserManagementAccessException {
        SortedSet<IAccessGroup> accessGroups;
        try {
            SortedSet<IAccessGroup> unboundedAccessGroups = this.userRepository.getAccessGroups(searchSpecs);
            if (pageSize > 0) {
                accessGroups = new TreeSet<IAccessGroup>(new GroupComparatorImpl());
                int i = 0;

                // We return (pageSize+1) elements if available, so that caller
                // can determine that there are "more" records to be shown past
                // the pageSize.
                for (Iterator<IAccessGroup> groupIter = unboundedAccessGroups.iterator(); (groupIter.hasNext() && i < pageSize + 1); i++) {
                    accessGroups.add(groupIter.next());
                }

            } else {
                accessGroups = unboundedAccessGroups;
            }
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new UserManagementAccessException(e);
        }
        return accessGroups;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#getApplicationUser(long)
     */
    public IApplicationUser getApplicationUser(long id) throws UserManagementAccessException, UserNotFoundException {
        IApplicationUser user = null;

        try {
            user = this.userRepository.getUser(id);
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new UserManagementAccessException(e);
        }

        return user;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#getUsersInAccessGroup(long)
     */
    public SortedSet<IApplicationUser> getUsersInAccessGroup(long groupId) throws UserManagementAccessException, GroupNotFoundException {
        SortedSet<IApplicationUser> users = null;
        try {
            IAccessGroup group = this.userRepository.getAccessGroup(groupId);
            users = this.userRepository.getUsersInAccessGroup(group);
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new UserManagementAccessException(e);
        }

        return users;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#linkExternalAccessGroup(com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroupLinkData)
     */
    public ILinkedAccessGroup[] linkExternalAccessGroups(IExternalGroupLinkData[] links) throws AccessGroupCreationFailedException {
        boolean errorOccurred = false;
        Exception exception = null;
        if (links == null) {
            throw new NullPointerException("array of links is null");
        }

        ILinkedAccessGroup[] linkedGroupsToReturn = new ILinkedAccessGroup[links.length];

        // Keep a cache of the domains consulted so that we don't requery for
        // the same domain every time (performance consideration):
        Map<String, IApplicationUserDomain> internalDomains = new HashMap<String, IApplicationUserDomain>();

        // Iterate through external link data and link to corresponding external
        // groups:
        for (int i = 0; i < links.length; i++) {
            IExternalGroupLinkData link = links[i];
            String domainName = link.getDomainName();
            IApplicationUserDomain internalDomain = null;
            try {
                if (internalDomains.get(domainName) == null) {
                    try {
                        internalDomain = this.userRepository.getApplicationUserDomain(domainName);
                    } catch (DomainNotFoundException e) {
                        try {
                            internalDomain = this.userRepository.createDomain(domainName);
                        } catch (DomainAlreadyExistsException e1) {
                            exception = e1;
                            errorOccurred = true;
                        }
                    }
                    internalDomains.put(domainName, internalDomain);
                } else {
                    internalDomain = internalDomains.get(domainName);
                }

                // Now create the link and use _X suffix in the case where a
                // title of the same name already exists in the given domain::
                byte[] externalIdToLinkTo = link.getExternalId();
                String titleFormat = link.getTitle() + "@@1";
                String titleToUse = link.getTitle();
                boolean linkSucceeded = false;
                // We loop here max attempts hoping we find a title that works
                // NOTE - the Unique Contrstraint violation logic we use in AccessDomainDO is not full proof.  It will 
                // throw a GroupAlreadyExistsException when a null value is inserted into a non-nillable field in Oracle
                // In the future, improve this logic.  Replace it with Hibernate alternative when we switch to Hibernate 3.0
                for (int j = 0; ((j<MAX_EXTERNAL_GROUP_LINK_ATTEMPTS) && (!linkSucceeded)); j++) {
                    try {
                        linkedGroupsToReturn[i] = internalDomain.linkExternalGroup(externalIdToLinkTo, titleToUse);
                        linkSucceeded = true;
                    } catch (GroupAlreadyExistsException e) {
                        exception = e;
                        titleToUse = titleFormat.replaceAll("@@1", String.valueOf(j));
                    }                    
                }
                
                if (!linkSucceeded) {
                    errorOccurred = true;
                }
            } catch (GroupLinkAlreadyExistsException e) {
                errorOccurred = true;
                exception = e;
            } catch (ApplicationUserRepositoryAccessException e) {
                errorOccurred = true;
                exception = e;
            } finally {
                if (errorOccurred) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Error occurred linking to external group with id: '" + link.getExternalId() + "'. Proceeding with remaining links.", exception);
                    }
                    throw new AccessGroupCreationFailedException(exception);
                }
            }
        }

        return linkedGroupsToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#getAccessGroupsContainingUser(long)
     */
    public Set<IAccessGroup> getAccessGroupsContainingUser(long userId) throws UserManagementAccessException, UserNotFoundException {
        Set<IAccessGroup> groups = null;
        if (RESERVED_SUPER_USER_ID == userId) {
            groups = Collections.EMPTY_SET;
        } else {
            groups = new HashSet<IAccessGroup>();
            try {
                IApplicationUser user = this.userRepository.getUser(userId);

                // Search internally:
                Collection<IInternalAccessGroup> internalGroups = this.userRepository.getInternalGroupsContainingUser(user);
                groups.addAll(internalGroups);

                // If the user is an imported user, we need to determine if any
                // linked groups from that domain refer to this user:
                String userDomain = user.getDomainName();
                if (!userDomain.equals(getDefaultLocalDomainName())) {
                    /*
                     * TODO: Determine a better way of determining above if the
                     * user is a local/imported user
                     */
                    try {
                        IApplicationUserDomain domain = this.userRepository.getApplicationUserDomain(userDomain);
                        IUserAccessProvider externalAccessProvider = this.externalDomainManager.getExternalDomain(userDomain).getUserAccessProvider();
                        IExternalUser externalUser = externalAccessProvider.getUser(user.getLogin());
                        Set<IExternalGroup> externalGroups = externalAccessProvider.getExternalGroupsContainingUser(externalUser);

                        // Now we lookup the linked groups corresponding to
                        // these
                        // external groups:
                        for (IExternalGroup externalGroup : externalGroups) {
                            try {
                                ILinkedAccessGroup linkedGroup = domain.getLinkedGroupByExternalId(externalGroup.getExternalId());
                                groups.add(linkedGroup);
                            } catch (GroupNotFoundException ignore) {
                            }
                        }
                    } catch (DomainNotFoundException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Unable to access external domain :'" + userDomain + "' for obtaining external groups containing user: '" + user.getLogin() + "'", e);
                        }
                    } catch (UserNotFoundException e) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Unable to locate user :'" + user.getLogin() + "' in external domain :'" + userDomain + "'", e);
                        }
                    } catch (ExternalUserAccessException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Unable to access external domain :'" + userDomain + "' for obtaining external groups containing user: '" + user.getLogin() + "'", e);
                        }
                    }
                }
            } catch (ApplicationUserRepositoryAccessException e) {
                throw new UserManagementAccessException(e);
            }
        }
        return groups;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#removeUsersFromAccessGroup(long,
     *      long[])
     */
    public void removeUsersFromAccessGroup(long groupId, long[] userIds) throws AccessGroupModificationFailedException, GroupNotFoundException, UserNotFoundException {
        Set<Long> missingUserIds = new HashSet<Long>();
        if (userIds == null) {
            throw new NullPointerException("user ids array is null");
        }
        try {
            IAccessGroup groupToModify = this.userRepository.getAccessGroup(groupId);
            if (!(groupToModify instanceof IInternalAccessGroup)) {
                throw new IllegalArgumentException("group with id:'" + groupId + "' is not an internal group and so cannot be modified");
            }

            Long[] userIdsAsObjects = new Long[userIds.length];
            for (int i = 0; i < userIds.length; i++) {
                userIdsAsObjects[i] = new Long(userIds[i]);
            }

            // Verify whether all users exist:
            Map<Long, IApplicationUser> usersBeingRemovedFromGroup = this.userRepository.getUsersInBulk(userIdsAsObjects);
            for (Long id : userIdsAsObjects) {
                if (!usersBeingRemovedFromGroup.containsKey(id)) {
                    missingUserIds.add(id);
                }
            }

            // Then make sure none of the given users have the given group as
            // their primary access group:
            Map<String, IApplicationUserDomain> userDomainCache = new HashMap<String, IApplicationUserDomain>();
            Collection<IApplicationUser> usersWithGroupAsPrimaryAccessGroup = this.userRepository.getUsersWithPrimaryAccessGroup(groupToModify);
            for (IApplicationUser user : usersWithGroupAsPrimaryAccessGroup) {

                // We only change those users that are being removed from this
                // group:
                if (usersBeingRemovedFromGroup.containsKey(user.getDestinyId())) {
                    String userDomainName = user.getDomainName();
                    IApplicationUserDomain userDomain = null;
                    if (!userDomainCache.containsKey(userDomainName)) {
                        userDomain = this.userRepository.getApplicationUserDomain(userDomainName);
                        userDomainCache.put(userDomainName, userDomain);
                    } else {
                        userDomain = userDomainCache.get(userDomainName);
                    }
                    userDomain.setPrimaryAccessGroupForUser(user, null);
                }
            }

            // Obtain the domain for this group:
            String domainName = groupToModify.getDomainName();
            IApplicationUserDomain domain = this.userRepository.getApplicationUserDomain(domainName);

            // Perform the modification:
            Collection<IApplicationUser> usersToRemove = usersBeingRemovedFromGroup.values();
            domain.removeUsersFromGroup((IInternalAccessGroup) groupToModify, usersToRemove);
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new AccessGroupModificationFailedException(e);
        } catch (DomainNotFoundException e) {
            throw new AccessGroupModificationFailedException(e);
        }

        // Throw an exception if any users were missing:
        if (missingUserIds.size() > 0) {
            throw new UserNotFoundException(missingUserIds);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#getExternalGroups(com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroupSearchSpec)
     */
    public SortedSet<IExternalGroup> getExternalGroups(IGroupSearchSpec[] searchSpecs, int pageSize) throws UserManagementAccessException {
        SortedSet<IExternalGroup> externalGroups = new TreeSet<IExternalGroup>(new GroupComparatorImpl());
        try {
            Set<String> domainNames = this.externalDomainManager.getAllDomainNames();
            for (String externalDomainName : domainNames) {
                try {
                    IExternalDomain domain = this.externalDomainManager.getExternalDomain(externalDomainName);
                    IUserAccessProvider userAccessProvider = domain.getUserAccessProvider();

                    // Note: The code below to return pageSize+1 elements
                    // ASSUMES, as is the case currently, that there is only ONE
                    // external domain supported (i.e. that this loop will be
                    // entered only once). If that changes, the code below would
                    // need to be revisited as we would be returning [nDomains *
                    // (pageSize+1)] users in that case.

                    Collection<IExternalGroup> unboundedGroupList = userAccessProvider.getExternalGroups(searchSpecs);
                    Collection<IExternalGroup> groupsToReturn = new ArrayList<IExternalGroup>();
                    if (pageSize > 0) {
                        int i = 0;

                        // We return (pageSize+1) elements if available, so that
                        // caller can determine that there are "more" records to
                        // be shown past the pageSize.
                        for (Iterator<IExternalGroup> groupIter = unboundedGroupList.iterator(); (groupIter.hasNext() && i < pageSize + 1); i++) {
                            groupsToReturn.add(groupIter.next());
                        }
                    } else {
                        groupsToReturn = unboundedGroupList;
                    }
                    externalGroups.addAll(groupsToReturn);
                } catch (DomainNotFoundException ignore) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("configured external domain with name: '" + externalDomainName + "' could not be located");
                    }
                }
            }
        } catch (ExternalUserAccessException e) {
            throw new UserManagementAccessException(e);
        }

        return externalGroups;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#setDefaultAccessControlAssignmentForGroup(long,
     *      java.lang.String)
     */
    public void setDefaultAccessControlAssignmentForGroup(long groupId, String accessControlAssignment) throws AccessGroupModificationFailedException, GroupNotFoundException {
        try {
            IAccessGroup groupToModify = this.userRepository.getAccessGroup(groupId);
            String domainName = groupToModify.getDomainName();
            IApplicationUserDomain domain = this.userRepository.getApplicationUserDomain(domainName);
            domain.setApplicableAccessControlForGroup(groupToModify, accessControlAssignment);
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new AccessGroupModificationFailedException(e);
        } catch (DomainNotFoundException e) {
            throw new AccessGroupModificationFailedException(e);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#updateGroup(long,
     *      com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroupModificationData)
     */
    public void updateGroup(long groupId, String title, String description) throws AccessGroupModificationFailedException, GroupAlreadyExistsException, GroupNotFoundException {
        IAccessGroup group = null;
        try {
            group = this.userRepository.getAccessGroup(groupId);
            String domainName = group.getDomainName();
            IApplicationUserDomain domain = this.userRepository.getApplicationUserDomain(domainName);
            if (!group.getTitle().equals(title)) {
                domain.setTitleForGroup(group, title);
            }
            if (!group.getDescription().equals(description)) {
                domain.setDescriptionForGroup(group, description);
            }
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new AccessGroupModificationFailedException(e);
        } catch (DomainNotFoundException e) {
            throw new AccessGroupModificationFailedException(e);
        }
    }

    /**
     * Fires a change event to the DCC event dispatch system
     * 
     * @param action
     * @param destinyId
     */
    protected void fireUserMgntChangeEvent(ActionTypeEnumType action, long destinyId) {
        IDCCServerEvent changeEvent = ChangeEventUtil.createEvent(action, destinyId);
        this.eventManager.fireEvent(changeEvent);
    }

    /**
     * Registers this component as a listener to the user management change
     * event
     */
    protected void registerForUserMgmtChangeEvents() {
        this.eventManager.registerForEvent(ChangeEventUtil.EVENT_NAME, this);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#addListener(com.bluejungle.destiny.container.shared.applicationusers.core.IUserManagementObserver)
     */
    public void addListener(IUserManagementObserver observer) {
        this.observers.add(observer);
    }

    /**
     * @see com.bluejungle.destiny.server.shared.events.IDestinyEventListener#onDestinyEvent(com.bluejungle.destiny.server.shared.events.IDCCServerEvent)
     */
    public void onDestinyEvent(IDCCServerEvent event) {
        // Extracts the information from the event
        final ActionTypeEnumType action = ActionTypeEnumType.getByName(event.getProperties().getProperty(ChangeEventUtil.ACTION_PROP_NAME));
        final long id = Long.valueOf(event.getProperties().getProperty(ChangeEventUtil.ID_PROP_NAME)).longValue();
        for (IUserManagementObserver observer : this.observers) {
            try {
                if (ActionTypeEnumType.USER_DELETE.equals(action)) {
                    observer.onUserDelete(id);
                } else if (ActionTypeEnumType.GROUP_DELETE.equals(action)) {
                    observer.onGroupDelete(id);
                }
            } catch (Throwable e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Notification of '" + action.getName() + "' on entry with id: '" + id + "' failed. Proceeding to notify next observer.", e);
                }
            }
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#getApplicationUser(java.lang.String)
     */
    public IApplicationUser getApplicationUser(String uniqueName) throws UserNotFoundException, UserManagementAccessException {
        IApplicationUser userToReturn = null;

        if (uniqueName == null) {
            throw new UserNotFoundException("no user exists with unique name: '" + uniqueName + "'");
        }

        // Split the unique name into the login name and domain name:
        String[] comps = uniqueName.split(IApplicationUser.LOGIN_AT_DOMAIN_SEPARATOR, 2);
        if (comps.length != 2) {
            throw new UserNotFoundException("no user exists with unique name: '" + uniqueName + "'");
        }

        // Retrieve the user:
        String login = comps[0];
        String domainName = comps[1];
        try {
            IApplicationUserDomain domain = getApplicationUserDomain(domainName);
            userToReturn = domain.getUser(login);
        } catch (DomainNotFoundException e) {
            throw new UserNotFoundException("no user exists with unique name: '" + uniqueName + "'", e);
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new UserManagementAccessException(e);
        }

        return userToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#updateApplicationUser(com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser,
     *      java.lang.String)
     */
    public void updateApplicationUser(IApplicationUser user, String password) throws UserNotFoundException, UserManagementAccessException {
        if (user == null) {
            throw new NullPointerException("user is null");
        }

        IApplicationUserDomain userDomain;
        try {
            userDomain = this.userRepository.getApplicationUserDomain(user.getDomainName());
            userDomain.updateUser(user, password);
        } catch (DomainNotFoundException e) {
            throw new UserNotFoundException("no user exists with unique name: " + user.getUniqueName());
        } catch (ApplicationUserRepositoryAccessException e) {
            throw new UserManagementAccessException(e);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#getPrimaryAccessGroupForUser(long)
     */
    public IAccessGroup getPrimaryAccessGroupForUser(long userId) throws UserNotFoundException, UserManagementAccessException {
        IAccessGroup group;
        if (RESERVED_SUPER_USER_ID == userId) {
            group = null;
        } else {
            try {
                // Retrieve the user with the given id:
                IApplicationUser user = this.userRepository.getUser(userId);
                group = this.userRepository.getPrimaryAccessGroupForUser(user);
            } catch (ApplicationUserRepositoryAccessException e) {
                throw new UserManagementAccessException(e);
            }
        }

        return group;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#canChangePassword(com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser)
     */
    public boolean canChangePassword(IApplicationUser applicationUser) {
        if (applicationUser == null) {
            throw new NullPointerException("applicationUser cannot be null.");
        }

        return isLocallyAuthenticated(applicationUser);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#isLocallyAuthenticated(com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser)
     */
    public boolean isLocallyAuthenticated(IApplicationUser applicationUser) {
        return (applicationUser.isManuallyCreated() || this.getAuthenticationMode() == AuthenticationModeEnumType.LOCAL);
    }

    /**
     * @throws UserManagementAccessException 
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager#getSuperUser()
     */
    public IApplicationUser getSuperUser() throws UserManagementAccessException {
        IApplicationUser userToReturn = null;
        
        try {
            userToReturn = this.userRepository.getSuperUser();
        } catch (ApplicationUserRepositoryAccessException exception) {
            throw new UserManagementAccessException(exception);
        }
        
        return userToReturn;
    }
}
