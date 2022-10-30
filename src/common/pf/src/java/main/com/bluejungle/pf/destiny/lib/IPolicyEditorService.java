package com.bluejungle.pf.destiny.lib;

/*
 * All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc. All rights reserved
 * worldwide.
 *
 * @author sergey
 *
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lib/IPolicyEditorService.java#30 $
 */

import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.pf.destiny.lifecycle.AttributeDescriptor;
import com.bluejungle.pf.destiny.lifecycle.CircularReferenceException;
import com.bluejungle.pf.destiny.lifecycle.DeploymentHistory;
import com.bluejungle.pf.destiny.lifecycle.DeploymentRecord;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.epicenter.action.IAction;
import com.bluejungle.destiny.services.policy.types.ExternalDataSourceConnectionInfo;
import com.bluejungle.destiny.services.policy.types.Realm;
import com.bluejungle.destiny.services.policy.types.ResourceTreeNode;
import com.bluejungle.destiny.services.policy.types.ResourceTreeNodeList;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * This interface defines the contract of the policy editor.
 */
public interface IPolicyEditorService {

    /**
     * Gets the configuration value specified by name.
     *
     * @param name
     *            the name of the configuration value to get.
     * @return the value of the configuration item, or null if the item is not
     *         set.
     * @throws PolicyServiceException
     *             when the service cannot get the configuration item because of
     *             an error.
     */
    String getConfigValue(String name) throws PolicyServiceException;

    /**
     * @param asOf
     *            as of date
     * @return total number of deployed policies as of the given date
     */
    int getNumDeployedPolicies(Date asOf) throws PolicyServiceException;

    /**
     * @param asOf
     *            as of date
     * @return the latest time of deployment up to and including the given asOf
     *         date
     */
    Date getLatestDeploymentTime(Date asOf) throws PolicyServiceException;

    /**
     * Given a name template, returns a <code>Collection</code> of
     * <code>DomainObjectDescriptor</code> objects.
     *
     * @param nameTemplate
     *            template (with wildcards) of an object name.
     * @param entityTypes
     *            types of the entities.
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects.
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    Collection<DomainObjectDescriptor> getDescriptorsForNameAndType(String nameTemplate, Collection<EntityType> entityTypes) throws PolicyServiceException;

    /**
     * Given a <code>Collection</code> of names and an <code>EntityType</code>,
     * returns a <code>Collection</code> of
     * <code>DomainObjectDescriptor</code> objects.
     *
     * @param names
     *            a <code>Collection</code> of <code>String</code> objects
     *            representing names of entities.
     * @param entityType
     *            type of the entity.
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects.
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    Collection<DomainObjectDescriptor> getDescriptorsForNamesAndType(Collection<String> names, EntityType entityType) throws PolicyServiceException;

    /**
     * Given a <code>Collection</code> of <code>Long</code> IDs, returns a
     * <code>Collection</code> of the correcponding <code>String</code>
     * objects with PQL.
     *
     * @param ids
     *            a <code>Collection</code> of <code>Long</code> IDs.
     * @return a <code>Collection</code> of <code>String</code> objects with
     *         PQL of the entities corresponding to the IDs.
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    Collection<String> getEntitiesForIds(Collection<Long> ids) throws PolicyServiceException;

    /**
     * Given a <code>Collection</code> of <code>names</code> of entities (essentially
     * the <code>EntityType</code> names), returns a <code>Collection</code> of the
     * corresponding <code>DODDigest</code> objects
     *
     * @param names a <code>Collection</code> of names
     */
    List<DODDigest> getDODDigests(Collection<String> names) throws PolicyServiceException;

    /**
     * Given a <code>Collection</code> of names and an <code>EntityType</code>,
     * returns a <code>Collection</code> of <code>String</code> objects witn
     * PQL representing domain objects.
     *
     * @param names
     *            a <code>Collection</code> of <code>String</code> objects
     *            representing names of entities.
     * @param entityType
     *            type of the entity.
     * @return a <code>Collection</code> of <code>String</code> objects witn
     *         PQL representing domain objects.
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    Collection<String> getEntitiesForNamesAndType(Collection<String> names, EntityType entityType) throws PolicyServiceException;

    /**
     * Given a name and a type, returns a <code>Collection</code> of
     * <code>String</code> objects of <code>referringType</code> that refer
     * to objects matching the name template and the specified entity type.
     *
     * @param nameTemplate template (with wildcards) of an object name.
     * @param entityType type of the entity for which we are finding the refernces.
     * @param referringType
     *            type of the referring entities, or <code>null</code>. When
     *            the value is <code>null</code>, all referring objects are
     *            returned regardless of their type.
     * @param onlyDirect
     *            when this falg is set to true, only direct references are
     *            returned; when false, a transitive closure of the referring
     *            objects is returned.
     * @return a <code>Collection</code> of <code>DomainObjectDescriptor</code> objects of
     *         <code>referringType</code> that refer to objects matching the
     *         name template and the specified entity type.
     * @throws PolicyServiceException when the operation cannot be completed.
     * @throws CircularReferenceException
     */
    Collection<DomainObjectDescriptor> getReferringObjectsForGroup(String nameTemplate, EntityType entityType, EntityType referringType, boolean onlyDirect) throws PolicyServiceException, CircularReferenceException;

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects and an "as of" <code>Date</code>, returns a
     * <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects for entities depending on the ones from the original collection
     * depended or will depend at the time specified by asOf, either directly or
     * indirectly. The descriptors represent deployed objects. NOTE:
     * Cross-dependencies within the original <code>Collection</code> are
     * ignored - for example, if the original collection is { A:(B,X,Y), B:(Z),
     * C:(Z) } only the {X,Y,Z} will be returned; B will not be included because
     * it is present in the original collection.
     *
     * @param descriptors
     *            a <code>Collection</code> of
     *            <code>DomainObjectDescriptor</code> objects for which we're
     *            finding the dependencies.
     * @param asOf
     *            the "as of" time for the historical/future dependencies.
     * @param onlyDirect
     *            when this falg is set to true, only direct references are
     *            returned; when false, a transitive closure of the referring
     *            objects is returned.
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects for entities on
     *         which the entities from the original collection depended or will
     *         depend at the time specified by asOf.
     * @throws PolicyServiceException
     *             when the operation cannot be completed.
     */
    Collection<DomainObjectDescriptor> getReferringObjectsAsOf(Collection<DomainObjectDescriptor> descriptors, Date asOf, boolean onlyDirect) throws PolicyServiceException, CircularReferenceException;

    /**
     * Given a <code>Collection</code> of <code>DODDigest</code>
     * objects, returns a <code>Collection</code> of
     * <code>DODDigest</code> objects for entities on which the
     * entities from the original collection depend, either directly or
     * indirectly. NOTE: Cross-dependencies within the original
     * <code>Collection</code> are ignored - for example, if the original
     * collection is { A:(B,X,Y), B:(Z), C:(Z) } only the {X,Y,Z} will be
     * returned; B will not be included because it is present in the original
     * collection.
     *
     * @param digests
     *            a <code>Collection</code> of
     *            <code>DODDigest</code> objects for which we're
     *            finding the dependencies.
     * @param directOnly
     *            return only "direct" dependencies
     * @return a <code>Collection</code> of
     *         <code>DODDigest</code> objects for entities on
     *         which the entities from the original collection depend.
     * @throws PolicyServiceException
     *             when the operation cannot be completed.
     */
    Collection<DODDigest> getDependenciesDigest(Collection<DODDigest> digests, boolean directOnly) throws PolicyServiceException, CircularReferenceException;

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects, returns a <code>Collection</code> of
     * <code>DomainObjectDescriptor</code> objects for entities on which the
     * entities from the original collection depend, either directly or
     * indirectly. NOTE: Cross-dependencies within the original
     * <code>Collection</code> are ignored - for example, if the original
     * collection is { A:(B,X,Y), B:(Z), C:(Z) } only the {X,Y,Z} will be
     * returned; B will not be included because it is present in the original
     * collection.
     *
     * @param descriptors
     *            a <code>Collection</code> of
     *            <code>DomainObjectDescriptor</code> objects for which we're
     *            finding the dependencies.
     * @param directOnly
     *            return only "direct" dependencies
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects for entities on
     *         which the entities from the original collection depend.
     * @throws PolicyServiceException
     *             when the operation cannot be completed.
     */
    Collection<DomainObjectDescriptor> getDependencies(Collection<DomainObjectDescriptor> descriptors, boolean directOnly) throws PolicyServiceException, CircularReferenceException;

    /**
     * Given an entity status, returns a <code>Collection</code> of
     * <code>DomainObjectDescriptor</code> objects in the specified state.
     *
     * @param status
     *            The development status of the object (empty, draft, approved,
     *            etc.)
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects in the specified
     *         state.
     * @throws PolicyServiceException
     *             when the operation cannot be completed.
     */
    Collection<DomainObjectDescriptor> getDescriptorsForState(DevelopmentStatus status) throws PolicyServiceException;

    /**
     * Given a <code>Collection</code> of <code>ID</code>s of
     * <code>DevelopmentEntity</code> objects and an "as of" <code>Date</code>,
     * returns a <code>Collection</code> of
     * <code>DomainObjectDescriptor</code> objects describing the deployed
     * objects at the time specified by the asOf.
     *
     * @param ids
     *            a <code>Collection</code> of IDs of
     *            <code>DevelopmentEntity</code> objects for which we need to
     *            obtain the deployed objects.
     * @param asOf
     *            the "as of" <code>Date</code> for which the deployed objects
     *            are to be examined.
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects describing the
     *         deployed objects at the time specified by asOf.
     * @throws PolicyServiceException
     *             when the operation cannot be completed.
     */
    Collection<DomainObjectDescriptor> getDeployedObjectDescriptors(Collection<Long> ids, Date asOf) throws PolicyServiceException;

    /**
     * Given a <code>Collection</code> of <code>ID</code>s of entities,
     * return a <code>Collection</code> of <code>DODDigest</code>s
     * describing these entities.  Accessibility information is not
     * filled in
     *
     * @param ids a <code>Collection</code> of ids
     * @return a <code>Collection</code> of <code>DODDigest</code> with these ids
     * @throws PolicyServiceException
     */
    Collection<DODDigest> getEntityDigestsForIDs(Collection<Long> ids) throws PolicyServiceException;

    /**
     * Given a <code>Collection</code> of <code>ID</code>s of entities,
     * return a <code>Collection</code> of <code>DomainObjectDescrptor</code>s
     * describign these entities.  The access information is not filled in.
     *
     * @param ids a <code>Collection</code> of ids
     * @return a <code>Collection</code> of <code>DomainObjectDescriptors</code> with these ids
     * @throws PolicyServiceException
     */
    Collection<DomainObjectDescriptor> getEntityDescriptorsForIDs(Collection<Long> ids) throws PolicyServiceException;

    /**
     * Given a <code>Collection</code> of IDs of
     * <code>DevelopmentEntity</code> objects and an "as of" <code>Date</code>,
     * returns a <code>Collection</code> of <code>String</code> objects
     * representing the state of the deployed objects at the time specified by
     * the asOf <code>Date</code>.
     *
     * @param ids
     *            a <code>Collection</code> of IDs of
     *            <code>DevelopmentEntity</code> objects for which we need to
     *            obtain the deployed objects.
     * @param asOf
     *            the "as of" <code>Date</code> for which the deployed objects
     *            are to be examined.
     * @return returns a <code>Collection</code> of <code>String</code>
     *         objects representing the PQL state of the deployed objects at the
     *         time specified by asOf.
     * @throws PolicyServiceException
     *             when the operation cannot be completed.
     */
    Collection<String> getDeployedObjects(Collection<Long> ids, Date asOf) throws PolicyServiceException;

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects and an "as of" <code>Date</code>, returns a
     * <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects for entities on which the entities from the original collection
     * depended or will depend at the time specified by asOf, either directly or
     * indirectly. The descriptors represent deployed objects. NOTE:
     * Cross-dependencies within the original <code>Collection</code> are
     * ignored - for example, if the original collection is { A:(B,X,Y), B:(Z),
     * C:(Z) } only the {X,Y,Z} will be returned; B will not be included because
     * it is present in the original collection.
     *
     * @param descriptors
     *            a <code>Collection</code> of
     *            <code>DomainObjectdescriptor</code> objects for which we're
     *            finding the dependencies.
     * @param asOf
     *            the "as of" time for the historical/future dependencies.
     * @return a <code>Collection</code> of
     *         <code>DomainObjectDescriptor</code> objects for entities on
     *         which the entities from the original collection depended or will
     *         depend at the time specified by asOf.
     * @throws PolicyServiceException
     *             when the operation cannot be completed.
     */
    Collection<DomainObjectDescriptor> getDependenciesAsOf(Collection<DomainObjectDescriptor> descriptors, Date asOf) throws PolicyServiceException, CircularReferenceException;

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
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    Collection<DomainObjectDescriptor> descriptorsForDeploymentRecord(DeploymentRecord record) throws PolicyServiceException;

    /**
     * Given a <code>DomainObjectDescriptor</code>, returns a
     * <code>Collection</code> of <code>TimeRelation</code> objects
     * representing the from and the to dates of deployment of the given object.
     *
     * @param descriptor
     *            a <code>DomainObjectDescriptor</code> of the object for
     *            which we need the deployment history.
     * @return a <code>Collection</code> of <code>TimeRelation</code>
     *         objects representing the from and the to dates of deployment of
     *         the given object.
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    Collection<DeploymentHistory> getDeploymentHistory(DomainObjectDescriptor descriptor) throws PolicyServiceException;

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects, schedules a deployment of all objects in the
     * <code>Collection</code> at the time specified by the "when" parameter.
     *
     * @param descriptors
     *            a <code>Collection</code> of
     *            <code>DomainObjectDescriptor</code> objects to be deployed.
     * @param when
     * @return a <code>DeploymentRecord</code> objects representing this
     * 		   deployment
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    DeploymentRecord scheduleDeployment(Collection<DomainObjectDescriptor> descriptors, Date when) throws PolicyServiceException;

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
     * @return a <code>DeploymentRecord</code> objects representing this
     * 		   deployment.
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    DeploymentRecord scheduleUndeployment(Collection<DomainObjectDescriptor> descriptors, Date when) throws PolicyServiceException;

    /**
     * Cancels a deployment action described by a <code>DeploymentRecod</code>.
     *
     * @param record
     *            the record describing the action to be canceled.
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    void cancelScheduledDeployment(DeploymentRecord record) throws PolicyServiceException;

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
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    Collection<DeploymentRecord> getDeploymentRecords(Date from, Date to) throws PolicyServiceException;

    /**
     * Returns a <code>Collection</code> of <code>Attributedescriptor</code>
     * objects for the specified type.
     *
     * @param type The type for which to get the <code>AttributeDescriptor</code>s.
     * @return a <code>Collection</code> of <code>AttributeDescriptor</code>
     * objects for the specified type.
     * @throws PolicyServiceException when the operation cannot complete.
     */
    Collection<AttributeDescriptor> getAttributeDescriptors(EntityType type) throws PolicyServiceException;

    /**
     * Returns a <code>Collection</code> of <code>LeafObject</code> objects
     * corresponding to collections of IDs.
     *
     * @param elementIds IDs of elements to retrieve.
     * @param userGroupIds IDs of user groups.
     * @param hostGroupIds IDs of host groups.
     * @return a <code>Collection</code> of <code>LeafObject</code> objects
     * of the specified type.
     * @throws PolicyServiceException when the operation cannot complete.
     */
    Collection<LeafObject> getLeafObjectsForIds(long[] elementIds, long[] userGroupIds, long[] hostGroupIds) throws PolicyServiceException;

    /**
     * Returns a <code>Collection</code> of <code>LeafObject</code> objects
     * of the specified type.
     *
     * @param filter an <code>IPredicate</code> defining the query.
     * @param namespaceId a leaf object namespace id
     * @param type the type of the desired <code>LeafObject</code>s.
     * @param limit the max number of leaf objects to return.
     * @return a <code>Collection</code> of <code>LeafObject</code>
     * objects of the specified type.
     * @throws PolicyServiceException when the operation cannot complete.
     */
    Collection<LeafObject> queryLeafObjects(IPredicate filter, String namespaceId, LeafObjectType type, int limit) throws PolicyServiceException;

    /**
     * Given a subject descriptor, returns all leaf objects matching the
     * predicate.
     *
     * @param descr
     *            the subject descriptor for the preview.
     * @return a <code>Collection</code> of <code>LeafObject</code> objects
     *         representing subjects matching the predicate of the given
     *         descriptor.
     * @throws PolicyServiceException
     *             when the predicat cannot be evaluated.
     * @throws CircularReferenceException
     */
    Collection<LeafObject> getMatchingSubjects(DomainObjectDescriptor descr) throws PolicyServiceException, CircularReferenceException;

    /**
     * Given an action descriptor, returns all leaf objects matching the
     * predicate.
     *
     * @param descr
     *            the action descriptor for the preview.
     * @return a <code>Collection</code> of <code>LeafObject</code> objects
     *         representing the leaf actions referenced by the predicate of the
     *         descriptor.
     * @throws PolicyServiceException
     *             when the predicat cannot be evaluated.
     * @throws CircularReferenceException
     */
    Collection<LeafObject> getMatchingActions(DomainObjectDescriptor descr) throws PolicyServiceException, CircularReferenceException;

    /**
     * Given a descriptor, returns a fully resolved version of the object.
     *
     * @param descr
     *            a descriptor of the object to be fully resolved.
     * @return a fully resolved object identified by the descriptor.
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     * @throws CircularReferenceException
     */
    String getFullyResolvedEntity(DomainObjectDescriptor descr) throws PolicyServiceException, CircularReferenceException;

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
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    Collection<DomainObjectDescriptor> ensureOperationIsAllowed(Collection<DomainObjectDescriptor> descriptors, IAction action) throws PolicyServiceException;

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
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    Collection<? extends IAction> allowedActions(Collection<DomainObjectDescriptor> descr) throws PolicyServiceException;

    /**
     * Returns a <code>Collection</code> of <code>AgentStatusDescriptor</code>
     * objects describing agents where the given policy or component is
     * deployed.
     *
     * @param obj
     *            a descriptor of the component or policy.
     * @param asOf
     *            the date when the given component or policy became active.
     * @param agentList
     *            a <code>Collection</code> of
     *            <code>AgentStatusDescriptor</code> objects for all agents.
     * @return a <code>Collection</code> of <code>AgentStatusDescriptor</code>
     *         objects describing agents where the given policy or component is
     *         deployed.
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    Collection<AgentStatusDescriptor> getAgentsForDeployedObject(Collection<AgentStatusDescriptor> agentList, DomainObjectDescriptor obj, Date asOf) throws PolicyServiceException;

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
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    Collection<DomainObjectDescriptor> deploymentStatusForAgent(AgentStatusDescriptor agent) throws PolicyServiceException;

    /**
     * Given a <code>Collection</code> of <code>AgentStatusDescriptor</code>
     * objects, sets the counts of policies and components of each descriptor to
     * the correct value.
     *
     * @param agents
     *            a <code>Collection</code> of
     *            <code>AgentStatusDescriptor</code> objects.
     * @return a <code>Collection</code> of <code>AgentStatusDescriptor</code>
     *         objects with the counts set to correct values.
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    Collection<AgentStatusDescriptor> addCountsToAgentDescriptors(Collection<AgentStatusDescriptor> agents) throws PolicyServiceException;

    /**
     * Saves the entities represented in <code>PQL</code>, and returns a list
     * of DODDigests that correspond to the original entities, in the same order,
     * as appeared in the <code>PQL</code> string
     *
     * @param pql the <code>PQL</code> string.
     * @return a <code>Collection</code> of <code>DODDigest</code>s
     *  of the entities in the same order they are listed in the <code>PQL</code> String.
     * @throws PolicyServiceException when the operation cannot complete.
     */
    List<DODDigest> saveEntitiesDigest(String pql) throws PolicyServiceException;

    /**
     * Saves the entities represented in <code>PQL</code>, and returns a list
     * of IDs of the original entities, in the same order they appear in the
     * <code>PQL</code> string.
     *
     * @param pql the <code>PQL</code> string.
     * @return a <code>Collection</code> of <code>DomainObjectDescriptor</code>s
     *  of the entities in the same order they are listed in the <code>PQL</code> String.
     * @throws PolicyServiceException when the operation cannot complete.
     */
    List<DomainObjectDescriptor> saveEntities(String pql) throws PolicyServiceException;

    /**
     * Returns a <code>String</code> with the name of the holder of an object
     * lock, or null if the object is not locked.
     *
     * @param key an object to check for being locked or unlocked.
     * @return a <code>String</code> with the name of the holder of the lock,
     *         or null if the object is not locked.
     */
    String getLockHolder(Object key);

    /**
     * Tries to acquire a lock for an object.
     *
     * @param key an object for which we need to acquire a lock.
     * @param force a flag indicating whether an existing lock, if any, should be broken.
     * @return the name of the current or the new holder of the lock.
     */
    String acquireLock(Object key, boolean force) throws PolicyServiceException;

    /**
     * Releases the lock if held by the specified holder.
     *
     * @param key the object the lock for which needs to be released.
     * @return the name of the current holder of the lock, or null if the object
     * is not (or no longer) locked.
     */
    String releaseLock(Object key) throws PolicyServiceException;

    /**
     * Prepares the system for running unit tests.
     *
     * @throws PolicyServiceException
     *             when the system cannot be prepared to run the unit tests.
     */
    void prepareForTests() throws PolicyServiceException;

    /**
     * Determine if there are objects ready to deploy
     *
     * @return true if there are objects ready to deploy; false otherwise
     * @throws PolicyServiceException if an error occurs while determining result.
     */
    boolean hasObjectsToDeploy() throws PolicyServiceException;

    /**
     * Returns a list of <code>DomainObjectUsage</code> objects for the
     * specified <code>List</code> of <code>DomainObjectDescriptor</code> objects.
     * @param descriptors a list of <code>DomainObjectDescriptor</code> objects
     * for which we need the usage data.
     * @return a <code>List</code> of <code>DomainObjectUsage</code> objects
     * in the same order as the <code>DomainObjectDescriptor</code> in the
     * original list.
     * @throws PolicyServiceException if an error occurs while determining result.
     */
    List<DomainObjectUsage> getUsageList( List<DomainObjectDescriptor> descriptors ) throws PolicyServiceException;

    /**
     * Returns a list of <code>DomainObjectUsage</code> objects for the
     * specified <code>List</code> of ids.
     * @param descriptors a list of ids, respresenting objectsfor which we need the usage data.
     * @return a <code>List</code> of <code>DomainObjectUsage</code> objects
     * in the same order as the ids in the original list.
     * @throws PolicyServiceException if an error occurs while determining result.
     */
    List<DomainObjectUsage> getUsageListById( List<Long> ids ) throws PolicyServiceException;

    /**
     * Retrieve the set of the current Dictionary Enrollment names
     * 
     * @return the set of the current Dictionary Enrollment names
     * @throws PolicyServiceException
     */
    Set<String> getDictionaryEnrollmentNames() throws PolicyServiceException;

    /**
     * Retrieve the set of the current Dictionary Enrollment names
     * 
     * @return the set of the current Dictionary Enrollment names
     * @throws PolicyServiceException
     */
    Set<Realm> getDictionaryEnrollmentRealms() throws PolicyServiceException;

    /**
     * Create external data source URL List
     * @param info
     * @throws PolicyServiceException
     * @see com.bluejungle.destiny.services.policy.PolicyEditorIF#getPortalURLList()
     */
    public String[] getPortalURLList() throws PolicyServiceException;
    
    /**
     * Create external data source
     * @param info
     * @throws PolicyServiceException
     * @see com.bluejungle.destiny.services.policy.PolicyEditorIF#createExternalDataSource(com.bluejungle.destiny.services.policy.types.ExternalDataSourceConnectionInfo)
     */
    public int createExternalDataSource(ExternalDataSourceConnectionInfo info) throws PolicyServiceException;
    
    /**
     * Fetch the children of given resource tree node
     * 
     * if the input node.value is null, fetch the root node
     * else fetch all children and set node.value with children updated
     * 
     * @see com.bluejungle.destiny.services.policy.PolicyEditorIF#getResourceTreeNodeChildren(com.bluejungle.destiny.services.policy.types.ResourceTreeNode)
     */
    public ResourceTreeNode getResourceTreeNodeChildren(int resourceID, ResourceTreeNode node) throws PolicyServiceException;
    

    /**
     * get portal resource preview from external data source
     * @param resourceID
     * @param descriptor
     * @return list of resource tree node
     * @throws PolicyServiceException
     */
    public ResourceTreeNodeList getPortalResourcePreview(int resourceID, DomainObjectDescriptor descriptor) throws PolicyServiceException;
}
