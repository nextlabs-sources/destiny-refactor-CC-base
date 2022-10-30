package com.bluejungle.pf.destiny.policymap;

/*
 * All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * San Mateo, CA. Ownership remains with Blue Jungle Inc. All rights reserved
 * worldwide.
 * 
 * @author sasha, sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/destiny/policymap/PolicyQueryImpl.java#1 $
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.server.shared.configuration.IActionConfigDO;
import com.bluejungle.destiny.server.shared.configuration.IDABSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.ITrustedDomainsConfigurationDO;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.ElementFieldData;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.domain.enrollment.ApplicationReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ComputerReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IFunctionApplication;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Predicates;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.utils.MultipartKey;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lib.IPolicyQuery;
import com.bluejungle.pf.destiny.lifecycle.DeploymentEntity;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.DefaultPQLVisitor;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.IHasPQL;
import com.bluejungle.pf.destiny.parser.IPQLVisitor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.deployment.AgentRequest;
import com.bluejungle.pf.domain.destiny.deployment.DeploymentBundle;
import com.bluejungle.pf.domain.destiny.deployment.DeploymentBundleFactory;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle;
import com.bluejungle.pf.domain.destiny.deployment.SubjectKeyMappingImpl;
import com.bluejungle.pf.domain.destiny.environment.RemoteAccessAttribute;
import com.bluejungle.pf.domain.destiny.environment.HeartbeatAttribute;
import com.bluejungle.pf.domain.destiny.exceptions.PolicyExceptions;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.misc.Target;
import com.bluejungle.pf.domain.destiny.obligation.CustomObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.misc.IEffectType;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;
import com.bluejungle.version.IVersion;
import com.bluejungle.versionfactory.VersionFactory;
import com.nextlabs.domain.enrollment.ContactReservedFieldEnumType;

/**
 * This class implements an interface for getting deployment bundles.
 */

public class PolicyQueryImpl implements IPolicyQuery, IInitializable, ILogEnabled {

    public static final ComponentInfo COMP_INFO = new ComponentInfo(
        IPolicyQuery.class.getName()
    ,   PolicyQueryImpl.class.getName()
    ,   IPolicyQuery.class.getName()
    ,   LifestyleType.SINGLETON_TYPE
    );

    // There really has to be a better way to do this
    private static final IVersion VERSION_WITH_EMAIL_SUPPORT =
        VersionFactory.makeVersion(2, 5, 0, 0, 0);

    private static final IVersion VERSION_WITH_CUSTOM_OBLIGATION_SUPPORT =
        VersionFactory.makeVersion(2, 5, 0, 0, 0);

    private static final IVersion VERSION_WITH_REMOTE_ADDRESS_SUPPORT =
        VersionFactory.makeVersion(2, 5, 0, 0, 0);

    private static final IVersion VERSION_WITH_COMMUNICATION_ACTIONS =
        VersionFactory.makeVersion(2, 5, 0, 0, 0);

    private static final IVersion VERSION_WITH_PRESENCE_ACTIONS =
        VersionFactory.makeVersion(2, 7, 0, 0, 0);

    private static final IVersion VERSION_WITH_TIME_SINCE_LAST_HEARTBEAT =
        VersionFactory.makeVersion(3, 5, 0, 0, 0);
	
    private static final IVersion VERSION_WITH_LIVEMEETING_ACTIONS =
        VersionFactory.makeVersion(4, 0, 0, 0, 0);

    private static final IVersion VERSION_WITH_URL_SUPPORT =
        VersionFactory.makeVersion(3, 5, 0, 0, 0);

    private static final IVersion VERSION_WITH_CONFIGURABLE_ACTIONS =
        VersionFactory.makeVersion(4, 1, 0, 0, 0);

    // the first version to support trusted app is 5.0.3
    // however, it doesn't really hurt to sent it to all 5.0 version
    // it only makes the bundle bigger.
    private static final IVersion VERSION_WITH_TRUSTED_APPLICATIONS =
        VersionFactory.makeVersion(5, 0, 0, 0, 0);
    
    private static final IVersion VERSION_WITH_ENHANCED_PQL_SUPPORT =
        VersionFactory.makeVersion(5, 5, 0, 0, 0);

    private static final IVersion VERSION_WITH_POLICY_EXCEPTIONS =
        VersionFactory.makeVersion(7, 0, 0, 0, 0);
    
    private static final IVersion VERSION_WITH_POLICY_TAGS =
        VersionFactory.makeVersion(7, 5, 0, 0, 0);
    
    private static final IVersion VERSION_WITH_RECIPIENT_ATTRIBUTES =
        VersionFactory.makeVersion(8, 0, 0, 0, 0);
    
    
    private static final String TRUSTED_APPLICATION_NAME = "Trusted Applications";

    private static final int NUM_KEYS = 7;
    private static final int USER_SID_INDEX = 0;
    private static final int USER_UID_INDEX = 1;
    private static final int USER_MAIL_INDEX = 2;
    private static final int HOST_SID_INDEX = 3;
    private static final int HOST_UID_INDEX = 4;
    private static final int APP_FP_INDEX = 5;
    private static final int CONTACT_EMAIL_INDEX = 6;

    private ServerTargetResolver resolver;
    private LifecycleManager lifecycleManager;
    private IDictionary dictionary;
    private IElementType userType;
    private IElementType hostType;
    private IElementType appType;
    private IElementType contactType;
    private IElementType clientType;
    private IElementType siteType;
    private Set<IPredicate> configuredActions;


    private static final BitSet EMPTY_BITSET = new BitSet();

    private static final int[] userKeyIndexes = new int[] {
        USER_SID_INDEX
    ,   USER_UID_INDEX
    ,   USER_MAIL_INDEX
    };

    private static final int[] hostKeyIndexes = new int[] {
        HOST_SID_INDEX
    ,   HOST_UID_INDEX
    };

    private int[] contactKeyIndexes = new int[] { CONTACT_EMAIL_INDEX };

    private int[] appKeyIndexes = new int[] { APP_FP_INDEX };

    private IElementField[] keyFields;

    private Log log;

    private Cache subjectCache;

    private static boolean loggedUndefinedSubjectKey = false;

    /**
     * @see IPolicyQuery#getDeploymentBundle(IDSubject[], IDSubject, Long,
     *      String, AgentTypeEnumType, IVersion, Calendar)
     */
    public IDeploymentBundle getDeploymentBundle(
        Long[] policyUserIds
    ,   Long agentHostId
    ,   Long agentId
    ,   String agentDomain
    ,   AgentTypeEnumType agentType
    ,   IVersion agentVersion
    ,   Calendar ts
    ) {
        Calendar now = Calendar.getInstance();
        Date lastDeployed = getLastDeploymentDate(ts, now);
        if (ts != null) {
            Date reqTime = ts.getTime();
            if (!lastDeployed.after(reqTime) && !resolver.haveDirectoryChangesSince(reqTime)) {
                Calendar res = Calendar.getInstance();
                res.setTime(lastDeployed);
                return DeploymentBundleFactory.makeBundle(res, agentVersion);
            }
        }

        String[] agentDomains = expandDomainList(agentDomain);

        resolver.buildMaps(agentDomains);

        Set<Long> subjectIds = new HashSet<Long>();
        subjectIds.add(agentHostId);
        subjectIds.addAll(Arrays.asList(policyUserIds));

        return buildBundle(
            agentDomains
        ,   agentType
        ,   now
        ,   subjectIds
        ,   agentHostId
        ,   agentId
        ,   agentVersion
        );
    }

    IDeploymentBundle buildBundle(
        String[] agentDomains
    ,   final AgentTypeEnumType agentType
    ,   Calendar now
    ,   Collection<Long> requestedSubjectIds
    ,   Long agentHostId
    ,   Long agentId
    ,   final IVersion agentVersion
    ) {
        long startTime = System.currentTimeMillis();

        ITargetResolutions resolutions = resolver.getSTRLog(agentDomains);

        if (resolutions == null) {
            // The resolutions are not available - we can't build the bundle.
            return null; // The caller will call us again on a next heartbeat.
        }
        if (log.isInfoEnabled()) {
            long timeDiff = System.currentTimeMillis()-startTime;
            log.info("Retrieving policy mappings took "+timeDiff+" milliseconds");
        }

        Calendar strTime = Calendar.getInstance();
        strTime.setTime(resolutions.getBuildTime());

        // Obtain subject-key mappings from the dictionary
        Collection<IDeploymentBundle.ISubjectKeyMapping> subjectKeyMappings =
            new ArrayList<IDeploymentBundle.ISubjectKeyMapping>(65535);
        ISubjectMappings mappings = resolutions.getSubjectMappings();
        BitSet unknownAppGroups;
        BitSet unknownUserGroups;
        BitSet unknownHostGroups;
        BitSet unknownAppPolicies;
        BitSet unknownUserPolicies;
        BitSet unknownHostPolicies;

        ISubjectMappings.Mapping unknownAppMapping =
            mappings.getMapping(IDeploymentBundle.KEY_OF_UNKNOWN_APPLICATION);
        if (unknownAppMapping != null) {
            unknownAppGroups = unknownAppMapping.getGroups();
            unknownAppPolicies = unknownAppMapping.getPolicies();
        } else {
            unknownAppGroups = EMPTY_BITSET;
            unknownAppPolicies = EMPTY_BITSET;
        }

        ISubjectMappings.Mapping unknownUserMapping =
            mappings.getMapping(IDeploymentBundle.KEY_OF_UNKNOWN_USER);
        if (unknownUserMapping != null) {
            unknownUserGroups = unknownUserMapping.getGroups();
            unknownUserPolicies = unknownUserMapping.getPolicies();
        } else {
            unknownUserGroups = EMPTY_BITSET;
            unknownUserPolicies = EMPTY_BITSET;
        }

        ISubjectMappings.Mapping unknownHostMapping =
            mappings.getMapping(IDeploymentBundle.KEY_OF_UNKNOWN_HOST);
        if (unknownHostMapping != null) {
            unknownHostGroups = unknownHostMapping.getGroups();
            unknownHostPolicies = unknownHostMapping.getPolicies();
        } else {
            unknownHostGroups = EMPTY_BITSET;
            unknownHostPolicies = EMPTY_BITSET;
        }
        Map<Long,BitSet> subjectToPolicy = new HashMap<Long,BitSet>(mappings.size());
        Map<Long,BitSet> subjectToGroup = new HashMap<Long,BitSet>(mappings.size());        
        try {

            boolean isDesktop = (agentType == AgentTypeEnumType.DESKTOP);

            for (SubjectData subjectData : getSubjectData(agentDomains)) {
                boolean needEmail = false;
                boolean needUID = false;
                boolean needHostId = false;
                boolean needGroups = false;
                Long id = subjectData.getKey();

                boolean isUser = subjectData.getType().equals(SubjectType.USER);
                boolean isHost = subjectData.getType().equals(SubjectType.HOST);
                boolean isApp = subjectData.getType().equals(SubjectType.APP);

                boolean isRequested = requestedSubjectIds.contains(id);
                if (mappings.isDefinedId(id)) {
                    if (isUser) {
                        BitSet groupsOfThisSubject = mappings.getGroupMappings(id);
                        assert groupsOfThisSubject != null; // see !mappings.isDefinedId(id) above
                        boolean hasDifferentiatedGroups = !groupsOfThisSubject.equals(unknownUserGroups);
                        needEmail = (isDesktop && (hasDifferentiatedGroups || isRequested));
                        if (subjectData.isContact()) {
                            // Contact
                            needGroups = isDesktop && hasDifferentiatedGroups;
                        } else {
                            // User
                            needGroups = (!isDesktop || isRequested || hasDifferentiatedGroups);
                            needUID = (!isDesktop || isRequested);
                        }
                    } else if (isHost) {
                        if (!isDesktop || isRequested) {
                            needGroups = true;
                            needUID = true;
                            needHostId = true;
                        }
                    } else if (isApp) {
                        needGroups = true;
                        needUID = true;
                    } else {
                        throw new IllegalStateException("Found an unexpected subject type: "+subjectData.getType());
                    }
                } else if (isDesktop && isRequested && isUser) {
                    needUID = true;
                    needEmail = true;
                } else if (!isDesktop && ((isUser && !subjectData.isContact()) || isHost)) {
                    needUID = true;
                    needHostId = isHost;
                } else {
                    continue;
                }
 
                if (needGroups) {
                    ISubjectMappings.Mapping m = mappings.getMapping(id);
                    subjectToPolicy.put(id, m.getPolicies());
                    subjectToGroup.put(id, m.getGroups());
                } else {
                    if (!needUID && !needEmail) {
                        continue;
                    }
                }

                boolean found = false;

                Object[] data = subjectData.getData();

                for (int index : subjectData.getKeyIndexes()) {
                    // TODO This code sends e-mail addresses of users
                    // only to desktop agents. When we change the agent type
                    // of the e-mail enforcer, this code needs to change.
                    if (index == USER_MAIL_INDEX || index == CONTACT_EMAIL_INDEX) {
                        if (!needEmail) {
                            continue;
                        }
                        if (data[index] == null) {
                            // If e-mail is not available, use unique name
                            Object idValue = subjectData.getUniqueName();
                            if (idValue != null) {
                                found = true;
                                String uid = (String)idValue;
                                String uidType = "uniqueName";
                                subjectKeyMappings.add(
                                    new SubjectKeyMappingImpl(
                                        id
                                    ,   subjectData.getType()
                                    ,   uid
                                    ,   uidType
                                    )
                                );
                            }
                        }
                    } else {
                        if (!needUID) {
                            continue;
                        }
                        /*
                         * TODO (sergey) The code below assumes that enforcers identify
                         * hosts by their unique name, not by their windows SID or
                         * UNIX identifier. I am not sure if there is a good reason
                         * for it. We should address this issue when we have more
                         * information on why we identify hosts by unique name.
                         */
                        if (needHostId) {
                            Object idValue = subjectData.getUniqueName();
                            if (idValue != null) {
                                found = true;
                                String uid = (String)idValue;
                                String uidType = "uniqueName";
                                subjectKeyMappings.add(
                                    new SubjectKeyMappingImpl(
                                        id
                                    ,   subjectData.getType()
                                    ,   uid
                                    ,   uidType
                                    )
                                );
                            }
                        }
                    }
                    Object idValue = data[index];
                    if (idValue != null) {
                        String uidType = keyFields[index].getName();
                        String[] uids;
                        if (idValue instanceof String) {
                            found = true;
                            uids = new String[] {(String)idValue};
                        } else if (idValue instanceof String[]) {
                            found = true;
                            uids = (String[])idValue;
                        } else {
                            if (!loggedUndefinedSubjectKey) {
                                log.error("Internal error: detected an ID of unknown type.");
                                loggedUndefinedSubjectKey = true;
                            }
                            continue;
                        }
                        for (String rawUid : uids) {
                            String uid;
                            if (index == USER_MAIL_INDEX) {
                                uid = rawUid.toLowerCase();
                            } else {
                                uid = rawUid;
                            }
                            subjectKeyMappings.add(
                                new SubjectKeyMappingImpl(
                                    id
                                ,   subjectData.getType()
                                ,   uid
                                ,   uidType
                                )
                            );
                        }
                    }
                }
                if (!found) {
                    log.warn( "Found a subject with no known IDs:"
                    +   " type="+subjectData.getType().getName()
                    +   ", unique name="+subjectData.getUniqueName()
                    +   ", internal key="+subjectData.getKey()
                    );
                }
            }
        } catch (Exception e) {
            log.warn("Unable to obtain subjects for the bundle.", e);
        }

        Map<Long,BitSet> actionToPolicy = new HashMap<Long,BitSet>(resolutions.getActionMappings());
        Map<String,BitSet> actionNamesToPolicy = new HashMap<String,BitSet>(resolutions.getActionNameMappings());

        if (log.isInfoEnabled()) {
            long timeDiff = System.currentTimeMillis()-startTime;
            log.info(
                "Assembling bundle took "+timeDiff+" milliseconds"
            );
        }

        List<String> deploymentPolicies = resolutions.getPolicies();

        final List<IDPolicy> policies = new ArrayList<IDPolicy>(deploymentPolicies.size());

        try {
            final AgentRequest agentRequest = new AgentRequest(agentId.longValue(), agentType);

            final Predicates.IDetector foreignSubjectAttrDetector = new Predicates.DefaultDetector() {
                /**
                 * @see Predicates.DefaultDetector#checkRelation(IRelation)
                 */
                @Override
                public boolean checkRelation(IRelation pred) {
                    return agentVersion != null
                                           && ((agentVersion.compareTo(VERSION_WITH_EMAIL_SUPPORT) < 0 && hasEmailAttribute(pred))
                                               || (agentVersion.compareTo(VERSION_WITH_URL_SUPPORT) < 0 && hasAttribute(pred, SubjectAttribute.APP_URL)));
                }
                private boolean hasEmailAttribute(IRelation pred) {
                    return hasAttribute(pred, SubjectAttribute.USER_EMAIL) || hasAttribute(pred, SubjectAttribute.USER_EMAIL_DOMAIN);
                }
                private boolean hasAttribute(IRelation pred, SubjectAttribute attr) {
                    return pred.getLHS() == attr || pred.getRHS() == attr;
                }
            };
            final Predicates.IDetector foreignRemoteAddressAttributeDetector = new Predicates.DefaultDetector() {
                /**
                 * @see Predicates.DefaultDetector#checkRelation(IRelation)
                 */
                @Override
                public boolean checkRelation(IRelation pred) {
                    return agentVersion != null
                        && agentVersion.compareTo(VERSION_WITH_REMOTE_ADDRESS_SUPPORT) < 0
                        && (isRemoteAddressAttribute(pred.getLHS()) || isRemoteAddressAttribute(pred.getRHS()));
                }
                private boolean isRemoteAddressAttribute(IExpression expr) {
                    return expr == RemoteAccessAttribute.REMOTE_ADDRESS;
                }
            };

            final Predicates.IDetector foreignTimeSinceLastHeartbeatDetector = new Predicates.DefaultDetector() {
                /**
                 * @see Predicates.DefaultDetector#checkRelation(IRelation)
                 */
                @Override
                public boolean checkRelation(IRelation pred) {
                    return agentVersion != null
                        && agentVersion.compareTo(VERSION_WITH_TIME_SINCE_LAST_HEARTBEAT) < 0
                        && (isHeartbeatAttribute(pred.getLHS()) || isHeartbeatAttribute(pred.getRHS()));
                }
                private boolean isHeartbeatAttribute(IExpression expr) {
                    return expr == HeartbeatAttribute.TIME_SINCE_LAST_HEARTBEAT;
                }
            };
                                                                               
            final Predicates.IDetector foreignActionsDetector = new Predicates.DefaultDetector() {
                /**
                 * @see Predicates.DefaultDetector#check(IPredicate)
                 */
                @Override
                public boolean check(IPredicate pred) {
                    return agentVersion != null 
                        && (    (agentVersion.compareTo(VERSION_WITH_COMMUNICATION_ACTIONS) < 0 && (pred == DAction.AVD || pred == DAction.MEETING))
                                || (agentVersion.compareTo(VERSION_WITH_PRESENCE_ACTIONS) < 0 && (pred == DAction.PRESENCE))
                                || (agentVersion.compareTo(VERSION_WITH_LIVEMEETING_ACTIONS) < 0 && (pred == DAction.RECORD
                                                                                                     || pred == DAction.QUESTION
                                                                                                     || pred == DAction.VOICE
                                                                                                     || pred == DAction.VIDEO
                                                                                                     || pred == DAction.JOIN))
                                || (agentVersion.compareTo(VERSION_WITH_CONFIGURABLE_ACTIONS) < 0 && configuredActions.contains(pred))
                                );
                }
            };

            final Predicates.IDetector foreignFunctionCallDetector = new Predicates.DefaultDetector() {
                /**
                 * @see Predicates.DefaultDetector#checkRelation(IRelation)
                 */
                @Override
                public boolean checkRelation(IRelation pred) {
                    return (agentVersion != null
                            && agentVersion.compareTo(VERSION_WITH_ENHANCED_PQL_SUPPORT) < 0
                            && (isFunctionCall(pred.getLHS()) || isFunctionCall(pred.getRHS())));
                }

                private boolean isFunctionCall(IExpression expr) {
                    return (expr instanceof IFunctionApplication);
                }
            };

            final Predicates.IDetector foreignOperatorsDetector = new Predicates.DefaultDetector() {
                /**
                 * @see Predicates.DefaultDetector#checkRelation(IRelation)
                 */
                @Override
                public boolean checkRelation(IRelation pred) {
                    return (agentVersion != null
                            && agentVersion.compareTo(VERSION_WITH_ENHANCED_PQL_SUPPORT) < 0
                            && (isEnhancedMultivalOperator(pred.getOp())));
                }

                private boolean isEnhancedMultivalOperator(RelationOp op) {
                    return (op == RelationOp.INCLUDES || op == RelationOp.EQUALS_UNORDERED);
                }
            };

            final Predicates.IDetector recipientAttributesDetector = new Predicates.DefaultDetector() {
                /**
                 * @see Predicates.DefaultDetector#checkRelation(IRelation)
                 */
                @Override
                public boolean checkRelation(IRelation pred) {
                    return (isRecipientSubject(pred.getLHS()) || isRecipientSubject(pred.getRHS()));
                }

                private boolean isRecipientSubject(IExpression expr) {
                    return ((expr instanceof SubjectAttribute) &&
                            ((SubjectAttribute)expr).getSubjectType() == SubjectType.RECIPIENT);
                }
                
            };

            IPQLVisitor policyCollector = new DefaultPQLVisitor() {

                public void visitPolicy(DomainObjectDescriptor descriptor, IDPolicy policy) {
                    boolean deployedToThisAgent = checkDeployment(policy);
                    boolean notSuitableForThisAgent =
                        Predicates.find(policy.getTarget().getSubjectPred(), foreignSubjectAttrDetector)
                    ||  Predicates.find(policy.getTarget().getToSubjectPred(), foreignSubjectAttrDetector)
                    ||  Predicates.find(policy.getConditions(), foreignRemoteAddressAttributeDetector)
                    ||  Predicates.find(policy.getConditions(), foreignTimeSinceLastHeartbeatDetector)
                    ||  Predicates.find(policy.getConditions(), foreignFunctionCallDetector)
                    ||  Predicates.find(policy.getConditions(), foreignOperatorsDetector)
                    ||  (Predicates.find(policy.getConditions(), recipientAttributesDetector) && agentVersion != null && agentVersion.compareTo(VERSION_WITH_RECIPIENT_ATTRIBUTES) < 0)
                    ||  (Predicates.find(policy.getTarget().getToSubjectPred(), recipientAttributesDetector) && agentVersion != null && agentVersion.compareTo(VERSION_WITH_RECIPIENT_ATTRIBUTES) < 0)
                    ||  Predicates.find(policy.getTarget().getActionPred(), foreignActionsDetector)
                    ||  (containsCustomObligations(policy) && agentVersion != null && agentVersion.compareTo(VERSION_WITH_CUSTOM_OBLIGATION_SUPPORT) < 0)
                    ||  (policy.getTarget().getToSubjectPred() != null && agentVersion != null && agentVersion.compareTo(VERSION_WITH_EMAIL_SUPPORT) < 0)
                    ||  (policy.getName().equals(TRUSTED_APPLICATION_NAME) && agentVersion != null && agentVersion.compareTo(VERSION_WITH_TRUSTED_APPLICATIONS) < 0)
                    ||  ((policy.getPolicyExceptions().getPolicies().size() > 0 || policy.hasAttribute(IDPolicy.EXCEPTION_ATTRIBUTE)) && agentVersion.compareTo(VERSION_WITH_POLICY_EXCEPTIONS) < 0)
                    ;
                    if (!deployedToThisAgent || notSuitableForThisAgent) {
                        if (!deployedToThisAgent) {
                            policy.setDescription("This policy is excluded because its deployment target does not match the agent");
                        } else if (notSuitableForThisAgent) {
                            policy.setDescription("This policy is excluded by the backward compatibility checker.");
                        }
                        policy.setTarget(Target.EMPTY_TARGET);
                        policy.setConditions(null);
                        policy.setPolicyExceptions(new PolicyExceptions());
                        policy.resetObligationArray(EffectType.ALLOW);
                        policy.resetObligationArray(EffectType.DENY);
                        policy.resetObligationArray(EffectType.DONT_CARE);
                    }

                    if (agentVersion != null && agentVersion.compareTo(VERSION_WITH_POLICY_TAGS) < 0) {
                        policy.clearTags();
                    }

                    policy.setDeploymentTarget(null);
                    policies.add(policy);
                }

                private boolean checkDeployment(IDPolicy policy) {
                    IPredicate dt = policy.getDeploymentTarget();
                    if (dt == null) {
                        return true;
                    }
                    return dt.match(agentRequest);
                }
            };

            for ( String pql : deploymentPolicies ) {
                DomainObjectBuilder.processInternalPQL(pql, policyCollector);
            }
        } catch (PQLException e) {
            throw new RuntimeException("exception while trying to parse policies for deployment bundle", e);
        }

        subjectToPolicy.put(IDeploymentBundle.KEY_OF_UNKNOWN_APPLICATION, unknownAppPolicies);
        subjectToGroup.put(IDeploymentBundle.KEY_OF_UNKNOWN_APPLICATION, unknownAppGroups);
        subjectKeyMappings.add(
            new SubjectKeyMappingImpl(
                IDeploymentBundle.KEY_OF_UNKNOWN_APPLICATION
            ,   SubjectType.APP
            ,   IDeploymentBundle.UID_OF_UNKNOWN_APPLICATION
            ,   "builtin")
        );
        subjectToPolicy.put(IDeploymentBundle.KEY_OF_UNKNOWN_USER, unknownUserPolicies);
        subjectToGroup.put(IDeploymentBundle.KEY_OF_UNKNOWN_USER, unknownUserGroups);
        subjectKeyMappings.add(
            new SubjectKeyMappingImpl(
                IDeploymentBundle.KEY_OF_UNKNOWN_USER
            ,   SubjectType.USER
            ,   IDeploymentBundle.UID_OF_UNKNOWN_USER
            ,   "builtin")
        );
        subjectToPolicy.put(IDeploymentBundle.KEY_OF_UNKNOWN_HOST, unknownHostPolicies);
        subjectToGroup.put(IDeploymentBundle.KEY_OF_UNKNOWN_HOST, unknownHostGroups);
        subjectKeyMappings.add(
            new SubjectKeyMappingImpl(
                IDeploymentBundle.KEY_OF_UNKNOWN_HOST
            ,   SubjectType.HOST
            ,   IDeploymentBundle.UID_OF_UNKNOWN_HOST
            ,   "builtin")
        );

        return DeploymentBundleFactory.makeBundle(toPQL(policies) + "\n" + getLocations(now),
                                                  subjectToPolicy,
                                                  actionToPolicy,
                                                  actionNamesToPolicy,
                                                  subjectToGroup,
                                                  subjectKeyMappings,
                                                  resolutions.getPoliciesForAllUsers(),
                                                  resolutions.getPoliciesForAllHosts(),
                                                  resolutions.getPoliciesForAllApps(),
                                                  strTime,
                                                  agentVersion);
    }

    private String getLocations(Calendar now) {
        StringBuffer res = new StringBuffer();
        Collection<DeploymentEntity> deploymentEntities;
        try {
            deploymentEntities = lifecycleManager.getAllDeployedEntities(EntityType.LOCATION, now.getTime(), DeploymentType.PRODUCTION);
        } catch (EntityManagementException e) {
            log.warn("Failed to get locations", e);
            throw new IllegalStateException("Unable to get locations: "+e.getMessage());
        }
        for (IHasPQL de : deploymentEntities) {
            res.append(de.getPql());
            res.append('\n');
        }
        return res.toString();
    }

    /**
     * @param policyUsers
     * @param agentHost
     * @param ts
     * @return
     */
    private Date getLastDeploymentDate(Calendar since, Calendar now) {
        // right now this checks for any updates to anything at all
        assert now != null; // This is a private method, it must be called with
                            // now != null.
        if (lifecycleManager == null) {
            log.trace("Could not get lifecycle manager.  Falling back to complete update mode.");
            return UnmodifiableDate.forDate(now.getTime());
        }

        try {
            Date sinceDate = (since == null) ? UnmodifiableDate.START_OF_TIME : since.getTime();
            return lifecycleManager.getLatestDeploymentTime(sinceDate, now.getTime(), DeploymentType.PRODUCTION);
        } catch (EntityManagementException e1) {
            log.warn("Could not check for deploymend updates.  Falling back to complete update mode.");
            return UnmodifiableDate.forDate(now.getTime());
        }
    }

    private String toPQL(List<IDPolicy> policies) {
        DomainObjectFormatter dof = new DomainObjectFormatter();

        for (IDPolicy policy : policies) {
            dof.formatPolicyDef(policy.getId(), policy);
        }

        return dof.getPQL();
    }

    /**
     * Given an agent domain, returns an array of all domains to which this agent
     * has access according to the trust relations set up for the agent's domain.
     *
     * @param agentDomain the domain of the agent.
     * @return an array of all trusted domains for the agent (including its own),
     * or an empty array if the agent's domain is null or empty.
     */
    private String[] expandDomainList(String agentDomain) {
        if (agentDomain == null || agentDomain.length() == 0) {
            log.warn("Agent domain is null - using an empty domain list.");
            return new String[0];
        }
        SortedSet<String> res = new TreeSet<String>();
        res.add(agentDomain);
        try {
            IDestinyConfigurationStore confStore = (IDestinyConfigurationStore) getManager().getComponent(DestinyConfigurationStoreImpl.COMP_INFO);
            IDABSComponentConfigurationDO dabsConfig = (IDABSComponentConfigurationDO) confStore.retrieveComponentConfiguration(ServerComponentType.DABS.getName());
            if (dabsConfig != null) {
                ITrustedDomainsConfigurationDO tdConfig = dabsConfig.getTrustedDomainsConfiguration();
                if (tdConfig != null) {
                    String[] trustedDomains = tdConfig.getTrustedDomains();
                    if (trustedDomains != null) {
                        for (int i = 0; i != trustedDomains.length; i++) {
                            if (trustedDomains[i] != null) {
                                String[] domains = trustedDomains[i].split(",");
                                for (int j = 0; j != domains.length; j++) {
                                    domains[j] = domains[j].trim();
                                }
                                if (Arrays.asList(domains).contains(agentDomain)) {
                                    log.info("Domain '" + agentDomain + "' is in the list of mutual trust: " + trustedDomains[i]);
                                    for (int j = 0; j != domains.length; j++) {
                                        res.add(domains[j]);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    log.warn("Unable to get trusted domain configuration.");
                }
            } else {
                log.warn("Unable to get DABS configuration - trusted domains will not be configured.");
            }
        } catch (Exception ignored) {
            log.warn("Exception getting a list of trusted domains", ignored);
        }
        if (log.isInfoEnabled()) {
            StringBuffer msg = new StringBuffer("Effective list of domains: ");
            boolean first = true;
            for (String domain : res) {
                if (!first) {
                    msg.append(", ");
                } else {
                    first = false;
                }
                msg.append(domain);
                if (agentDomain.equals(domain)) {
                    msg.append("(REQUESTED)");
                }
            }
            log.info(msg);
        }
        return res.toArray(new String[res.size()]);
    }

    public static class SubjectData implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Long key;
        private final SubjectType type;
        private final String uniqueName;
        private final Object[] data;
        private final boolean isContact;
        private final int[] keyIndexes;
        public SubjectData(
            Long key
        ,   SubjectType type
        ,   int[] keyIndexes
        ,   String uniqueName
        ,   Object[] data
        ,   boolean isContact
        ) {
            this.key = key;
            this.type = type;
            this.keyIndexes = keyIndexes;
            this.uniqueName = uniqueName;
            this.data = data.clone();
            this.isContact = isContact;
        }
        public Long getKey() {
            return key;
        }
        public SubjectType getType() {
            return type;
        }
        public int[] getKeyIndexes() {
            return keyIndexes;
        }
        public String getUniqueName() {
            return uniqueName;
        }
        public Object[] getData() {
            return data;
        }
        public boolean isContact() {
            return isContact;
        }
    }

    public static class SubjectDataInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Date timestamp;
        private final Iterable<SubjectData> data;

        public SubjectDataInfo(Date timestamp, Iterable<SubjectData> data) {
            this.timestamp = timestamp;
            this.data = data;
        }

        public Date getTimestamp() {
            return timestamp;
        }
        public Iterable<SubjectData> getData() {
            return data;
        }
    }

    /*
     * This method is synchronized to avoid multiple threads
     * filling the same cache several times.
     * Since querying user data may take significant time,
     * as well as using significant amount of memory,
     * this synchronization improves scalability. The issue becomes
     * even more severe because the previous step in building
     * the bundle (i.e. map building) is synchronized. All threads
     * that waited for map building to complete will jump into this
     * method at the same time, greatly increasing the probability
     * that the resulting contention would hurt performance.
     */
    private synchronized Iterable<SubjectData> getSubjectData(String[] domains) throws DictionaryException {
        if (subjectCache == null) {
            return readSubjectData(domains);
        }
        // Try getting a cached value
        MultipartKey key;
        if (domains != null && domains.length != 0) {
            key = new MultipartKey((Object[])domains);
        } else {
            // MultipartKey does not allow zero-length arrays:
            key = new MultipartKey(new Object[] {""});
        }
        try {
            Date latest = dictionary.getLatestConsistentTime();
            Element element = subjectCache.get(key);
            SubjectDataInfo info = null;
            if (element != null) {
                info = (SubjectDataInfo)element.getValue();
            }
            // if the cached value is missing or expired...
            if (info == null || info.getTimestamp().before(latest)) {
                // ...obtain or replace a new cache value
                Iterable<SubjectData> data = readSubjectData(domains);
                info = new SubjectDataInfo(latest, data);
                // Store the new value in the cache
                subjectCache.put(new Element(key, info));
            } else {
                log.info("Using cached subjects for "+key);
            }
            return info.getData();
        } catch (Exception e) {
            return readSubjectData(domains);
        }
    }

    private Iterable<SubjectData> readSubjectData(String[] agentDomains) throws DictionaryException {
        long start = System.currentTimeMillis();

        if (log.isInfoEnabled()) {
            StringBuffer msg = new StringBuffer(
                "Querying subjects for domains {"
            );
            boolean first = true;
            for (String domain : agentDomains) {
                if (!first) {
                    msg.append(", ");
                } else {
                    first = false;
                }
                msg.append(domain);
            }
            msg.append('}');
            log.info(msg);
        }

        List<SubjectData> res = new ArrayList<SubjectData>();

        List<IPredicate> domainPredicates = new ArrayList<IPredicate>(agentDomains.length);
        for ( int i = 0 ; i != agentDomains.length ; i++ ) {
            IEnrollment enrollment = dictionary.getEnrollment(agentDomains[i]);
            if ( enrollment != null ) {
                domainPredicates.add(dictionary.condition(enrollment));
            }
        }

        IPredicate domainCondition;
        if (domainPredicates.size() > 1) {
            domainCondition = new CompositePredicate( BooleanOp.OR, domainPredicates);
        } else if (domainPredicates.size() == 1) {
            domainCondition = domainPredicates.get(0);
        } else {
            domainCondition = PredicateConstants.TRUE;
        }

        IDictionaryIterator<ElementFieldData> subjectFields = null;
        try {
            // The query condition is formed as follows:
            // (domain=agentDomains[0] OR domain=agentDomains[..]) OR (type=application OR type=contact)
            subjectFields = dictionary.queryFields(
                keyFields
            ,   new CompositePredicate(
                    BooleanOp.OR
                ,   Arrays.asList( new IPredicate[] {
                        domainCondition
                    ,   dictionary.condition(appType)
                    ,   dictionary.condition(contactType)
                    ,   dictionary.condition(userType)
                    }
                )
            )
            ,   dictionary.getLatestConsistentTime()
            ,   null
            ,   null
            );
            while (subjectFields.hasNext()) {
                ElementFieldData subjectData = subjectFields.next();
                SubjectType type;
                int[] keyIndexes;
                if (subjectData.getType().equals(userType)) {
                    type = SubjectType.USER;
                    keyIndexes = userKeyIndexes;
                } else if (subjectData.getType().equals(contactType)) {
                    type = SubjectType.USER;
                    keyIndexes = contactKeyIndexes;
                } else if (subjectData.getType().equals(hostType)) {
                    type = SubjectType.HOST;
                    keyIndexes = hostKeyIndexes;
                } else if (subjectData.getType().equals(appType)) {
                    type = SubjectType.APP;
                    keyIndexes = appKeyIndexes;
                } else if (subjectData.getType().equals(clientType) ||
                           subjectData.getType().equals(siteType)) {
                    // Doesn't belong here.  Probably picked up by a too-generous domain query
                    continue;
                } else {
                    // Completely unknown subject.  Oops
                    throw new IllegalStateException("Found an unexpected subject type: "+subjectData.getType());
                }
                res.add(
                    new SubjectData(
                        subjectData.getInternalKey()
                    ,   type
                    ,   keyIndexes
                    ,   subjectData.getUniqueName()
                    ,   subjectData.getData()
                    ,   subjectData.getType().equals(contactType)
                                    )
                    );
            }
        } finally {
            if (subjectFields != null) {
                subjectFields.close();
                subjectFields = null;
            }
        }
        if (log.isInfoEnabled()) {
            log.info(
                "Querying subjects took "
            +   (System.currentTimeMillis()-start)
            +   " milliseconds."
            );
        }
        return res;
    }

    private static boolean hasCustomObligationsOfType(IDPolicy pol, IEffectType eff) {
        Collection<IObligation> obligations = pol.getObligations(eff);
        
        if (obligations == null) {
            return false;
        }

        for (IObligation o : obligations) {
            if (CustomObligation.OBLIGATION_NAME.equals(o.getType())) {
                return true;
            }
        }

        return false;
    }

    private static boolean containsCustomObligations(IDPolicy pol) {
        return (hasCustomObligationsOfType(pol, EffectType.ALLOW) ||
                hasCustomObligationsOfType(pol, EffectType.DENY) ||
                hasCustomObligationsOfType(pol, EffectType.DONT_CARE));
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return ComponentManagerFactory.getComponentManager();
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        IComponentManager cm = getManager();
        resolver = cm.getComponent(ServerTargetResolver.COMP_INFO);
        dictionary = cm.getComponent(Dictionary.COMP_INFO);

        try {
            userType = dictionary.getType(ElementTypeEnumType.USER.getName());
            hostType = dictionary.getType(ElementTypeEnumType.COMPUTER.getName());
            appType = dictionary.getType(ElementTypeEnumType.APPLICATION.getName());
            contactType = dictionary.getType(ElementTypeEnumType.CONTACT.getName());
            clientType = dictionary.getType(ElementTypeEnumType.CLIENT_INFO.getName());
            siteType = dictionary.getType(ElementTypeEnumType.SITE.getName());

            keyFields = new IElementField[NUM_KEYS];
            keyFields[USER_SID_INDEX] = userType.getField(UserReservedFieldEnumType.WINDOWS_SID.getName());
            keyFields[USER_UID_INDEX] = userType.getField(UserReservedFieldEnumType.UNIX_ID.getName());
            keyFields[USER_MAIL_INDEX] = userType.getField(UserReservedFieldEnumType.MAIL.getName());
            keyFields[HOST_SID_INDEX] = hostType.getField(ComputerReservedFieldEnumType.WINDOWS_SID.getName());
            keyFields[HOST_UID_INDEX] = hostType.getField(ComputerReservedFieldEnumType.UNIX_ID.getName());
            keyFields[APP_FP_INDEX] = appType.getField(ApplicationReservedFieldEnumType.APP_FINGER_PRINT.getName());
            keyFields[CONTACT_EMAIL_INDEX] = contactType.getField(ContactReservedFieldEnumType.MAIL.getName());
        } catch (DictionaryException e) {
            throw new IllegalStateException("Unable to obtain system types from the dictionary.", e);
        }
        lifecycleManager = (LifecycleManager) cm.getComponent(LifecycleManager.COMP_INFO);
        // Setup Subject Cache
        CacheManager cacheManager;
        try {
            cacheManager = CacheManager.create();
            final String cacheName = getClass().getName();
            subjectCache = cacheManager.getCache(cacheName);
            if (subjectCache == null) {
                subjectCache = new Cache(cacheName, 8, false, true, 0, 0);
                cacheManager.addCache(subjectCache);
            }
        } catch (CacheException e) {
            if (getLog().isWarnEnabled()) {
                getLog().warn("Unable to create subject cache -using uncached mode.", e);
            }
            // caching will be disabled
            subjectCache = null;
        }

        // Get list of configured actions
        IDestinyConfigurationStore store = (IDestinyConfigurationStore) cm.getComponent(DestinyConfigurationStoreImpl.COMP_INFO);
        configuredActions = new HashSet<IPredicate>();

        if (store.retrieveActionListConfig() != null) {
            for (IActionConfigDO configAction : store.retrieveActionListConfig().getActions()) {
                configuredActions.add(DAction.getAction(configAction.getName()));
            }
        }
    }

    /**
     * Returns the log.
     * 
     * @return the log.
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * Sets the log
     * 
     * @param log The log to set.
     */
    public void setLog(Log log) {
        this.log = log;
    }

}
