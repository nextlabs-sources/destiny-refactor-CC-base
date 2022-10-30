/*
 * Created on Jun 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;

import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.DomainAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.DomainNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.IUserSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.core.InitializationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomainManager;
import com.bluejungle.destiny.container.shared.applicationusers.external.IGroupSearchSpec;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * This class serves as the interface to the application user repository
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/repository/IApplicationUserRepository.java#3 $
 */

public interface IApplicationUserRepository {

    /**
     * Initialize
     * 
     * @param configuration
     *            properties
     * @throws InvalidConfigurationException
     */
    public void initialize(Properties properties, IHibernateRepository dataSource, IExternalDomainManager externalDomainManager) throws InvalidConfigurationException, InitializationException;

    /**
     * Returns whether the given password matched the super user password
     * 
     * @param password
     * @return
     * @throws AuthenticationFailedException 
     */
    public IAuthenticatedUser authenticateSuperUser(String password) throws AuthenticationFailedException;

    /**
     * Retrieve the super user
     * 
     * @return the super user
     * @throws ApplicationUserRepositoryAccessException 
     */
    public IApplicationUser getSuperUser() throws ApplicationUserRepositoryAccessException;

    /**
     * Returns a collection of domain names that are currently in the repository
     * 
     * @throws ApplicationUserRepositoryAccessException
     * @return set of domain names
     */
    public Set<String> getAllDomainNames() throws ApplicationUserRepositoryAccessException;

    /**
     * Returns the domain name of the default admin
     * 
     * @return
     */
    public String getDefaultAdminDomainName();

    /**
     * Returns a collection of all the existing application users
     * <IApplicationUser>
     * 
     * @param array
     *            of search specs
     * @throws ApplicationUserRepositoryAccessException
     * @return set of all existing application users
     */
    public SortedSet<IApplicationUser> getApplicationUsers(IUserSearchSpec[] searchSpecs) throws ApplicationUserRepositoryAccessException;

    /**
     * Returns a collection <IAccessGroup>of all the existing access groups
     * 
     * @throws ApplicationUserRepositoryAccessException
     * @return collection
     */
    public SortedSet<IAccessGroup> getAccessGroups(IGroupSearchSpec[] searchSpecs) throws ApplicationUserRepositoryAccessException;

    /**
     * Returns the application domain with the given name
     * 
     * @param name
     * @throws ApplicationUserRepositoryAccessException
     * @return application domain
     */
    public IApplicationUserDomain getApplicationUserDomain(String domainName) throws ApplicationUserRepositoryAccessException, DomainNotFoundException;

    /**
     * Returns the authentication domain with the given name
     * 
     * @param domainName
     * @throws ApplicationUserRepositoryAccessException
     * @return
     */
    public IAuthenticationDomain getAuthenticationDomain(String domainName) throws ApplicationUserRepositoryAccessException, DomainNotFoundException;

    /**
     * Returns the authentication domain with the given name and authenticator
     * override - used if we want to authenticate the user password against a
     * different authentication server during application user authentication.
     * 
     * @param domainName
     * @throws ApplicationUserRepositoryAccessException
     * @return
     */
    public IAuthenticationDomain getAuthenticationDomain(String domainName, IAuthenticator authenticatorOverride) throws ApplicationUserRepositoryAccessException, DomainNotFoundException;

    /**
     * Creates a domain with the given name
     * 
     * @param domainName
     * @throws ApplicationUserRepositoryAccessException
     * @return an application domain
     */
    public IApplicationUserDomain createDomain(String domainName) throws ApplicationUserRepositoryAccessException, DomainAlreadyExistsException;

    /**
     * Deltes a domain with the given name
     * 
     * @param domainName
     * @throws ApplicationUserRepositoryAccessException
     */
    public void deleteDomain(String domainName) throws ApplicationUserRepositoryAccessException, DomainNotFoundException;

    /**
     * Retrieves a user the given destiny id
     * 
     * @param id
     * @return application user
     * @throws ApplicationUserRepositoryAccessException
     * @throws UserNotFoundException
     */
    public IApplicationUser getUser(long id) throws ApplicationUserRepositoryAccessException, UserNotFoundException;

    /**
     * Returns a map of destiny ids to users for the given array of destiny ids.
     * The main purpose of this method is to optimize lookup to the repository.
     * If a given id is not found, a UserNotFoundException will NOT be thrown.
     * 
     * @param ids
     * @return a map from ids to user entries
     * @throws ApplicationUserRepositoryAccessException
     */
    public Map<Long, IApplicationUser> getUsersInBulk(Long[] ids) throws ApplicationUserRepositoryAccessException;

    /**
     * Return the access group for the given id
     * 
     * @param groupId
     * @return access group
     * @throws ApplicationUserRepositoryAccessException
     * @throws GroupNotFoundException
     */
    public IAccessGroup getAccessGroup(long groupId) throws ApplicationUserRepositoryAccessException, GroupNotFoundException;

    /**
     * Returns the list of application users <IApplicationUser>in the given
     * group
     * 
     * @param group
     * @return collection of application users
     * @throws ApplicationUserRepositoryAccessException
     */
    public SortedSet<IApplicationUser> getUsersInAccessGroup(IAccessGroup group) throws ApplicationUserRepositoryAccessException;

    /**
     * Returns a collection of users <IApplicationUser>that refer to the given
     * access group as their primary access group
     * 
     * @param group
     * @return collection of application users
     * @throws ApplicationUserRepositoryAccessException
     */
    public Set<IApplicationUser> getUsersWithPrimaryAccessGroup(IAccessGroup group) throws ApplicationUserRepositoryAccessException;

    /**
     * Returns a collection of internal groups containing the given user as a
     * member
     * 
     * @param user
     * @return collection of groups
     * @throws ApplicationUserRepositoryAccessException
     */
    public Set<IInternalAccessGroup> getInternalGroupsContainingUser(IApplicationUser user) throws ApplicationUserRepositoryAccessException;

    /**
     * Returns the primary access group for the given user. Null if none exists.
     * 
     * @param user
     * @return primary access group
     * @throws ApplicationUserRepositoryAccessException
     */
    public IAccessGroup getPrimaryAccessGroupForUser(IApplicationUser user) throws ApplicationUserRepositoryAccessException;
}
