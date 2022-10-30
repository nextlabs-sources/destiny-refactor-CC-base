/*
 * Created on Apr 5, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.destiny.policymap;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.lib.DTOUtils;

/**
 * In the future this might become the log for Server Target Resolver. For now
 * this just records the last rebuild time.
 *
 * @author sasha
 * @author sergey
 * @author amorgan
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/pf/src/java/main/com/bluejungle/pf/destiny/policymap/STRLog.java#5 $:
 */

public class STRLog implements ITargetResolutions, Serializable {

    /**
     * The default serial version ID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * The ID of this STRLog record.
     */
    private Long id;

    /** timestamp of the last build of the maps */
    private Date buildTime;

    /**
     * The domains over which this STRLog is relevant
     */
    private String[] domains;

    /* These two fields, actionMappings and actionNameMappings,
     * represent roughly the same information.  actionMappings maps
     * the built-in action ids to policies.  actionNameMappings does
     * it by name, but includes the new user defined actions as well.
     * The name mapping is given to new policy controllers, the id
     * mapping to older ones.  Keep these in sync or bad things will
     * happen.
     */
    private Map<Long,BitSet> actionMappings;
    private Map<String,BitSet> actionNameMappings;
    private ISubjectMappings subjectMappings;
    private List<String> policies;
    private BitSet policiesForAllUsers;
    private BitSet policiesForAllHosts;
    private BitSet policiesForAllApps;

    /**
     * Version of the log entry -- used for locking only
     */
    private long version;

    /**
     * Constructor, package visibility for STR use only.
     *
     * @param domains an array of domain names identifying this STRLog record.
     * @param buildTime time of the last build of the maps
     */
    STRLog(String[] domains, Date buildTime) {
        if (domains == null) {
            throw new NullPointerException("domains");
        }
        if (buildTime == null) {
            throw new NullPointerException("buildTime");
        }
        this.domains = domains.clone();
        for (String domain : this.domains) {
            if (domain == null) {
                throw new IllegalArgumentException("domains[]");
            }
        }
        Arrays.sort(this.domains, String.CASE_INSENSITIVE_ORDER);
        this.buildTime = UnmodifiableDate.forDate(buildTime);
    }

    /**
     * Internal constructor for hibernate use only.
     */
    STRLog() {
    }

    /**
     * @return timestamp of the last build of the maps
     */
    public Date getBuildTime() {
        return buildTime;
    }

    /**
     * package visibility for STR use only.
     *
     * @param buildTime
     */
    void setBuildTime(Date buildTime) {
        this.buildTime = UnmodifiableDate.forDate(buildTime);
    }

    /**
     * internal method for hibernate use only. Returns the id.
     *
     * @return the id.
     */
    Long getId() {
        return this.id;
    }

    /**
     * internal method for hibernate use only. Sets the id
     *
     * @param id The id to set.
     */
    void setId(Long id) {
        this.id = id;
    }

    /**
     * Retrieve the domains.
     *
     * @return the domains.
     */
    String[] getDomains() {
        return this.domains;
    }

    /**
     * Set the domains
     *
     * @param domains The domains to set.
     */
    void setDomains(String[] domains) {
        this.domains = domains;
    }

    /**
     * Returns the version.
     *
     * @return the version.
     */
    public long getVersion() {
        return this.version;
    }

    /**
     * Sets the version
     *
     * @param version The version to set.
     */
    void setVersion(long version) {
        this.version = version;
    }

    public String toString() {
        StringBuffer domainsAsString = new StringBuffer();
        for (int i = 0; i < this.domains.length; i++) {
            domainsAsString.append(this.domains[i]);
        }
        return "Build Time: " + this.buildTime + ", Domains: " + domainsAsString.toString() + ", version: " + version;
    }

    /**
     * @return the policiesForAllUsers
     */
    public BitSet getPoliciesForAllUsers() {
        return policiesForAllUsers;
    }

    /**
     * Sets the policiesForAllUsers.
     * @param policiesForAllUsers the policiesForAllUsers to set
     */
    public void setPoliciesForAllUsers(BitSet policiesForAllUsers) {
        this.policiesForAllUsers = policiesForAllUsers;
    }

    /**
     * Gets the policiesForAllUsers's inner array. For hibernate use only.
     */
    byte[] getPoliciesForAllUsersData() {
        return DTOUtils.serializeBitset(policiesForAllUsers);
    }

    /**
     * Sets the policiesForAllUsers. For hibernate use only.
     * @param data The value to set.
     */
    void setPoliciesForAllUsersData(byte[] data) {
        policiesForAllUsers = DTOUtils.deserializeBitset(data);
    }

    /**
     * @return the policiesForAllHosts
     */
    public BitSet getPoliciesForAllHosts() {
        return policiesForAllHosts;
    }

    /**
     * Sets the policiesForAllHosts.
     * @param policiesForAllHosts the policiesForAllHosts to set
     */
    public void setPoliciesForAllHosts(BitSet policiesForAllHosts) {
        this.policiesForAllHosts = policiesForAllHosts;
    }

    /**
     * Gets the policiesForAllHosts's inner array. For hibernate use only.
     */
    byte[] getPoliciesForAllHostsData() {
        return DTOUtils.serializeBitset(policiesForAllHosts);
    }

    /**
     * Sets the policiesForAllHosts. For hibernate use only.
     * @param data The value to set.
     */
    void setPoliciesForAllHostsData(byte[] data) {
        policiesForAllHosts = DTOUtils.deserializeBitset(data);
    }

    /**
     * @return the policiesForAllApps
     */
    public BitSet getPoliciesForAllApps() {
        return policiesForAllApps;
    }

    /**
     * Sets the policiesForAllApps.
     * @param policiesForAllApps the policiesForAllApps to set
     */
    public void setPoliciesForAllApps(BitSet policiesForAllApps) {
        this.policiesForAllApps = policiesForAllApps;
    }

    /**
     * Gets the policiesForAllApps's inner array. For hibernate use only.
     */
    byte[] getPoliciesForAllAppsData() {
        return DTOUtils.serializeBitset(policiesForAllApps);
    }

    /**
     * Sets the policiesForAllApps. For hibernate use only.
     * @param data The value to set.
     */
    void setPoliciesForAllAppsData(byte[] data) {
        policiesForAllApps = DTOUtils.deserializeBitset(data);
    }

    /**
     * @return the actionNameMappings
     */
    public Map<String,BitSet> getActionNameMappings() {
        return actionNameMappings;
    }

    /**
     * Sets the actionNameMappings.
     * @param actionNameMappings the actionNameMappings to set
     */
    public void setActionNameMappings(Map<String,BitSet> actionNameMappings) {
        this.actionNameMappings = actionNameMappings;
    }

    /**
     * @return the actionMappings
     */
    public Map<Long,BitSet> getActionMappings() {
        return actionMappings;
    }

    /**
     * Sets the actionMappings.
     * @param actionMappings the actionMappings to set
     */
    public void setActionMappings(Map<Long,BitSet> actionMappings) {
        this.actionMappings = actionMappings;
    }

    /**
     * @return the subjectMappings
     */
    public ISubjectMappings getSubjectMappings() {
        return subjectMappings;
    }

    /**
     * Sets the subjectMappings.
     * @param subjectMappings the subjectMappings to set
     */
    public void setSubjectMappings(ISubjectMappings subjectMappings) {
        this.subjectMappings = subjectMappings;
    }

    /**
     * @return the policies
     */
    public List<String> getPolicies() {
        return policies;
    }

    /**
     * Sets the policies.
     * @param policies the policies to set
     */
    public void setPolicies(List<String> policies) {
        this.policies = policies;
    }

}
