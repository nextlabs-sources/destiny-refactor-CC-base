/*
 * Created on Jun 1, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl;

import com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.DomainAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.DomainNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupComparatorImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager;
import com.bluejungle.destiny.container.shared.applicationusers.core.IUserSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.core.InitializationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserComparatorImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.external.ExternalUserAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomainManager;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser;
import com.bluejungle.destiny.container.shared.applicationusers.external.IGroupSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ApplicationUserRepositoryAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAuthenticatedUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAuthenticationDomain;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IInternalAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ILinkedAccessGroup;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Criterion;
import net.sf.hibernate.expression.Expression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/repository/hibernateimpl/HibernateApplicationUserRepository.java#3 $
 */

public class HibernateApplicationUserRepository implements IApplicationUserRepository {

    private static final Log LOG = LogFactory.getLog(HibernateApplicationUserRepository.class.getName());

    /**
     * <code>DEFAULT_LOCAL_DOMAIN</code>
     */
    private static final String DEFAULT_LOCAL_DOMAIN = "Local";

    // This shouldn't be static. Is't a quick hack to fix a design problem at
    // the framework level
    private static IExternalDomainManager EXTERNAL_DOMAIN_MANAGER;

    private static final String ALL_DOMAIN_NAMES_QUERY = "select domain.name from AccessDomainDO domain order by domain.name";
    private static final String APPLICATION_USER_DOMAIN_NAME_PROPERTY = "name";
    private static final String APPLICATION_USER_LAST_NAME_PROPERTY = "lastName";
    private static final String ACCESS_GROUP_TITLE_PROPERTY = "title";
    private static final String APPLICATION_USER_ID_PROPERTY = "id";
    private static final String ACCESS_GROUP_ID_PROPERTY = "id";
    private static final String APPLICATION_USER_PRIMARY_ACCESS_GROUP_PROPERTY = "hibernatePrimaryAccessGroup";
    private static final String APPLICATION_USER_ACCESS_DOMAIN_PROPERTY_NAME = "accessDomain";
    private static final String ACCESS_GROUP_MEMBERS_PROPERTY = "members";
    private static final String APPLICATION_USER_LOGIN_PROPERTY = "login";

    private static final String SUPER_USER_LOGIN = "Administrator";
    private static final String SUPER_USER_FIRST_NAME = "Administrator";
    private static final String SUPER_USER_LAST_NAME = "";

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#initialize(java.util.Properties,
     *      com.bluejungle.framework.datastore.hibernate.IHibernateRepository,
     *      com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomainManager)
     */
    public void initialize(Properties properties, IHibernateRepository dataSource, IExternalDomainManager externalDomainManager) throws InvalidConfigurationException, InitializationException {

        if (externalDomainManager == null) {
            throw new NullPointerException("externalDomainManager cannot be null.");
        }

        EXTERNAL_DOMAIN_MANAGER = externalDomainManager;
    }

    /**
     * @throws AuthenticationFailedException
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#authenticateSuperUser(java.lang.String)
     */
    public IAuthenticatedUser authenticateSuperUser(String password) throws AuthenticationFailedException {
        if (password == null) {
            throw new NullPointerException("password cannot be null.");
        }

        IAuthenticatedUser userToReturn = null;
       
        try {
        IAuthenticationDomain authenticationDomain = getAuthenticationDomain(getDefaultAdminDomainName());
        userToReturn = authenticationDomain.authenticateUser(IApplicationUserManager.SUPER_USER_USERNAME, password);
        } catch (ApplicationUserRepositoryAccessException exception) {
            throw new AuthenticationFailedException("Failed to authenticate Super User", exception);
        } catch (DomainNotFoundException exception) {
            throw new AuthenticationFailedException("Failed to authenticate Super User", exception);
        }
        
        return userToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#getSuperUser()
     */
    public IApplicationUser getSuperUser() throws ApplicationUserRepositoryAccessException {
        IApplicationUser userToReturn = null;

        try {
            IApplicationUserDomain userDomain = getApplicationUserDomain(getDefaultAdminDomainName());
            userToReturn = userDomain.getUser(IApplicationUserManager.SUPER_USER_USERNAME);
        } catch (UserNotFoundException exception) {
            throw new ApplicationUserRepositoryAccessException("Failed to retrieve Super User", exception);
        } catch (DomainNotFoundException exception) {
            throw new ApplicationUserRepositoryAccessException("Failed to retrieve Super User", exception);
        }

        return userToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#getAllDomainNames()
     */
    public Set<String> getAllDomainNames() throws ApplicationUserRepositoryAccessException {
        // Could cache all of the domains in memory, but this will break if this
        // method is called in a cluster. Need a cluster cache
        Set<String> domainNamesToReturn = new HashSet<String>();

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            Query query = hSession.createQuery(ALL_DOMAIN_NAMES_QUERY);
            List<String> queryResults = query.list();
            domainNamesToReturn.addAll(queryResults);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            StringBuffer errorMessageBuffer = new StringBuffer("Failed to retrieve application user domain names.");
            throw new ApplicationUserRepositoryAccessException(errorMessageBuffer.toString(), exception);
        } finally {
            closeSession(hSession);
        }

        return domainNamesToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#getDefaultAdminDomainName()
     */
    public String getDefaultAdminDomainName() {
        return DEFAULT_LOCAL_DOMAIN;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#getApplicationUsers(com.bluejungle.destiny.container.shared.applicationusers.core.IUserSearchSpec[])
     */
    public SortedSet<IApplicationUser> getApplicationUsers(IUserSearchSpec[] searchSpecs) throws ApplicationUserRepositoryAccessException {
        SortedSet<IApplicationUser> usersToReturn = new TreeSet<IApplicationUser>(new UserComparatorImpl());

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            Criteria queryCriteria = hSession.createCriteria(BaseApplicationUserDO.class);
            if (searchSpecs != null) {
                buildUserSearchCriteria(queryCriteria, searchSpecs);
            }
            List<BaseApplicationUserDO> queryResults = queryCriteria.list();
            usersToReturn.addAll(queryResults);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            StringBuffer errorMessageBuffer = new StringBuffer("Failed to query application users.");
            throw new ApplicationUserRepositoryAccessException(errorMessageBuffer.toString(), exception);
        } finally {
            closeSession(hSession);
        }

        return usersToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#getAccessGroups(com.bluejungle.destiny.container.shared.applicationusers.external.IGroupSearchSpec[])
     */
    public SortedSet<IAccessGroup> getAccessGroups(IGroupSearchSpec[] searchSpecs) throws ApplicationUserRepositoryAccessException {
        SortedSet<IAccessGroup> groupsToReturn = new TreeSet<IAccessGroup>(new GroupComparatorImpl());

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            Criteria queryCriteria = hSession.createCriteria(BaseAccessGroupDO.class);
            if (searchSpecs != null) {
                buildUserGroupSearchCriteria(queryCriteria, searchSpecs);
            }
            List<BaseAccessGroupDO> queryResults = queryCriteria.list();
            groupsToReturn.addAll(queryResults);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            StringBuffer errorMessageBuffer = new StringBuffer("Failed to query access groups.");
            throw new ApplicationUserRepositoryAccessException(errorMessageBuffer.toString(), exception);
        } finally {
            closeSession(hSession);
        }

        return groupsToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#getApplicationUserDomain(java.lang.String)
     */
    public IApplicationUserDomain getApplicationUserDomain(String domainName) throws ApplicationUserRepositoryAccessException, DomainNotFoundException {
        if (domainName == null) {
            throw new NullPointerException("domainName cannot be null.");
        }

        IApplicationUserDomain domainToReturn = null;

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            Criteria queryCriteria = hSession.createCriteria(AccessDomainDO.class);
            queryCriteria.add(Expression.eq(APPLICATION_USER_DOMAIN_NAME_PROPERTY, domainName));
            domainToReturn = (IApplicationUserDomain) queryCriteria.uniqueResult();
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            StringBuffer errorMessageBuffer = new StringBuffer("Failed to retrieve application user domain with name, ");
            errorMessageBuffer.append(domainName);
            errorMessageBuffer.append(".");
            throw new ApplicationUserRepositoryAccessException(errorMessageBuffer.toString(), exception);
        } finally {
            closeSession(hSession);
        }

        if (domainToReturn == null) {
            StringBuffer errorMessageBuffer = new StringBuffer("Application user domain with name, ");
            errorMessageBuffer.append(domainName);
            errorMessageBuffer.append(", could not be found.");
            throw new DomainNotFoundException(errorMessageBuffer.toString());
        }

        return domainToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#getAuthenticationDomain(java.lang.String)
     */
    public IAuthenticationDomain getAuthenticationDomain(String domainName) throws ApplicationUserRepositoryAccessException, DomainNotFoundException {
        if (domainName == null) {
            throw new NullPointerException("domainName cannot be null.");
        }
        IApplicationUserDomain applicationUserDomain = getApplicationUserDomain(domainName);
        return new HibernateAuthenticationDomainImpl((AccessDomainDO) applicationUserDomain);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#getAuthenticationDomain(java.lang.String,
     *      com.bluejungle.destiny.container.shared.applicationusers.auth.IAuthenticator)
     */
    public IAuthenticationDomain getAuthenticationDomain(String domainName, IAuthenticator authenticatorOverride) throws ApplicationUserRepositoryAccessException, DomainNotFoundException {
        if (domainName == null) {
            throw new NullPointerException("domainName cannot be null.");
        }
        IApplicationUserDomain applicationUserDomain = getApplicationUserDomain(domainName);
        return new HibernateAuthenticationDomainImpl((AccessDomainDO) applicationUserDomain, authenticatorOverride);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#createDomain(java.lang.String)
     */
    public IApplicationUserDomain createDomain(String domainName) throws ApplicationUserRepositoryAccessException, DomainAlreadyExistsException {
        if (domainName == null) {
            throw new NullPointerException("domainName cannot be null.");
        }

        AccessDomainDO domainToCreate = new AccessDomainDO(domainName);
        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            hSession.save(domainToCreate);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            if (HibernateUtils.isUniqueConstraintViolation(exception, LOG)) {
                StringBuffer errorMessage = new StringBuffer("The domain with the name, ");
                errorMessage.append(domainName);
                errorMessage.append(", already exists.  Please select a different name and try again.");
                throw new DomainAlreadyExistsException(errorMessage.toString(), exception);
            } else {
                throw new ApplicationUserRepositoryAccessException("Failed to create domain.", exception);
            }
        } finally {
            closeSession(hSession);
        }

        return domainToCreate;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#deleteDomain(java.lang.String)
     */
    public void deleteDomain(String domainName) throws ApplicationUserRepositoryAccessException, DomainNotFoundException {
        if (domainName == null) {
            throw new NullPointerException("domainName cannot be null.");
        }

        IApplicationUserDomain domainToDelete = getApplicationUserDomain(domainName);
        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            hSession.delete(domainToDelete);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            throw new ApplicationUserRepositoryAccessException("Failed to delete domain.", exception);
        } finally {
            closeSession(hSession);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#getUser(long)
     */
    public IApplicationUser getUser(long id) throws ApplicationUserRepositoryAccessException, UserNotFoundException {
        Long idAsLong = new Long(id);
        Long[] ids = { idAsLong };
        Map<Long, IApplicationUser> retrievedUsers = getUsersInBulk(ids);
        if (!retrievedUsers.containsKey(idAsLong)) {
            StringBuffer errorMessage = new StringBuffer("Failed to retrieve user with id, ");
            errorMessage.append(idAsLong);
            errorMessage.append(".");
            throw new UserNotFoundException(errorMessage.toString());
        }

        return retrievedUsers.get(idAsLong);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#getUsersInBulk(java.lang.Long[])
     */
    public Map<Long, IApplicationUser> getUsersInBulk(Long[] ids) throws ApplicationUserRepositoryAccessException {
        if (ids == null) {
            throw new NullPointerException("ids cannot be null.");
        }

        Map<Long, IApplicationUser> usersToReturn = new HashMap<Long, IApplicationUser>();

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            Criteria queryCriteria = hSession.createCriteria(BaseApplicationUserDO.class);
            queryCriteria.add(Expression.in(APPLICATION_USER_ID_PROPERTY, ids));
            List<BaseApplicationUserDO> queryResults = queryCriteria.list();
            for (BaseApplicationUserDO nextUser : queryResults) {
                usersToReturn.put(nextUser.getDestinyId(), nextUser);
            }
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            StringBuffer errorMessageBuffer = new StringBuffer("Failed to retrieve application user.");
            throw new ApplicationUserRepositoryAccessException(errorMessageBuffer.toString(), exception);
        } finally {
            closeSession(hSession);
        }

        return usersToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#getAccessGroup(long)
     */
    public IAccessGroup getAccessGroup(long groupId) throws ApplicationUserRepositoryAccessException, GroupNotFoundException {
        IAccessGroup groupToReturn;

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            Criteria queryCriteria = hSession.createCriteria(BaseAccessGroupDO.class);
            queryCriteria.add(Expression.eq(ACCESS_GROUP_ID_PROPERTY, new Long(groupId)));
            groupToReturn = (IAccessGroup) queryCriteria.uniqueResult();
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            StringBuffer errorMessageBuffer = new StringBuffer("Failed to retrieve user group with id, ");
            errorMessageBuffer.append(groupId);
            errorMessageBuffer.append(".");
            throw new ApplicationUserRepositoryAccessException(errorMessageBuffer.toString(), exception);
        } finally {
            closeSession(hSession);
        }

        if (groupToReturn == null) {
            StringBuffer errorMessageBuffer = new StringBuffer("Access group with id, ");
            errorMessageBuffer.append(groupId);
            errorMessageBuffer.append(", could not be found.");
            throw new GroupNotFoundException(errorMessageBuffer.toString());
        }

        return groupToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#getUsersInAccessGroup(com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup)
     */
    public SortedSet<IApplicationUser> getUsersInAccessGroup(IAccessGroup group) throws ApplicationUserRepositoryAccessException {
        if (group == null) {
            throw new NullPointerException("group cannot be null.");
        }

        SortedSet<IApplicationUser> membersToReturn;

        if (group instanceof IInternalAccessGroup) {
            membersToReturn = new TreeSet<IApplicationUser>(new UserComparatorImpl());
            Set<BaseApplicationUserDO> members = ((BaseAccessGroupDO) group).getMembers();
            membersToReturn.addAll(members);
        } else {
            try {
                membersToReturn = getUsersInLinkedAccessGroup((ILinkedAccessGroup) group);
            } catch (HibernateException exception) {
                StringBuffer errorMessage = new StringBuffer("Failed to retrieve users in access group with title, ");
                errorMessage.append(group.getTitle());
                errorMessage.append(", and ID, ");
                errorMessage.append(group.getDestinyId());
                errorMessage.append(".");
                throw new ApplicationUserRepositoryAccessException(errorMessage.toString(), exception);
            } catch (ExternalUserAccessException exception) {
                StringBuffer errorMessage = new StringBuffer("Failed to retrieve users in access group with title, ");
                errorMessage.append(group.getTitle());
                errorMessage.append(", and ID, ");
                errorMessage.append(group.getDestinyId());
                errorMessage.append(".");
                throw new ApplicationUserRepositoryAccessException(errorMessage.toString(), exception);
            }
        }
        return membersToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#getUsersWithPrimaryAccessGroup(com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup)
     */
    public Set<IApplicationUser> getUsersWithPrimaryAccessGroup(IAccessGroup group) throws ApplicationUserRepositoryAccessException {
        Set<IApplicationUser> usersToReturn = new HashSet<IApplicationUser>();

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            Criteria queryCriteria = hSession.createCriteria(BaseApplicationUserDO.class);
            Criteria joinCriteria = queryCriteria.createCriteria(APPLICATION_USER_PRIMARY_ACCESS_GROUP_PROPERTY);
            joinCriteria.add(Expression.eq(ACCESS_GROUP_ID_PROPERTY, group.getDestinyId()));
            List<BaseApplicationUserDO> usersRetrieved = queryCriteria.list();
            usersToReturn.addAll(usersRetrieved);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            StringBuffer errorMessageBuffer = new StringBuffer("Failed to retrieve user with primary access group with id, ");
            errorMessageBuffer.append(group.getDestinyId());
            errorMessageBuffer.append(".");
            throw new ApplicationUserRepositoryAccessException(errorMessageBuffer.toString(), exception);
        } finally {
            closeSession(hSession);
        }

        return usersToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#getInternalGroupsContainingUser(com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser)
     */
    public Set<IInternalAccessGroup> getInternalGroupsContainingUser(IApplicationUser user) throws ApplicationUserRepositoryAccessException {
        Set<IInternalAccessGroup> groupsToReturn = new HashSet<IInternalAccessGroup>();

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            Criteria queryCriteria = hSession.createCriteria(InternalAccessGroupDO.class);
            Criteria joinCriteria = queryCriteria.createCriteria(ACCESS_GROUP_MEMBERS_PROPERTY);
            joinCriteria.add(Expression.eq(APPLICATION_USER_ID_PROPERTY, user.getDestinyId()));
            List<InternalAccessGroupDO> groupsRetrieved = queryCriteria.list();
            groupsToReturn.addAll(groupsRetrieved);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            StringBuffer errorMessageBuffer = new StringBuffer("Failed to retrieve user groups containing user with id, ");
            errorMessageBuffer.append(user.getDestinyId());
            errorMessageBuffer.append(".");
            throw new ApplicationUserRepositoryAccessException(errorMessageBuffer.toString(), exception);
        } finally {
            closeSession(hSession);
        }

        return groupsToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserRepository#getPrimaryAccessGroupForUser(com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser)
     */
    public IAccessGroup getPrimaryAccessGroupForUser(IApplicationUser user) throws ApplicationUserRepositoryAccessException {
        IAccessGroup groupToReturn = null;

        BaseApplicationUserDO userCasted = (BaseApplicationUserDO) user;
        if (userCasted.hasPrimaryAccessGroup()) {
            groupToReturn = userCasted.getPrimaryAccessGroup();
        }

        return groupToReturn;
    }

    /**
     * Method to create initial seed data domain
     * @return the domain created
     * 
     * @throws DomainAlreadyExistsException
     * @throws ApplicationUserRepositoryAccessException
     * 
     */
    IApplicationUserDomain createInitialDomain() throws ApplicationUserRepositoryAccessException, DomainAlreadyExistsException {
        return this.createDomain(DEFAULT_LOCAL_DOMAIN);
    }

    /**
     * Method to create Adminstrator user. Seed data method
     * 
     * @param administratorPassword
     * @throws DomainNotFoundException
     * @throws UserAlreadyExistsException
     * @throws ApplicationUserRepositoryAccessException
     */
    public void createAdministrator(String administratorPassword) throws ApplicationUserRepositoryAccessException, DomainNotFoundException, UserAlreadyExistsException {
        if (administratorPassword == null) {
            throw new NullPointerException("administratorPassword cannot be null.");
        }

        String defaultDomainName = this.getDefaultAdminDomainName();
        IApplicationUserDomain defaultDomain = this.getApplicationUserDomain(defaultDomainName);
        SuperUserDO superUser = new SuperUserDO(
                SUPER_USER_LOGIN
              , SUPER_USER_FIRST_NAME
              , SUPER_USER_LAST_NAME
              , administratorPassword
              , (AccessDomainDO) defaultDomain);

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            hSession.save(superUser);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            if (HibernateUtils.isUniqueConstraintViolation(exception, LOG)) {
                StringBuffer errorMessage = new StringBuffer("Failed to create super user.  It already exists.");
                throw new UserAlreadyExistsException(errorMessage.toString(), exception);
            } else {
                throw new ApplicationUserRepositoryAccessException("Failed to create super user.", exception);
            }
        } finally {
            closeSession(hSession);
        }
    }

    // Quick hack to fix a design problem at the framework level
    static IExternalDomainManager getExternalDomainManager() {
        return EXTERNAL_DOMAIN_MANAGER;
    }

    /**
     * @param queryCriteria
     * @param searchSpecs
     */
    private void buildUserSearchCriteria(Criteria queryCriteria, IUserSearchSpec[] searchSpecs) {
        if (queryCriteria == null) {
            throw new NullPointerException("queryCriteria cannot be null.");
        }

        if (searchSpecs == null) {
            throw new NullPointerException("searchSpecs cannot be null.");
        }

        queryCriteria.add(buildUserSearchCriteron(searchSpecs));
    }

    /**
     * 
     * @param searchSpecs
     * @return
     */
    private Criterion buildUserSearchCriteron(IUserSearchSpec[] searchSpecs) {
        Criterion criterionToReturn = buildSingleUserSearchCriterion(searchSpecs[0]);
        if (searchSpecs.length > 1) {
            criterionToReturn = buildOredUserSearchCriterion(searchSpecs, criterionToReturn, 1);
        }

        return criterionToReturn;
    }

    /**
     * 
     * @param searchSpecs
     * @param currentCriteron
     * @param searchSpecIndex
     * @return
     */
    private Criterion buildOredUserSearchCriterion(IUserSearchSpec[] searchSpecs, Criterion currentCriteron, int searchSpecIndex) {
        Criterion criterionToReturn = currentCriteron;

        if (searchSpecIndex < searchSpecs.length) {
            Criterion leftHandSideCriterion = buildSingleUserSearchCriterion(searchSpecs[searchSpecIndex]);
            Criterion orCriterion = Expression.or(currentCriteron, leftHandSideCriterion);

            criterionToReturn = buildOredUserSearchCriterion(searchSpecs, orCriterion, searchSpecIndex + 1);
        }

        return criterionToReturn;
    }

    /**
     * @param searchSpecs
     * @return
     */
    private Criterion buildSingleUserSearchCriterion(IUserSearchSpec searchSpecs) {
        return Expression.ilike(APPLICATION_USER_LAST_NAME_PROPERTY, searchSpecs.getLastNameStartsWith() + "%");
    }

    /**
     * @param queryCriteria
     * @param searchSpecs
     */
    private void buildUserGroupSearchCriteria(Criteria queryCriteria, IGroupSearchSpec[] searchSpecs) {
        if (queryCriteria == null) {
            throw new NullPointerException("queryCriteria cannot be null.");
        }

        if (searchSpecs == null) {
            throw new NullPointerException("searchSpecs cannot be null.");
        }

        queryCriteria.add(buildUserGroupSearchCriteron(searchSpecs));
    }

    /**
     * 
     * @param searchSpecs
     * @return
     */
    private Criterion buildUserGroupSearchCriteron(IGroupSearchSpec[] searchSpecs) {
        Criterion criterionToReturn = buildSingleUserGroupSearchCriterion(searchSpecs[0]);
        if (searchSpecs.length > 1) {
            criterionToReturn = buildOredUserGroupSearchCriterion(searchSpecs, criterionToReturn, 1);
        }

        return criterionToReturn;
    }

    /**
     * 
     * @param searchSpecs
     * @param currentCriteron
     * @param searchSpecIndex
     * @return
     */
    private Criterion buildOredUserGroupSearchCriterion(IGroupSearchSpec[] searchSpecs, Criterion currentCriteron, int searchSpecIndex) {
        Criterion criterionToReturn = currentCriteron;

        if (searchSpecIndex < searchSpecs.length) {
            Criterion leftHandSideCriterion = buildSingleUserGroupSearchCriterion(searchSpecs[searchSpecIndex]);
            Criterion orCriterion = Expression.or(currentCriteron, leftHandSideCriterion);

            criterionToReturn = buildOredUserGroupSearchCriterion(searchSpecs, orCriterion, searchSpecIndex + 1);
        }

        return criterionToReturn;
    }

    /**
     * @param searchSpecs
     * @return
     */
    private Criterion buildSingleUserGroupSearchCriterion(IGroupSearchSpec searchSpecs) {
        return Expression.ilike(ACCESS_GROUP_TITLE_PROPERTY, searchSpecs.getTitleStartsWith() + "%");
    }

    /**
     * @param group
     */
    private SortedSet<IApplicationUser> getUsersInLinkedAccessGroup(ILinkedAccessGroup linkedGroup) throws HibernateException, ExternalUserAccessException {
        if (linkedGroup == null) {
            throw new NullPointerException("linkedGroup cannot be null.");
        }

        // FIX ME - This is going to perform horribly!
        SortedSet<IApplicationUser> usersToReturn = new TreeSet<IApplicationUser>(new UserComparatorImpl());

        Collection<IExternalUser> externalUsers = linkedGroup.getExternalMembers();
        Map<String, Set<String>>  domainToUserMap = new HashMap<String, Set<String>>();
        
        for (IExternalUser nextExternalUser : externalUsers) {
            String domainName = nextExternalUser.getDomainName();
            Set<String> userLoginsForDomain = domainToUserMap.get(domainName);
            if (userLoginsForDomain == null) {
                userLoginsForDomain = new HashSet<String>();
                domainToUserMap.put(domainName, userLoginsForDomain);
            }
            userLoginsForDomain.add(nextExternalUser.getLogin());
        }

        // Now, we have a map from domain to login. Add all users from each
        // domain
        // Note: tried to not perform seperate queries for each domain, but it
        // turns out to be more trouble than it's worth, since we're not
        // expecting a large number of domains
        for (Map.Entry<String, Set<String>> nextEntry : domainToUserMap.entrySet()) {
            String nextDomain = nextEntry.getKey();
            Set<String> nextUserLogins = nextEntry.getValue();
            usersToReturn.addAll(getUsersForLogins(nextDomain, nextUserLogins));
        }

        return usersToReturn;
    }

    /**
     * @param domainToUserMap
     * @return
     * @throws HibernateException
     */
    private Collection<BaseApplicationUserDO> getUsersForLogins(String domain, Set<String> userLogins) throws HibernateException {
        // FIX ME - Doesn't handle large sets!!!
        if (domain == null) {
            throw new NullPointerException("domain cannot be null.");
        }
        if (userLogins == null) {
            throw new NullPointerException("userLogins cannot be null.");
        }

        Collection<BaseApplicationUserDO> valuesToReturn;

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            Criteria queryCriteria = hSession.createCriteria(BaseApplicationUserDO.class);
            Criteria joinCriteria = queryCriteria.createCriteria(APPLICATION_USER_ACCESS_DOMAIN_PROPERTY_NAME);
            joinCriteria.add(Expression.eq(APPLICATION_USER_DOMAIN_NAME_PROPERTY, domain));
            queryCriteria.add(Expression.in(APPLICATION_USER_LOGIN_PROPERTY, userLogins));
            valuesToReturn = queryCriteria.list();
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            throw exception;
        } finally {
            closeSession(hSession);
        }

        return valuesToReturn;

    }

    /**
     * Retrieve a Hibernate Session
     * 
     * @return a Hibernate Session
     */
    private Session getSession() throws HibernateException {
        IHibernateRepository dataSource = this.getDataSource();
        return dataSource.getCountedSession();
    }

    /**
     * Close the specified Hibernate Session retrieved from
     * {@see {@link #getSession()}
     * 
     * @param hSession
     */
    private void closeSession(Session hSession) {
        HibernateUtils.closeSession(getDataSource(), LOG);
    }

    /**
     * Returns a data source object that can be used to create sessions.
     * 
     * @return IHibernateDataSource Data source object
     */
    private IHibernateRepository getDataSource() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) componentManager.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());

        if (dataSource == null) {
            throw new IllegalStateException("Required datasource not initialized for Access Domain.");
        }

        return dataSource;
    }
}
