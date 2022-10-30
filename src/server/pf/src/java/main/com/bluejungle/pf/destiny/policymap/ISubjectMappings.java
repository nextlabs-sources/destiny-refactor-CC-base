package com.bluejungle.pf.destiny.policymap;

import java.util.BitSet;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006
 * by Blue Jungle Inc, San Mateo, CA. Ownership remains with
 * Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/destiny/policymap/ISubjectMappings.java#1 $
 */

/**
 * Defines an interface for reporting subject mappings.
 */
public interface ISubjectMappings {

    interface Mapping {
        public long getId();
        public BitSet getPolicies();
        public BitSet getGroups();
    }

    boolean isDefinedId(long id);

    BitSet getPolicyMappings(long id);

    BitSet getGroupMappings(long id);

    Mapping getMapping(long id);

    void addMappings(
        long[] ids
    ,   BitSet[] policyMappings
    ,   BitSet[] groupMappings
    ,   BitSet defaultPolicies
    ,   BitSet defaultGroups);

    int size();

}
