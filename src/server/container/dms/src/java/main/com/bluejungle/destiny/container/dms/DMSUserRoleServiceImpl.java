package com.bluejungle.destiny.container.dms;

// All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc,
// Redwood City CA,
// Ownership remains with Blue Jungle Inc, All rights reserved worldwide.

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserCreationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserDeleteFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserImportFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserManagerFactoryImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserModificationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationModeEnumType;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerFactory;
import com.bluejungle.destiny.container.shared.applicationusers.core.IUserSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserManagementAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.external.ExternalUserAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomain;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser;
import com.bluejungle.destiny.container.shared.applicationusers.service.UserManagementServiceHelper;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.services.management.UserRoleServiceIF;
import com.bluejungle.destiny.services.management.types.AuthenticationModeEnumDTO;
import com.bluejungle.destiny.services.management.types.DuplicateLoginNameException;
import com.bluejungle.destiny.services.management.types.UserDTO;
import com.bluejungle.destiny.services.management.types.UserDTOList;
import com.bluejungle.destiny.services.management.types.UserDTOQueryResults;
import com.bluejungle.destiny.services.management.types.UserManagementMetadata;
import com.bluejungle.destiny.services.management.types.UserQuerySpec;
import com.bluejungle.destiny.services.management.types.UserQueryTerm;
import com.bluejungle.destiny.services.management.types.UserQueryTermSet;
import com.bluejungle.destiny.services.policy.types.DMSExternalUserQueryResultsDTO;
import com.bluejungle.destiny.services.policy.types.DMSRoleData;
import com.bluejungle.destiny.services.policy.types.DMSRoleDataList;
import com.bluejungle.destiny.services.policy.types.DMSUserData;
import com.bluejungle.destiny.services.policy.types.SubjectDTO;
import com.bluejungle.destiny.services.policy.types.SubjectDTOList;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.pf.destiny.lib.DMSServiceImpl;
import com.bluejungle.pf.destiny.lib.IDMSService;
import com.bluejungle.pf.destiny.lib.PolicyServiceException;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;

/**
 * TODO Write file summary here.
 *
 * @author pkeni
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/DMSUserRoleServiceImpl.java#1 $
 */

/**
 * @author pkeni
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DMSUserRoleServiceImpl implements UserRoleServiceIF {

    private static final Log LOG = LogFactory.getLog(DMSUserRoleServiceImpl.class.getName());

    IDMSService pfDMSService;
    IApplicationUserManager appUserManager;

    /**
     *
     */
    public DMSUserRoleServiceImpl() {
        super();
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        pfDMSService = (IDMSService) manager.getComponent(DMSServiceImpl.COMP_INFO);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.bluejungle.destiny.services.management.UserRoleServiceIF#getAllUsers()
     */
    public UserDTOList getAllUsers() throws RemoteException {
        IApplicationUserManager appUserManager = getApplicationUserManager();
        UserDTOList userList = null;
        try {
            Collection applicationUsers = appUserManager.getApplicationUsers(null, 0);

            // We need to filter out the super user:
            for (Iterator iter = applicationUsers.iterator(); iter.hasNext();) {
                IApplicationUser user = (IApplicationUser) iter.next();
                if (user.getLogin().equals(IApplicationUserManager.SUPER_USER_USERNAME)) {
                    applicationUsers.remove(user);
                    break;
                }
            }

            userList = convertApplicationUserSetToSubjectList(applicationUsers);
        } catch (UserManagementAccessException e) {
            LOG.error("Failed to retrieve all users", e);
            throw new RemoteException("Unable to access external user list");
        } catch (UserNotFoundException exception) {
            LOG.error("Failed to retrieve all users.  Unable to convert to web service objects", exception);
            throw new RemoteException("Failed to retrieve all users.  Unable to convert to web service objects");
        }
        return userList;
    }


    /**
     * @see com.bluejungle.destiny.services.management.UserRoleServiceIF#getUser(java.math.BigInteger)
     */
    public UserDTO getUser(BigInteger userId) throws RemoteException {
        UserDTO userToReturn = null;

        IApplicationUserManager appUserManager = getApplicationUserManager();
        try {
            IApplicationUser user = appUserManager.getApplicationUser(userId.longValue());

            userToReturn = UserManagementServiceHelper.convertUserToDTO(user);
        } catch (UserManagementAccessException e) {
            LOG.error("Failed to retrieve user for ID, " + userId + ".  Unable to access external user list.", e);
            throw new RemoteException("Failed to retrieve user for ID, " + userId + ".  Unable to access external user list.");
        } catch (UserNotFoundException exception) {
            LOG.error("Failed to retrieve user for ID, " + userId + ".  It could not be found in the persistence store.", exception);
            throw new RemoteException("Failed to retrieve user for ID, " + userId + ".  It could not be found in the persistence store.");
        }
        return userToReturn;
    }

    /**
     * @see com.bluejungle.destiny.services.management.UserRoleServiceIF#runUserQuery(com.bluejungle.destiny.services.management.types.UserQueryTermSet)
     */
    public UserDTOQueryResults runUserQuery(UserQuerySpec userQuerySpec) throws RemoteException {
        IApplicationUserManager appUserManager = getApplicationUserManager();
        UserDTOQueryResults userQueryResults = null;
        try {
            SortedSet users = null;
            UserQueryTermSet userQueryTermSet = userQuerySpec.getUserQueryTermSet();
            if (userQueryTermSet != null) {
                UserQueryTerm[] userQueryTermArr = userQueryTermSet.getUserQueryTerm();

                // Convert the query term array to a search spec array:
                IUserSearchSpec[] userSearchSpecs = new UserSearchSpecImpl[userQueryTermArr.length];
                for (int i = 0; i < userQueryTermArr.length; i++) {
                    UserQueryTerm term = userQueryTermArr[i];
                    IUserSearchSpec spec = new UserSearchSpecImpl(term);
                    userSearchSpecs[i] = spec;
                }

                // Run query:
                users = appUserManager.getApplicationUsers(userSearchSpecs, userQuerySpec.getMaxResults().intValue());
            }

            UserDTOList userList = convertApplicationUserSetToSubjectList(users);
            userQueryResults = new UserDTOQueryResults(userList);
        } catch (UserManagementAccessException e) {
            LOG.error("Unable to run user query", e);
            throw new RemoteException("Unable to run user query");
        } catch (UserNotFoundException exception) {
            LOG.error("Failed to retrieve all users.  Unable to convert to web service objects", exception);
            throw new RemoteException("Failed to retrieve all users.  Unable to convert to web service objects");
        }
        return userQueryResults;
    }

    /**
     * @see com.bluejungle.destiny.services.management.UserRoleServiceIF#runExternalUserQuery(com.bluejungle.destiny.services.policy.types.UserQueryTermSet)
     */
    public DMSExternalUserQueryResultsDTO runExternalUserQuery(UserQuerySpec userQueryTermSpec) throws RemoteException {
        IApplicationUserManager appUserManager = getApplicationUserManager();
        SortedSet externalUsers = null;
        try {
            UserQueryTermSet userQueryTermSet = userQueryTermSpec.getUserQueryTermSet();
            if (userQueryTermSet != null) {
                UserQueryTerm[] userQueryTermArr = userQueryTermSet.getUserQueryTerm();

                // Convert the query term array to a search spec array:
                IUserSearchSpec[] userSearchSpecs = new UserSearchSpecImpl[userQueryTermArr.length];
                for (int i = 0; i < userQueryTermArr.length; i++) {
                    UserQueryTerm term = userQueryTermArr[i];
                    IUserSearchSpec spec = new UserSearchSpecImpl(term);
                    userSearchSpecs[i] = spec;
                }

                // Run query:
                externalUsers = appUserManager.getExternalUsers(userSearchSpecs, userQueryTermSpec.getMaxResults().intValue());
            }
        } catch (UserManagementAccessException e) {
            LOG.error("Failed to run external user query", e);
            throw new RemoteException("Failed to run external user query");
        }
        return convertExternalUserSetToQueryResult(externalUsers);
    }

    /**
     * Converts a set of <IApplicationUser>entries into SubjectDTOList
     *
     * @param users
     * @return subject list
     */
    private UserDTOList convertApplicationUserSetToSubjectList(Collection users) throws ServiceNotReadyFault, UserNotFoundException, UserManagementAccessException {
        UserDTOList subjectList = null;
        if (users != null) {
            subjectList = new UserDTOList();
            UserDTO[] subjectArr = new UserDTO[users.size()];
            Iterator iter = users.iterator();
            for (int i = 0; iter.hasNext(); i++) {
                IApplicationUser user = (IApplicationUser) iter.next();
                UserDTO userDTO = UserManagementServiceHelper.convertUserToDTO(user);
                subjectArr[i] = userDTO;
            }
            subjectList.setUsers(subjectArr);
        }
        return subjectList;
    }

    /**
     * Converts a set of <IExternalUser>entries into UserQueryResultsDTO
     *
     * @param users
     * @return query results
     */
    private DMSExternalUserQueryResultsDTO convertExternalUserSetToQueryResult(Collection users) {
        DMSExternalUserQueryResultsDTO results = null;
        if (users != null) {
            results = new DMSExternalUserQueryResultsDTO();
            SubjectDTOList subjectList = new SubjectDTOList();
            SubjectDTO[] subjectArr = new SubjectDTO[users.size()];
            Iterator iter = users.iterator();
            for (int i = 0; iter.hasNext(); i++) {
                IExternalUser user = (IExternalUser) iter.next();
                subjectArr[i] = new SubjectDTO();
                subjectArr[i].setId(new BigInteger("-1"));
                subjectArr[i].setUniqueName(user.getUniqueName());
                subjectArr[i].setUid(user.getLogin());
                subjectArr[i].setType(SubjectType.USER.getName());
                subjectArr[i].setName(user.getDisplayName());
            }
            subjectList.setSubjects(subjectArr);
            results.setMatchingAgents(subjectList);
        }
        return results;
    }

    /**
     * @see com.bluejungle.destiny.services.management.UserRoleServiceIF#importExternalUsers(com.bluejungle.destiny.services.policy.types.SubjectDTOList)
     */
    public UserDTOList importExternalUsers(SubjectDTOList userOrRoleList) throws RemoteException {
        UserDTOList listToReturn = null;
        try {
            IApplicationUserManager appUserManager = getApplicationUserManager();
            if (userOrRoleList != null && userOrRoleList.getSubjects() != null) {
                List importedUsers = new LinkedList(); // Use a list to match
                // order of provided
                // list if possible
                SubjectDTO[] usersToImport = userOrRoleList.getSubjects();
                for (int i = 0; i < usersToImport.length; i++) {
                    SubjectDTO user = usersToImport[i];

                    // Lookup the user from the external domain - we assume
                    // there's a single extenral domain here (for 1.0):
                    String login = user.getUid();
                    Set extDomainSingleton = appUserManager.getExternalDomains();
                    IExternalDomain domain = (IExternalDomain) extDomainSingleton.iterator().next();
                    IExternalUser userToImport = domain.getUserAccessProvider().getUser(login);
                    IApplicationUser importedUser = appUserManager.importExternalUser(userToImport);
                    importedUsers.add(importedUser);
                }
                listToReturn = convertApplicationUserSetToSubjectList(importedUsers);
            } else {
                listToReturn = new UserDTOList();
            }
        } catch (UserManagementAccessException e) {
            LOG.error("Failed to import external users", e);
            throw new RemoteException("Failed to import external users");
        } catch (ApplicationUserImportFailedException e) {
            LOG.error("Failed to import external users", e);
            throw new RemoteException("Failed to import external users");
        } catch (UserNotFoundException e) {
            LOG.error("Failed to import external users", e);
            throw new RemoteException("Failed to import external users");
        } catch (ExternalUserAccessException e) {
            LOG.error("Failed to import external users", e);
            throw new RemoteException("Failed to import external users");
        } catch (UserAlreadyExistsException e) {
            LOG.error("Failed to import external users.", e);
            throw new RemoteException("Failed to import external users");
        }

        return listToReturn;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.bluejungle.destiny.services.management.UserRoleServiceIF#getAllRoles()
     */
    public SubjectDTOList getAllRoles() throws RemoteException {
        SubjectDTOList roleList = new SubjectDTOList();
        Collection roles;
        try {
            roles = pfDMSService.getAllRoles();
        } catch (PolicyServiceException e) {
            LOG.error("Failed to retrieve all roles.", e);
            throw new RemoteException("Error getting all roles");
        }
        roleList.setSubjects((SubjectDTO[]) (roles.toArray(new SubjectDTO[roles.size()])));
        return roleList;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.bluejungle.destiny.services.management.UserRoleServiceIF#getUserData(com.bluejungle.destiny.services.policy.types.SubjectDTO)
     */
    public DMSUserData getUserData(SubjectDTO userOrRole) throws RemoteException {
        try {
            return pfDMSService.getUserData(userOrRole);
        } catch (PolicyServiceException e) {
            LOG.error("Failed to retrieve user data.", e);
            throw new RemoteException("Failed to retrieve user data");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.bluejungle.destiny.services.management.UserRoleServiceIF#getRoleData(com.bluejungle.destiny.services.policy.types.SubjectDTO,
     *      com.bluejungle.destiny.services.policy.types.holders.DMSRoleDataHolder,
     *      com.bluejungle.destiny.services.policy.types.holders.DMSRoleDataHolder)
     */
    public DMSRoleData getRoleData(SubjectDTO userOrRole) throws RemoteException {
        try {
            return pfDMSService.getRoleData(userOrRole);
        } catch (PolicyServiceException e) {
            LOG.error("Failed to retrieve role data.", e);
            throw new RemoteException("Error getting role data");
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see com.bluejungle.destiny.services.management.UserRoleServiceIF#getRoleData(com.bluejungle.destiny.services.policy.types.SubjectDTO,
     *      com.bluejungle.destiny.services.policy.types.holders.DMSRoleDataHolder,
     *      com.bluejungle.destiny.services.policy.types.holders.DMSRoleDataHolder)
     */
    public DMSRoleDataList getAllRoleData() throws RemoteException {
        Collection data = null;
        try {
            data = pfDMSService.getAllRoleData();
        } catch (PolicyServiceException e) {
            LOG.error("Failed to retrieve role data", e);
            throw new RemoteException("Error getting role data");
        }

        if (data == null)
            return null;
        DMSRoleDataList roleDataList = new DMSRoleDataList();
        roleDataList.setRolesdata((DMSRoleData[]) (data.toArray(new DMSRoleData[data.size()])));
        return roleDataList;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.bluejungle.destiny.services.management.UserRoleServiceIF#getRoleData(com.bluejungle.destiny.services.policy.types.SubjectDTO,
     *      com.bluejungle.destiny.services.policy.types.holders.DMSRoleDataHolder,
     *      com.bluejungle.destiny.services.policy.types.holders.DMSRoleDataHolder)
     */
    public DMSRoleData getRoleDataById(BigInteger id) throws RemoteException {
        try {
            return pfDMSService.getRoleDataById(new Long(id.longValue()));
        } catch (PolicyServiceException e) {
            LOG.error("Failed to retrieve role data by id", e);
            throw new RemoteException("Error getting role data");
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see com.bluejungle.destiny.services.management.UserRoleServiceIF#setUserData(com.bluejungle.destiny.services.policy.types.SubjectDTO,
     *      com.bluejungle.destiny.services.policy.types.DMSUserData)
     */
    public void setUserData(UserDTO user, DMSUserData data) throws RemoteException {
        try {
            updateUser(user);
            pfDMSService.setUserData(user, data);
        } catch (PolicyServiceException e) {
            throw new RemoteException("Error setting user data", e);
        } catch (ServiceNotReadyFault e) {
            throw new RemoteException("Error setting user data", e);
        } catch (UserNotFoundException e) {
            throw new RemoteException("Error setting user data", e);
        } catch (UserManagementAccessException e) {
            throw new RemoteException("Error setting user data", e);
        } catch (ApplicationUserModificationFailedException exception) {
            throw new RemoteException("Error setting user data", exception);
        } catch (GroupNotFoundException exception) {
            throw new RemoteException("Error setting user data", exception);
        }
    }

    private void updateUser(UserDTO user) throws ServiceNotReadyFault, UserNotFoundException, UserManagementAccessException, ApplicationUserModificationFailedException, GroupNotFoundException {
        IApplicationUserManager appUserManager = getApplicationUserManager();
        ApplicationUser appUser = new ApplicationUser(user);
        appUserManager.updateApplicationUser(appUser, user.getPassword());

        Long primaryAccessGroupId = null;
        BigInteger primaryAccessGroupIdToSet = user.getPrimaryUserGroupId();
        if (primaryAccessGroupIdToSet != null) {
            primaryAccessGroupId = new Long(primaryAccessGroupIdToSet.longValue());
        }
        appUserManager.setPrimaryAccessGroupForUser(user.getId().longValue(), primaryAccessGroupId);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.bluejungle.destiny.services.management.UserRoleServiceIF#setRoleData(com.bluejungle.destiny.services.policy.types.SubjectDTO,
     *      com.bluejungle.destiny.services.policy.types.DMSRoleData)
     */
    public void setRoleData(SubjectDTO role, DMSRoleData data) throws RemoteException {
        try {
            pfDMSService.setRoleData(role, data);
        } catch (PolicyServiceException e) {
            LOG.error("Failed to set role data", e);
            throw new RemoteException("Error setting user data");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.bluejungle.destiny.services.management.UserRoleServiceIF#setRoleData(com.bluejungle.destiny.services.policy.types.SubjectDTO,
     *      com.bluejungle.destiny.services.policy.types.DMSRoleData)
     */
    public void setRoleDataById(BigInteger id, DMSRoleData data) throws RemoteException {
        try {
            pfDMSService.setRoleDataById(new Long(id.longValue()), data);
        } catch (PolicyServiceException e) {
            LOG.error("Failed to set role data by id", e);
            throw new RemoteException("Error setting user data");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.bluejungle.destiny.services.management.UserRoleServiceIF#addToUsers(com.bluejungle.destiny.services.policy.types.SubjectDTO)
     */
    public void addToUsers(SubjectDTO user) throws RemoteException {
        try {
            pfDMSService.addToUsers(user);
        } catch (PolicyServiceException e) {
            LOG.error("Failed to set user data", e);
            throw new RemoteException("Error setting user data");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.bluejungle.destiny.services.management.UserRoleServiceIF#deleteFromUsers(com.bluejungle.destiny.services.policy.types.SubjectDTO)
     */
    public void deleteFromUsers(SubjectDTO user) throws RemoteException {
        // TODO: Need to do some handling for orphaned records etc.
        // pfDMSService.deleteFromUsers(user);
        try {
            IApplicationUserManager appUserManager = getApplicationUserManager();
            if (user != null) {
                // Lookup the user from the external domain - we assume
                // we're always deleting from the default local domain here.
                // Change after 1.0 to delete from the domain that this user
                // actually belongs to.
                if (user.getId() == null) {
                    throw new RemoteException("No id specified for user");
                }

                appUserManager.deleteApplicationUser(user.getId().longValue());
            }
        } catch (UserNotFoundException e) {
            LOG.error("Failed to delete user.  Could not find specified user", e);
            throw new RemoteException("User with login: '" + user.getUid() + "' could not be found.");
        } catch (ApplicationUserDeleteFailedException e) {
            LOG.error("Failed to delete user.", e);
            throw new RemoteException("User with login: '" + user.getUid() + "' could not be deleted.");
        }
    }

    /**
     * @see com.bluejungle.destiny.services.management.UserRoleServiceIF#createUser(com.bluejungle.destiny.services.management.types.NewUser)
     */
    public UserDTO createUser(UserDTO newUser, DMSUserData userData) throws RemoteException, DuplicateLoginNameException {
        if (newUser == null) {
            throw new NullPointerException("newUser");
        }

        UserDTO createdUser = null;
        try {
            IApplicationUserManager appUserManager = getApplicationUserManager();
            IApplicationUser newAppUser = appUserManager.createUser(newUser.getUid(), newUser.getFirstName(), newUser.getLastName(), newUser.getPassword());
            createdUser = UserManagementServiceHelper.convertUserToDTO(newAppUser);
            setUserData(createdUser, userData);
            LOG.info("Created user " + newUser.getFirstName() + " " + newUser.getLastName());
        } catch (ApplicationUserCreationFailedException e) {
            throw new RemoteException("failed to create user", e);
        } catch (UserAlreadyExistsException e) {
            throw new DuplicateLoginNameException();
        } catch (UserNotFoundException exception) {
            // Shouldn't happen
            throw new RemoteException("Filed to create user", exception);
        } catch (UserManagementAccessException exception) {
            throw new RemoteException("Filed to create user", exception);
        }

        return createdUser;
    }

    /**
     * @see com.bluejungle.destiny.services.management.UserRoleServiceIF#getUserManagementMetadata()
     */
    public UserManagementMetadata getUserManagementMetadata() throws RemoteException {
        IApplicationUserManager userManager = getApplicationUserManager();

        AuthenticationModeEnumDTO authModeDTO = null;
        AuthenticationModeEnumType authMode = userManager.getAuthenticationMode();
        if (authMode == AuthenticationModeEnumType.REMOTE) {
            authModeDTO = AuthenticationModeEnumDTO.REMOTE;
        } else if (authMode == AuthenticationModeEnumType.HYBRID) {
            authModeDTO = AuthenticationModeEnumDTO.HYBRID;
        } else if (authMode == AuthenticationModeEnumType.LOCAL) {
            authModeDTO = AuthenticationModeEnumDTO.LOCAL;
        }

        return new UserManagementMetadata(authModeDTO);
    }

    /**
     * Retrieve the Application user manager
     *
     * @return the Application User Manager
     * @throws ServiceNotReadyFault
     */
    private IApplicationUserManager getApplicationUserManager() throws ServiceNotReadyFault {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        if (this.appUserManager == null) {
            if (!compMgr.isComponentRegistered(IApplicationUserManagerFactory.COMP_NAME)) {
                throw new ServiceNotReadyFault();
            }

            IApplicationUserManagerFactory appUserManagerFactory = (IApplicationUserManagerFactory) compMgr.getComponent(ApplicationUserManagerFactoryImpl.class);
            this.appUserManager = appUserManagerFactory.getSingleton();
        }
        return this.appUserManager;
    }

    /**
     *
     * @author safdar
     */
    private class UserSearchSpecImpl implements IUserSearchSpec {

        private String lastNameStartsWith;

        // TODO: Also need to fix the hack where the name() is ignored and the
        // value() is treated as the last-name-start-string.
        public UserSearchSpecImpl(UserQueryTerm term) {
            this.lastNameStartsWith = (String) term.getValue();
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.core.IUserSearchSpec#getLastNameStartsWith()
         */
        public String getLastNameStartsWith() {
            return this.lastNameStartsWith;
        }
    }

    private static class ApplicationUser implements IApplicationUser {

        private UserDTO user;
        private IAccessGroup primaryAccessGroupCache;

        ApplicationUser(UserDTO user) {
            this.user = user;
        }

        public String getDefaultRole() {
            return null;
        }

        public Long getDestinyId() {
            return null;
        }

        public boolean isManuallyCreated() {
            return true;
        }

        public String getDomainName() {
            String uniqueName = getUniqueName();
            int atIndex = uniqueName.indexOf(IApplicationUser.LOGIN_AT_DOMAIN_SEPARATOR);
            String domainName = uniqueName.substring(atIndex + 1);
            return domainName;
        }

        public String getLogin() {
            return user.getUid();
        }

        public String getFirstName() {
            return user.getFirstName();
        }

        public String getLastName() {
            return user.getLastName();
        }

        public String getUniqueName() {
            return user.getUniqueName();
        }

        public String getDisplayName() {
            return user.getName();
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser#getPrimaryAccessGroup()
         */
        public IAccessGroup getPrimaryAccessGroup() throws IllegalStateException, UserManagementAccessException {
            if (this.primaryAccessGroupCache == null) {
                BigInteger primaryAccessGroupId = this.user.getPrimaryUserGroupId();
                if (primaryAccessGroupId == null) {
                    throw new IllegalStateException("Primary access group not set");
                }

                IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
                IApplicationUserManagerFactory applicationUserManagerFactory = (IApplicationUserManagerFactory) componentManager.getComponent(ApplicationUserManagerFactoryImpl.class);
                IApplicationUserManager userManager = applicationUserManagerFactory.getSingleton();

                try {
                    this.primaryAccessGroupCache = userManager.getAccessGroup(primaryAccessGroupId.longValue());
                } catch (GroupNotFoundException exception) {
                    /*
                     * This isn't ideal. Should do this look up in the
                     * constructor and throw exceptions from the constructor.
                     * Does not make sense to add this exception to the method
                     * signature
                     */
                    throw new IllegalStateException("Primary access group with id, " + primaryAccessGroupId + ", could not be found.");
                }
            }

            return this.primaryAccessGroupCache;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser#hasPrimaryAccessGroup()
         */
        public boolean hasPrimaryAccessGroup() {
            return (this.user.getPrimaryUserGroupId() != null);
        }
    }
}
