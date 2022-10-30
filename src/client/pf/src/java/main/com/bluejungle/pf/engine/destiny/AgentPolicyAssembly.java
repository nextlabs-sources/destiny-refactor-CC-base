package com.bluejungle.pf.engine.destiny;

/*
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * San Mateo CA, Ownership remains with Blue Jungle Inc,
 * All rights reserved worldwide.
 *
 * @author pkeni
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/engine/destiny/AgentPolicyAssembly.java#1 $
 */

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.expressions.EvalValue;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.Multivalue;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.pf.destiny.parser.DefaultPQLVisitor;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundleV2;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle.ISubjectKeyMapping;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.subject.AgentSubjectManager;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectManager;
import com.bluejungle.pf.domain.destiny.subject.Location;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.subject.ISubjectType;

/**
 * Agent Policy Assembly.
 */

public class AgentPolicyAssembly implements IAgentPolicyAssembly, ITargetResolver {

    private final Calendar ts;
    private final IDPolicy[] policies;
    private final Map<String,Location> locationByName = new HashMap<String,Location>();
    private final Map<String,IAgentPolicyAssembly.ISubjectInfo>[] subjectByUid = new Map[SubjectType.numElements()];
    private final BitSet policiesForAllUsers;
    private final BitSet policiesForAllHosts;
    private final BitSet policiesForAllApps;
    private final BitSet policiesForAllActions;
    private final Map<Long,BitSet> subjectToPolicy;
    private final Map<String,BitSet> actionNameToPolicy;
    private final Map<SubjectType,IEvalValue> groupsOfUnknownType =
        new HashMap<SubjectType, IEvalValue>();
    private final Map<SubjectType,BitSet> policiesOfUnknownType =
        new HashMap<SubjectType, BitSet>();

    {
        for ( int i = 0 ; i != subjectByUid.length ; i++ ) {
            subjectByUid[i] = new HashMap<String,IAgentPolicyAssembly.ISubjectInfo>();
        }
    }

    AgentPolicyAssembly(IDeploymentBundleV2 bundle, IComponentManager cm) {
        this.ts = bundle.getTimestamp();
        this.policiesForAllApps = bundle.getPoliciesForAllApps();
        this.policiesForAllHosts = bundle.getPoliciesForAllHosts();
        this.policiesForAllUsers = bundle.getPoliciesForAllUsers();

        this.actionNameToPolicy = bundle.getActionNameToPolicy();
        this.subjectToPolicy = bundle.getSubjectToPolicy();


        final SortedMap<Long,IDPolicy> policyById = new TreeMap<Long,IDPolicy>();
        try {
            // Parse the PQL from the bundle to collect all policies and locations
            // (there will be nothing else in the bundle - server side makes sure of that).
            DomainObjectBuilder.processInternalPQL(
                bundle.getDeploymentEntities()
            ,   new DefaultPQLVisitor() {
                public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
                    policyById.put(descr.getId(), policy);
                }
                public void visitLocation(DomainObjectDescriptor descriptor, Location location) {
                    locationByName.put(descriptor.getName(), location);
                }
            });
        } catch (PQLException e) {
            throw new RuntimeException(e);
        }

        this.policies = policyById.values().toArray(new IDPolicy[policyById.size()]);

        this.policiesForAllActions = new BitSet();
        for (int i = 0; i < policies.length; i++) {
            IDPolicy p = policies[i];

            this.policiesForAllActions.set(i, p.getTarget().getActionPred().match(null));
        }

        Map<Long,BitSet> subjToGroup = bundle.getSubjectToGroup();
        Map<Long,BitSet> subjToPolicy = bundle.getSubjectToPolicy();
        Map<Long,List<String>> subjToEmail = new HashMap<Long,List<String>>();
        final Map<Long,IEvalValue> subjToEmailVal = new HashMap<Long,IEvalValue>();

        addUnknownGroup(SubjectType.USER, subjToGroup.get(IDeploymentBundle.KEY_OF_UNKNOWN_USER));
        addUnknownGroup(SubjectType.HOST, subjToGroup.get(IDeploymentBundle.KEY_OF_UNKNOWN_HOST));
        addUnknownGroup(SubjectType.APP, subjToGroup.get(IDeploymentBundle.KEY_OF_UNKNOWN_APPLICATION));
        addUnknownPolicy(SubjectType.USER, subjToPolicy.get(IDeploymentBundle.KEY_OF_UNKNOWN_USER));
        addUnknownPolicy(SubjectType.HOST, subjToPolicy.get(IDeploymentBundle.KEY_OF_UNKNOWN_HOST));
        addUnknownPolicy(SubjectType.APP, subjToPolicy.get(IDeploymentBundle.KEY_OF_UNKNOWN_APPLICATION));

        for ( ISubjectKeyMapping subjectKeyMapping : bundle.getSubjectKeyMappings()) {
            String subjectUid = uidWithCorrectCase(subjectKeyMapping);
            final Long subjectId = subjectKeyMapping.getId();
            if (subjectKeyMapping.getUidType().equals(UserReservedFieldEnumType.MAIL.getName())) {
                List<String> emails;
                if (subjToEmail.containsKey(subjectId)) {
                    emails = subjToEmail.get(subjectId);
                } else {
                    emails = new ArrayList<String>();
                    subjToEmail.put(subjectId, emails);
                }
                emails.add(subjectUid);
            }
            BitSet subjectGroupSet = subjToGroup.get(subjectId);
            ISubjectType subjType = subjectKeyMapping.getSubjectType();
            final IEvalValue subjectGroups;
            if (subjectGroupSet != null) {
                subjectGroups = EvalValue.build(Multivalue.create(subjectGroupSet));
            } else if (groupsOfUnknownType.containsKey(subjType)) {
                subjectGroups = groupsOfUnknownType.get(subjType);
            } else {
                subjectGroups = IEvalValue.EMPTY;
            }
            subjectByUid[subjType.getType()].put(
                subjectUid
            ,   new IAgentPolicyAssembly.ISubjectInfo() {
                public IEvalValue getSubjectGroups() {
                    return subjectGroups;
                }
                public Long getSubjectID() {
                    return subjectId;
                }
                public void addDynamicAttributes(DynamicAttributes attr) {
                    if (attr == null) {
                        return;
                    }
                    IEvalValue emails = subjToEmailVal.get(subjectId);
                    if (emails != null) {
                        attr.put(
                            SubjectAttribute.USER_EMAIL.getName()
                        ,   emails
                        );
                    }
                }
            });
        }
        for (Map.Entry<Long,List<String>> emails : subjToEmail.entrySet()) {
            subjToEmailVal.put(
                emails.getKey()
            ,   EvalValue.build(Multivalue.create(emails.getValue()))
            );
        }
        ((AgentSubjectManager)cm.getComponent(IDSubjectManager.COMP_INFO)).setAgentPolicyAssembly(this);
    }

    private String uidWithCorrectCase(ISubjectKeyMapping subjectKeyMapping) {
        String uid = subjectKeyMapping.getUid();

        // Host name lookup is done after converting the name to lower-case, so make sure that it's lower-case
        if (subjectKeyMapping.getSubjectType().getName().equals("host") &&
            subjectKeyMapping.getUidType().equals("uniqueName")) {
            uid = uid.toLowerCase();
        }

        // Strip trailing spaces from user sid
        if (subjectKeyMapping.getSubjectType().getName().equals("user") &&
            subjectKeyMapping.getUidType().equals("unixId")) {
            uid = uid.replaceAll("\\s+$", "");
        }

        return uid;
    }

    public BitSet getApplicablePolicies(EvaluationRequest request) {
        BitSet actionNameToPolicyBitSet = actionNameToPolicy.get(request.getAction().getName());

        if (actionNameToPolicyBitSet == null) {
            // Unknown action. If and only if a policy applies to all actions, it applies to this one
            actionNameToPolicyBitSet = policiesForAllActions;
        }

        BitSet candidatePolicies = (BitSet)actionNameToPolicyBitSet.clone();
        candidatePolicies.and(policiesForSubjectId(request.getUser().getId(), policiesForAllUsers, SubjectType.USER));
        candidatePolicies.and(policiesForSubjectId(request.getHost().getId(), policiesForAllHosts, SubjectType.HOST));
        candidatePolicies.and(policiesForSubjectId(request.getApplication().getId(), policiesForAllApps, SubjectType.APP));
        candidatePolicies.andNot(request.getFromResourceInfo().getNonMatchingFromPolicies());
        candidatePolicies.andNot(request.getToResourceInfo().getNonMatchingToPolicies());
        return candidatePolicies;
    }

    /**
     * @return
     */
    public Calendar getTimestamp() {
        return ts;
    }

    /**
     * @see IAgentPolicyAssembly#existsSubject(String, ISubjectType)
     */
    public boolean existsSubject(String uid, ISubjectType subjectType) {
        if (subjectType == null) {
            throw new NullPointerException("subjectType");
        }
        // The assembly will generate an ID for any application
        // with a non-null, non-empty UID.
        return (subjectType == SubjectType.APP && uid != null && uid.length() != 0) 
             || subjectByUid[subjectType.getType()].containsKey(uid);
    }

    /**
     * @see IAgentPolicyAssembly#getLocation(String)
     */
    public Location getLocation(String name) {
        return locationByName.get(name);
    }

    /**
     * Returns the policies.
     *
     * @return the policies.
     */
    public IDPolicy[] getPolicies() {
        return policies;
    }

    /**
     * Returns the number of policies.
     *
     * @return the number of policies.
     */
    public int getPolicyCount() {
        return policies.length;
    }

    private BitSet policiesForSubjectId(Long subjId, BitSet forAll, SubjectType type) {
        BitSet res = subjectToPolicy.get(subjId);
        if (res == null) {
            res = policiesOfUnknownType.get(type);
        }
        if (res != null) {
            res = (BitSet)res.clone();
            res.or(forAll);
        } else {
            res = (BitSet)forAll.clone();
        }
        return res;
    }

    /**
     * @see IAgentPolicyAssembly#getSubjectInfo(String, SubjectType)
     */
    public ISubjectInfo getSubjectInfo(String uid, ISubjectType subjectType) {
        if (subjectType == null) {
            throw new NullPointerException("subjectType");
        }
        return subjectByUid[subjectType.getType()].get(uid);
    }

    /**
     * @see IEngineSubjectResolver#getGroupsForSubject(String, ISubjectType)
     */
    public IEvalValue getGroupsForSubject(String uid, ISubjectType subjectType) {
        if (subjectType == null) {
            throw new NullPointerException("subjectType");
        }
        IAgentPolicyAssembly.ISubjectInfo subjInfo = getSubjectInfo(uid, subjectType);
        if (subjInfo != null) {
            return subjInfo.getSubjectGroups();
        }
        if (groupsOfUnknownType.containsKey(subjectType)) {
            return groupsOfUnknownType.get(subjectType);
        }
        return IEvalValue.EMPTY;
    }

    private void addUnknownGroup(SubjectType type, BitSet groups) {
        if (groups != null) {
            groupsOfUnknownType.put(type, EvalValue.build(Multivalue.create(groups)));
        } else {
            groupsOfUnknownType.put(type, IEvalValue.EMPTY);
        }
    }

    private void addUnknownPolicy(SubjectType type, BitSet policies) {
        if (policies != null) {
            policiesOfUnknownType.put(type, policies);
        } else {
            policiesOfUnknownType.put(type, new BitSet());
        }
    }

}
