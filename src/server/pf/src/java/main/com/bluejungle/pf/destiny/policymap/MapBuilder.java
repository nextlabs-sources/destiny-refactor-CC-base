package com.bluejungle.pf.destiny.policymap;

/*
 * Created on July 21, 2006
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by NextLabs Inc.,
 * San Mateo, CA. Ownership remains with NextLabs Inc.
 * All rights reserved worldwide.
 * @author sasha, sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/destiny/policymap/MapBuilder.java#1 $:
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.UTFDataFormatException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IMElementBase;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Predicates;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.expressions.Predicates.DefaultTransformer;
import com.bluejungle.framework.expressions.Predicates.ITransformer;
import com.bluejungle.framework.utils.IStreamable;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lib.DictionaryHelper;
import com.bluejungle.pf.destiny.parser.IHasPQL;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.action.IDActionManager;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecRef;
import com.bluejungle.pf.domain.destiny.common.ServerSpecManager;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectSpec;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.engine.destiny.EvaluationRequest;

/**
 * This class builds maps on a separate thread.
 */

class MapBuilder implements Runnable {
    private final Log log = LogFactory.getLog(MapBuilder.class.getName());
    private IDActionManager actionManager;
    private final IElementType appType;
    private final StaticAttributeTracker attrTracker;
    private final ServerSpecManager specManager;
    private final IEnrollment[] domains;
    private static final IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
    private final IDictionary dictionary;
    private final DictionaryHelper dictHelper;
    private final Date consistentTime;
    private final STRLog strLog;
    private IElementType userType;
    private IElementType contactType;
    private IElementType hostType;
    private SortedMap<Long,ReferenceResolver.IParsedEntity> policyById;

    // Communicate back to caller
    private boolean successfulRun = true;
    private Throwable failureException = null;

    // Working variables
    private static final long ACCEPTABLE_MAP_BUILD_LATENCY_MS = 1000 * 60 * 5;

    private static final Map<IExpression,ResourceAttribute> resourceToUserAttribute = new HashMap<IExpression, ResourceAttribute>();

    private final ReferenceResolver referenceResolver;

    static {
        resourceToUserAttribute.put(ResourceAttribute.OWNER, ResourceAttribute.OWNER_LDAP_GROUP);
        resourceToUserAttribute.put(ResourceAttribute.OWNER_GROUP, ResourceAttribute.OWNER_LDAP_GROUP);
        resourceToUserAttribute.put(ResourceAttribute.OWNER_LDAP_GROUP, ResourceAttribute.OWNER_LDAP_GROUP);
        resourceToUserAttribute.put(ResourceAttribute.PORTAL_CREATED_BY, ResourceAttribute.PORTAL_CREATED_BY_LDAP_GROUP);
        resourceToUserAttribute.put(ResourceAttribute.PORTAL_CREATED_BY_GROUP, ResourceAttribute.PORTAL_CREATED_BY_LDAP_GROUP);
        resourceToUserAttribute.put(ResourceAttribute.PORTAL_CREATED_BY_LDAP_GROUP, ResourceAttribute.PORTAL_CREATED_BY_LDAP_GROUP);
        resourceToUserAttribute.put(ResourceAttribute.PORTAL_MODIFIED_BY, ResourceAttribute.PORTAL_MODIFIED_BY_LDAP_GROUP);
        resourceToUserAttribute.put(ResourceAttribute.PORTAL_MODIFIED_BY_GROUP, ResourceAttribute.PORTAL_MODIFIED_BY_LDAP_GROUP);
        resourceToUserAttribute.put(ResourceAttribute.PORTAL_MODIFIED_BY_LDAP_GROUP, ResourceAttribute.PORTAL_MODIFIED_BY_LDAP_GROUP);
    }

    private static final Set<IExpression> ldapGroupAttributes = new HashSet<IExpression>();

    static {
        ldapGroupAttributes.add(ResourceAttribute.OWNER_LDAP_GROUP);
        ldapGroupAttributes.add(ResourceAttribute.PORTAL_CREATED_BY_LDAP_GROUP);
        ldapGroupAttributes.add(ResourceAttribute.PORTAL_MODIFIED_BY_LDAP_GROUP);
    }

    private static final Set<IExpression> userRefAttributes = new HashSet<IExpression>();

    static {
        userRefAttributes.add(ResourceAttribute.OWNER);
        userRefAttributes.add(ResourceAttribute.PORTAL_CREATED_BY);
        userRefAttributes.add(ResourceAttribute.PORTAL_MODIFIED_BY);
    }

    MapBuilder(Collection<? extends IHasPQL> deployedEntities, STRLog strLog) {
        if (strLog == null) {
            throw new NullPointerException("strLog");
        }
       
        this.strLog = strLog;

        referenceResolver = new ReferenceResolver(deployedEntities);

        String[] domainNames = strLog.getDomains();
        if (domainNames == null) {
            throw new NullPointerException("domainNames");
        } else {
            domains = referenceResolver.initDomains(domainNames);
        }
        
        specManager = componentManager.getComponent(ServerSpecManager.COMP_INFO);
        actionManager = componentManager.getComponent(IDActionManager.COMP_INFO);
        attrTracker = componentManager.getComponent(StaticAttributeTracker.COMP_INFO);
        dictionary = componentManager.getComponent( Dictionary.COMP_INFO );
        dictHelper = new DictionaryHelper(specManager, dictionary);
        try {
            consistentTime = dictionary.getLatestConsistentTime();
        } catch ( DictionaryException e ) {
            throw new IllegalStateException("Unable to get a consistent time from the dictionary: "+e.getMessage());
        }
      
        try {
            userType = dictionary.getType(ElementTypeEnumType.USER.getName());
            contactType = dictionary.getType(ElementTypeEnumType.CONTACT.getName());
            hostType = dictionary.getType(ElementTypeEnumType.COMPUTER.getName());
            appType = dictionary.getType(ElementTypeEnumType.APPLICATION.getName());
        } catch (DictionaryException de) {
            throw new IllegalStateException("Dictionary does not contain a definition for a required type: "+de.getMessage());
        }
    }

    /**
     * This is the actual method that performs the map building.
     * @param session the Hibernate session to which the changes
     * are saved.
     * @throws PQLException if deployment entities contain entries
     * with invalid PQL.
     * @throws HibernateException if a Hibernate-related action fails.
     * @throws DictionaryException if a dictionary-related action fails.
     */
    private void doBuild(Session session) throws PQLException, HibernateException, DictionaryException {
        // Make sure that the attribute information (static/dynamic) is up to date
        attrTracker.update();

        referenceResolver.resolve();

        policyById = referenceResolver.getPolicyById();
        Map<Long,BitSet> actionMappings = new StreamableLongMap();
        Map<String,BitSet> actionNameMappings = new StreamableStringMap();

        int i = 0;

        // Build action-to-policy mapping
        for (Object actionObj : actionManager.allActions()) {
            IDAction action = (IDAction)actionObj;
            EvaluationRequest request = new EvaluationRequest();
            request.setAction(action);
            BitSet applicable = new BitSet();
            i = 0;
            for ( Map.Entry<Long,ReferenceResolver.IParsedEntity> entry : policyById.entrySet() ) {
                ReferenceResolver.IParsedEntity parsed = entry.getValue();
                IDPolicy policy = (IDPolicy)parsed.getParsed();
                if (policy.getTarget().getActionPred().match(request)) {
                    applicable.set(i);
                }
                i++;
            }
            actionMappings.put(action.getId(), applicable);
            actionNameMappings.put(action.getName(), applicable);
        }

        ISubjectMappings subjMappings = new SubjectMappings();

        // Build application-to-policy mapping
        BitSet policiesForAllApps = new BitSet();
        BitSet groupsOfUnknownApps = new BitSet();
        BitSet policiesOfUnknownApps = new BitSet();
        buildSubjectMapping(
            dictionary.condition(appType)
        ,   SubjectType.APP
        ,   subjMappings
        ,   policiesForAllApps
        ,   groupsOfUnknownApps
        ,   policiesOfUnknownApps
            );

        // Build a composite domain-based condition
        IPredicate domainCondition;
        if (domains.length > 1) {
            List<IPredicate> conditions = new ArrayList<IPredicate>(domains.length);
            for ( i = 0 ; i != domains.length ; i++ ) {
                conditions.add(dictionary.condition(domains[i]));
            }
            domainCondition = new CompositePredicate(BooleanOp.OR, conditions);
        } else if (domains.length == 1) {
            domainCondition = dictionary.condition(domains[0]);
        } else {
            domainCondition = PredicateConstants.FALSE;
        }

        // Build user-to-policy mapping
        BitSet policiesForAllUsers = new BitSet();
        BitSet groupsOfUnknownUsers = new BitSet();
        BitSet policiesOfUnknownUsers = new BitSet();

        buildSubjectMapping(
            new CompositePredicate(
                BooleanOp.OR
            ,   Arrays.asList(
                dictionary.condition(contactType)
            ,   dictionary.condition(userType)
                )
                )
        ,   SubjectType.USER
        ,   subjMappings
        ,   policiesForAllUsers
        ,   groupsOfUnknownUsers
        ,   policiesOfUnknownUsers
            );

        // Build host-to-policy mapping
        BitSet policiesForAllHosts = new BitSet();
        BitSet groupsOfUnknownHosts = new BitSet();
        BitSet policiesOfUnknownHosts = new BitSet();
        buildSubjectMapping(
            new CompositePredicate(
                BooleanOp.AND
            ,   Arrays.asList(
                dictionary.condition(hostType)
            ,   domainCondition
                )
                )
        ,   SubjectType.HOST
        ,   subjMappings
        ,   policiesForAllHosts
        ,   groupsOfUnknownHosts
        ,   policiesOfUnknownHosts
            );

        // Add mappings for unknown subjects
        subjMappings.addMappings(
            new long[] {
                IDeploymentBundle.KEY_OF_UNKNOWN_APPLICATION
                ,   IDeploymentBundle.KEY_OF_UNKNOWN_USER
                ,   IDeploymentBundle.KEY_OF_UNKNOWN_HOST
            }
        ,   new BitSet[] {
            policiesOfUnknownApps
            ,   policiesOfUnknownUsers
            ,   policiesOfUnknownHosts
        }
    ,   new BitSet[] {
        groupsOfUnknownApps
        ,   groupsOfUnknownUsers
        ,   groupsOfUnknownHosts
    }
,   null
,   null
            );

        // Store preprocessed policies
        DomainObjectFormatter dof = new DomainObjectFormatter();
        List<String> policies = new StreamableList();
        for ( Map.Entry<Long,ReferenceResolver.IParsedEntity> entry : policyById.entrySet() ) {
            IDPolicy policy = (IDPolicy)entry.getValue().getParsed();
            dof.reset();
            dof.formatPolicyDef(entry.getKey(), policy);
            policies.add(dof.getPQL());
        }

        // Compose the resolution parts of strLog
        strLog.setActionMappings(actionMappings);
        strLog.setActionNameMappings(actionNameMappings);
        strLog.setSubjectMappings(subjMappings);
        strLog.setPolicies(policies);
        strLog.setPoliciesForAllApps(policiesForAllApps);
        strLog.setPoliciesForAllUsers(policiesForAllUsers);
        strLog.setPoliciesForAllHosts(policiesForAllHosts);
    }

    /**
     * This method builds policy mappings for subjects of the specified type,
     * and returns a <code>BitSet</code> representing policies that apply to all
     * subjects of the given type.
     *
     * @param allSubjects an <code>IPredicate</code> that defines the subset of
     * subjects to consider in this method. For users and hosts this condition is
     * based on the required domains; for application this condition is
     * a constant <code>true</code>.
     * @param subjType The subject type for which to build the mapping.
     * @param session Hibernate session on which to save the mapping.
     * @return a <code>BitSet</code> representing policies that apply to all
     * subjects of the given type.
     * @throws DictionaryException when the operation cannot complete because of
     * an error related to using the dictionary.
     * @throws HibernateException when the operation cannot complete because of
     * an error related to using the database.
     */
    private void buildSubjectMapping(
        final IPredicate allSubjects
    ,   final SubjectType subjType
    ,   ISubjectMappings subjectMappings
    ,   BitSet forAllSubjectsReturn
    ,   final BitSet groupsForUnknownSubjects
    ,   BitSet policiesForUnknownSubjects
        ) throws DictionaryException, HibernateException {
        List<Long> subjIdList = new ArrayList<Long>();
        IDictionaryIterator<Long> subjIter = dictionary.queryKeys(allSubjects, consistentTime);
        try {
            while (subjIter.hasNext()) {
                subjIdList.add(subjIter.next());
            }
        } finally {
            subjIter.close();
        }
        // Prepare "parallel" arrays of subject IDs and applicable policy BitSets
        final long[] subjIds = new long[subjIdList.size()];
        final BitSet[] applicable = new BitSet[subjIdList.size()];
        final BitSet[] groups = new BitSet[subjIdList.size()];
        for ( int i = 0 ; i != subjIds.length ; i++ ) {
            subjIds[i] = (subjIdList.get(i)).longValue();
            applicable[i] = new BitSet(policyById.size());
            groups[i] = new BitSet();
        }
        subjIdList = null; // release the memory

        // This transformer removes attributes referencing types
        // other than subjType, and ignores all dynamic attributes.
        final Predicates.ITransformer keepOnlyStaticAttributesOfThisType = new Predicates.DefaultTransformer() {
            /**
             * @see DefaultTransformer#transformRelation(IRelation)
             */
            public IPredicate transformRelation(IRelation rel) {
                if (isDynamicOrUnrelated(rel.getLHS()) || isDynamicOrUnrelated(rel.getRHS())) {
                    return null;
                } else {
                    return rel;
                }
            }
            /**
             * This method returns true if the relation needs to be removed:
             * Relations on attributes of unrelated types and
             * relations on dynamic attributes need to be removed.
             * @param attr the <code>IExpression</code> to check.
             * @return true if the attribute is to be removed, false otherwise.
             */
            private boolean isDynamicOrUnrelated(IExpression attr) {
                if (attr instanceof IDSubjectAttribute) {
                    IDSubjectAttribute subjAttr = (IDSubjectAttribute)attr;
                    return subjAttr.getSubjectType() != subjType || attrTracker.isDynamic(subjAttr);
                } else {
                    return false;
                }
            }
        };

        Predicates.IDetector findDynamicAttributesOfType = new Predicates.DefaultDetector() {
            public boolean checkRelation(IRelation rel) {
                return isRelatedDynamic(rel.getLHS()) || isRelatedDynamic(rel.getRHS());
            }

            private boolean isRelatedDynamic(IExpression attr) {
                if (attr instanceof IDSubjectAttribute) {
                    IDSubjectAttribute subjAttr = (IDSubjectAttribute)attr;
                    return subjAttr.getSubjectType() == subjType && attrTracker.isDynamic(subjAttr);
                } else {
                    return false;
                }
            }
        };

        final Predicates.ITransformer buildSubjectGroups = new Predicates.DefaultTransformer() {
            private final Map<IPredicate,IPredicate> cache = new HashMap<IPredicate,IPredicate>();
            private final SpecAttribute groupAttr = SubjectAttribute.getGroupAttribute(subjType);
            private IPredicate singleSubjectPredicate = null;
            private int currentGroupIndex = 0;
            private final Predicates.IDetector wrongSubjectDetector = new Predicates.DefaultDetector() {
                // This detector determines whether or not a predicate
                // contains references to attributes of unrelated types.
                public boolean checkRelation(IRelation rel) {
                    if (rel.getLHS() instanceof IAttribute || rel.getRHS() instanceof IAttribute) {
                        // An expression contains attributes
                        if (rel.getLHS() instanceof IDSubjectAttribute || rel.getRHS() instanceof IDSubjectAttribute) {
                            // An expression contains a subject attribute
                            if (rel.getLHS() instanceof IDSubjectAttribute && checkSubjectAttribute((IDSubjectAttribute)rel.getLHS())) {
                                return true;
                            }
                            if (rel.getRHS() instanceof IDSubjectAttribute && checkSubjectAttribute((IDSubjectAttribute)rel.getRHS())) {
                                return true;
                            }
                        } else {
                            // An expression contains a non-subject attribute
                            return true;
                        }
                    }
                    return false;
                }
                boolean checkSubjectAttribute(IDSubjectAttribute attr) {
                    return attr.getSubjectType() != subjType || attrTracker.isDynamic(attr);
                }
            };
            /**
             * @see DefaultTransformer#transformCompositeStart(ICompositePredicate)
             */
            public void transformCompositeStart(ICompositePredicate pred) {
                if (singleSubjectPredicate != null) {
                    return;
                }
                if (!Predicates.find(pred, wrongSubjectDetector)) {
                    singleSubjectPredicate = pred;
                }
            }
            /**
             * @see DefaultTransformer#transformCompositeEnd(ICompositePredicate, IPredicate)
             */
            public IPredicate transformCompositeEnd(ICompositePredicate orig, IPredicate converted) {
                IPredicate res;
                if (singleSubjectPredicate == orig) {
                    singleSubjectPredicate = null;
                    res = convertSubjectToSyntheticGroup(converted);
                } else {
                    res = converted;
                }
                return res;
            }
            /**
             * @see DefaultTransformer#transformRelation(IRelation)
             */
            public IPredicate transformRelation(IRelation rel) {
                if (singleSubjectPredicate != null) {
                    return rel;
                }
                if ((subjType == SubjectType.USER) && (ldapGroupAttributes.contains(rel.getLHS()) || ldapGroupAttributes.contains(rel.getRHS()))) {
                    return convertLdapGroupRefToSyntheticGroup(rel);
                } else if ((subjType == SubjectType.USER) && (ReferenceResolver.userGroupAttributes.contains(rel.getLHS()) ||
                                                              ReferenceResolver.userGroupAttributes.contains(rel.getRHS()))) {
                    return convertGroupRefToSyntheticGroup(rel);
                } else if ((subjType == SubjectType.USER) && (userRefAttributes.contains(rel.getLHS()) ||
                                                              userRefAttributes.contains(rel.getRHS()))) {
                    return convertUserRefToSyntheticGroup(rel);
                } else if (isStaticAttributeOfThisType(rel.getLHS()) || isStaticAttributeOfThisType(rel.getRHS())) {
                    return convertSubjectToSyntheticGroup(rel);
                } else {
                    return rel;
                }
            }
            /**
             * Converts the given predicate to a synthetic group predicate.
             * @param pred the predicate to convert.
             * @return the resulting synthetic group.
             */
            private IPredicate convertSubjectToSyntheticGroup(IPredicate pred) {
                if (cache.containsKey(pred)) {
                    return cache.get(pred);
                }
                int index = getIndexOfNextGroup();
                IPredicate res = groupAttr.buildRelation(RelationOp.HAS, groupAttr.build(""+index));
                IPredicate queryPred = Predicates.transform(pred, keepOnlyStaticAttributesOfThisType);
                if (queryPred == null) {
                    queryPred = PredicateConstants.TRUE;
                }
                try {
                    IDictionaryIterator<Long> subjIter = dictionary.queryKeys(
                        new CompositePredicate(
                            BooleanOp.AND
                        ,   Arrays.asList(
                            allSubjects
                        ,   dictHelper.toDictionaryPredicate(queryPred, null, subjType)
                            )
                            )
                    ,   consistentTime
                        );
                    addToAllSets(subjIter, subjIds, groups, index);
                } catch (DictionaryException e) {
                    log.error("Error building subject groups: "+e.getMessage());
                }
                cache.put(pred, res);
                IPredicate forUnknown = Predicates.transform(pred, unknownSubjectTransformer);
                try {
                    if (forUnknown.match(null)) {
                        groupsForUnknownSubjects.set(index);
                    }
                } catch (Exception ignored) {
                }
                return res;
            }

            /**
             * Converts a relation of a single user reference to a group-based predicate.
             * @param rel the relation to convert.
             * @return the resulting synthetic group.
             */
            private IPredicate convertUserRefToSyntheticGroup(IRelation rel) {
                if (cache.containsKey(rel)) {
                    return cache.get(rel);
                }

                final IExpression userName;
                final IExpression userAttribute;

                if (userRefAttributes.contains(rel.getLHS())) {
                    userName = rel.getRHS();
                    userAttribute = resourceToUserAttribute.get(rel.getLHS());
                } else {
                    userName = rel.getLHS();
                    userAttribute = resourceToUserAttribute.get(rel.getRHS());
                }

                if (!(userName instanceof Constant)) {
                    return rel;
                }

                // If the operator is == and the user is not found in the domains
                // for which we are building maps, the result is a constant FALSE.
                // If the operator is != and the user is not found in the domains
                // for which we are building maps, the result is a constant TRUE.
                IPredicate res = (rel.getOp() == RelationOp.EQUALS) ?
                                 PredicateConstants.FALSE
                                 :   PredicateConstants.TRUE;

                try {
                    String nameConst = (String)((Constant)userName).getValue().getValue();
                    IMElementBase name = dictionary.getByUniqueName(nameConst, consistentTime);
                    if (name != null) {
                        long id = name.getInternalKey().longValue();
                        int i = Arrays.binarySearch(subjIds, id);
                        if (i >= 0) {
                            int index = getIndexOfNextGroup();
                            res = new Relation(
                                equalityToContainment(rel.getOp())
                            ,   userAttribute
                            ,   Constant.build(index)
                                );
                            groups[i].set(index);
                        }
                    } else {
                        log.error(
                            "Resource component references an unknown user: '"
                        +   nameConst
                        +   "'"
                            );
                    }
                } catch (DictionaryException e) {
                    log.error("Error building solo user group: "+e.getMessage());
                }
                cache.put(rel, res);

                return res;
            }

            /**
             * Converts a relation of a resource LDAP user to a group-based predicate.
             * @param rel the relation to convert.
             * @return the resulting synthetic group.
             */
            private IPredicate convertLdapGroupRefToSyntheticGroup(IRelation rel) {
                if (cache.containsKey(rel)) {
                    return cache.get(rel);
                }
                final IExpression groupNameExpr;
                final IExpression userAttribute;
                if (ldapGroupAttributes.contains(rel.getLHS())) {
                    groupNameExpr = rel.getRHS();
                    userAttribute = resourceToUserAttribute.get(rel.getLHS());
                } else {
                    groupNameExpr = rel.getLHS();
                    userAttribute = resourceToUserAttribute.get(rel.getRHS());
                }
                if (!(groupNameExpr instanceof Constant)) {
                    throw new IllegalArgumentException(
                        "Found an user LDAP group attribute in a relation to non-constant."
                        );
                }
                int index = getIndexOfNextGroup();
                IPredicate res = new Relation(
                    // No conversion of equality to containment here,
                    // because the predicate that we pass to the dictionary
                    // already factors in the relation's operator.
                    RelationOp.HAS
                ,   userAttribute
                ,   Constant.build(index)
                    );
                if (rel.getOp() == RelationOp.NOT_EQUALS) {
                    groupsForUnknownSubjects.set(index);
                }
                try {
                    IDictionaryIterator<Long> subjIter = dictionary.queryKeys(
                        new CompositePredicate(
                            BooleanOp.AND
                        ,   Arrays.asList(
                            allSubjects
                        ,   dictHelper.toDictionaryPredicate(
                            new Relation(
                                rel.getOp()
                            ,   SubjectAttribute.USER_LDAP_GROUP
                            ,   groupNameExpr
                                )
                        ,   null
                        ,   SubjectType.USER
                            )
                            )
                            )
                    ,   consistentTime
                        );
                    addToAllSets(subjIter, subjIds, groups, index);
                } catch (DictionaryException e) {
                    log.error("Error building subject groups: "+e.getMessage());
                }
                cache.put(rel, res);
                return res;
            }

            /**
             * Converts a relation of a resource user group to a group-based predicate.
             * @param rel the relation to convert.
             * @return the resulting synthetic group.
             */
            private IPredicate convertGroupRefToSyntheticGroup(IRelation rel) {
                if (cache.containsKey(rel)) {
                    return cache.get(rel);
                }

                final IExpression specRefExpr;
                final IExpression userAttribute;

                if (ReferenceResolver.userGroupAttributes.contains(rel.getLHS())) {
                    specRefExpr = rel.getRHS();
                    userAttribute = resourceToUserAttribute.get(rel.getLHS());
                } else {
                    specRefExpr = rel.getLHS();
                    userAttribute = resourceToUserAttribute.get(rel.getRHS());
                }

                int index = getIndexOfNextGroup();
                IPredicate res = new Relation(
                    equalityToContainment(rel.getOp())
                ,   userAttribute
                ,   Constant.build(index)
                    );

                if (!(specRefExpr instanceof ReferenceResolver.PredicateReferenceExpression)) {
                    throw new IllegalArgumentException(
                        "Found a user group attribute in a relation to expression other than a spec reference"
                        );
                }
                IPredicate pred = ((ReferenceResolver.PredicateReferenceExpression)specRefExpr).getReferencedPredicate();
                IPredicate queryPred = Predicates.transform(
                    pred
                ,   keepOnlyStaticAttributesOfThisType
                    );
                if (queryPred == null) {
                    queryPred = PredicateConstants.TRUE;
                }
                try {
                    IDictionaryIterator<Long> subjIter = dictionary.queryKeys(
                        new CompositePredicate(
                            BooleanOp.AND
                        ,    Arrays.asList(
                            allSubjects
                        ,   dictHelper.toDictionaryPredicate(
                            queryPred
                        ,   null
                        ,   SubjectType.USER
                            )
                            )
                            )
                    ,   consistentTime
                        );
                    addToAllSets(subjIter, subjIds, groups, index);
                } catch (DictionaryException e) {
                    log.error("Error building subject groups: "+e.getMessage());
                }
                cache.put(rel, res);
                IPredicate forUnknown = Predicates.transform(pred, unknownSubjectTransformer);
                try {
                    if (forUnknown.match(null)) {
                        groupsForUnknownSubjects.set(index);
                    }
                } catch (Exception ignored) {
                }
                return res;
            }
            /**
             * Returns <code>true</code> if the expression is a static attribute
             * of the correct subject type, or a resource attribute related to
             * the current subject type; <code>false</code> otherwise.
             * @param expr the expression to check for being a subject attribute.
             * @return <code>true</code> if the expression is a static attribute
             * of the correct subject type, or a resource attribute related to
             * the current subject type; <code>false</code> otherwise.
             */
            private boolean isStaticAttributeOfThisType(IExpression expr) {
                return (expr instanceof IDSubjectAttribute)
                    && ((IDSubjectAttribute)expr).getSubjectType() == subjType
                    && attrTracker.isStatic((IDSubjectAttribute)expr);
            }
            /**
             * Converts == to "has" and != to "does not have"; throws exception if
             * the argument is neither == nor !=.
             * @param operator the operator to convert.
             * @return has if operator is ==, does not have if operator is !=.
             * @throws IllegalArgumentException when operator is neither == nor !=.
             */
            private RelationOp equalityToContainment(RelationOp operator) {
                if (operator == RelationOp.EQUALS) {
                    return RelationOp.HAS;
                } else if (operator == RelationOp.NOT_EQUALS) {
                    return RelationOp.DOES_NOT_HAVE;
                } else {
                    throw new IllegalArgumentException(
                        "Found a group membership attribute in a non-equality-based relation"
                        );
                }
            }
            private int getIndexOfNextGroup() {
                return currentGroupIndex++;
            }
        };

        BitSet forAllSubjects = new BitSet(policyById.size());
        int i = 0;
        for ( Map.Entry<Long,ReferenceResolver.IParsedEntity> entry : policyById.entrySet()) {
            IDPolicy policy = (IDPolicy)entry.getValue().getParsed();
            IPredicate subjectCondition = Predicates.transform(
                policy.getTarget().getSubjectPred()
            ,   keepOnlyStaticAttributesOfThisType
                );
            if (subjectCondition == null) {
                subjectCondition = PredicateConstants.TRUE;
            }

            // See if the policy should be added to all or to a subset of all subjects
            boolean tryAllSubjects = false;

            // This test isn't perfect.  Dynamic attributes aren't a sure indicator that
            // this policy applies to all subjects, but coming up with a precise rule
            // is complex.  Consider (dynamic OR static) vs. (dynamic AND static) and,
            // for that matter, NOT (dynamic AND NOT static).  However, we should always
            // err on the side of inclusion, so that means that a policy with a dynamic
            // attribute should be assumed to be applicable to all subjects.
            if (subjectCondition == PredicateConstants.TRUE || Predicates.find(policy.getTarget().getSubjectPred(), findDynamicAttributesOfType)) {
                tryAllSubjects = true;
            } else if (subjectCondition instanceof IDSubjectSpec.SubjectSpecBuiltin) {
                tryAllSubjects = (subjectCondition != IDSubjectSpec.EMPTY);
            } else if (policy.getMainEffect() == EffectType.ALLOW) {
                tryAllSubjects = (policy.getOtherwiseEffect() == EffectType.DENY);
            }

            if (tryAllSubjects) {
                // Policy has to be added to all subjects
                forAllSubjects.set(i++);
            } else {
                // Policy needs to be added to a subset of all subjects
                subjIter = dictionary.queryKeys(
                    new CompositePredicate(
                        BooleanOp.AND
                    ,   Arrays.asList(
                        allSubjects
                    ,   dictHelper.toDictionaryPredicate(subjectCondition, null, subjType)
                        )
                        )
                ,   consistentTime
                    );
                IPredicate forUnknown = Predicates.transform(subjectCondition, unknownSubjectTransformer);
                try {
                    if (forUnknown.match(null)) {
                        policiesForUnknownSubjects.set(i);
                    }
                } catch (Exception ignored) {
                }
                addToAllSets(subjIter, subjIds, applicable, i++);
            }
            IPredicate transformedSubj = Predicates.transform(
                policy.getTarget().getSubjectPred()
            ,   buildSubjectGroups
                );
            policy.getTarget().setSubjectPred(
                transformedSubj!=null ? transformedSubj : PredicateConstants.TRUE
                );
            if (policy.getTarget().getToSubjectPred() != null) {
                IPredicate transformedToSubj = Predicates.transform(
                    policy.getTarget().getToSubjectPred()
                ,   buildSubjectGroups
                    );
                policy.getTarget().setToSubjectPred(
                    transformedToSubj!=null ? transformedToSubj : PredicateConstants.TRUE
                    );
            }
            if (subjType == SubjectType.USER) {
                // Process the resource predicate to deal with user-referencing attribute
                IPredicate transformedFrom = Predicates.transform(
                    policy.getTarget().getFromResourcePred()
                ,   buildSubjectGroups
                    );
                policy.getTarget().setFromResourcePred(
                    transformedFrom!=null ? transformedFrom : PredicateConstants.TRUE
                    );
                if (policy.getTarget().getToResourcePred() != null) {
                    IPredicate transformedTo = Predicates.transform(
                        policy.getTarget().getToResourcePred()
                    ,   buildSubjectGroups
                        );
                    policy.getTarget().setToResourcePred(
                        transformedTo!=null ? transformedTo : PredicateConstants.TRUE
                        );
                }
            }
        }
        subjectMappings.addMappings(
            subjIds
        ,   applicable
        ,   groups
        ,   policiesForUnknownSubjects
        ,   groupsForUnknownSubjects
            );
        forAllSubjectsReturn.or(forAllSubjects);
    }

    private void addToAllSets(IDictionaryIterator<Long> subjIter, long[] subjIds, BitSet[] sets, int index) throws DictionaryException {
        try {
            while (subjIter.hasNext()) {
                int i = Arrays.binarySearch(subjIds, subjIter.next().longValue());
                if (i > -1) {
                    sets[i].set(index);
                } else {
                    throw new IllegalStateException(
                        "Detected IDs in the subset that were not present in the original set."
                        );
                }
            }
        } finally {
            subjIter.close();
        }
    }

    public boolean runWasSuccessful() {
        return successfulRun;
    }

    private void setFailureException(Throwable e) {
        failureException = e;
        successfulRun = false;
    }

    public Throwable getFailureException() {
        return failureException;
    }

    public void run() {
        Session session = null;
        Transaction tx = null;
        long start = System.currentTimeMillis();
        try {
            SessionFactory sf = (SessionFactory) componentManager.getComponent(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName());
            session = sf.openSession();
            tx = session.beginTransaction();
            doBuild(session);
            tx.commit();
        } catch (HibernateException e) {
            rollbackAndLogError( tx, e );
        } catch ( PQLException e ) {
            rollbackAndLogError( tx, e );
        } catch ( DictionaryException e ) {
            rollbackAndLogError( tx, e );
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (HibernateException he) {
                    log.error("Failed to close connect while building STR maps", he);
                }
            }
            final long timeToBuild = System.currentTimeMillis() - start;
            log.info(new Object() {
                public String toString() {
                    StringBuffer res = new StringBuffer();
                    res.append("It took ");
                    res.append(timeToBuild);
                    res.append(" milliseconds to build resolution maps for [");
                    String[] domains = strLog.getDomains();
                    for (int i = 0 ; i != domains.length ; i++) {
                        if (i != 0) {
                            res.append(", ");
                        }
                        res.append(domains[i]);
                    }
                    res.append("]");
                    return res.toString();
                }
            });
            if (timeToBuild > ACCEPTABLE_MAP_BUILD_LATENCY_MS) {
                log.warn(
                    "It took " + timeToBuild
                +   " mS to build resolution maps, which is"
                +   " longer than the allowed latency of "
                +   ACCEPTABLE_MAP_BUILD_LATENCY_MS
                +   " mS."
                    );
            }
        }
    }

    /**
     * This utility method attempts to rollback a transaction and
     * log the reason why the transaction has been rolled back.
     *
     * @param tx the transaction to rollback.
     * @param e the exception that caused the rollback.
     */
    private void rollbackAndLogError(Transaction tx, Exception e) {
        if (tx != null) {
            try {
                tx.rollback();
            } catch (HibernateException he) {
                log.error("Rollback attempt has failed.", he);
            }
        }
        log.error("Exception while updating the database:", e);

        // Let the caller know what happened
        setFailureException(e);
    }

    public static class SubjectMappings implements ISubjectMappings, IStreamable {
        private final Log log = LogFactory.getLog(SubjectMappings.class.getName());

        private class MappingImpl implements Mapping, Comparable<MappingImpl> {
            public long id;
            public BitSet policies;
            public BitSet groups;
            public MappingImpl(long id, BitSet policies, BitSet groups) {
                this.id = id;
                this.policies = policies;
                this.groups = groups;
            }
            public int compareTo(MappingImpl o) {
                if (id == o.id) {
                    return 0;
                } else {
                    return ((id - o.id) > 0) ? 1 : -1;
                }
            }
            @Override
            public String toString() {
                return "[" + id + ", p=" + policies + ", g=" + groups + "]";
            }
            public long getId() {
                return id;
            }
            public BitSet getGroups() {
                return groups;
            }
            public BitSet getPolicies() {
                return policies;
            }
            
        }

        private static final long serialVersionUID = 1L;

        private MappingImpl[] mappings = new MappingImpl[0];

        public SubjectMappings() {
        }

        public void addMappings(
            long[] ids
        ,   BitSet[] policyMappings
        ,   BitSet[] groupMappings
        ,   BitSet defaultPolicies
        ,   BitSet defaultGroups) {
            int add = 0;
            for (int i = 0 ; i != ids.length ; i++) {
                if (!policyMappings[i].equals(defaultPolicies)
                    || !groupMappings[i].equals(defaultGroups)) {
                    add++;
                }
            }
            if (add == 0) {
                return;
            }
            MappingImpl[] newMappings = new MappingImpl[add+mappings.length];
            System.arraycopy(mappings, 0, newMappings, add, mappings.length);
            mappings = newMappings;
            for (int i = 0, j = 0 ; i != ids.length ; i++) {
                if (!policyMappings[i].equals(defaultPolicies)
                    || !groupMappings[i].equals(defaultGroups)) {
                    mappings[j++] = new MappingImpl(
                        ids[i]
                    ,   policyMappings[i]
                    ,   groupMappings[i]
                        );
                }
            }
            Arrays.sort(mappings);
        }

        public BitSet getGroupMappings(long id) {
            int index = find(id);
            return index >= 0 ? mappings[index].groups : null;
        }

        public BitSet getPolicyMappings(long id) {
            int index = find(id);
            return index >= 0 ? mappings[index].policies : null;
        }

        public Mapping getMapping(long id) {
            int index = find(id);
            return index >= 0 ? mappings[index] : null;
        }

        public boolean isDefinedId(long id) {
            return find(id) >= 0;
        }

        public int size() {
            return mappings.length;
        }

        private int find(long id) {
            return Arrays.binarySearch(mappings, new MappingImpl(id, null, null));
        }

        private final ByteArrayOutputStream os = new ByteArrayOutputStream();

        /**
         * @see IStreamable#getSize()
         */
        public int getSize() {
            try {
                writeOutput();
            } catch (IOException ioe) {
                log.error("Exception thrown by writeOutput: " + ioe.getMessage());
                return 0;
            }
            return os.size();
        }

        /**
         * @see IStreamable#getStream()
         */
        public InputStream getStream() {
            try {
                writeOutput();
            } catch (IOException ioe) {
                log.error("Exception thrown by writeOutput: " + ioe.getMessage());
                return new ByteArrayInputStream(new byte[0]);
            }
            return new ByteArrayInputStream(os.toByteArray());
        }

        private void writeOutput() throws IOException {
            if (os.size() != 0) {
                return;
            }
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeUTF(getClass().getName());
            writeExternal(oos);
            oos.flush();
        }

        /**
         * @see IStreamable#readFromStream(DataInputStream)
         */
        public void readExternal(ObjectInput in) throws ClassNotFoundException, IOException {
            int size = in.readInt();
            long[] ids = new long[size];
            BitSet[] policyMappings = new BitSet[size];
            BitSet[] groupMappings = new BitSet[size];
            for (int i = 0 ; i != size ; i++) {
                ids[i] = in.readLong();
                policyMappings[i] = readBitSet(in);
                groupMappings[i] = readBitSet(in);
            }
            addMappings(ids, policyMappings, groupMappings, null, null);
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            int len = mappings.length;
            out.writeInt(len);
            for (int i = 0 ; i != len ; i++) {
                MappingImpl m = mappings[i];
                out.writeLong(m.id);
                writeBitSet(out, m.policies);
                writeBitSet(out, m.groups);
            }
        }
    }

    public static class StreamableLongMap extends StreamableMap<Long> {
        public StreamableLongMap() {
        }

        public void readExternal(ObjectInput in) throws ClassNotFoundException, IOException {
            int size = in.readInt();
            data = new HashMap<Long,BitSet>((3*size)/2);
            for (int i = 0 ; i != size ; i++) {
                data.put(new Long(in.readLong()), readBitSet(in));
            }
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(size());
            for (Map.Entry<Long,BitSet> entry : entrySet() ) {
                out.writeLong(entry.getKey().longValue());
                writeBitSet(out, entry.getValue());
            }
        }
    }

    public static class StreamableStringMap extends StreamableMap<String> {
        public StreamableStringMap() {
        }

        public void readExternal(ObjectInput in) throws ClassNotFoundException, IOException {
            int size = in.readInt();
            data = new HashMap<String,BitSet>((3*size)/2);
            for (int i = 0 ; i != size ; i++) {
                data.put(in.readUTF(), readBitSet(in));
            }
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(size());
            for (Map.Entry<String,BitSet> entry : entrySet() ) {
                out.writeUTF(entry.getKey());
                writeBitSet(out, entry.getValue());
            }
        }
    }

    private static abstract class StreamableMap<T> implements Map<T,BitSet>, IStreamable {
        private final ByteArrayOutputStream os = new ByteArrayOutputStream();
        private final Log log = LogFactory.getLog(StreamableMap.class.getName());
        protected Map<T,BitSet> data = new HashMap<T,BitSet>();

        public int getSize() {
            try {
                writeOutput();
            } catch (IOException ioe) {
                log.error("Exception thrown by writeOutput: " + ioe.getMessage());
                return 0;
            }
            return os.size();
        }

        public InputStream getStream() {
            try {
                writeOutput();
            } catch (IOException ioe) {
                log.error("Exception thrown by writeOutput: " + ioe.getMessage());
                return new ByteArrayInputStream(new byte[0]);
            }
            return new ByteArrayInputStream(os.toByteArray());
        }

        private void writeOutput() throws IOException {
            if (os.size()>0) {
                return;
            }
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeUTF(getClass().getName());
            writeExternal(oos);
            oos.flush();
        }

        public void clear() {
            data.clear();
        }
        public boolean containsKey(Object key) {
            return data.containsKey(key);
        }
        public boolean containsValue(Object value) {
            return data.containsValue(value);
        }
        public Set<Map.Entry<T,BitSet>> entrySet() {
            return data.entrySet();
        }
        public BitSet get(Object key) {
            return data.get(key);
        }
        public boolean isEmpty() {
            return data.isEmpty();
        }
        public Set<T> keySet() {
            return data.keySet();
        }
        public BitSet put(T key, BitSet value) {
            return data.put(key, value);
        }
        public void putAll(Map<? extends T,? extends BitSet> t) {
            data.putAll(t);
        }
        public BitSet remove(Object key) {
            return data.remove(key);
        }
        public int size() {
            return data.size();
        }
        public Collection<BitSet> values() {
            return data.values();
        }
    }

    public static class StreamableList implements List<String>, IStreamable {
        private List<String> data = new ArrayList<String>();
        private final ByteArrayOutputStream os = new ByteArrayOutputStream();
        private final Log log = LogFactory.getLog(StreamableList.class.getName());

        public StreamableList() {
        }

        public int getSize() {
            try {
                writeOutput();
            } catch (IOException ioe) {
                log.error("Exception thrown by writeOutput(): " + ioe.getMessage());
                return 0;
            }
            return os.size();
        }

        public InputStream getStream() {
            try {
                writeOutput();
            } catch (IOException ioe) {
                log.error("Exception thrown by writeOutput(): " + ioe.getMessage());
                return new ByteArrayInputStream(new byte[0]);
            }
            return new ByteArrayInputStream(os.toByteArray());
        }
        public void readExternal(ObjectInput in) throws ClassNotFoundException, IOException {
            int size = in.readInt();
            data = new ArrayList<String>(size);
            for (int i = 0 ; i != size ; i++) {
                try {
                    add((String)in.readObject());
                } catch (OptionalDataException ode) {
                    add(in.readUTF());
                }
            }
        }
        public boolean add(String o) {
            return data.add(o);
        }
        public void add(int index, String element) {
            data.add(index, element);
        }
        public boolean addAll(Collection<? extends String> c) {
            return data.addAll(c);
        }
        public boolean addAll(int index, Collection<? extends String> c) {
            return data.addAll(index, c);
        }
        public void clear() {
            data.clear();
        }
        public boolean contains(Object o) {
            return data.contains(o);
        }
        public boolean containsAll(Collection<?> c) {
            return data.containsAll(c);
        }
        public String get(int index) {
            return data.get(index);
        }
        public int indexOf(Object o) {
            return data.indexOf(o);
        }
        public boolean isEmpty() {
            return data.isEmpty();
        }
        public Iterator<String> iterator() {
            return data.iterator();
        }
        public int lastIndexOf(Object o) {
            return data.lastIndexOf(o);
        }
        public ListIterator<String> listIterator() {
            return data.listIterator();
        }
        public ListIterator<String> listIterator(int index) {
            return data.listIterator(index);
        }
        public String remove(int index) {
            return data.remove(index);
        }
        public boolean remove(Object o) {
            return data.remove(o);
        }
        public boolean removeAll(Collection<?> c) {
            return data.removeAll(c);
        }
        public boolean retainAll(Collection<?> c) {
            return data.retainAll(c);
        }
        public String set(int index, String element) {
            return data.set(index, element);
        }
        public int size() {
            return data.size();
        }
        public List<String> subList(int fromIndex, int toIndex) {
            return data.subList(fromIndex, toIndex);
        }
        public Object[] toArray() {
            return data.toArray();
        }
        public <T> T[] toArray(T[] a) {
            return data.toArray(a);
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(size());
            for (String str : this ) {
                try {
                    out.writeUTF(str);
                } catch (UTFDataFormatException udfe) {
                    // writeUTF is unable to handle strings > 64K in size. writeObject can. We have to make sure
                    // to read them with readObject, however
                    out.writeObject(str);
                }
            }
        }
        private void writeOutput() throws IOException {
            if (os.size() > 0) {
                return;
            }
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeUTF(getClass().getName());
            writeExternal(oos);
            oos.flush();
        }
    }

    private static Field bitsetBits;
    private static Field bitsetSize;
    private static Method bitsetRecalcSize;

    static {
        Class<BitSet> bitsetClass = BitSet.class;

        // We pull out and make public various fields of the bitset for performance reasons.  I don't
        // know that we have done any timing tests on this and this is very brittle as it depends on
        // private names.  Yay!
        // There is no possible way to recover from an exception here - we need this stuff - so quit
        // as gracelessly as possible
        try {
            try {
                bitsetBits = bitsetClass.getDeclaredField("bits");
            } catch (NoSuchFieldException e) {
                // Try the 1.6 name
                bitsetBits = bitsetClass.getDeclaredField("words");
            }
            
            bitsetBits.setAccessible(true);

            try {
                bitsetSize = bitsetClass.getDeclaredField("unitsInUse");
            } catch (NoSuchFieldException e) {
                // Try the 1.6 name
                bitsetSize = bitsetClass.getDeclaredField("wordsInUse");
            }
            bitsetSize.setAccessible(true);

            try {
                bitsetRecalcSize = bitsetClass.getDeclaredMethod("recalculateUnitsInUse");
            } catch (NoSuchMethodException e) {
                // Try the 1.6 name
                bitsetRecalcSize = bitsetClass.getDeclaredMethod("recalculateWordsInUse");
            }

            bitsetRecalcSize.setAccessible(true);

        } catch (SecurityException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    private static BitSet readBitSet(DataInput in) throws IOException {
        short k = in.readShort();
        long[] bits = new long[k];
        for (int j = 0 ; j != k ; j++) {
            bits[j] = in.readLong();
        }
        BitSet res = new BitSet();
        try {
            bitsetBits.set(res, bits);
            bitsetSize.set(res, new Integer(bits.length));
            bitsetRecalcSize.invoke(res);
        } catch (IllegalAccessException iae) {
            throw new IOException(iae.getMessage());
        } catch (InvocationTargetException ite) {
            throw new IOException(ite.getMessage());
        }
        return res;
    }

    private static void writeBitSet(DataOutput out, BitSet bs) throws IOException {
        try {
            long[] bits = (long[])bitsetBits.get(bs);
            out.writeShort(bits.length);
            for (int i=0 ; i != bits.length ; i++) {
                out.writeLong(bits[i]);
            }
        } catch (IllegalAccessException iae) {
            throw new IOException(iae.getMessage());
        }
    }

    /**
     * Transformer regarding to unknown user 
     * DefaultTransformer + transformRelation for unknown user, whose behavior is as follows:   
     * If the relation's operation is "!=", return TRUE because unknown user is not in any group.
     * Otherwise (e.g. the relation's opearation is "="), resolve to FALSE because unknown user is not in any group.
     * 
     * Note: This does not address the case where the relationship is "does not have".  
     * One can argue that, because unknown user does not have anything, this should return TRUE.  
     * However, it is not clear in what situation unknownSubjectTransformer() is invoked with "does not have" relation, 
     * or how to test it.  We chose not to address this, until this issue is better understood.    
     */
    private static final Predicates.ITransformer unknownSubjectTransformer = new Predicates.DefaultTransformer() {
        @Override
        public IPredicate transformRelation(IRelation pred) {
            if (pred.getOp() == RelationOp.NOT_EQUALS)
                return PredicateConstants.TRUE;
            else
                return PredicateConstants.FALSE;
        }
    };
}
