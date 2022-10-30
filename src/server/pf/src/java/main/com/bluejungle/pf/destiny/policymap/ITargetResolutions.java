package com.bluejungle.pf.destiny.policymap;

import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.Map;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006
 * by Blue Jungle Inc, San Mateo, CA. Ownership remains with
 * Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/destiny/policymap/ITargetResolutions.java#1 $
 */

/**
 * This interface combines all data relevant to
 * target resolutions.
 */
public interface ITargetResolutions {

    ISubjectMappings getSubjectMappings();

    Map<Long,BitSet> getActionMappings();

    Map<String,BitSet> getActionNameMappings();

    List<String> getPolicies();

    BitSet getPoliciesForAllUsers();

    BitSet getPoliciesForAllHosts();

    BitSet getPoliciesForAllApps();

    Date getBuildTime();

}
