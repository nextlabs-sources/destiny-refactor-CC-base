package com.bluejungle.destiny.services.policy;

/*
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 * 
 * @author sergey
 */

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.Calendar;

import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.services.policy.types.Access;
import com.bluejungle.destiny.services.policy.types.AccessList;
import com.bluejungle.destiny.services.policy.types.AgentStatusDescriptorDTO;
import com.bluejungle.destiny.services.policy.types.AgentStatusList;
import com.bluejungle.destiny.services.policy.types.AttributeDescriptorList;
import com.bluejungle.destiny.services.policy.types.CircularReferenceFault;
import com.bluejungle.destiny.services.policy.types.ComponentList;
import com.bluejungle.destiny.services.policy.types.DeploymentHistoryList;
import com.bluejungle.destiny.services.policy.types.DeploymentRecordDTO;
import com.bluejungle.destiny.services.policy.types.DeploymentRecordList;
import com.bluejungle.destiny.services.policy.types.DomainObjectEnum;
import com.bluejungle.destiny.services.policy.types.DomainObjectEnumList;
import com.bluejungle.destiny.services.policy.types.DomainObjectStateEnum;
import com.bluejungle.destiny.services.policy.types.DomainObjectUsageListDTO;
import com.bluejungle.destiny.services.policy.types.EntityDescriptorDTO;
import com.bluejungle.destiny.services.policy.types.EntityDescriptorList;
import com.bluejungle.destiny.services.policy.types.EntityDigestList;
import com.bluejungle.destiny.services.policy.types.ExternalDataSourceConnectionInfo;
import com.bluejungle.destiny.services.policy.types.ExternalDataSourceFault;
import com.bluejungle.destiny.services.policy.types.LeafObjectDTO;
import com.bluejungle.destiny.services.policy.types.LeafObjectList;
import com.bluejungle.destiny.services.policy.types.LeafObjectSearchSpecDTO;
import com.bluejungle.destiny.services.policy.types.ListOfIds;
import com.bluejungle.destiny.services.policy.types.LockRequestType;
import com.bluejungle.destiny.services.policy.types.ObligationDescriptorList;
import com.bluejungle.destiny.services.policy.types.PolicyActionsDescriptorList;
import com.bluejungle.destiny.services.policy.types.PolicyEditorRoles;
import com.bluejungle.destiny.services.policy.types.PolicyServiceFault;
import com.bluejungle.destiny.services.policy.types.RealmList;
import com.bluejungle.destiny.services.policy.types.ResourceTreeNode;
import com.bluejungle.destiny.services.policy.types.ResourceTreeNodeList;
import com.bluejungle.destiny.services.policy.types.StringList;

/**
 * This class provides default implementation of the PolicyEditorIF interface.
 * This eliminates the compile-time dependency on the generated code from
 * code in other libraries that must implement the interface.
 */
public class DefaultPolicyEditorIF implements PolicyEditorIF {

    /**
     * @see PolicyEditorIF#getConfigValue(String)
     */
    public String getConfigValue( String name ) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getNumDeployedPolicies()
     */
    public int getNumDeployedPolicies() throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return 0;
    }

    /**
     * @see PolicyEditorIF#getLatestDeploymentTime()
     */
    public Calendar getLatestDeploymentTime() throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#objectLock(java.math.BigInteger, LockRequestType)
     */
    public String objectLock(BigInteger id, LockRequestType type) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getDescriptorsForNameAndType(java.lang.String, DomainObjectEnumList)
     */
    public EntityDescriptorList getDescriptorsForNameAndType(String nameTemplate, DomainObjectEnumList domainObjectEnumList) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getEntitiesForNamesAndType(StringList, DomainObjectEnum)
     */
    public String getEntitiesForNamesAndType(StringList nameList, DomainObjectEnum domainObjectType) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getEntitiesForDescriptors(EntityDescriptorList)
     */
    public String getEntitiesForDescriptors(EntityDescriptorList descriptorListReq) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getDODDigests(StringList)
     */
    public EntityDigestList getDODDigests(StringList types) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getDODDigestsByeIds(ListOfIds)
     */
    public EntityDigestList getDODDigestsByIds(ListOfIds ids) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getDescriptorsByIds(ListOfIds)
     */
    public EntityDescriptorList getDescriptorsByIds(ListOfIds ids) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#saveEntities(java.lang.String)
     */
    public EntityDescriptorList saveEntities(String pql) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#saveEntitiesDigest(java.lang.String)
     */
    public EntityDigestList saveEntitiesDigest(String pql) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getReferringObjectsForGroup(java.lang.String, DomainObjectEnum, DomainObjectEnum, boolean)
     */
    public EntityDescriptorList getReferringObjectsForGroup(String nameTemplate, DomainObjectEnum domainObjectEnum, DomainObjectEnum referringType, boolean onlyDirect) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getReferringObjectsAsOf(EntityDescriptorList, java.util.Calendar, boolean)
     */
    public EntityDescriptorList getReferringObjectsAsOf(EntityDescriptorList descriptors, Calendar theDate, boolean onlyDirect) throws RemoteException, ServiceNotReadyFault, CircularReferenceFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getDependencies(EntityDescriptorList)
     */
    public EntityDescriptorList getDependencies(EntityDescriptorList descriptorListReq, boolean directOnly) throws RemoteException, ServiceNotReadyFault, CircularReferenceFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getDependenciesDigest(EntityDigestList)
     */
    public EntityDigestList getDependenciesDigest(EntityDigestList descriptorListReq, boolean directOnly) throws RemoteException, ServiceNotReadyFault, CircularReferenceFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getDescriptorsForState(DomainObjectStateEnum,String,long)
     */
    public EntityDescriptorList getDescriptorsForState(DomainObjectStateEnum objectState) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getSubjectAttributes(DomainObjectEnum)
     */
    public AttributeDescriptorList getSubjectAttributes(DomainObjectEnum domainObjectType) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getResourceAttributes()
     */
    public AttributeDescriptorList getResourceAttributes(String subtypeName) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getResourceAttributes()
     */
    public AttributeDescriptorList getCustomResourceAttributes(String subtypeName) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getDeployedObjectDescriptors(EntityDescriptorList, java.util.Calendar)
     */
    public EntityDescriptorList getDeployedObjectDescriptors(EntityDescriptorList descriptors, Calendar theDate) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getDeployedObjects(EntityDescriptorList, java.util.Calendar)
     */
    public String getDeployedObjects(EntityDescriptorList descriptors, Calendar theDate) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getDependenciesAsOf(EntityDescriptorList, java.util.Calendar)
     */
    public EntityDescriptorList getDependenciesAsOf(EntityDescriptorList descriptors, Calendar theDate) throws RemoteException, ServiceNotReadyFault, CircularReferenceFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#descriptorsForDeploymentRecord(DeploymentRecordDTO)
     */
    public EntityDescriptorList descriptorsForDeploymentRecord(DeploymentRecordDTO record) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getDeploymentHistory(EntityDescriptorDTO)
     */
    public DeploymentHistoryList getDeploymentHistory(EntityDescriptorDTO descriptor) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#scheduleDeployment(EntityDescriptorList, java.util.Calendar)
     */
    public DeploymentRecordDTO scheduleDeployment(EntityDescriptorList descriptors, Calendar theDate) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
    	return null;
    }

    /**
     * @see PolicyEditorIF#scheduleUndeployment(EntityDescriptorList, java.util.Calendar)
     */
    public DeploymentRecordDTO scheduleUndeployment(EntityDescriptorList descriptors, Calendar theDate) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
    	return null;
    }

    /**
     * @see PolicyEditorIF#cancelScheduledDeployment(DeploymentRecordDTO)
     */
    public void cancelScheduledDeployment(DeploymentRecordDTO record) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
    }

    /**
     * @see PolicyEditorIF#getDeploymentRecords(long, long)
     */
    public DeploymentRecordList getDeploymentRecords(long from, long to) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getLeafObjectsByIds(ListOfIds,ListOfIds,ListOfIds)
     */
    public LeafObjectList getLeafObjectsByIds(ListOfIds elements, ListOfIds userGroups, ListOfIds hostGroups) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

	public LeafObjectList runLeafObjectQuery(LeafObjectSearchSpecDTO searchSpec) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }
		
    /**
     * @see PolicyEditorIF#getMatchingSubjects(EntityDescriptorDTO)
     */
    public LeafObjectList getMatchingSubjects(EntityDescriptorDTO descriptor) throws RemoteException, ServiceNotReadyFault, CircularReferenceFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getMatchingActions(EntityDescriptorDTO)
     */
    public LeafObjectList getMatchingActions(EntityDescriptorDTO descriptor) throws RemoteException, ServiceNotReadyFault, CircularReferenceFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getFullyResolvedEntity(EntityDescriptorDTO)
     */
    public String getFullyResolvedEntity(EntityDescriptorDTO descriptor) throws RemoteException, ServiceNotReadyFault, CircularReferenceFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#prepareForTests()
     */
    public void prepareForTests() throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
    }

    /**
     * @see PolicyEditorIF#ensureOperationIsAllowed(EntityDescriptorList, Access)
     */
    public EntityDescriptorList ensureOperationIsAllowed(EntityDescriptorList descriptorListReq, Access desiredAccess) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#allowedActions(EntityDescriptorList)
     */
    public AccessList allowedActions(EntityDescriptorList descriptorListReq) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getAgentList()
     */
    public AgentStatusList getAgentList() throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getAgentsForDeployedObject(EntityDescriptorDTO, java.util.Calendar)
     */
    public AgentStatusList getAgentsForDeployedObject(EntityDescriptorDTO descriptor, Calendar theDate) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#deploymentStatusForAgent(AgentStatusDescriptorDTO)
     */
    public EntityDescriptorList deploymentStatusForAgent(AgentStatusDescriptorDTO descriptor) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getSuperUser()
     */
    public LeafObjectDTO getSuperUser() throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }
    
    /**
     * @see PolicyEditorIF#changePassword(java.lang.String, java.lang.String, java.lang.String)
     */
    public void changePassword(String uniqueName, String oldPassword, String newPassword) throws RemoteException, InvalidPasswordFault {
    }

    /**
     * @see PolicyEditorIF#getObligationDescriptors()
     */
    public ObligationDescriptorList getObligationDescriptors() throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getPolicyActionsDescriptors()
     */
    public PolicyActionsDescriptorList getPolicyActionsDescriptors() throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getAccessGroupsForUser()
     */
    public LeafObjectList getAccessGroupsForUser (BigInteger id) throws RemoteException, InvalidPasswordFault {
        return null;
    }

    /**
     * @see PolicyEditorIF#getAllowedEntitiesForUser()
     */
    public ComponentList getAllowedEntitiesForUser (BigInteger id) throws RemoteException, InvalidPasswordFault {
        return null;
    }
    
    public void updateComputersWithAgents() {
        
    }
        
    public boolean hasObjectsToDeploy() {
        return false;
    }

    public DomainObjectUsageListDTO getUsageList( EntityDescriptorList descriptorListReq ) throws RemoteException, ServiceNotReadyFault {
        return null;
    }
    
    public StringList getDictionaryEnrollmentNames( PolicyEditorRoles roles ) throws RemoteException, ServiceNotReadyFault {
        return null;
    }

    public RealmList getDictionaryEnrollmentRealms( PolicyEditorRoles roles ) throws RemoteException, ServiceNotReadyFault {
        return null;
    }

    public StringList getPortalURLList() throws RemoteException, ExternalDataSourceFault {
        return null;
    }
  
    public int createExternalDataSource(ExternalDataSourceConnectionInfo info) throws RemoteException, ExternalDataSourceFault {
        return 0;
    }

    public ResourceTreeNodeList getResourceTreeNodeChildren(int sourceID, ResourceTreeNode node) throws RemoteException, ExternalDataSourceFault {
        return null;
    }
    
    public ResourceTreeNodeList getMatchingPortalResource(int arg0, EntityDescriptorDTO arg1) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return null;
    }
    
    public void executePush() throws PolicyServiceFault{
    	//nothing
    }
    
    public void schedulePush(Calendar scheduleTime) throws PolicyServiceFault{
    	//nothing
    }
}
