/**
 * DMSUserGroupServiceImpl.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis 1.2 May 03, 2005
 * (02:20:24 EDT) WSDL2Java emitter.
 */

package com.bluejungle.destiny.container.dms;

import com.bluejungle.destiny.container.shared.applicationusers.core.AccessGroupCreationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.AccessGroupDeletionFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.AccessGroupModificationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserManagerFactoryImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerFactory;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserManagementAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroupLinkData;
import com.bluejungle.destiny.container.shared.applicationusers.external.IGroupSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IInternalAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ILinkedAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.service.UserManagementServiceHelper;
import com.bluejungle.destiny.framework.types.RelationalOpDTO;
import com.bluejungle.destiny.framework.types.CommitFault;
import com.bluejungle.destiny.framework.types.IDList;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.destiny.framework.types.UniqueConstraintViolationFault;
import com.bluejungle.destiny.framework.types.UnknownEntryFault;
import com.bluejungle.destiny.services.management.types.ExternalUserGroup;
import com.bluejungle.destiny.services.management.types.ExternalUserGroupList;
import com.bluejungle.destiny.services.management.types.ExternalUserGroupQueryResults;
import com.bluejungle.destiny.services.management.types.UserDTO;
import com.bluejungle.destiny.services.management.types.UserDTOList;
import com.bluejungle.destiny.services.management.types.UserGroupDTO;
import com.bluejungle.destiny.services.management.types.UserGroupInfo;
import com.bluejungle.destiny.services.management.types.UserGroupQueryField;
import com.bluejungle.destiny.services.management.types.UserGroupQueryResults;
import com.bluejungle.destiny.services.management.types.UserGroupQuerySpec;
import com.bluejungle.destiny.services.management.types.UserGroupQueryTerm;
import com.bluejungle.destiny.services.management.types.UserGroupQueryTermSet;
import com.bluejungle.destiny.services.management.types.UserGroupReduced;
import com.bluejungle.destiny.services.management.types.UserGroupReducedList;
import com.bluejungle.destiny.services.management.types.UserGroupServiceIF;
import com.bluejungle.destiny.services.policy.types.Access;
import com.bluejungle.destiny.services.policy.types.AccessList;
import com.bluejungle.destiny.services.policy.types.DefaultAccessAssignment;
import com.bluejungle.destiny.services.policy.types.DefaultAccessAssignmentList;
import com.bluejungle.destiny.services.policy.types.Principal;
import com.bluejungle.destiny.services.policy.types.PrincipalType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.pf.destiny.lib.AccessPolicyComponent;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.action.ActionManager;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.common.GroupAccess;
import com.bluejungle.pf.domain.destiny.common.UserAccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class DMSUserGroupServiceImpl implements UserGroupServiceIF {

    private static final Log LOG = LogFactory.getLog(DMSUserGroupServiceImpl.class.getName());

    private static final Map ACTION_TO_ACCESS_MAP = new HashMap();
    private static final Map ACCESS_TO_ACTION_MAP = new HashMap();
    private static final IDAction NO_OP_ACTION;
    static {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        ActionManager actionManager = (ActionManager) componentManager.getComponent(ActionManager.class);
        IDAction readAction = actionManager.getAction(IDAction.READ_NAME);
        IDAction writeAction = actionManager.getAction(IDAction.WRITE_NAME);
        IDAction deleteAction = actionManager.getAction(IDAction.DELETE_NAME);
        IDAction deployAction = actionManager.getAction(IDAction.DEPLOY_NAME);
        IDAction approveAction = actionManager.getAction(IDAction.APPROVE_NAME);
        IDAction adminAction = actionManager.getAction(IDAction.ADMIN_NAME);

        ACTION_TO_ACCESS_MAP.put(readAction, Access.READ);
        ACTION_TO_ACCESS_MAP.put(writeAction, Access.WRITE);
        ACTION_TO_ACCESS_MAP.put(deleteAction, Access.DELETE);
        ACTION_TO_ACCESS_MAP.put(deployAction, Access.DEPLOY);
        ACTION_TO_ACCESS_MAP.put(approveAction, Access.APPROVE);
        ACTION_TO_ACCESS_MAP.put(adminAction, Access.ADMIN);

        ACCESS_TO_ACTION_MAP.put(Access.READ, readAction);
        ACCESS_TO_ACTION_MAP.put(Access.WRITE, writeAction);
        ACCESS_TO_ACTION_MAP.put(Access.DELETE, deleteAction);
        ACCESS_TO_ACTION_MAP.put(Access.DEPLOY, deployAction);
        ACCESS_TO_ACTION_MAP.put(Access.APPROVE, approveAction);
        ACCESS_TO_ACTION_MAP.put(Access.ADMIN, adminAction);

        NO_OP_ACTION = actionManager.getAction(IDAction.NOP_NAME);
    }

    private IApplicationUserManager applicationUserManager;

    public UserGroupReducedList getAllUserGroups() throws RemoteException, ServiceNotReadyFault, CommitFault, UnauthorizedCallerFault {
        UserGroupReducedList listToReturn;

        IApplicationUserManager applicationUserManager = getApplicationUserManager();
        try {
            Collection userGroups = applicationUserManager.getAccessGroups(null, 0);
            listToReturn = buildUserGroupReducedList(userGroups);
        } catch (UserManagementAccessException exception) {
            getLog().error("Failed to retrieve user groups", exception);
            throw new CommitFault();
        }

        return listToReturn;
    }

    public UserGroupReducedList getUserGroupsForUser(UserDTO user) throws RemoteException, ServiceNotReadyFault, CommitFault, UnauthorizedCallerFault {
        UserGroupReducedList listToReturn;

        IApplicationUserManager applicationUserManager = getApplicationUserManager();
        try {
            Collection userGroups = applicationUserManager.getAccessGroupsContainingUser(user.getId().longValue());
            listToReturn = buildUserGroupReducedList(userGroups);
        } catch (UserManagementAccessException exception) {
            getLog().error("Failed to retrieve user groups for user", exception);
            throw new CommitFault();
        } catch (UserNotFoundException exception) {
            getLog().error("Failed to retrieve user groups for user", exception);
            throw new CommitFault();
        }

        return listToReturn;
    }

    public UserGroupDTO getUserGroup(BigInteger userGroupId) throws RemoteException, ServiceNotReadyFault, UnknownEntryFault, CommitFault, UnauthorizedCallerFault {
        UserGroupDTO groupToReturn = null;

        try {
            IAccessGroup accessGroup = getAccessGroup(userGroupId.longValue());
            groupToReturn = buildUserGroupDTO(accessGroup);
        } catch (GroupNotFoundException exception) {
            getLog().error("Failed to retrieve user group with ID, " + userGroupId + ".  A group with this id was not found", exception);
            throw new UnknownEntryFault();
        } catch (UserManagementAccessException exception) {
            getLog().error("Failed to retrieve user group with ID, " + userGroupId, exception);
            throw new CommitFault();
        }

        return groupToReturn;
    }

    /**
     * @see UserGroupServiceIF#getUsersInUserGroup(BigInteger)
     */
    public UserDTOList getUsersInUserGroup(BigInteger userGroupId) throws RemoteException, ServiceNotReadyFault, UnknownEntryFault, CommitFault, UnauthorizedCallerFault {
        UserDTOList listToReturn;
        UserDTO[] usersToReturnInList;

        IApplicationUserManager applicationUserManager = getApplicationUserManager();
        try {
            Collection users = applicationUserManager.getUsersInAccessGroup(userGroupId.longValue());
            usersToReturnInList = new UserDTO[users.size()];
            Iterator usersIterator = users.iterator();
            for (int i = 0; usersIterator.hasNext(); i++) {
                IApplicationUser nextUser = (IApplicationUser) usersIterator.next();
                usersToReturnInList[i] = UserManagementServiceHelper.convertUserToDTO(nextUser);
            }

            listToReturn = new UserDTOList(usersToReturnInList);
        } catch (GroupNotFoundException exception) {
            getLog().error("Failed to retrieve user group with ID, " + userGroupId + ".  A group with this id was not found", exception);
            throw new UnknownEntryFault();
        } catch (UserManagementAccessException exception) {
            getLog().error("Failed to retrieve user groups", exception);
            throw new CommitFault();
        } catch (UserNotFoundException exception) {
            // Should never happen
            getLog().error("Failed to create User DTO instance", exception);
            throw new CommitFault();
        }

        return listToReturn;
    }

    public void addUsersToUserGroup(BigInteger userGroupId, IDList userIds) throws RemoteException, ServiceNotReadyFault, UnknownEntryFault, CommitFault, UnauthorizedCallerFault {
        BigInteger[] userIdsArray = userIds.getIDList();
        if (userIdsArray != null) {
            long[] usersIdsLongArray = toLongArray(userIdsArray);

            IApplicationUserManager applicationUserManager = getApplicationUserManager();

            try {
                applicationUserManager.addUsersToAccessGroup(userGroupId.longValue(), usersIdsLongArray);
            } catch (AccessGroupModificationFailedException exception) {
                getLog().error("Failed to add users to group with id, " + userGroupId, exception);
                throw new CommitFault();
            } catch (GroupNotFoundException exception) {
                getLog().error("Failed to add users to group with id, " + userGroupId + ".  A group with this id was not found", exception);
                throw new CommitFault();
            } catch (UserNotFoundException exception) {
                getLog().error("Failed to add users to group with id, " + userGroupId + ".  A user selected could not be found in the persistence store", exception);
                throw new CommitFault();
            }
        }
    }

    public void removeUsersFromUserGroup(BigInteger userGroupId, IDList userIds) throws RemoteException, ServiceNotReadyFault, UnknownEntryFault, CommitFault, UnauthorizedCallerFault {
        BigInteger[] userIdsArray = userIds.getIDList();
        if (userIdsArray != null) {
            long[] usersIdsLongArray = toLongArray(userIdsArray);

            IApplicationUserManager applicationUserManager = getApplicationUserManager();

            try {
                applicationUserManager.removeUsersFromAccessGroup(userGroupId.longValue(), usersIdsLongArray);
            } catch (AccessGroupModificationFailedException exception) {
                getLog().error("Failed to remove users from group with id, " + userGroupId, exception);
                throw new CommitFault();
            } catch (GroupNotFoundException exception) {
                getLog().error("Failed to remove users from group with id, " + userGroupId + ".  A group with this id was not found", exception);
                throw new CommitFault();
            } catch (UserNotFoundException exception) {
                getLog().error("Failed to remove users to group with id, " + userGroupId + ".  A user selected could not be found in the persistence store", exception);
                throw new CommitFault();
            }
        }
    }

    public DefaultAccessAssignmentList getDefaultAccessAssignments(BigInteger userGroupId) throws RemoteException, ServiceNotReadyFault, UnknownEntryFault, CommitFault, UnauthorizedCallerFault {
        DefaultAccessAssignmentList accessAssignmentListToReturn = new DefaultAccessAssignmentList();

        SortedSet sortedDefaultAccessAssignmentList = new TreeSet(new DefaultAccessAssignmentComparator());

        try {
            IAccessGroup userGroup = getAccessGroup(userGroupId.longValue());
            String accessControlPQL = userGroup.getApplicableAccessControl();
            AccessPolicyComponent accessPolicyComponent = new AccessPolicyComponent(accessControlPQL);
            Collection accessList = accessPolicyComponent.getAllUserGroupActions();
            Iterator accessListIterator = accessList.iterator();
            while (accessListIterator.hasNext()) {
                Object nextAccessPQLComponent = (Object) accessListIterator.next();
                DefaultAccessAssignment defaultAccessAssignement = buildDefaultAccessAssignment(nextAccessPQLComponent);
                sortedDefaultAccessAssignmentList.add(defaultAccessAssignement);
            }

            if (!sortedDefaultAccessAssignmentList.isEmpty()) {
                DefaultAccessAssignment[] defaultAccessAssignments = new DefaultAccessAssignment[sortedDefaultAccessAssignmentList.size()];
                Iterator sortedDefaultAccessAssignmentsIterator = sortedDefaultAccessAssignmentList.iterator();
                for (int i = 0; sortedDefaultAccessAssignmentsIterator.hasNext(); i++) {
                    DefaultAccessAssignment nextAccessAssignment = (DefaultAccessAssignment) sortedDefaultAccessAssignmentsIterator.next();
                    defaultAccessAssignments[i] = nextAccessAssignment;
                }

                accessAssignmentListToReturn.setDefaultAccessAssignment(defaultAccessAssignments);
            }
        } catch (GroupNotFoundException exception) {
            getLog().error("Failed to retrieve user group with ID, " + userGroupId + ".  A group with this id was not found", exception);
            throw new UnknownEntryFault();
        } catch (UserManagementAccessException exception) {
            getLog().error("Failed to retrieve user group with ID, " + userGroupId, exception);
            throw new CommitFault();
        } catch (UserNotFoundException exception) {
            getLog().error("Failed to retrieve user principal associated with default object access list for group is ID, " + userGroupId, exception);
            throw new CommitFault();
        }

        return accessAssignmentListToReturn;
    }

    public void setDefaultAccessAssignments(BigInteger userGroupId, DefaultAccessAssignmentList defaultAccessAssignments) throws RemoteException, ServiceNotReadyFault, UnknownEntryFault, CommitFault, UnauthorizedCallerFault {
        AccessPolicyComponent accessPolicyComponent = new AccessPolicyComponent();
        DefaultAccessAssignment[] defaultAccessAssignmentsToSet = defaultAccessAssignments.getDefaultAccessAssignment();

        try {
            String pqlToSet = null;
            if (defaultAccessAssignmentsToSet != null) {
                for (int i = 0; i < defaultAccessAssignmentsToSet.length; i++) {
                    DefaultAccessAssignment nextAssignment = defaultAccessAssignmentsToSet[i];
                    AccessList accessInAssignmentList = nextAssignment.getDefaultAccess();
                    Access[] accessInAssignment = accessInAssignmentList.getAccess();
                    Collection actionsToSet = new HashSet();
                    if ((accessInAssignment != null) && (accessInAssignment.length > 0)) {

                        for (int j = 0; j < accessInAssignment.length; j++) {
                            Access nextAccess = accessInAssignment[j];
                            if (!ACCESS_TO_ACTION_MAP.containsKey(nextAccess)) {
                                throw new IllegalArgumentException("Unknown access: " + nextAccess);
                            }

                            IDAction nextAction = (IDAction) ACCESS_TO_ACTION_MAP.get(nextAccess);
                            actionsToSet.add(nextAction);
                        }
                    } else {
                        actionsToSet.add(NO_OP_ACTION);
                    }

                    Principal nextPrincipal = nextAssignment.getPrinciapl();
                    Long nextPrincpalId = new Long(nextPrincipal.getID().longValue());
                    PrincipalType nextPrincipalType = nextPrincipal.getType();
                    if (nextPrincipalType.equals(PrincipalType.USER)) {
                        accessPolicyComponent.setActionsForUser(nextPrincpalId, actionsToSet);
                    } else if (nextPrincipalType.equals(PrincipalType.USER_GROUP)) {
                        accessPolicyComponent.setActionsForGroup(nextPrincpalId, actionsToSet);
                    } else {
                        throw new IllegalArgumentException("Uknown PrincipalType: " + nextPrincipalType);
                    }
                }
            } 
            
            pqlToSet = accessPolicyComponent.toPQL();

            getApplicationUserManager().setDefaultAccessControlAssignmentForGroup(userGroupId.longValue(), pqlToSet);
        } catch (AccessGroupModificationFailedException exception) {
            getLog().error("Failed to set default object access assignments for group with id, " + userGroupId, exception);
            throw new CommitFault();
        } catch (GroupNotFoundException exception) {
            getLog().error("Failed to set deault object access assignments.  Could not retrieve target group with with ID, " + userGroupId, exception);
            throw new CommitFault();
        } catch (PQLException exception) {
            getLog().error("Failed to set deault object access assignments for group with ID, " + userGroupId + ".  Could not build access PQL.", exception);
            throw new CommitFault();
        }
    }

    public void linkExternalGroups(ExternalUserGroupList externalGroupList) throws RemoteException, ServiceNotReadyFault, CommitFault, UnauthorizedCallerFault {
        ExternalUserGroup[] externalUserGroups = externalGroupList.getExternalUserGroup();
        if (externalUserGroups != null) {
            IExternalGroupLinkData[] externalGroupLinkData = new IExternalGroupLinkData[externalUserGroups.length];
            for (int i = 0; i < externalUserGroups.length; i++) {
                ExternalUserGroup nextExternalUserGroup = externalUserGroups[i];
                externalGroupLinkData[i] = new ExternalGroupLinkDataImpl(nextExternalUserGroup);
            }

            try {
                ILinkedAccessGroup[] createdGroups = applicationUserManager.linkExternalAccessGroups(externalGroupLinkData);
                
                for (int i = 0; i < createdGroups.length; i++) {
                    try {
                        createIntialDefaultAccessAssignment(createdGroups[i]);
                    } catch (PQLException exception) {
                        getLog().warn("Failed to set initial Default Access Assignments for group with ID, " + createdGroups[i].getDestinyId(), exception);
                    } catch (GroupNotFoundException exception) {
                        getLog().warn("Failed to set initial Default Access Assignments for group with ID, " + createdGroups[i].getDestinyId(), exception);           
                    } catch (AccessGroupModificationFailedException exception) {
                        getLog().warn("Failed to set initial Default Access Assignments for group with ID, " + createdGroups[i].getDestinyId(), exception);
                    } catch (ServiceNotReadyFault exception) {
                        getLog().warn("Failed to set initial Default Access Assignments for group with ID, " + createdGroups[i].getDestinyId(), exception);
                    }
                }
            } catch (AccessGroupCreationFailedException exception) {
                getLog().error("Failed to link external user groups.");
                throw new CommitFault();
            }
        }
    }

    public void updateGroup(UserGroupDTO userGroupToUpdate) throws RemoteException, ServiceNotReadyFault, CommitFault, UnauthorizedCallerFault {
        IApplicationUserManager applicationUserManager = getApplicationUserManager();

        try {
            applicationUserManager.updateGroup(userGroupToUpdate.getId().longValue(), userGroupToUpdate.getTitle(), userGroupToUpdate.getDescription());
        } catch (GroupAlreadyExistsException exception) {
            getLog().error("Failed to update user group with ID, " + userGroupToUpdate.getId(), exception);
            throw new UniqueConstraintViolationFault(new String[] { "title" });
        } catch (GroupNotFoundException exception) {
            getLog().error("Failed to update user group with ID, " + userGroupToUpdate.getId() + ".  A group with this ID cannot be found", exception);
            throw new CommitFault();
        } catch (AccessGroupModificationFailedException exception) {
            getLog().error("Failed to update user group with ID, " + userGroupToUpdate.getId(), exception);
            throw new CommitFault();
        }
    }

    public void deleteGroup(BigInteger userGroupId) throws RemoteException, ServiceNotReadyFault, UnknownEntryFault, CommitFault, UnauthorizedCallerFault {
        IApplicationUserManager applicationUserManager = getApplicationUserManager();

        try {
            applicationUserManager.deleteGroup(userGroupId.longValue());
        } catch (GroupNotFoundException exception) {
            // Ignore
        } catch (AccessGroupDeletionFailedException exception) {
            getLog().error("Failed to delete user group with title, " + userGroupId, exception);
            throw new CommitFault();
        }
    }

    public UserGroupQueryResults runUserGroupQuery(UserGroupQuerySpec userGroupQuerySpec) throws RemoteException, ServiceNotReadyFault, CommitFault, UnauthorizedCallerFault {
        UserGroupQueryResults resultsToReturn = new UserGroupQueryResults();
        UserGroupQueryTermSet userGroupQueryTermSet = userGroupQuerySpec.getUserGroupQueryTermSet();
        UserGroupQueryTerm[] userGroupQueryTerms = userGroupQueryTermSet.getUserGroupQueryTerm();
        if (userGroupQueryTerms != null) {
            IGroupSearchSpec[] searchSpec = new IGroupSearchSpec[userGroupQueryTerms.length];
            for (int i = 0; i < userGroupQueryTerms.length; i++) {
                final UserGroupQueryTerm nextTerm = userGroupQueryTerms[i];
                if (nextTerm.getQueryField() != UserGroupQueryField.TITLE) {
                    throw new IllegalStateException("Unknown query field, " + nextTerm.getQueryField() + ".  Only title searches are currently supported");
                }

                if (nextTerm.getQueryOperator() != RelationalOpDTO.starts_with) {
                    throw new IllegalStateException("Unknown query operator, " + nextTerm.getQueryOperator() + ".  Only starts with operator currently supported");
                }

                searchSpec[i] = new IGroupSearchSpec() {

                    public String getTitleStartsWith() {
                        return (String) nextTerm.getQueryValue();
                    }
                };
            }

            IApplicationUserManager applicationUserManager = getApplicationUserManager();
            try {

                SortedSet matchingUserGroups = applicationUserManager.getAccessGroups(searchSpec, userGroupQuerySpec.getMaxResults().intValue());
                UserGroupReducedList matchingUserGroupList = buildUserGroupReducedList(matchingUserGroups);
                resultsToReturn.setMatchingUserGroups(matchingUserGroupList);
            } catch (UserManagementAccessException exception) {
                getLog().error("Failed to run external user group query.", exception);
                throw new CommitFault();
            }
        } else {
            resultsToReturn.setMatchingUserGroups(new UserGroupReducedList());
        }

        return resultsToReturn;
    }

    public ExternalUserGroupQueryResults runExternalUserGroupQuery(UserGroupQuerySpec userGroupQueryTermSpec) throws RemoteException, ServiceNotReadyFault, CommitFault, UnauthorizedCallerFault {
        ExternalUserGroupQueryResults resultsToReturn = new ExternalUserGroupQueryResults();

        UserGroupQueryTermSet userGroupQueryTermSet = userGroupQueryTermSpec.getUserGroupQueryTermSet();
        UserGroupQueryTerm[] userGroupQueryTerms = userGroupQueryTermSet.getUserGroupQueryTerm();
        if (userGroupQueryTerms != null) {
            IGroupSearchSpec[] searchSpec = new IGroupSearchSpec[userGroupQueryTerms.length];
            for (int i = 0; i < userGroupQueryTerms.length; i++) {
                final UserGroupQueryTerm nextTerm = userGroupQueryTerms[i];
                if (nextTerm.getQueryField() != UserGroupQueryField.TITLE) {
                    throw new IllegalStateException("Unknown query field, " + nextTerm.getQueryField() + ".  Only title searches are currently supported");
                }

                if (nextTerm.getQueryOperator() != RelationalOpDTO.starts_with) {
                    throw new IllegalStateException("Unknown query operator, " + nextTerm.getQueryOperator() + ".  Only starts with operator currently supported");
                }

                searchSpec[i] = new IGroupSearchSpec() {

                    public String getTitleStartsWith() {
                        return (String) nextTerm.getQueryValue();
                    }
                };
            }

            IApplicationUserManager applicationUserManager = getApplicationUserManager();
            try {

                SortedSet matchingUserGroups = applicationUserManager.getExternalGroups(searchSpec, userGroupQueryTermSpec.getMaxResults().intValue());
                ExternalUserGroupList matchingUserGroupList = buildExternalUserGroupList(matchingUserGroups);
                resultsToReturn.setMatchingExternalUserGroups(matchingUserGroupList);
            } catch (UserManagementAccessException exception) {
                getLog().error("Failed to run external user group query.", exception);
                throw new CommitFault();
            }
        } else {
            resultsToReturn.setMatchingExternalUserGroups(new ExternalUserGroupList());
        }

        return resultsToReturn;
    }

    public UserGroupDTO createUserGroup(UserGroupInfo userGroupInfo) throws RemoteException, ServiceNotReadyFault, CommitFault, UnauthorizedCallerFault {
        UserGroupDTO groupToReturn = null;

        IApplicationUserManager applicationUserManager = getApplicationUserManager();

        try {
            IInternalAccessGroup createdGroup = applicationUserManager.createAccessGroup(userGroupInfo.getTitle().toString(), userGroupInfo.getDescription());
            
            try {
                createIntialDefaultAccessAssignment(createdGroup);
            } catch (PQLException exception) {
                getLog().warn("Failed to set initial Default Access Assignments for group with ID, " + createdGroup.getDestinyId(), exception);
            } catch (GroupNotFoundException exception) {
                getLog().warn("Failed to set initial Default Access Assignments for group with ID, " + createdGroup.getDestinyId(), exception);           
            } catch (AccessGroupModificationFailedException exception) {
                getLog().warn("Failed to set initial Default Access Assignments for group with ID, " + createdGroup.getDestinyId(), exception);
            } catch (ServiceNotReadyFault exception) {
                getLog().warn("Failed to set initial Default Access Assignments for group with ID, " + createdGroup.getDestinyId(), exception);
            }
            
            groupToReturn = buildUserGroupDTO(createdGroup);
        } catch (GroupAlreadyExistsException exception) {
            getLog().error("Failed to create user group with title, " + userGroupInfo.getTitle(), exception);
            throw new UniqueConstraintViolationFault(new String[] { "title" });
        } catch (AccessGroupCreationFailedException exception) {
            getLog().error("Failed to create user group with title, " + userGroupInfo.getTitle(), exception);
            throw new CommitFault();
        }


        return groupToReturn;
    }

    /**
     * Set the initial default access assignments for the specified group
     * 
     * @param userGroup
     * @throws PQLException
     * @throws ServiceNotReadyFault
     * @throws AccessGroupModificationFailedException
     * @throws GroupNotFoundException
     */
    private void createIntialDefaultAccessAssignment(IAccessGroup userGroup) throws PQLException, ServiceNotReadyFault, AccessGroupModificationFailedException, GroupNotFoundException {
        if (userGroup == null) {
            throw new NullPointerException("userGroup cannot be null.");
        }
        
        Long userGroupId = userGroup.getDestinyId();
        AccessPolicyComponent accessPolicyComponent = new AccessPolicyComponent();       
        accessPolicyComponent.setActionsForGroup(userGroupId, Collections.singleton(NO_OP_ACTION));
        String pqlToSet = accessPolicyComponent.toPQL();
        getApplicationUserManager().setDefaultAccessControlAssignmentForGroup(userGroupId.longValue(), pqlToSet);
    }
    
    /**
     * @param nextAccessPQLComponent
     * @return
     * @throws UserManagementAccessException
     * @throws UserNotFoundException
     * @throws ServiceNotReadyFault
     * @throws NumberFormatException
     * @throws GroupNotFoundException
     */
    private DefaultAccessAssignment buildDefaultAccessAssignment(Object nextAccessPQLComponent) throws UserManagementAccessException, UserNotFoundException, ServiceNotReadyFault, NumberFormatException, GroupNotFoundException {
        DefaultAccessAssignment defaultAccessAssignement = null;
        if (nextAccessPQLComponent instanceof UserAccess) {
            defaultAccessAssignement = buildDefaultAccessAssignment((UserAccess) nextAccessPQLComponent);
        } else if (nextAccessPQLComponent instanceof GroupAccess) {
            defaultAccessAssignement = buildDefaultAccessAssignment((GroupAccess) nextAccessPQLComponent);
        } else {
            throw new IllegalArgumentException("Unknown pql component: " + nextAccessPQLComponent.getClass().getName());
        }
        return defaultAccessAssignement;
    }

    /**
     * @param userAccessPQLComponent
     * @return
     * @throws UserManagementAccessException
     * @throws UserNotFoundException
     * @throws ServiceNotReadyFault
     * @throws NumberFormatException
     */
    private DefaultAccessAssignment buildDefaultAccessAssignment(UserAccess userAccessPQLComponent) throws UserManagementAccessException, UserNotFoundException, ServiceNotReadyFault, NumberFormatException {
        IApplicationUser applicationUser = getApplicationUser(userAccessPQLComponent.getUserId().longValue());
        BigInteger principalId = BigInteger.valueOf(applicationUser.getDestinyId().longValue());
        String principalDisplayName = applicationUser.getDisplayName();
        Principal accessUserPrincipal = new Principal(principalId, principalDisplayName, PrincipalType.USER);

        Collection actions = userAccessPQLComponent.getActions();
        AccessList groupAccessList = buildAccessList(actions);
        return new DefaultAccessAssignment(accessUserPrincipal, groupAccessList);
    }

    /**
     * @param groupAccessPQLComponent
     * @return
     * @throws ServiceNotReadyFault
     * @throws UserManagementAccessException
     * @throws GroupNotFoundException
     */
    private DefaultAccessAssignment buildDefaultAccessAssignment(GroupAccess groupAccessPQLComponent) throws ServiceNotReadyFault, UserManagementAccessException, GroupNotFoundException {
        IAccessGroup accessGroup = getAccessGroup(groupAccessPQLComponent.getGroupId().longValue());
        BigInteger principalId = BigInteger.valueOf(accessGroup.getDestinyId().longValue());
        String principalDisplayName = accessGroup.getTitle();
        Principal accessGroupPrincipal = new Principal(principalId, principalDisplayName, PrincipalType.USER_GROUP);

        Collection actions = groupAccessPQLComponent.getActions();
        AccessList groupAccessList = buildAccessList(actions);
        return new DefaultAccessAssignment(accessGroupPrincipal, groupAccessList);
    }

    /**
     * @param actions
     * @return
     */
    private AccessList buildAccessList(Collection actions) {
        AccessList accessListToReturn = new AccessList();

        // Cannot directly create an array because some actions may not be sent
        // (i.e. we don't know the array size)
        ArrayList accessArrayList = new ArrayList(actions.size());
        Iterator actionsIterator = actions.iterator();
        for (int i = 0; actionsIterator.hasNext(); i++) {
            IDAction nextIDAction = (IDAction) actionsIterator.next();
            if (ACTION_TO_ACCESS_MAP.containsKey(nextIDAction)) {
                Access nextAccess = (Access) ACTION_TO_ACCESS_MAP.get(nextIDAction);
                accessArrayList.add(nextAccess);
            }
        }
        Access[] accessArray = (Access[]) accessArrayList.toArray(new Access[0]);
        accessListToReturn.setAccess(accessArray);
        return accessListToReturn;
    }

    /**
     * 
     * @param userId
     * @return
     * @throws UserManagementAccessException
     * @throws UserNotFoundException
     * @throws ServiceNotReadyFault
     */
    private IApplicationUser getApplicationUser(long userId) throws UserManagementAccessException, UserNotFoundException, ServiceNotReadyFault {
        IApplicationUserManager applicationUserManager = getApplicationUserManager();
        return applicationUserManager.getApplicationUser(userId);
    }

    /**
     * @param userGroupId
     * @return
     * @throws ServiceNotReadyFault
     * @throws UserManagementAccessException
     * @throws GroupNotFoundException
     */
    private IAccessGroup getAccessGroup(long userGroupId) throws ServiceNotReadyFault, UserManagementAccessException, GroupNotFoundException {
        IApplicationUserManager applicationUserManager = getApplicationUserManager();
        return applicationUserManager.getAccessGroup(userGroupId);
    }

    /**
     * Build a UserGroupReducedList from a Collection of IAccessGroup instances
     * 
     * @param userGroups
     *            the source Collection of IAccessGroup instances
     * @return the built UserGroupReducedList
     */
    private UserGroupReducedList buildUserGroupReducedList(Collection userGroups) {
        UserGroupReduced[] userGroupsToReturnInList = new UserGroupReduced[userGroups.size()];
        Iterator userGroupsIterator = userGroups.iterator();
        for (int i = 0; userGroupsIterator.hasNext(); i++) {
            IAccessGroup nextUserGroup = (IAccessGroup) userGroupsIterator.next();
            userGroupsToReturnInList[i] = buildUserGroupReduced(nextUserGroup);
        }

        return new UserGroupReducedList(userGroupsToReturnInList);
    }

    /**
     * Build a UserGroupReduced instance from an IAccessGroup
     * 
     * @param sourceGroup
     *            the source group from which to build the UserGroupReduced
     * @return a built UserGroupReduced instance
     */
    private UserGroupReduced buildUserGroupReduced(IAccessGroup sourceGroup) {
        if (sourceGroup == null) {
            throw new NullPointerException("sourceGroup cannot be null.");
        }

        BigInteger id = BigInteger.valueOf(sourceGroup.getDestinyId().longValue());
        String title = sourceGroup.getTitle();
        String domain = sourceGroup.getDomainName();
        boolean isExternallyLinked = false;
        boolean isOrphaned = false;
        byte[] externalId = null;
        if (sourceGroup instanceof ILinkedAccessGroup) {
            isExternallyLinked = true;
            ILinkedAccessGroup linkedAccessGroup = (ILinkedAccessGroup) sourceGroup;
            externalId = linkedAccessGroup.getExternalId();
            isOrphaned = linkedAccessGroup.isOrphaned();
        }
        return new UserGroupReduced(id, title, externalId, isExternallyLinked, isOrphaned, domain);
    }

    /**
     * Build a ExternalUserGroupList from a Collection of IExternalGroup
     * instances
     * 
     * @param userGroups
     *            the source Collection of IExternalGroup instances
     * @return the built ExternalUserGroupList
     */
    private ExternalUserGroupList buildExternalUserGroupList(Collection userGroups) {
        ExternalUserGroup[] userGroupsToReturnInList = new ExternalUserGroup[userGroups.size()];
        Iterator userGroupsIterator = userGroups.iterator();
        for (int i = 0; userGroupsIterator.hasNext(); i++) {
            IExternalGroup nextUserGroup = (IExternalGroup) userGroupsIterator.next();
            userGroupsToReturnInList[i] = buildExternalUserGroup(nextUserGroup);
        }

        return new ExternalUserGroupList(userGroupsToReturnInList);
    }

    /**
     * Build a ExternalUserGroup instance from an IExternalGroup
     * 
     * @param sourceGroup
     *            the source group from which to build the ExternalUserGroup
     * @return a built ExternalUserGroup instance
     */
    private ExternalUserGroup buildExternalUserGroup(IExternalGroup sourceGroup) {
        if (sourceGroup == null) {
            throw new NullPointerException("sourceGroup cannot be null.");
        }

        String title = sourceGroup.getTitle();
        byte[] externalId = sourceGroup.getExternalId();
        String domain = sourceGroup.getDomainName();

        return new ExternalUserGroup(title, externalId, domain);
    }

    /**
     * Build a UserGroupDTO instance from an IAccessGroup
     * 
     * @param sourceGroup
     *            the source group from which to build the UserGroupDTO
     * @return a built UserGroupDTO instance
     */
    private UserGroupDTO buildUserGroupDTO(IAccessGroup sourceGroup) {
        if (sourceGroup == null) {
            throw new NullPointerException("sourceGroup cannot be null.");
        }

        BigInteger id = BigInteger.valueOf(sourceGroup.getDestinyId().longValue());
        String title = sourceGroup.getTitle();
        String description = sourceGroup.getDescription();
        boolean isExternallyLinked = false;
        byte[] externalId = null;
        String qualifiedExternalName = null;
        if (sourceGroup instanceof ILinkedAccessGroup) {
            isExternallyLinked = true;
            ILinkedAccessGroup linkedSourceGroup = (ILinkedAccessGroup) sourceGroup;
            externalId = linkedSourceGroup.getExternalId();
            qualifiedExternalName = linkedSourceGroup.getQualifiedExternalName();
        }

        return new UserGroupDTO(id, title, description, externalId, isExternallyLinked, qualifiedExternalName);
    }

    /**
     * Utility method to translate a BigInteger array to a long array
     * 
     * @param userIdsArray
     *            the array to translate
     * @return the translated array
     */
    private long[] toLongArray(BigInteger[] userIdsArray) {
        if (userIdsArray == null) {
            throw new NullPointerException("userIdsArray cannot be null.");
        }

        long[] usersIdsLongArray = new long[userIdsArray.length];
        for (int i = 0; i < userIdsArray.length; i++) {
            usersIdsLongArray[i] = userIdsArray[i].longValue();
        }
        return usersIdsLongArray;
    }

    /**
     * Retrieve the Application user manager
     * 
     * @return the Application User Manager
     * @throws ServiceNotReadyFault
     */
    private IApplicationUserManager getApplicationUserManager() throws ServiceNotReadyFault {
        if (this.applicationUserManager == null) {
            IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
            if (!componentManager.isComponentRegistered(IApplicationUserManagerFactory.COMP_NAME)) {
                throw new ServiceNotReadyFault();
            }

            IApplicationUserManagerFactory appUserManagerFactory = (IApplicationUserManagerFactory) componentManager.getComponent(ApplicationUserManagerFactoryImpl.class);
            this.applicationUserManager = appUserManagerFactory.getSingleton();
        }

        return this.applicationUserManager;
    }

    /**
     * Retrieve a reference to a Log
     * 
     * @return a reference to a Log
     */
    private Log getLog() {
        return LOG;
    }

    private class ExternalGroupLinkDataImpl implements IExternalGroupLinkData {

        private final ExternalUserGroup group;

        private ExternalGroupLinkDataImpl(ExternalUserGroup group) {
            super();
            this.group = group;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroupLinkData#getDomainName()
         */
        public String getDomainName() {
            return this.group.getDomain();
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroupLinkData#getExternalId()
         */
        public byte[] getExternalId() {
            return this.group.getExternalId();
        }

        /**
         * @see com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroupLinkData#getTitle()
         */
        public String getTitle() {
            return this.group.getTitle();
        }
    }

    /**
     * @author sgoldstein
     */
    private static class DefaultAccessAssignmentComparator implements Comparator {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object objectOne, Object objectTwo) {
            int valueToReturn = -1;

            // For now, just compare titles. Maybe add principal type in the
            // future
            DefaultAccessAssignment defaultAccessAssignmentOne = (DefaultAccessAssignment) objectOne;
            DefaultAccessAssignment defaultAccessAssignmentTwo = (DefaultAccessAssignment) objectTwo;

            String displayNameOne = defaultAccessAssignmentOne.getPrinciapl().getDisplayName();
            String displayNameTwo = defaultAccessAssignmentTwo.getPrinciapl().getDisplayName();

            if (displayNameOne.equals(displayNameTwo)) {
                // Be consistent with equals. If the display name is the same,
                // compare the id's
                BigInteger principalOneId = defaultAccessAssignmentOne.getPrinciapl().getID();
                BigInteger principalTwoId = defaultAccessAssignmentTwo.getPrinciapl().getID();
                valueToReturn = principalOneId.compareTo(principalTwoId);
            } else {
                valueToReturn = displayNameOne.compareTo(displayNameTwo);
            }

            return valueToReturn;
        }
    }
}
