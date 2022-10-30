package com.bluejungle.pf.destiny.services;

import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginException;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.pf.destiny.lib.AgentStatusDescriptor;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectSearchSpec;
import com.bluejungle.pf.destiny.lib.PolicyServiceException;
import com.bluejungle.pf.destiny.lifecycle.AttributeDescriptor;
import com.bluejungle.pf.destiny.lifecycle.CircularReferenceException;
import com.bluejungle.pf.destiny.lifecycle.DeploymentHistory;
import com.bluejungle.pf.destiny.lifecycle.DeploymentRecord;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.ObligationDescriptor;
import com.bluejungle.pf.destiny.lifecycle.PolicyActionsDescriptor;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.epicenter.action.IAction;
import com.bluejungle.destiny.services.policy.types.EntityDescriptorDTO;
import com.bluejungle.destiny.services.policy.types.ExternalDataSourceConnectionInfo;
import com.bluejungle.destiny.services.policy.types.ExternalDataSourceType;
import com.bluejungle.destiny.services.policy.types.PolicyEditorRoles;
import com.bluejungle.destiny.services.policy.types.Realm;
import com.bluejungle.destiny.services.policy.types.ResourceTreeNode;
import com.bluejungle.destiny.services.policy.types.ResourceTreeNodeList;

import javax.xml.rpc.ServiceException;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/*
 * All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc. All rights reserved
 * worldwide.
 * 
 * @author sergey
 * 
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/pf/src/java/main/com/bluejungle/pf/destiny/services/IPolicyEditorClient.java#7 $
 */

/**
 * @author sergey
 */
public interface IPolicyEditorClient extends IConfigurable{
	PropertyKey<String> LOCATION_CONFIG_PARAM = new PropertyKey<String>("location");
	PropertyKey<String> USERNAME_CONFIG_PARAM = new PropertyKey<String>("username");
	PropertyKey<String> PASSWORD_CONFIG_PARAM = new PropertyKey<String>("password");
    
    /**
     * Returns user who is currently logged in, or null if user is not logged
     * in.
     * 
     * @return user who is currently logged in or null
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    IDSubject getLoggedInUser() throws PolicyEditorException;

    /**
     * Determine if the current user is allowed to change their password
     * 
     * @return true if allowed; false otherwise
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    boolean canChangePassword() throws PolicyEditorException;

    /**
     * Determines if the client is logged in.
     * 
     * @return true if the client is logged in, false otherwise.
     */
    boolean isLoggedIn();

    /**
     * Returns the latest deployment time.
     * 
     * @return the latest deployment time.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Calendar getLatestDeploymentTime() throws PolicyEditorException;

    /**
     * Returns the number of deployed policies.
     * 
     * @return the number of deployed policies.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    int getNumDeployedPolicies() throws PolicyEditorException;

    /**
     * Given an <code>EntityType</code>, returns a <code>Collection</code>
     * of <code>DomainObjectDescriptor</code> objects representing the
     * corresponding entities.
     * 
     * @param nameTemplate
     *            a <code>String</code> with wildcards '%' and '.' defining a
     *            template for the names of desired objects.
     * @param type
     *            Type of the entity.
     * @param includeHidden
     *            hidden entities are included only when this flag is set.
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects representing the
     *         corresponding entities.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<DomainObjectDescriptor> getDescriptorsForNameAndType(String nameTemplate, EntityType type, boolean includeHidden) throws PolicyEditorException;

    /**
     * Given a <code>Collection</code> of <code>EntityType</code> objects,
     * returns a <code>Collection</code> of
     * <code>DomainObjectDescriptor</code> objects representing the
     * corresponding entities.
     * 
     * @param nameTemplate
     *            a <code>String</code> with wildcards '%' and '.' defining a
     *            template for the names of desired objects.
     * @param types
     *            a <code>Collection</code> of <code>EntityType</code>
     *            objects.
     * @param includeHidden
     *            hidden entities are included only when this flag is set.
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects representing the
     *         corresponding entities.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<DomainObjectDescriptor> getDescriptorsForNameAndTypes(String nameTemplate, Collection<EntityType> types, boolean includeHidden) throws PolicyEditorException;

    /**
     * Given an <code>EntityType</code>, returns a <code>Collection</code>
     * of policy objects with names matching the template and the corresponding
     * type.
     * 
     * @param nameTemplate
     *            a <code>String</code> with wildcards '%' and '.' defining a
     *            template for the names of desired objects.
     * @param type
     *            Type of the entity.
     * @param includeHidden
     *            hidden entities are included only when this flag is set.
     * @return a <code>Collection</code> of policy objects representing names
     *         of the corresponding entities.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<? extends IHasId> getEntitiesForNamesAndType(Collection<String> names, EntityType type, boolean includeHidden) throws PolicyEditorException;

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>s,
     * returns a <code>Collection</code> of domain objects.
     * 
     * @param descriptors
     *            a <code>Collection</code> of
     *            <code>DomainObjectDescriptor</code> objects representing
     *            names of entities.
     * @return a <code>Collection</code> of domain objects.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<? extends IHasId> getEntitiesForDescriptors(Collection<DomainObjectDescriptor> descriptors) throws PolicyEditorException;

    /**
     * Given a <code>Collection</code> of <code>Strings</code> representing names
     * of entitye types, return a <code>List</code> of correspodning
     * <code>DODDigest</code>s, a lightweight domain object
     *
     * @param types, the names of the entity types (e.g. PORTAL, FOLDER)
     * @return a <code>List</code> of lightweight domain objects
     * @throws PolicyEditorException when the operation can not complete
     */
    List<DODDigest> getDODDigests(Collection<String> types) throws PolicyEditorException;

    /**
     * Given a <code>List</code> of ids of domain objects (presumably acquired from
     * <code>getDODDigests</code>) return a <code>List</code> of the actual
     * <code>DomainObjectDescriptor</code>s.
     *
     * @param ids, a <code>List</code> of ids of domain objects
     * @return a <code>List</code> of DomainObjectDescriptor
     * @throws PolicyEditorException when the operation can not complete
     */
    List<DomainObjectDescriptor> getDescriptorsByIds(List<Long> ids) throws PolicyEditorException;

    /**
     * Saves the given <code>Collection</code> of domain objects.
     *
     * @param entities the <code>Collection</code> of domain objects to be saved
     * @return a <code>List</code> of domain object digets corresponding to the saved
     * objects
     * @throws PolicyEditorException when the operation cannot complete
     */
    List<DODDigest> saveEntitiesDigest(Collection<? extends IHasId> entities) throws PolicyEditorException;

    /**
     * Saves the given <code>Collection</code> of domain objects.
     * 
     * @param entities
     *            the <code>Collection</code> of domain objects to be saved.
     * @return a <code>List</code> of descriptors corresponding to the saved
     *         objects from <code>entities</code>.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    List<DomainObjectDescriptor> saveEntities(Collection<? extends IHasId> entities) throws PolicyEditorException;

    /**
     * Given a name and a type, returns a <code>Collection</code> of
     * <code>DomainObjectDescriptor</code> objects of
     * <code>referringType</code> that refer to objects matching the name
     * template and the specified entity type.
     * 
     * @param nameTemplate
     *            template (with wildcards) of an object name.
     * @param type
     *            the type of the entity for which we are finding the refernces.
     * @param referringType
     *            type of the referring entities, or <code>null</code>. When
     *            the value is <code>null</code>, all referring objects are
     *            returned regardless of their type.
     * @param onlyDirect
     *            when this falg is set to true, only direct references are
     *            returned; when false, a transitive closure of the referring
     *            objects is returned.
     * @param includeHidden
     *            specifies whether to include hidden objects in the returned
     *            collection
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects of
     *         <code>referringType</code> that refer to objects matching the
     *         name template and the specified entity type.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<DomainObjectDescriptor> getReferringObjectsForGroup(String nameTemplate, EntityType type, EntityType referringType, boolean onlyDirect, boolean includeHidden) throws PolicyEditorException;

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects and an asOf <code>Date</code>, returns a
     * <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects referring to the objects in the initial <code>Collection</code>.
     * 
     * @param initial
     *            a <code>Collection</code> of
     *            <code>DomainObjectDescriptor</code> objects for which to
     *            find the dependencies.
     * @param asOf
     *            the asOf <code>Date</code> for the query.
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects referring to the
     *         objects in the initial <code>Collection</code>.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     * @throws CircularReferenceException
     *             when a circular dependency exists.
     */
    Collection<DomainObjectDescriptor> getReferringObjectsAsOf(Collection<DomainObjectDescriptor> initial, Date asOf, boolean onlyDirect) throws PolicyEditorException, CircularReferenceException;

    /**
     * Given a <code>Collection</code> of <code>DODDigest</code>
     * objects, returns a <code>Collection</code> of
     * <code>DODDigest</code> objects referred to by objects in
     * the initial <code>Collection</code>.
     * 
     * @param initial
     *            a <code>Collection</code> of
     *            <code>DODDigest</code> objects for which to
     *            find the dependencies.
     * @return a <code>Collection</code> of
     *         <code>DODDigest</code> objects referred to by
     *         objects in the initial <code>Collection</code>.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<DODDigest> getDependenciesDigest(Collection<DODDigest> initial) throws CircularReferenceException, PolicyEditorException;

    /**
     * Given a <code>Collection</code> of <code>DODDigest</code>
     * objects, returns a <code>Collection</code> of
     * <code>DODDigest</code> objects referred to by objects in
     * the initial <code>Collection</code>.
     * 
     * @param initial
     *            a <code>Collection</code> of
     *            <code>DODDigest</code> objects for which to
     *            find the dependencies.
     * @param directOnly
     *        only return direct dependencies
     * @return a <code>Collection</code> of
     *         <code>DODDigest</code> objects referred to by
     *         objects in the initial <code>Collection</code>.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<DODDigest> getDependenciesDigest(Collection<DODDigest> initial, boolean directOnly) throws CircularReferenceException, PolicyEditorException;

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects, returns a <code>Collection</code> of
     * <code>DomainObjectDescriptor</code> objects referred to by objects in
     * the initial <code>Collection</code>.
     * 
     * @param initial
     *            a <code>Collection</code> of
     *            <code>DomainObjectDescriptor</code> objects for which to
     *            find the dependencies.
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects referred to by
     *         objects in the initial <code>Collection</code>.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<DomainObjectDescriptor> getDependencies(Collection<DomainObjectDescriptor> initial) throws CircularReferenceException, PolicyEditorException;

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects, returns a <code>Collection</code> of
     * <code>DomainObjectDescriptor</code> objects referred to by objects in
     * the initial <code>Collection</code>.
     * 
     * @param initial
     *            a <code>Collection</code> of
     *            <code>DomainObjectDescriptor</code> objects for which to
     *            find the dependencies.
     * @param directOnly
     *        only return direct dependencies
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects referred to by
     *         objects in the initial <code>Collection</code>.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<DomainObjectDescriptor> getDependencies(Collection<DomainObjectDescriptor> initial, boolean directOnly) throws CircularReferenceException, PolicyEditorException;

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects and an asOf <code>Date</code>, returns a
     * <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects referred to by objects in the initial <code>Collection</code>.
     * 
     * @param initial
     *            a <code>Collection</code> of
     *            <code>DomainObjectDescriptor</code> objects for which to
     *            find the dependencies.
     * @param asOf
     *            the asOf <code>Date</code> for the query.
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects referred to by
     *         objects in the initial <code>Collection</code>.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<DomainObjectDescriptor> getDependenciesAsOf(Collection<DomainObjectDescriptor> initial, Date asOf) throws CircularReferenceException, PolicyEditorException;

    /**
     * Given an entity status, returns a <code>Collection</code> of
     * <code>DomainObjectDescriptor</code> objects in the specified state.
     * 
     * @param status
     *            The development status of the object (empty, draft, approved,
     *            etc.)
     * @param includeHidden
     *            hidden entities are included only when this flag is set.
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects in the specified
     *         state.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<DomainObjectDescriptor> getDescriptorsForState(DevelopmentStatus status, boolean includeHidden) throws PolicyEditorException;

    /**
     * Given an <code>EntityType</code>, returns all attributes applicable to
     * it.
     * 
     * @param type
     *            the required <code>EntityType</code>.
     * @return a <code>Collection</code> of <code>String</code> objects
     *         representing the names of the attributes for the given type.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<AttributeDescriptor> getAttributesForType(EntityType type) throws PolicyEditorException;

    /**
     * Given an <code>EntityType</code>, returns all custom attributes applicable to
     * it.
     * 
     * @param type
     *            the required <code>EntityType</code>.
     * @return a <code>Collection</code> of <code>String</code> objects
     *         representing the names of the custom attributes for the given type.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<AttributeDescriptor> getCustomAttributesForType(EntityType type) throws PolicyEditorException;

    /**
     * Returns all the <code>ObligationDescriptor</code>s
     * @return a <code>Collection</code> of <code>ObligationDescriptor</code> objects
     * @throws PolicyEditorException when the operation can not complete
     */
    Collection<ObligationDescriptor> getObligationDescriptors() throws PolicyEditorException;

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects and an "as of" <code>Date</code>, returns a
     * <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects describing the deployed objects at the time specified by the asOf
     * <code>Date</code>.
     * 
     * @param descriptors
     *            a <code>Collection</code> of
     *            <code>DomainObjectDescriptor</code> objects for which we
     *            need to obtain the deployed objects.
     * @param asOf
     *            the "as of" <code>Date</code> for which the deployed objects
     *            are to be examined.
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects describing the
     *         deployed objects at the time specified by asOf.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<DomainObjectDescriptor> getDeployedObjectDescriptors(Collection<DomainObjectDescriptor> descriptors, Date asOf) throws PolicyEditorException;

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects and an "as of" <code>Date</code>, returns a
     * <code>Collection</code> of domain objects representing the state of the
     * deployed objects at the time specified by the asOf <code>Date</code>.
     * 
     * @param descriptors
     *            a <code>Collection</code> of
     *            <code>DomainObjectDescriptor</code> objects for which we
     *            need to obtain the deployed objects.
     * @param asOf
     *            the "as of" <code>Date</code> for which the deployed objects
     *            are to be examined.
     * @return returns a <code>Collection</code> of domain objects
     *         representing the state of the deployed objects at the time
     *         specified by asOf.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<? extends IHasId> getDeployedObjects(Collection<DomainObjectDescriptor> descriptors, Date asOf) throws PolicyEditorException;

    /**
     * Given a <code>DeploymentRecord</code>, returns a
     * <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects that were deployed or undeployed in a deployment action described
     * by the record.
     * 
     * @param record
     *            the <code>DeploymentRecord</code> the deployed or undeployed
     *            objects for which we are querying.
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects that were deployed
     *         or undeployed in a deployment action described by the record.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<DomainObjectDescriptor> getDescriptorsForDeploymentRecord(DeploymentRecord record) throws PolicyEditorException;

    /**
     * Given a <code>DomainObjectDescriptor</code>, returns a
     * <code>Collection</code> of <code>TimeRelation</code> objects
     * representing the from and the to dates of deployment of the given object.
     * 
     * @param descriptor
     *            a <code>DomainObjectDescriptor</code> of the object for
     *            which we need the deployment history.
     * @return a <code>List</code> of <code>TimeRelation</code> objects
     *         representing the from and the to dates of deployment of the given
     *         object.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<DeploymentHistory> getDeploymentHistory(DomainObjectDescriptor descriptor) throws PolicyEditorException;

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects, schedules a deployment of all objects in the
     * <code>Collection</code> at the time specified by the "when" parameter.
     * 
     * @param descriptors
     *            a <code>Collection</code> of
     *            <code>DomainObjectDescriptor</code> objects to be deployed.
     * @param when
     *            the date when the deployment is to happen.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    DeploymentRecord scheduleDeployment(Collection<DomainObjectDescriptor> descriptors, Date when) throws PolicyEditorException;

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects, schedules an undeployment of all objects in the
     * <code>Collection</code> at the time specified by the "when" parameter.
     * 
     * @param descriptors
     *            a <code>Collection</code> of
     *            <code>DomainObjectDescriptor</code> objects to be
     *            undeployed.
     * @param when
     *            the date when the undeployment is to happen.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    DeploymentRecord scheduleUndeployment(Collection<DomainObjectDescriptor> descriptors, Date when) throws PolicyEditorException;

    /**
     * Cancels a deployment action described by a <code>DeploymentRecod</code>.
     * 
     * @param record
     *            the record describing the action to be canceled.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    void cancelScheduledDeployment(DeploymentRecord record) throws PolicyEditorException;

    /**
     * Given a from and a to dates, returns a <code>Collection</code> of
     * <code>DeploymentRecord</code> objects representing all deployment and
     * undeployment actions that happened in the interval [from, to], inclusive.
     * 
     * @param from
     *            the from <code>Date</code>.
     * @param to
     *            the to <code>Date</code>.
     * @return a <code>Collection</code> of <code>DeploymentRecord</code>
     *         objects representing all deployment and undeployment actions that
     *         happened in the interval [from, to], inclusive.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<DeploymentRecord> getDeploymentRecords(Date from, Date to) throws PolicyEditorException;

    /**
     * Search for leaf objects using the parameters in the specific Leaf Object
     * Search Specification
     * 
     * @param searchSpec
     * @return a list of leag objects matching the search spec
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    List<LeafObject> runLeafObjectQuery(LeafObjectSearchSpec searchSpec) throws PolicyEditorException;

    /**
     * Given an array of leaf object ids, returns a <code>List</code> of
     * corresponding <code>LeafObject</code>s.
     * 
     * @param elementIds
     *            an array of element IDs for which to get the
     *            <code>LeafObject</code>s.
     * @param userGroupIds
     *            an array of user group IDs for which to get the
     *            <code>LeafObject</code>s.
     * @param hostGroupIds
     *            an array of host group IDs for which to get the
     *            <code>LeafObject</code>s.
     * @return a <code>List</code> of <code>LeafObject</code>s
     *         corresponding to the specified IDs.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    List<LeafObject> getLeafObjectsForIds(long[] elementIds, long[] userGroupIds, long[] hostGroupIds) throws PolicyEditorException;

    /**
     * Given a descriptor of a subject group, returns a <code>Collection</code>
     * of <code>LeafObject</code>s belonging to that group.
     * 
     * @param descr
     *            a descriptor of a subject group.
     * @return a <code>Collection</code> of <code>LeafObject</code>s
     *         belonging to the specified group.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<LeafObject> getMatchingSubjects(DomainObjectDescriptor descr) throws PolicyEditorException;

    /**
     * Given a <code>List</code> of <code>DomainObjectDescriptor</code>
     * objects, returns a <code>List</code> of <code>DomainObjectUsage</code>
     * objects for the descriptors in the request, arranged in the same order as
     * the descriptors in the request.
     * 
     * @param descriptors
     *            a <code>List</code> of <code>DomainObjectDescriptor</code>
     *            objects for which we need <code>DomainObjectUsage</code>
     *            objects.
     * @return a <code>List</code> of <code>DomainObjectUsage</code> objects
     *         for the descriptors in the request, arranged in the same order as
     *         the descriptors in the request.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    List<DomainObjectUsage> getUsageList(List<DomainObjectDescriptor> descriptors) throws PolicyEditorException;

    /**
     * Retrieve the system super user as a <code>LeafObject</code>
     * 
     * @return the system super user as a <code>LeafObject</code>
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    LeafObject getSuperUser() throws PolicyEditorException;

    /**
     * Given a descriptor of an action group, returns a <code>Collection</code>
     * of <code>IDAction</code>s referenced by that group.
     * 
     * @param descr
     *            a descriptor of an action group.
     * @return a <code>Collection</code> of <code>IDAction</code>s
     *         referenced by that group.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<? extends IAction> getMatchingActions(DomainObjectDescriptor descr) throws PolicyEditorException;

    /**
     * Returns a <code>ResourcePreview</code> object for the given resource
     * descriptor.
     * 
     * @param descr
     *            a resource descriptor for which to obtain a preview object.
     * @return a <code>ResourcePreview</code> object for the given resource
     *         descriptor.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    ResourcePreview getResourcePreview(DomainObjectDescriptor descr) throws PolicyEditorException;

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>s
     * and a desired action, returns a subset of the initial
     * <code>Collection</code> that includes only the descriptors of objects
     * for which the access policy does not allow the action of the specified
     * type. When the desired action is allowed on all objects from the initial
     * <code>Collection</code>, an empty <code>Collection</code> is
     * returned.
     * 
     * @param descriptors
     *            a <code>Collection</code> of
     *            <code>DomainObjectDescriptor</code>s.
     * @param action
     *            the desired action.
     * @return a subset of the initial <code>Collection</code> that includes
     *         only the descriptors of objects for which the access policy does
     *         not allow the action of the specified type. When the desired
     *         action is allowed on all objects from the initial
     *         <code>Collection</code>, an empty <code>Collection</code> is
     *         returned.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<DomainObjectDescriptor> ensureOperationIsAllowed(Collection<DomainObjectDescriptor> descriptors, IAction action) throws PolicyEditorException;

    /**
     * Given a <code>Collection</code> of <code>DomainObjectdescriptor</code>s,
     * returns a <code>Collection</code> of actions allowed for all objects
     * from the given <code>Collection</code>. If no such action exists, an
     * empty <code>Collection</code> is returned.
     * 
     * @param descriptors
     *            a <code>Collection</code> of
     *            <code>DomainObjectDescriptor</code>s.
     * @return a <code>Collection</code> of actions allowed on all objects
     *         from the given <code>Collection</code>. If no such action
     *         exists, an empty <code>Collection</code> is returned.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<? extends IAction> allowedActions(Collection<DomainObjectDescriptor> descriptors) throws PolicyEditorException;

    /**
     * Given a Policy Framework object, returns a <code>Collection</code> of
     * actions allowed for this object. If no such action exists, an empty
     * <code>Collection</code> is returned.
     * 
     * @param descriptors
     *            a <code>Collection</code> of
     *            <code>DomainObjectDescriptor</code>s.
     * @return a <code>Collection</code> of actions allowed for this object.
     *         If no such action exists, an empty <code>Collection</code> is
     *         returned.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<? extends IAction> allowedActions(IHasId object) throws PolicyEditorException;

    /**
     * Returns a <code>Collection</code> of entities this user has access to.
     * 
     * @return a <code>Collection</code> of entities the logged in user has
     *         access to.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<EntityType> allowedEntities() throws PolicyEditorException;

    /**
     * Returns a <code>Collection</code> of <code>AgentStatusDescriptor</code>
     * objects describing the current status of deployment on each agent.
     * 
     * @return a <code>Collection</code> of <code>AgentStatusDescriptor</code>
     *         objects describing the current status of deployment on each
     *         agent.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<AgentStatusDescriptor> getAgentList() throws PolicyEditorException;

    /**
     * Returns a <code>Collection</code> of <code>AgentStatusDescriptor</code>
     * objects describing agents where the given policy or component is
     * deployed.
     * 
     * @param obj
     *            a descriptor of the component or policy.
     * @param asOf
     *            the date when the given component or policy became active.
     * @return a <code>Collection</code> of <code>AgentStatusDescriptor</code>
     *         objects describing agents where the given policy or component is
     *         deployed.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<AgentStatusDescriptor> getAgentsForDeployedObject(DomainObjectDescriptor obj, Date asOf) throws PolicyEditorException;

    /**
     * Given an <code>AgentStatusDescriptor</code>, returns a
     * <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects describing the deployed content of the specified agent.
     * 
     * @param agent
     *            the <code>AgentStatusDescriptor</code> of the agent for
     *            which the status is needed.
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects describing the
     *         deployed content of the specified agent.
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    Collection<DomainObjectDescriptor> deploymentStatusForAgent(AgentStatusDescriptor agent) throws PolicyEditorException;

    /**
     * Changes the password for the currently-logged in user
     * 
     * @param oldPassword
     *            user's old password
     * @param newPassword
     *            new password
     * @throws InvalidPasswordException
     *             if the old password is not correct
     * @throws PolicyEditorException
     *             if the operation cannot complete
     */
    void changePassword(String oldPassword, String newPassword) throws PolicyEditorException, InvalidPasswordException;

    /**
     * Given a name of a configuration item, returns its value or null if the
     * item is not available.
     * 
     * @param name
     *            the name of the configuration item to return.
     * @return the name of the configuration item or null if it is not
     *         available.
     * @throws PolicyEditorException
     *             when it is impossible to contact the server.
     */
    String getConfigValue(String name) throws PolicyEditorException;

    /**
     * Update the known list of computers with agents
     * 
     * @throws ServiceException
     * @throws RemoteException
     * @throws ServiceNotReadyFault
     */
    void updateComputersWithAgents() throws PolicyEditorException;

    /**
     * Determine if there are object ready to deploy
     * 
     * @return true if there are object to deploy; false otherwise
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    boolean hasObjectsToDeploy() throws PolicyEditorException;

    /**
     * Retrieve the set of Dictionary enrollment names
     * 
     * @param the policy editor role (CORPORATE, FILESYSTEM, PORTAL)
     * @return the set of Dictionary enrollment names
     * @throws PolicyEditorException
     */
    Set<String> getDictionaryEnrollmentNames(PolicyEditorRoles role) throws PolicyEditorException;

    /**
     * Retrieve the set of Dictionary enrollment Realms 
     * 
     * @param the policy editor role (CORPORATE, FILESYSTEM, PORTAL)
     * @return the set of Dictionary enrollment names
     * @throws PolicyEditorException
     */
    Set<Realm> getDictionaryEnrollmentRealms(PolicyEditorRoles role) throws PolicyEditorException;
    
    /**
     * Get Portal URL list
     * @return
     * @throws PolicyEditorException
     */
    List<String> getPortalURLList() throws PolicyEditorException;

    /**
     * Create external data source connection
     * @param externalDataSourceConnectionInfo
     * @return source connection ID
     * @throws PolicyEditorException
     */
    int createExternalDataSource(ExternalDataSourceConnectionInfo externalDataSourceConnectionInfo) throws PolicyEditorException;
    
    
    /**
     * get ResourceTreeNode Children, the resource tree node can not be null
     * all children will be set inside the resource tree node
     * @param sourceID
     * @param resourceTreeNode
     * @throws PolicyEditorException
     */
    ResourceTreeNode getResourceTreeNodeChildren(int sourceID, ResourceTreeNode resourceTreeNode) throws PolicyEditorException;
 
    /**
     * convert the resource URL to normalized resource URL
     * @param url
     * @param type
     * @return
     */
    String getNormalizedResourceURL(String url, ExternalDataSourceType type);
    

    /**
     * The Preview function for external portal resource
     * @param id the connection token for portal data source
     * @param descriptor
     * @return resource tree node list
     * @throws PolicyEditorException
     */
    ResourceTreeNodeList getMatchingPortalResource(int sourceID, EntityDescriptorDTO descriptor) throws PolicyEditorException;

    /**
     * Get the list of all permitted actions
     * @return A collection of PolicyActionsDescriptor
     * @throws PolicyEditorException
     */
    Collection<PolicyActionsDescriptor> getAllPolicyActions() throws PolicyEditorException;

    /**
     * Determine if the currently logged in user is the holder of teh lock for
     * the specified object
     * 
     * @param key
     *            an object to check for being locked or unlocked.
     * @return true if the logged in user holds the lock; false otherwise
     * @throws PolicyEditorException
     *             when the operation cannot complete.
     */
    boolean hasLock(IHasId object) throws PolicyEditorException;

    /**
     * Tries to acquire a lock for an object. If the acquisition fails, a
     * PolicyEditorException is thrown
     * 
     * @param key
     *            an object for which we need to acquire a lock.
     * @param force
     *            a flag indicating whether an existing lock, if any, should be
     *            broken.
     * @throws PolicyEditorException
     *             when the operation cannot complete or if the lock is already
     *             owned.
     */
    void acquireLock(IHasId object, boolean force) throws PolicyEditorException;

    /**
     * Releases the lock if held by the specified holder.
     * 
     * @param object
     *            the object the lock for which needs to be released.
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    void releaseLock(IHasId object) throws PolicyEditorException;
    
    void executePush() throws PolicyEditorException;
    
    void executePush(Date scheduleTime) throws PolicyEditorException;
    
    void login() throws LoginException;

}
