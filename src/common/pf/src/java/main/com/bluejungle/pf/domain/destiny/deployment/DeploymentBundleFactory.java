package com.bluejungle.pf.domain.destiny.deployment;

/*
 * All sources, binaries and HTML pages (C) Copyright 2005 by NextLabs Inc,
 * San Mateo, CA. Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @author sasha, sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/deployment/DeploymentBundleFactory.java#1 $
 */

import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

import com.bluejungle.version.IVersion;
import com.bluejungle.versionfactory.VersionFactory;

public class DeploymentBundleFactory {
    private static final IVersion VERSION_WITH_NEW_BUNDLE = VersionFactory.makeVersion(4, 5, 0, 0, 0);

    public static IDeploymentBundle makeBundle(String policies,
                                               Map<Long, BitSet> subjectToPolicy,
                                               Map<Long, BitSet> actionToPolicy,
                                               Map<String, BitSet> actionNameToPolicy,
                                               Map<Long, BitSet> subjectToGroup,
                                               Collection<IDeploymentBundle.ISubjectKeyMapping> subjectKeyMappings,
                                               BitSet policiesForAllUsers,
                                               BitSet policiesForAllHosts,
                                               BitSet policiesForAllApps,
                                               Calendar timestamp,
                                               IVersion version) {
        if (version != null && version.compareTo(VERSION_WITH_NEW_BUNDLE) < 0) {
            return new DeploymentBundle(policies,
                                        subjectToPolicy,
                                        actionToPolicy,
                                        subjectToGroup,
                                        subjectKeyMappings,
                                        policiesForAllUsers,
                                        policiesForAllHosts,
                                        policiesForAllApps,
                                        timestamp);
        } else {
            return new DeploymentBundleV2(policies,
                                          subjectToPolicy,
                                          actionNameToPolicy,  // differs from DeploymentBundle
                                          subjectToGroup,
                                          subjectKeyMappings,
                                          policiesForAllUsers,
                                          policiesForAllHosts,
                                          policiesForAllApps,
                                          timestamp);
        }
    }

    public static IDeploymentBundle makeBundle(String policies,
                                               Map<Long, BitSet> subjectToPolicy,
                                               Map<Long, BitSet> actionToPolicy,
                                               Map<String, BitSet> actionNameToPolicy,
                                               Map<Long, BitSet> subjectToGroup,
                                               Collection<IDeploymentBundle.ISubjectKeyMapping> subjectKeyMappings,
                                               BitSet policiesForAllUsers,
                                               BitSet policiesForAllHosts,
                                               BitSet policiesForAllApps,
                                               Calendar timestamp) {
        return makeBundle(policies, subjectToPolicy, actionToPolicy, actionNameToPolicy, subjectToGroup, subjectKeyMappings, policiesForAllUsers, policiesForAllHosts, policiesForAllApps, timestamp, VERSION_WITH_NEW_BUNDLE);
    }

    // empty bundle
    public static IDeploymentBundle makeBundle(Calendar timestamp,
                                               IVersion version) {
        if (version != null && version.compareTo(VERSION_WITH_NEW_BUNDLE) < 0) {
            return new DeploymentBundle(timestamp);
        } else {
            return new DeploymentBundleV2(timestamp);
        }
    }

    public static IDeploymentBundle makeBundle(Calendar timestamp) {
        return makeBundle(timestamp, VERSION_WITH_NEW_BUNDLE);
    }
}
