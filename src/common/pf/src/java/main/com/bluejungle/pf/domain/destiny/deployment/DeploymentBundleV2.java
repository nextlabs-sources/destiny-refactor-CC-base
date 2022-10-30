package com.bluejungle.pf.domain.destiny.deployment;

/*
 * All sources, binaries and HTML pages (C) Copyright 2009 by NextLabs Inc,
 * San Mateo, CA. Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/deployment/DeploymentBundleV2.java#1 $
 */

import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A deployment bundle is a self-contained collection of information
 * required for policy deployment.
 */

public class DeploymentBundleV2 extends DeploymentBundle implements IDeploymentBundleV2 {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    // Replacement for actionToPolicy in DeploymentBundle
    private Map<String, BitSet> actionNameToPolicy;

    /**
     * timestamp of this deployment bundle
     */
    private Calendar timestamp;

    public DeploymentBundleV2(Calendar timestamp) {
        super(timestamp);
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
    public DeploymentBundleV2(
        String deploymentEntities
    ,   Map<Long,BitSet> subjectToPolicy
    ,   Map<String,BitSet> actionNameToPolicy
    ,   Map<Long,BitSet> subjectToGroup
    ,   Collection<ISubjectKeyMapping> subjectKeyMappings
    ,   BitSet policiesForAllUsers
    ,   BitSet policiesForAllHosts
    ,   BitSet policiesForAllApps
    ,   Calendar timestamp) {

        super(deploymentEntities,
              subjectToPolicy,
              new HashMap<Long, BitSet>(), // we do actions a different way
              subjectToGroup,
              subjectKeyMappings,
              policiesForAllUsers,
              policiesForAllHosts,
              policiesForAllApps,
              timestamp);

        if (actionNameToPolicy == null) {
            throw new NullPointerException("actionNameToPolicy");
        }
        this.actionNameToPolicy = Collections.unmodifiableMap(actionNameToPolicy);
    }

    /**
     * @see IDeploymentBundleV2#getActionToPolicy()
     */
    public Map<String, BitSet> getActionNameToPolicy() {
        return actionNameToPolicy;
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
            out.append(getDeploymentEntities());
            out.append("\nPolicies that apply to all users: ");
            out.append(getPoliciesForAllUsers());
            out.append("\nPolicies that apply to all hosts: ");
            out.append(getPoliciesForAllHosts());
            out.append("\nPolicies that apply to all applications: ");
            out.append(getPoliciesForAllApps());
            out.append("\nSubject-Policy Mappings:\n");
            mapToStringV2(out, getSubjectToPolicy());
            out.append("Action-Policy Mappings:\n");
            strmapToStringV2(out, actionNameToPolicy);
            out.append("Subject-Group Mappings:\n");
            mapToStringV2(out, getSubjectToGroup());
            out.append("Subject keys:\n");
            for (ISubjectKeyMapping mapping : getSubjectKeyMappings()) {
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
     * Converts all int->bitset map entries to strings.
     *
     * @param out the StringBuffer to which to add the output.
     * @param map the map to be converted.
     */
    private static void mapToStringV2(StringBuffer out, Map<Long,BitSet> map) {
        for (Map.Entry<Long,BitSet> entry : map.entrySet()) {
            out.append(entry.getKey());
            out.append(" -> ");
            out.append(entry.getValue());
            out.append('\n');
        }
    }

    /**
     * Converts all string->bitset map entries to strings.
     *
     * @param out the StringBuffer to which to add the output.
     * @param map the map to be converted.
     */
    private static void strmapToStringV2(StringBuffer out, Map<String,BitSet> map) {
        for (Map.Entry<String,BitSet> entry : map.entrySet()) {
            out.append(entry.getKey());
            out.append(" -> ");
            out.append(entry.getValue());
            out.append('\n');
        }
    }
}
