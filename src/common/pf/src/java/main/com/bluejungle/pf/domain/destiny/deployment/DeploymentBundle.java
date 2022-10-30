package com.bluejungle.pf.domain.destiny.deployment;

/*
 * All sources, binaries and HTML pages (C) Copyright 2005 by NextLabs Inc,
 * San Mateo, CA. Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @author sasha, sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/deployment/DeploymentBundle.java#1 $
 */

import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * A deployment bundle is a self-contained collection of information
 * required for policy deployment.
 */
public class DeploymentBundle implements IDeploymentBundle {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -8134309673472047755L;
    
    private boolean empty;

    /**
     * deployment entities, expressed as one PQL <code>String</code>.
     */
    private String deploymentEntities;

    private Map<Long,BitSet> subjectToPolicy;
    private Map<Long,BitSet> actionToPolicy;
    private Map<Long,BitSet> subjectToGroup;
    private BitSet policiesForAllUsers;
    private BitSet policiesForAllHosts;
    private BitSet policiesForAllApps;
    private Collection<ISubjectKeyMapping> subjectKeyMappings;

    /**
     * timestamp of this deployment bundle
     */
    private Calendar timestamp;

    public DeploymentBundle(Calendar timestamp) {
        if (timestamp == null) {
            throw new NullPointerException("timestamp cannot be null");
        }
        this.timestamp = timestamp;
        empty = true;
    }

    /**
     * Constructor
     *
     * @param deploymentEntities
     * @param entityMap
     * @param subjectMap
     * @param subjects
     * @param timestamp
     */
    public DeploymentBundle(
        String deploymentEntities
    ,   Map<Long,BitSet> subjectToPolicy
    ,   Map<Long,BitSet> actionToPolicy
    ,   Map<Long,BitSet> subjectToGroup
    ,   Collection<ISubjectKeyMapping> subjectKeyMappings
    ,   BitSet policiesForAllUsers
    ,   BitSet policiesForAllHosts
    ,   BitSet policiesForAllApps
    ,   Calendar timestamp) {
        if (timestamp == null) {
            throw new NullPointerException("timestamp cannot be null");
        }
        if (deploymentEntities != null && deploymentEntities.length() != 0) {
            if (subjectToPolicy == null) { // Empty mapping is OK
                throw new NullPointerException("subjectToPolicy");
            }
            if (actionToPolicy == null) { // Empty mapping is OK
                throw new NullPointerException("actionToPolicy");
            }
            if (subjectToGroup == null) { // Empty mapping is OK
                throw new NullPointerException("subjectToGroup");
            }
            if (subjectKeyMappings == null) { // Empty mapping is OK
                throw new NullPointerException("subjectKeyMappings");
            }
            if (policiesForAllApps == null) {
                throw new NullPointerException("policiesForAllApps");
            }
            if (policiesForAllHosts == null) {
                throw new NullPointerException("policiesForAllHosts");
            }
            if (policiesForAllUsers == null) {
                throw new NullPointerException("policiesForAllUsers");
            }
            this.deploymentEntities = deploymentEntities;
            this.subjectToPolicy = Collections.unmodifiableMap(subjectToPolicy);
            this.actionToPolicy = Collections.unmodifiableMap(actionToPolicy);
            this.subjectToGroup = Collections.unmodifiableMap(subjectToGroup);
            this.subjectKeyMappings = Collections.unmodifiableCollection(subjectKeyMappings);
            this.policiesForAllApps = (BitSet)policiesForAllApps.clone();
            this.policiesForAllHosts = (BitSet)policiesForAllHosts.clone();
            this.policiesForAllUsers = (BitSet)policiesForAllUsers.clone();
            empty = false;
        } else {
            empty = true;
        }
        this.timestamp = timestamp;
    }

    /**
     * @see IDeploymentBundle#getTimestamp()
     */
    public Calendar getTimestamp() {
        return timestamp;
    }

    /**
     * @see IDeploymentBundle#getDeploymentEntities()
     */
    public String getDeploymentEntities() {
        if (empty) {
            throw new RuntimeException("Deployment bundle is empty");
        }
        return deploymentEntities;
    }

    /**
     * @see IDeploymentBundle#getActionToPolicy()
     */
    public Map<Long,BitSet> getActionToPolicy() {
        return actionToPolicy;
    }

    /**
     * @see IDeploymentBundle#getSubjectKeyMappings()
     */
    public Collection<ISubjectKeyMapping> getSubjectKeyMappings() {
        return subjectKeyMappings;
    }

    /**
     * @see IDeploymentBundle#getSubjectToGroup()
     */
    public Map<Long,BitSet> getSubjectToGroup() {
        return subjectToGroup;
    }

    /**
     * @see IDeploymentBundle#getSubjectToPolicy()
     */
    public Map<Long,BitSet> getSubjectToPolicy() {
        return subjectToPolicy;
    }

    /**
     * @see IDeploymentBundle#isEmpty()
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * @see IDeploymentBundle#getPoliciesForAllApps()
     */
    public BitSet getPoliciesForAllApps() {
        if (empty) {
            throw new IllegalStateException("Call to IDeploymentBundle#getPoliciesForAllApps() of an empty bundle.");
        }
        assert policiesForAllApps != null; // Constructor checks this when the bundle is not empty
        return (BitSet)policiesForAllApps.clone();
    }

    /**
     * @see IDeploymentBundle#getPoliciesForAllHosts()
     */
    public BitSet getPoliciesForAllHosts() {
        if (empty) {
            throw new IllegalStateException("Call to IDeploymentBundle#getPoliciesForAllHosts() of an empty bundle.");
        }
        assert policiesForAllHosts != null; // Constructor checks this when the bundle is not empty
        return (BitSet)policiesForAllHosts.clone();
    }

    /**
     * @see IDeploymentBundle#getPoliciesForAllUsers()
     */
    public BitSet getPoliciesForAllUsers() {
        if (empty) {
            throw new IllegalStateException("Call to IDeploymentBundle#getPoliciesForAllUsers() of an empty bundle.");
        }
        assert policiesForAllUsers != null; // Constructor checks this when the bundle is not empty
        return (BitSet)policiesForAllUsers.clone();
    }

    /**
     * Converts the bundle to a String for printing.
     *
     * @see Object#toString()
     */
    public String toString() {
        StringBuffer out = new StringBuffer("Deployment Bundle:\n");
        if ( !isEmpty() ) {
            out.append("Deployment entities:\n");
            out.append(deploymentEntities);
            out.append("\nPolicies that apply to all users: ");
            out.append(policiesForAllUsers);
            out.append("\nPolicies that apply to all hosts: ");
            out.append(policiesForAllHosts);
            out.append("\nPolicies that apply to all applications: ");
            out.append(policiesForAllApps);
            out.append("\nSubject-Policy Mappings:\n");
            mapToString(out, subjectToPolicy);
            out.append("Action-Policy Mappings:\n");
            mapToString(out, actionToPolicy);
            out.append("Subject-Group Mappings:\n");
            mapToString(out, subjectToGroup);
            out.append("Subject keys:\n");
            for (ISubjectKeyMapping mapping : subjectKeyMappings) {
                out.append(mapping.getUid());
                out.append(" (");
                out.append(mapping.getSubjectType().getName());
                out.append('-');
                out.append(mapping.getUidType());
                out.append(") -> ");
                out.append(mapping.getId());
                out.append('\n');
            }
        } else {
            out.append("<EMPTY>");
        }
        return out.toString();
    }

    /**
     * Converts all map entries to strings.
     *
     * @param out the StringBuffer to which to add the output.
     * @param map the map to be converted.
     */
    private static void mapToString(StringBuffer out, Map<Long,BitSet> map) {
        for (Map.Entry<Long,BitSet> entry : map.entrySet()) {
            out.append(entry.getKey());
            out.append(" -> ");
            out.append(entry.getValue());
            out.append('\n');
        }
    }

}
