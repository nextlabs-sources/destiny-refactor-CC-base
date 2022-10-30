package com.nextlabs.shared.tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginException;
import com.bluejungle.destiny.services.policy.types.EntityDescriptorDTO;
import com.bluejungle.destiny.services.policy.types.ExternalDataSourceConnectionInfo;
import com.bluejungle.destiny.services.policy.types.ExternalDataSourceType;
import com.bluejungle.destiny.services.policy.types.PolicyEditorRoles;
import com.bluejungle.destiny.services.policy.types.Realm;
import com.bluejungle.destiny.services.policy.types.ResourceTreeNode;
import com.bluejungle.destiny.services.policy.types.ResourceTreeNodeList;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.pf.destiny.lib.AgentStatusDescriptor;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectSearchSpec;
import com.bluejungle.pf.destiny.lifecycle.AttributeDescriptor;
import com.bluejungle.pf.destiny.lifecycle.CircularReferenceException;
import com.bluejungle.pf.destiny.lifecycle.DeploymentRecord;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.ObligationDescriptor;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.IPolicyEditorClient;
import com.bluejungle.pf.destiny.services.InvalidPasswordException;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.destiny.services.ResourcePreview;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.epicenter.action.IAction;

public class MockPolicyEditorClient implements IPolicyEditorClient, IInitializable, IConfigurable {
	private boolean login;
	private String hostUrl;
	private String username;
	private String password;
	
	public MockPolicyEditorClient() {
	}

	public MockPolicyEditorClient(String hostUrl, String username, String password) {
		super();
		this.hostUrl = hostUrl;
		this.username = username;
		this.password = password;
	}
	
	public final String getHostUrl() {
		return hostUrl;
	}

	public final String getPassword() {
		return password;
	}

	public final String getUsername() {
		return username;
	}

	public void acquireLock(IHasId arg0, boolean arg1) throws PolicyEditorException {
	}

	public Collection<? extends IAction> allowedActions(Collection<DomainObjectDescriptor> arg0)
			throws PolicyEditorException {
		return null;
	}

	public Collection<? extends IAction> allowedActions(IHasId arg0) throws PolicyEditorException {
		return null;
	}

	public Collection<EntityType> allowedEntities() throws PolicyEditorException {
		return null;
	}

	public void cancelScheduledDeployment(DeploymentRecord arg0) throws PolicyEditorException {
	}

	public boolean canChangePassword() throws PolicyEditorException {
		return false;
	}

	public void changePassword(String arg0, String arg1) throws PolicyEditorException, InvalidPasswordException {
	}

	public int createExternalDataSource(ExternalDataSourceConnectionInfo arg0) throws PolicyEditorException {
		return 0;
	}

	public Collection<DomainObjectDescriptor> deploymentStatusForAgent(AgentStatusDescriptor arg0)
			throws PolicyEditorException {
		return null;
	}

	public Collection<DomainObjectDescriptor> ensureOperationIsAllowed(Collection<DomainObjectDescriptor> arg0,
			IAction arg1) throws PolicyEditorException {
		return arg0;
	}

	public void executePush() throws PolicyEditorException {
	}

	public Collection<AgentStatusDescriptor> getAgentList() throws PolicyEditorException {
		return null;
	}

	public Collection<AgentStatusDescriptor> getAgentsForDeployedObject(DomainObjectDescriptor arg0, Date arg1)
			throws PolicyEditorException {
		return null;
	}

	public Collection<AttributeDescriptor> getAttributesForType(EntityType arg0) throws PolicyEditorException {
		return null;
	}

	public String getConfigValue(String arg0) throws PolicyEditorException {
		return null;
	}

	public Collection<DomainObjectDescriptor> getDependencies(Collection<DomainObjectDescriptor> arg0)
			throws CircularReferenceException, PolicyEditorException {
		return arg0;
	}

	public Collection<DomainObjectDescriptor> getDependenciesAsOf(Collection<DomainObjectDescriptor> arg0, Date arg1)
			throws CircularReferenceException, PolicyEditorException {
		return arg0;
	}

	public Collection<DomainObjectDescriptor> getDeployedObjectDescriptors(Collection<DomainObjectDescriptor> arg0,
			Date arg1) throws PolicyEditorException {
		return arg0;
	}

	public Collection<? extends IHasId> getDeployedObjects(Collection<DomainObjectDescriptor> arg0, Date arg1)
			throws PolicyEditorException {
		return null;
	}

	public Collection<TimeRelation> getDeploymentHistory(DomainObjectDescriptor arg0) throws PolicyEditorException {
		return null;
	}

	public Collection<DeploymentRecord> getDeploymentRecords(Date arg0, Date arg1) throws PolicyEditorException {
		return null;
	}

	public Collection<DomainObjectDescriptor> getDescriptorsForDeploymentRecord(DeploymentRecord arg0)
			throws PolicyEditorException {
		return null;
	}

	public Collection<DomainObjectDescriptor> getDescriptorsForNameAndType(String arg0, EntityType arg1, boolean arg2)
			throws PolicyEditorException {
		return new ArrayList<DomainObjectDescriptor>();
	}

	public Collection<DomainObjectDescriptor> getDescriptorsForNameAndTypes(String arg0, Collection<EntityType> arg1,
			boolean arg2) throws PolicyEditorException {
		return null;
	}

	public Collection<DomainObjectDescriptor> getDescriptorsForState(DevelopmentStatus arg0, boolean arg1)
			throws PolicyEditorException {
		return null;
	}

	public Set<String> getDictionaryEnrollmentNames(PolicyEditorRoles arg0) throws PolicyEditorException {
		return null;
	}

	public Set<Realm> getDictionaryEnrollmentRealms(PolicyEditorRoles arg0) throws PolicyEditorException {
		return Collections.EMPTY_SET;
	}

	public Collection<? extends IHasId> getEntitiesForDescriptors(Collection<DomainObjectDescriptor> arg0)
			throws PolicyEditorException {
		return Collections.EMPTY_LIST;
	}

	public Collection<? extends IHasId> getEntitiesForNamesAndType(Collection<String> arg0, EntityType arg1,
			boolean arg2) throws PolicyEditorException {
		return Collections.EMPTY_LIST;
	}

	public Calendar getLatestDeploymentTime() throws PolicyEditorException {
		return null;
	}

	public List<LeafObject> getLeafObjectsForIds(long[] arg0, long[] arg1, long[] arg2) throws PolicyEditorException {
		return Collections.EMPTY_LIST;
	}

	public IDSubject getLoggedInUser() throws PolicyEditorException {
		return null;
	}

	public Collection<? extends IAction> getMatchingActions(DomainObjectDescriptor arg0) throws PolicyEditorException {
		return null;
	}

	public ResourceTreeNodeList getMatchingPortalResource(int arg0, EntityDescriptorDTO arg1)
			throws PolicyEditorException {
		return null;
	}

	public Collection<LeafObject> getMatchingSubjects(DomainObjectDescriptor arg0) throws PolicyEditorException {
		return null;
	}

	public String getNormalizedResourceURL(String arg0, ExternalDataSourceType arg1) {
		return null;
	}

	public int getNumDeployedPolicies() throws PolicyEditorException {
		return 0;
	}

	public List<String> getPortalURLList() throws PolicyEditorException {
		return null;
	}

	public Collection<DomainObjectDescriptor> getReferringObjectsAsOf(Collection<DomainObjectDescriptor> arg0,
			Date arg1, boolean arg2) throws PolicyEditorException, CircularReferenceException {
		return null;
	}

	public Collection<DomainObjectDescriptor> getReferringObjectsForGroup(String arg0, EntityType arg1,
			EntityType arg2, boolean arg3, boolean arg4) throws PolicyEditorException {
		return null;
	}

	public ResourcePreview getResourcePreview(DomainObjectDescriptor arg0) throws PolicyEditorException {
		return null;
	}

	public ResourceTreeNode getResourceTreeNodeChildren(int arg0, ResourceTreeNode arg1) throws PolicyEditorException {
		return null;
	}

	public LeafObject getSuperUser() throws PolicyEditorException {
		return null;
	}

	public List<DomainObjectUsage> getUsageList(List<DomainObjectDescriptor> arg0) throws PolicyEditorException {
		return Collections.EMPTY_LIST;
	}

	public boolean hasLock(IHasId arg0) throws PolicyEditorException {
		return false;
	}

	public boolean hasObjectsToDeploy() throws PolicyEditorException {
		return false;
	}

	public boolean isLoggedIn() {
		return login;
	}

	public void login() throws LoginException {
		login = true;
	}

	public void releaseLock(IHasId arg0) throws PolicyEditorException {
	}

	public List<LeafObject> runLeafObjectQuery(LeafObjectSearchSpec arg0) throws PolicyEditorException {
		return Collections.EMPTY_LIST;
	}

	public List<DomainObjectDescriptor> saveEntities(Collection<? extends IHasId> arg0) throws PolicyEditorException {
		return Collections.EMPTY_LIST;
	}

	public DeploymentRecord scheduleDeployment(Collection<DomainObjectDescriptor> arg0, Date arg1) throws PolicyEditorException {
		return null;
	}

	public DeploymentRecord scheduleUndeployment(Collection<DomainObjectDescriptor> arg0, Date arg1) throws PolicyEditorException {
		return null;
	}

	public void updateComputersWithAgents() throws PolicyEditorException {
	}

	public IConfiguration getConfiguration() {
		return null;
	}

	public void setConfiguration(IConfiguration arg0) {
	}

	public Collection<ObligationDescriptor> getObligationDescriptors() throws PolicyEditorException {
		return Collections.EMPTY_LIST;
	}

	/**
	 * @see com.bluejungle.pf.destiny.services.IPolicyEditorClient#executePush(java.util.Date)
	 */
	public void executePush(Date scheduleTime) throws PolicyEditorException {
	}

	/**
	 * @see com.bluejungle.framework.comp.IInitializable#init()
	 */
	public void init() {
	}
}
