package com.bluejungle.pf.domain.destiny.deployment;

/*
 * Created on Dec 13, 2004
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *
 * @author sasha, sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/deployment/IDeploymentBundle.java#1 $
 */

import java.io.Serializable;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

import com.bluejungle.pf.domain.epicenter.subject.ISubjectType;

/**
 * IDeploymentBundle represents a self-contained collection of policies along with all the supporting objects that are required to enforce those policies.
 * A bundle can be empty, or it can contain information.  If a bundle is empty, it means there are no updates available
 * for deployment.  An empty bundle contains nothing but a timestamp.
 */

public interface IDeploymentBundle extends Serializable {

    /**
     * This constant represents the key of the getSubjectToGroup entry
     * for unknown users.
     */
    long KEY_OF_UNKNOWN_USER = Long.MAX_VALUE-1;

    /**
     * This constant represents the key of the getSubjectToGroup entry
     * for unknown computers.
     */
    long KEY_OF_UNKNOWN_HOST = Long.MAX_VALUE-2;

    /**
     * This constant represents the key of the getSubjectToGroup entry
     * for unknown applications.
     */
    long KEY_OF_UNKNOWN_APPLICATION = Long.MAX_VALUE-3;

    /**
     * This constant represents the UID of the getSubjectToGroup entry
     * for unknown users.
     */
    String UID_OF_UNKNOWN_USER = "{UNKNOWN-USER}";

    /**
     * This constant represents the UID of the getSubjectToGroup entry
     * for unknown computers.
     */
    String UID_OF_UNKNOWN_HOST = "{UNKNOWN-HOST}";

    /**
     * This constant represents the UID of the getSubjectToGroup entry
     * for unknown applications.
     */
    String UID_OF_UNKNOWN_APPLICATION = "{UNKNOWN-APP}";

    /**
     * This interface defines the contract for mappings
     * of subject IDs to system-dependent IDs (UIDs).
     * Subject key mappings <code>Collection</code>
     * contains objects implementing this interface.
     */
    interface ISubjectKeyMapping {
        /**
         * Returns the UID.
         * @return the UID.
         */
        String getUid();
        /**
         * Returns the name of the UID type.
         * @return the name of the UID type.
         */
        String getUidType();
        /**
         * Returns the internal ID of the subject.
         * @return the internal ID of the subject.
         */
        Long getId();
        /**
         * Returns the subject type of the subject represented in this mapping.
         * @return the subject type of the subject represented in this mapping.
         */
        ISubjectType getSubjectType();
    }

    /**
     * Checks whether this bundle contains any information or not.  Attempts
     * to retrieve any fields besides a timestamp on an empty bundle results
     * in a runtime exception.
     *
     * @return true if this bundle has no new information, false otherwise
     */
    boolean isEmpty();

    /**
     * @return all the deployment entities represented as one PQL program
     * @throws RuntimeException if the bundle is empty
     */
    String getDeploymentEntities();

    /**
     * @return all the subjects in this bundle, members of the set are of type IDSubject
     * @throws IllegalStateException if the bundle is empty
     */
    Collection<ISubjectKeyMapping> getSubjectKeyMappings();

    /**
     * @return subject id -> policy bitset
     * @throws IllegalStateException if the bundle is empty
     */
    Map<Long,BitSet> getSubjectToPolicy();

    /**
     * @return action id -> policy bitset
     * @throws IllegalStateException if the bundle is empty
     */
    Map<Long,BitSet> getActionToPolicy();

    /**
     * @return subject id -> group bitset
     * @throws IllegalStateException if the bundle is empty
     */
    Map<Long,BitSet> getSubjectToGroup();

    /**
     * Returns a <code>BitSet</code> defining the policies that apply to all users.
     * @return a <code>BitSet</code> defining the policies that apply to all users.
     */
    BitSet getPoliciesForAllUsers();

    /**
     * Returns a <code>BitSet</code> defining the policies that apply to all hosts.
     * @return a <code>BitSet</code> defining the policies that apply to all hosts.
     */
    BitSet getPoliciesForAllHosts();

    /**
     * Returns a <code>BitSet</code> defining the policies that apply to all applications.
     * @return a <code>BitSet</code> defining the policies that apply to all applications.
     */
    BitSet getPoliciesForAllApps();

    /**
     * @return the time this bundle was acquired
     */
    Calendar getTimestamp();

}
