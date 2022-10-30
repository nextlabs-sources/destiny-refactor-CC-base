/*
 * Created on Jan 3, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentDO;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentPolicyAssemblyStatus;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentProfileStatus;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentRegistrationDO;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO;
import com.bluejungle.version.IVersion;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * This class implements the persisted Agent object.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */
public class AgentDO implements IAgentDO {

    static final Integer NO_PUSH_PORT = new Integer(-1);

    /**
     * Private variables:
     */
    private final Calendar currentTime = Calendar.getInstance();
    private Long id;
    private IAgentType type;
    private String host;
    private Integer pushPort;
    private Calendar lastHeartbeat;
    private boolean isPushReady;
    private ICommProfileDO commProfile;
    private IAgentProfileDO agentProfile;
    private AgentPolicyAssemblyStatus policyAssemblyStatus;
    private AgentProfileStatus profileStatus;
    private Set registrations;
    private boolean registered;
    private IVersion version;

    /**
     * Zero-argument Constructor
     * 
     */
    public AgentDO() {
        policyAssemblyStatus = new AgentPolicyAssemblyStatus();
        profileStatus = new AgentProfileStatus();
    }

    /**
     * Returns the id.
     * 
     * @return the id.
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the id
     * 
     * @param id
     *            The id to set.
     */
    public void setId(Long id) {
        if (id == null) {
            throw new NullPointerException("id cannot be null.");
        }

        this.id = id;
    }

    /**
     * Returns the type.
     * 
     * @return the type.
     */
    public IAgentType getType() {
        return this.type;
    }

    /**
     * Sets the type
     * 
     * @param type
     *            The type to set.
     */
    public void setType(IAgentType type) {
        if (type == null) {
            throw new NullPointerException("type cannot be null.");
        }

        this.type = type;
    }

    /**
     * Returns the host.
     * 
     * @return the host.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Sets the host
     * 
     * @param host
     *            The host to set.
     */
    public void setHost(String host) {
        if (host == null) {
            throw new NullPointerException("host cannot be null.");
        }

        this.host = host;
    }

    /**
     * Returns the pushPort.
     * 
     * @return the pushPort.
     */
    public Integer getPushPort() {
        if (this.pushPort.equals(NO_PUSH_PORT)) {
            throw new IllegalStateException("Push port is not yet set");
        }

        return this.getPushPortInternal();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.agentmgr.IAgentDO#hasPushPort()
     */
    public boolean hasPushPort() {
        return !this.pushPort.equals(NO_PUSH_PORT);
    }

    /**
     * Sets the pushPort
     * 
     * @param pushPort
     *            The pushPort to set.
     */
    public void setPushPort(Integer pushPort) {
        if (pushPort == null) {
            throw new NullPointerException("pushPort cannot be null.");
        }

        this.pushPort = pushPort;
    }

    /**
     * Returns the lastHeartbeat.
     * 
     * @return the lastHeartbeat.
     */
    public Calendar getLastHeartbeat() {
        return this.lastHeartbeat;
    }

    /**
     * Sets the lastHeartbeat
     * 
     * @param lastHeartbeat
     *            The lastHeartbeat to set.
     */
    public void setLastHeartbeat(Calendar lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    /**
     * Returns whether the agent is online.
     * 
     * @return online status.
     */
    public boolean isOnline() {
        if (!isRegistered()) {
            return false;
        }
        if (getLastHeartbeat() == null) {
            return false;
        } else {
            final Calendar expectedHeartbeat = Calendar.getInstance();
            expectedHeartbeat.setTimeInMillis(getLastHeartbeat().getTimeInMillis());
            expectedHeartbeat.add(Calendar.DAY_OF_YEAR, +1);
            if (expectedHeartbeat.after(this.currentTime)) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Returns the agentProfile.
     * 
     * @return the agentProfile.
     */
    public IAgentProfileDO getAgentProfile() {
        return this.agentProfile;
    }

    /**
     * Sets the agentProfile
     * 
     * @param agentProfile
     *            The agentProfile to set.
     */
    public void setAgentProfile(IAgentProfileDO agentProfile) {
        if (agentProfile == null) {
            throw new NullPointerException("agentProfile cannot be null.");
        }

        this.agentProfile = agentProfile;
    }

    /**
     * Returns the commProfile.
     * 
     * @return the commProfile.
     */
    public ICommProfileDO getCommProfile() {
        return this.commProfile;
    }

    /**
     * Sets the commProfile
     * 
     * @param commProfile
     *            The commProfile to set.
     */
    public void setCommProfile(ICommProfileDO commProfile) {
        if (commProfile == null) {
            throw new NullPointerException("commProfile cannot be null.");
        }

        this.commProfile = commProfile;
    }

    /**
     * Returns the isPushReady.
     * 
     * @return the isPushReady.
     */
    public boolean getIsPushReady() {
        return this.isPushReady;
    }

    /**
     * Sets the isPushEnabled
     * 
     * @param isPushEnabled
     *            The isPushEnabled to set.
     */
    public void setIsPushReady(boolean isPushReady) {
        this.isPushReady = isPushReady;
    }

    /**
     * Returns the policyAssemblyStatus.
     * 
     * @return the policyAssemblyStatus.
     */
    public IAgentPolicyAssemblyStatus getPolicyAssemblyStatus() {
        return this.policyAssemblyStatus;
    }

    /**
     * Sets the policyAssemblyStatus
     * 
     * @param policyAssemblyStatus
     *            The policyAssemblyStatus to set.
     */
    public void setPolicyAssemblyStatus(AgentPolicyAssemblyStatus policyAssemblyStatus) {
        if (policyAssemblyStatus == null) {
            throw new NullPointerException("policyAssemblyStatus cannot be null.");
        }

        this.policyAssemblyStatus = policyAssemblyStatus;
    }

    /**
     * Reset the policy assembly status
     */
    public void resetPolicyAssemblyStatus() {
        this.policyAssemblyStatus = new AgentPolicyAssemblyStatus();
    }

    /**
     * Returns the profileStatus.
     * 
     * @return the profileStatus.
     */
    public IAgentProfileStatus getProfileStatus() {
        return this.profileStatus;
    }

    /**
     * Sets the profileStatus
     * 
     * @param profileStatus
     *            The profileStatus to set.
     */
    public void setProfileStatus(AgentProfileStatus profileStatus) {
        if (profileStatus == null) {
            throw new NullPointerException("profileStatus cannot be null.");
        }

        this.profileStatus = profileStatus;
    }

    /**
     * Reset the profile status
     */
    public void resetProfileStatus() {
        this.profileStatus = new AgentProfileStatus();
    }

    /**
     * Returns the registrations.
     * 
     * @return the registrations.
     */
    public Set getRegistrations() {
        return this.registrations;
    }

    /**
     * Adds a new registration
     * 
     */
    public IAgentRegistrationDO addNewRegistration() {
        if (this.registrations == null) {
            this.registrations = new HashSet();
        }
        AgentRegistrationDO registration = new AgentRegistrationDO();
        registration.setRegistrationTime(Calendar.getInstance());
        registration.setAgent(this);
        this.registrations.add(registration);
        return registration;
    }

    /**
     * Sets the registrations
     * 
     * @param registrations
     *            The registrations to set.
     */
    public void setRegistrations(Set registrations) {
        if (registrations == null) {
            throw new NullPointerException("registrations cannot be null.");
        }

        this.registrations = registrations;
    }

    /**
     * Set whether or not this agent is registered
     * 
     * @param registered
     *            true for registered; false otherwise
     */
    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    /**
     * Determine if this agent is registered
     * 
     * @return true if registered; false otherwise
     */
    public boolean isRegistered() {
        return this.registered;
    }

    /**
     * Returns the version of the component
     * 
     * @return the version of the component
     */
    public IVersion getVersion() {
        return this.version;
    }

    /**
     * sets the version of the component
     */
    public void setVersion(IVersion version) {
        if (version == null) {
            throw new NullPointerException("version  cannot be null.");
        }

        this.version = version;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        // this is not suggested by Hibernate, but I think it's the best options
        boolean valueToReturn = false;
        if ((obj != null) && (obj.getClass().equals(AgentDO.class))) {
            valueToReturn = this.getId().equals(((AgentDO) obj).getId());
        }

        return valueToReturn;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.getId().hashCode();
    }

    /**
     * Used for hibernate only
     * 
     */
    Integer getPushPortInternal() {
        return this.pushPort;
    }

    /**
     * For Hibernate use only
     * 
     * @param pushPort
     */
    void setPushPortInternal(Integer pushPort) {
        this.setPushPort(pushPort);
    }
}
