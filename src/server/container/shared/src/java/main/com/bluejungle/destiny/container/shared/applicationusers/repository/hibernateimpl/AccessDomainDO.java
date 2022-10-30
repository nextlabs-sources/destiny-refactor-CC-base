/*
 * Created on May 10, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2006 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl;

import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserCreationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserImportFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserComparatorImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ApplicationUserRepositoryAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.GroupLinkAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IInternalAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ILinkedAccessGroup;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

import net.sf.hibernate.CallbackException;
import net.sf.hibernate.Criteria;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Lifecycle;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Expression;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/repository/hibernateimpl/AccessDomainDO.java#1 $
 */

public class AccessDomainDO implements IApplicationUserDomain, Lifecycle {

    private static final Log LOG = LogFactory.getLog(AccessDomainDO.class.getName());

    private static final String ACCESS_GROUP_NAME_PROPERTY = "title";
    private static final String ACCESS_GROUP_DOMAIN_PROPERTY = "accessDomain";
    private static final String LINKED_ACCESS_GROUP_EXTERNAL_ID_PROPERTY = "externalId";
    private static final String APPLICATION_USER_LOGIN_PROPERTY = "login";
    private static final String APPLICATION_USER_PRIMARY_ACCESS_GROUP_PROPERTY = "hibernatePrimaryAccessGroup";
    private static final String APPLICATION_USER_DOMAIN_PROPERTY = "accessDomain";
    private static final String ACCESS_GROUP_ID_PROPERTY = "id";

    private static final String DELETE_ALL_APPLICATION_USERS_QUERY = "from BaseApplicationUserDO u where u.accessDomain=?";
    private static final String DELETE_ALL_ACCESS_GROUPS_QUERY = "from BaseAccessGroupDO g where g.accessDomain=?";
    private static final Type ACCESS_DOMAIN_HIBERNATE_TYPE = Hibernate.entity(AccessDomainDO.class);

    /** the ID of domain */
    private Long id;

    /** the name of domain */
    private String name;

    /**
     * Create an instance of AccessDomainDO. For Hibernate purposes only
     */
    AccessDomainDO() {
    }

    /**
     * Create an instance of AccessDomainDO
     * 
     * @param name
     */
    AccessDomainDO(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }

        this.name = name;
    }

    /**
     * access id attribute of domain DO
     * 
     * @return the id of domain
     */
    public Long getId() {
        return this.id;
    }

    /**
     * access name attribute of domain DO
     * 
     * @return the name of domain
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain#addUsersToGroup(com.bluejungle.destiny.container.shared.applicationusers.repository.IInternalAccessGroup,
     *      java.util.Collection)
     */
    public void addUsersToGroup(IInternalAccessGroup group, Collection users) throws ApplicationUserRepositoryAccessException {
        if (group == null) {
            throw new NullPointerException("group, users cannot be null.");
        }

        if (users == null) {
            throw new NullPointerException("users cannot be null.");
        }

        ((BaseAccessGroupDO) group).addMembers(users);
        updateUserGroupImpl(group);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain#createAccessGroup(java.lang.String,
     *      java.lang.String)
     */
    public IInternalAccessGroup createAccessGroup(String title, String description) throws ApplicationUserRepositoryAccessException, GroupAlreadyExistsException {
        if (title == null) {
            throw new NullPointerException("title, description cannot be null.");
        }

        if (description == null) {
            throw new NullPointerException("description cannot be null.");
        }

        InternalAccessGroupDO internalAccessGroup = new InternalAccessGroupDO(title, description, this);
        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            hSession.save(internalAccessGroup);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            if (HibernateUtils.isUniqueConstraintViolation(exception, LOG)) {
                StringBuffer errorMessage = new StringBuffer("The group with the name, ");
                errorMessage.append(title);
                errorMessage.append(", already exists.  Please select a different name and try again.");
                throw new GroupAlreadyExistsException(errorMessage.toString(), exception);
            } else {
                throw new ApplicationUserRepositoryAccessException("Failed to create user group.", exception);
            }
        } finally {
            closeSession(hSession);
        }

        return internalAccessGroup;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain#createNewUser(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public IApplicationUser createNewUser(String login, String fn, String ln, String password) throws ApplicationUserCreationFailedException, UserAlreadyExistsException {
        if (login == null) {
            throw new NullPointerException("login cannot be null.");
        }

        if (login.equals(IApplicationUserManager.SUPER_USER_USERNAME)) {
            throw new UserAlreadyExistsException("A user cannot be created with the Super User username");
        }

        if (fn == null) {
            throw new NullPointerException("fn cannot be null.");
        }

        if (ln == null) {
            throw new NullPointerException("ln cannot be null.");
        }

        if (password == null) {
            throw new NullPointerException("password cannot be null.");
        }

        InternalApplicationUserDO userToCreate = new InternalApplicationUserDO(login, fn, ln, password, this);
        try {
            insertUser(userToCreate);
        } catch (HibernateException exception) {
            throw new ApplicationUserCreationFailedException("Failed to create user user.", exception);
        }

        return userToCreate;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain#deleteAccessGroup(com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup)
     */
    public void deleteAccessGroup(IAccessGroup groupToDelete) throws ApplicationUserRepositoryAccessException {
        if (groupToDelete == null) {
            throw new NullPointerException("groupToDelete cannot be null.");
        }

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();

            // This is a little ugly, but not sure what else can be done. Need
            // to remove reference to primary access group
            Criteria queryCriteria = hSession.createCriteria(BaseApplicationUserDO.class);
            Criteria joinCriteria = queryCriteria.createCriteria(APPLICATION_USER_PRIMARY_ACCESS_GROUP_PROPERTY);
            joinCriteria.add(Expression.eq(ACCESS_GROUP_ID_PROPERTY, groupToDelete.getDestinyId()));
            List usersRetrieved = queryCriteria.list();
            Iterator usersRetrievedIterator = usersRetrieved.iterator();
            while (usersRetrievedIterator.hasNext()) {
                BaseApplicationUserDO nextUser = (BaseApplicationUserDO) usersRetrievedIterator.next();
                nextUser.clearPrimaryAccessGroup();
                hSession.save(nextUser);
            }

            // Most likely that group is associated with session after deleting
            // users
            Object persistentGroup = hSession.get(groupToDelete.getClass(), ((BaseAccessGroupDO) groupToDelete).getId());
            hSession.delete(persistentGroup);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            StringBuffer errorMessageBuffer = new StringBuffer("Failed to delete access group with ID, ");
            errorMessageBuffer.append(groupToDelete.getDestinyId());
            errorMessageBuffer.append(".");
            throw new ApplicationUserRepositoryAccessException(errorMessageBuffer.toString(), exception);
        } finally {
            closeSession(hSession);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain#deleteApplicationUser(com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser)
     */
    public void deleteApplicationUser(IApplicationUser userToDelete) throws ApplicationUserRepositoryAccessException, UserNotFoundException {
        if (userToDelete == null) {
            throw new NullPointerException("userToDelete cannot be null.");
        }

        // This is a bit of a hack, but it's an extra safegaurd. Should never
        // happen
        if (userToDelete instanceof SuperUserDO) {
            throw new IllegalArgumentException("Super User cannot be deleted.");
        }

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            hSession.delete(userToDelete);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            StringBuffer errorMessageBuffer = new StringBuffer("Failed to delete user with ID, ");
            errorMessageBuffer.append(userToDelete.getDestinyId());
            errorMessageBuffer.append(".");
            throw new ApplicationUserRepositoryAccessException(errorMessageBuffer.toString(), exception);
        } finally {
            closeSession(hSession);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain#getAccessGroup(java.lang.String)
     */
    public IAccessGroup getAccessGroup(String title) throws ApplicationUserRepositoryAccessException, GroupNotFoundException {
        if (title == null) {
            throw new NullPointerException("title cannot be null.");
        }

        IAccessGroup accessGroupToReturn = null;

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            Criteria queryCriteria = hSession.createCriteria(BaseAccessGroupDO.class);
            queryCriteria.add(Expression.eq(ACCESS_GROUP_NAME_PROPERTY, title));
            this.addDomainCriteriaForAccessGroup(queryCriteria);
            accessGroupToReturn = (IAccessGroup) queryCriteria.uniqueResult();
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            StringBuffer errorMessageBuffer = new StringBuffer("Failed to retrieve access group with title, ");
            errorMessageBuffer.append(title);
            errorMessageBuffer.append(".");
            throw new ApplicationUserRepositoryAccessException(errorMessageBuffer.toString(), exception);
        } finally {
            closeSession(hSession);
        }

        if (accessGroupToReturn == null) {
            StringBuffer errorMessageBuffer = new StringBuffer("Access group with title, ");
            errorMessageBuffer.append(title);
            errorMessageBuffer.append(", could not be found.");
            throw new GroupNotFoundException(errorMessageBuffer.toString());
        }

        return accessGroupToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain#getApplicationUsers()
     */
    public SortedSet getApplicationUsers() throws ApplicationUserRepositoryAccessException {
        SortedSet userToReturn;

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            Criteria queryCriteria = hSession.createCriteria(BaseApplicationUserDO.class);
            this.addDomainCriteriaForApplicationUser(queryCriteria);
            List allApplicationUsers = queryCriteria.list();
            userToReturn = new TreeSet(new UserComparatorImpl());
            userToReturn.addAll(allApplicationUsers);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            StringBuffer errorMessageBuffer = new StringBuffer("Failed to retrieve application users.");
            throw new ApplicationUserRepositoryAccessException(errorMessageBuffer.toString(), exception);
        } finally {
            closeSession(hSession);
        }

        return userToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain#getLinkedGroupByExternalId(byte[])
     */
    public ILinkedAccessGroup getLinkedGroupByExternalId(byte[] externalId) throws GroupNotFoundException, ApplicationUserRepositoryAccessException {
        if (externalId == null) {
            throw new NullPointerException("externalId cannot be null.");
        }

        ILinkedAccessGroup accessGroupToReturn = null;

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            Criteria queryCriteria = hSession.createCriteria(LinkedAccessGroupDO.class);
            queryCriteria.add(Expression.eq(LINKED_ACCESS_GROUP_EXTERNAL_ID_PROPERTY, externalId));
            this.addDomainCriteriaForAccessGroup(queryCriteria);
            accessGroupToReturn = (ILinkedAccessGroup) queryCriteria.uniqueResult();
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            StringBuffer errorMessageBuffer = new StringBuffer("Failed to retrieve linked access group with external ID, ");
            errorMessageBuffer.append(externalId);
            errorMessageBuffer.append(".");
            throw new ApplicationUserRepositoryAccessException(errorMessageBuffer.toString(), exception);
        } finally {
            closeSession(hSession);
        }

        if (accessGroupToReturn == null) {
            StringBuffer errorMessageBuffer = new StringBuffer("Linked access group with external ID, ");
            errorMessageBuffer.append(externalId);
            errorMessageBuffer.append(", could not be found.");
            throw new GroupNotFoundException(errorMessageBuffer.toString());
        }

        return accessGroupToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain#getUser(java.lang.String)
     */
    public IApplicationUser getUser(String login) throws ApplicationUserRepositoryAccessException, UserNotFoundException {
        return getUser(login, true);
    }

    /**
     * Retrieve the user by login in a case insensitive way. This was added to
     * fix a bug in which case insensitive login against AD was not working
     * because after authentication with AD, the corresponding Application User
     * was being looked up in a case sensitive fashion in the database, leading
     * to an improper failed authentication
     * 
     * Note that I didn't add this method to the {@see IApplicationUserDomain} 
     * interface because frankly the object model of the application users api
     * is not clear to me and I had no idea if it belonged or not
     * 
     * @param login
     * @return the retrieved user
     * @throws UserNotFoundException 
     * @throws ApplicationUserRepositoryAccessException 
     */
    public IApplicationUser getUserIgnoreCase(String login) throws ApplicationUserRepositoryAccessException, UserNotFoundException {
        return getUser(login, false);
    }
    
    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain#importExternalUser(com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser)
     */
    public IApplicationUser importExternalUser(IExternalUser userToImport) throws ApplicationUserImportFailedException, UserAlreadyExistsException {
        if (userToImport == null) {
            throw new NullPointerException("userToImport cannot be null.");
        }

        ImportedApplicationUserDO importedUser = new ImportedApplicationUserDO(userToImport.getLogin(), userToImport.getFirstName(), userToImport.getLastName(), this);
        try {
            insertUser(importedUser);
            updateUserAuthHandlerId(importedUser);
        } catch (HibernateException exception) {
            throw new ApplicationUserImportFailedException("Failed to import user.", exception);
        }

        return importedUser;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain#linkExternalGroup(byte[],
     *      java.lang.String)
     */
    public ILinkedAccessGroup linkExternalGroup(byte[] externalId, String title) throws GroupAlreadyExistsException, GroupLinkAlreadyExistsException, ApplicationUserRepositoryAccessException {
        if (externalId == null) {
            throw new NullPointerException("externalId cannot be null.");
        }

        if (title == null) {
            throw new NullPointerException("title cannot be null.");
        }

        LinkedAccessGroupDO linkedAccessGroup = new LinkedAccessGroupDO(externalId, title, this);
        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            hSession.save(linkedAccessGroup);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            if (HibernateUtils.isUniqueConstraintViolation(exception, LOG)) {
                StringBuffer errorMessage = new StringBuffer("A group with the name, ");
                errorMessage.append(title);
                errorMessage.append(", or the externalId, ");
                errorMessage.append(externalId);
                errorMessage.append(", already exists.  Please select a different name and try again.  Also, please verify that this external group has not already been linked.");
                throw new GroupAlreadyExistsException(errorMessage.toString(), exception);
            } else {
                throw new ApplicationUserRepositoryAccessException("Failed to create user group.", exception);
            }
        } finally {
            closeSession(hSession);
        }

        return linkedAccessGroup;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain#removeUsersFromGroup(com.bluejungle.destiny.container.shared.applicationusers.repository.IInternalAccessGroup,
     *      java.util.Collection)
     */
    public void removeUsersFromGroup(IInternalAccessGroup group, Collection users) throws ApplicationUserRepositoryAccessException {
        if (group == null) {
            throw new NullPointerException("group, users cannot be null.");
        }

        if (users == null) {
            throw new NullPointerException("users cannot be null.");
        }

        ((BaseAccessGroupDO) group).deleteMembers(users);
        updateUserGroupImpl(group);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain#setApplicableAccessControlForGroup(com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup,
     *      java.lang.String)
     */
    public void setApplicableAccessControlForGroup(IAccessGroup groupToUpdate, String acl) throws ApplicationUserRepositoryAccessException {
        if (groupToUpdate == null) {
            throw new NullPointerException("groupToUpdate cannot be null.");
        }

        if (acl == null) {
            throw new NullPointerException("acl cannot be null.");
        }

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            ((BaseAccessGroupDO) groupToUpdate).setApplicableAccessControl(acl);
            hSession.update(groupToUpdate);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            throw new ApplicationUserRepositoryAccessException("Failed to update group.", exception);
        } finally {
            closeSession(hSession);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain#setDescriptionForGroup(com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup,
     *      java.lang.String)
     */
    public void setDescriptionForGroup(IAccessGroup groupToUpdate, String description) throws ApplicationUserRepositoryAccessException {
        if (groupToUpdate == null) {
            throw new NullPointerException("groupToUpdate cannot be null.");
        }

        if (description == null) {
            throw new NullPointerException("description cannot be null.");
        }

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            ((BaseAccessGroupDO) groupToUpdate).setDescription(description);
            hSession.update(groupToUpdate);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            throw new ApplicationUserRepositoryAccessException("Failed to update user group.", exception);
        } finally {
            closeSession(hSession);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain#setPrimaryAccessGroupForUser(com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser,
     *      com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup)
     */
    public void setPrimaryAccessGroupForUser(IApplicationUser user, IAccessGroup primaryGroup) throws ApplicationUserRepositoryAccessException {
        if (user == null) {
            throw new NullPointerException("user cannot be null.");
        }

        BaseApplicationUserDO appUserDO = (BaseApplicationUserDO) user;
        if (primaryGroup == null) {
            appUserDO.clearPrimaryAccessGroup();
        } else {
            appUserDO.setPrimaryAccessGroup(primaryGroup);
        }

        updateUserImpl(appUserDO);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain#setTitleForGroup(com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup,
     *      java.lang.String)
     */
    public void setTitleForGroup(IAccessGroup groupToUpdate, String newTitle) throws ApplicationUserRepositoryAccessException, GroupAlreadyExistsException {
        if (groupToUpdate == null) {
            throw new NullPointerException("groupToUpdate cannot be null.");
        }

        if (newTitle == null) {
            throw new NullPointerException("newTitle cannot be null.");
        }

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            ((BaseAccessGroupDO) groupToUpdate).setTitle(newTitle);
            hSession.update(groupToUpdate);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            if (HibernateUtils.isUniqueConstraintViolation(exception, LOG)) {
                StringBuffer errorMessage = new StringBuffer("The group with the name, ");
                errorMessage.append(newTitle);
                errorMessage.append(", already exists.  Please select a different name and try again.");
                throw new GroupAlreadyExistsException(errorMessage.toString(), exception);
            } else {
                throw new ApplicationUserRepositoryAccessException("Failed to update user group.", exception);
            }
        } finally {
            closeSession(hSession);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain#updateUser(com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser,
     *      java.lang.String)
     */
    public void updateUser(IApplicationUser user, String password) throws UserNotFoundException, ApplicationUserRepositoryAccessException {
        if (user == null) {
            throw new NullPointerException("user cannot be null.");
        }

        String userName = user.getLogin();
        if (userName.equals(IApplicationUserManager.SUPER_USER_USERNAME)) {
            SuperUserDO superUser = getSuperUser();
            superUser.setPassword(password);
            updateUserImpl(superUser);
        } else {
            // Try to retrieve the user
            BaseApplicationUserDO applicationUserToUpdate = (BaseApplicationUserDO) getUser(user.getLogin());

            String firstNameToUpdate = user.getFirstName();
            if (firstNameToUpdate != null) {
                applicationUserToUpdate.setFirstName(firstNameToUpdate);
            }

            String lastNameToUpdate = user.getLastName();
            if (lastNameToUpdate != null) {
                applicationUserToUpdate.setLastName(lastNameToUpdate);
            }

            if (password != null) {
                if (!(applicationUserToUpdate instanceof InternalApplicationUserDO)) {
                    throw new IllegalArgumentException("Password of imported user cannot be changed.  In this case, the password argument must be null.");
                }

                ((InternalApplicationUserDO) applicationUserToUpdate).setPassword(password);
            }

            updateUserImpl(applicationUserToUpdate);
        }
    }

    /**
     * @see net.sf.hibernate.Lifecycle#onDelete(net.sf.hibernate.Session)
     */
    public boolean onDelete(Session s) throws CallbackException {
        try {
            deleteAccessGroups(s);
            deleteApplicationUsers(s);
        } catch (HibernateException exception) {
            throw new CallbackException("Failed to delete dependent application users and/or access groups.", exception);
        }

        return false;
    }

    /**
     * @see net.sf.hibernate.Lifecycle#onLoad(net.sf.hibernate.Session,
     *      java.io.Serializable)
     */
    public void onLoad(Session s, Serializable id) {
    }

    /**
     * @see net.sf.hibernate.Lifecycle#onSave(net.sf.hibernate.Session)
     */
    public boolean onSave(Session s) throws CallbackException {
        return false;
    }

    /**
     * @see net.sf.hibernate.Lifecycle#onUpdate(net.sf.hibernate.Session)
     */
    public boolean onUpdate(Session s) throws CallbackException {
        return false;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object otherDomain) {
        boolean valueToReturn = false;
        if ((otherDomain != null) && (otherDomain instanceof AccessDomainDO)) {
            // Hibernate recommends not using ID, but I feel that it's the best
            // way to test equality. The reasons they provide are valid, but the
            // trade off of using the fields for equality is not worth it
            valueToReturn = this.getId().equals(((AccessDomainDO) otherDomain).getId());
        }
        return valueToReturn;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.getId().hashCode();
    }

    /**
     * set name attribute of domain DO. Required by Hibernate
     * 
     * @param the
     *            name of domain
     */
    void setName(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }
        this.name = name;
    }

    /**
     * set id attribute of domain DO. Required by Hibernate
     * 
     * @param the
     *            id of domain
     */
    void setId(Long id) {
        if (id == null) {
            throw new NullPointerException("id cannot be null.");
        }
        this.id = id;
    }

    /**
     * Retieve user by login and case
     * 
     * @throws ApplicationUserRepositoryAccessException 
     * @throws UserNotFoundException 
     */
    private IApplicationUser getUser(String login, boolean caseSensitive) throws ApplicationUserRepositoryAccessException, UserNotFoundException {
        if (login == null) {
            throw new NullPointerException("login cannot be null.");
        }

        IApplicationUser userToReturn = null;

        if (login.equals(IApplicationUserManager.SUPER_USER_USERNAME)) {
            userToReturn = getSuperUser();
        } else {
            Session hSession = null;
            Transaction transaction = null;
            try {
                hSession = getSession();
                transaction = hSession.beginTransaction();
                Criteria queryCriteria = hSession.createCriteria(BaseApplicationUserDO.class);
                
                if (caseSensitive) {
                    queryCriteria.add(Expression.eq(APPLICATION_USER_LOGIN_PROPERTY, login));
                } else {
                    queryCriteria.add(Expression.ilike(APPLICATION_USER_LOGIN_PROPERTY, login));
                }
                    
                this.addDomainCriteriaForApplicationUser(queryCriteria);
                List usersFound = queryCriteria.list();
                if (usersFound.size() == 1) {
                    userToReturn = (IApplicationUser) usersFound.get(0);
                } else if ((usersFound.size() > 1) && (!caseSensitive)) {
                    // Look for a direct case match
                    Iterator usersFoundIterator = usersFound.iterator();
                    while ((usersFoundIterator.hasNext()) && (userToReturn == null)) {
                        IApplicationUser nextUser = (IApplicationUser) usersFoundIterator.next();
                        if (nextUser.getLogin().equals(login)) {
                            userToReturn = nextUser;
                        }                        
                    }
                }
                
                transaction.commit();
            } catch (HibernateException exception) {
                HibernateUtils.rollbackTransation(transaction, LOG);
                StringBuffer errorMessageBuffer = new StringBuffer("Failed to retrieve user with login, ");
                errorMessageBuffer.append(login);
                errorMessageBuffer.append(".");
                throw new ApplicationUserRepositoryAccessException(errorMessageBuffer.toString(), exception);
            } finally {
                closeSession(hSession);
            }
        }

        if (userToReturn == null) {
            StringBuffer errorMessageBuffer = new StringBuffer("Appliction User with login, ");
            errorMessageBuffer.append(login);
            errorMessageBuffer.append(", could not be found.");
            throw new UserNotFoundException(errorMessageBuffer.toString());
        }

        return userToReturn;
    }
    
	private void updateUserAuthHandlerId(BaseApplicationUserDO user) throws HibernateException {
		Session session = null;
		Transaction transaction = null;

		try {
			session = getSession();
			transaction = session.beginTransaction();
			Connection conn = session.connection();
			PreparedStatement stmt = conn.prepareStatement("UPDATE APPLICATION_USER SET AUTH_HANDLER_ID=(SELECT ID FROM AUTH_HANDLER_REGISTRY ) WHERE ID= ? ");
			stmt.setLong(1, user.getId());
			stmt.executeUpdate();
			transaction.commit();
		} catch (Exception e) {
			throw new HibernateException(e);
		} finally {
			closeSession(session);
		}
	}
    
    /**
     * Insert a new user into the database
     * 
     * @param userToCreate
     * @throws UserAlreadyExistsException
     * @throws HibernateException
     */
    private void insertUser(BaseApplicationUserDO userToCreate) throws UserAlreadyExistsException, HibernateException {
        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            hSession.save(userToCreate);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            if (HibernateUtils.isUniqueConstraintViolation(exception, LOG)) {
                StringBuffer errorMessage = new StringBuffer("The user with the login, ");
                errorMessage.append(userToCreate.getLogin());
                errorMessage.append(", already exists.  Please select a different login and try again.");
                throw new UserAlreadyExistsException(errorMessage.toString(), exception);
            } else {
                throw exception;
            }
        } finally {
            closeSession(hSession);
        }
    }

    /**
     * @return
     * @throws HibernateException
     * @throws ApplicationUserRepositoryAccessException
     */
    private SuperUserDO getSuperUser() throws ApplicationUserRepositoryAccessException {
        SuperUserDO userToReturn = null;

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            Criteria queryCriteria = hSession.createCriteria(SuperUserDO.class);
            userToReturn = (SuperUserDO) queryCriteria.uniqueResult();
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            throw new ApplicationUserRepositoryAccessException("Failed to retrieve Super User", exception);
        } finally {
            closeSession(hSession);
        }

        return userToReturn;
    }

    /**
     * Update an existing user
     * 
     * @param superUser
     * @throws ApplicationUserRepositoryAccessException
     */
    private void updateUserImpl(IApplicationUser applicationUser) throws ApplicationUserRepositoryAccessException {
        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            hSession.update(applicationUser);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            throw new ApplicationUserRepositoryAccessException("Failed to save user.", exception);
        } finally {
            closeSession(hSession);
        }
    }

    /**
     * Save the specified user group
     * 
     * @param group
     *            the group to save
     * @throws ApplicationUserRepositoryAccessException
     */
    private void updateUserGroupImpl(IInternalAccessGroup group) throws ApplicationUserRepositoryAccessException {
        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            hSession.update(group);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, LOG);
            throw new ApplicationUserRepositoryAccessException("Failed to save user group.", exception);
        } finally {
            closeSession(hSession);
        }
    }

    /**
     * @param queryCriteria
     */
    private void addDomainCriteriaForAccessGroup(Criteria queryCriteria) {
        if (queryCriteria == null) {
            throw new NullPointerException("queryCriteria cannot be null.");
        }

        queryCriteria.add(Expression.eq(ACCESS_GROUP_DOMAIN_PROPERTY, this));
    }

    /**
     * @param queryCriteria
     */
    private void addDomainCriteriaForApplicationUser(Criteria queryCriteria) {
        if (queryCriteria == null) {
            throw new NullPointerException("queryCriteria cannot be null.");
        }

        queryCriteria.add(Expression.eq(APPLICATION_USER_DOMAIN_PROPERTY, this));
    }

    /**
     * Delete all application users for this access domain
     * 
     * @throws HibernateException
     */
    private void deleteApplicationUsers(Session hSession) throws HibernateException {
        hSession.delete(DELETE_ALL_APPLICATION_USERS_QUERY, this, ACCESS_DOMAIN_HIBERNATE_TYPE);
    }

    /**
     * Delete all access groups for this access domain
     * 
     * @throws HibernateException
     */
    private void deleteAccessGroups(Session hSession) throws HibernateException {
        hSession.delete(DELETE_ALL_ACCESS_GROUPS_QUERY, this, ACCESS_DOMAIN_HIBERNATE_TYPE);
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
