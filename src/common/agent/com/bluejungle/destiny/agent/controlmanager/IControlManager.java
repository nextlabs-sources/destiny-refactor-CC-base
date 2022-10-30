/*
 * Created on Dec 14, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager;

import java.math.BigInteger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bluejungle.destiny.services.management.types.AgentProfileDTO;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.types.AgentTypeDTO;
import com.bluejungle.domain.usersubjecttype.UserSubjectTypeEnumType;
import com.bluejungle.framework.utils.DynamicAttributes;
/**
 * This interface is implemented by ControlManager
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public interface IControlManager {

    public static final String NAME = "Control Manager";

    /**
     * @return the agent profile.
     */
    public AgentProfileDTO getAgentProfile();

    /**
     * @return the communication profile
     */
    public CommProfileDTO getCommunicationProfile();

    /**
     * Sends a heartbeat to the server.
     */
    public void sendHeartBeat();

    /**
     * Sends a request to the notification app
     * 
     * @param userId
     *            SID of user
     * @param time
     *            time of action
     * @param action
     *            action that this notification is related to
     * @param enforcement
     *            if not supplied, it will be DENY
     * @param resource
     *            file name
     * @param message
     *            single message to be displayed to the user
     */
    public void notifyUser(String userId, String time, String action, String enforcement, String resource, String message);

    /**
     * Sends a request to the notification app
     * 
     * @param userId
     *            SID of user
     * @param time
     *            time of action
     * @param action
     *            action that this notification is related to
     * @param enforcement
     *            if not supplied, it will be DENY
     * @param resource
     *            file name
     * @param messageList
     *            collection of messages to be displayed to the user
     */
    public void notifyUser(String userId, String time, String action, String enforcement, String resource, Collection messageList);

    /**
     * Sends a custom obligation request to the notification app
     *
     * @param userId
     *            SID of the user
     * @param commandLine
     *            executable command line
     */
    public void executeCustomObligations(String userId, String commandLine);

    /**
     * add user to list of logged in users
     * 
     * @param userSID
     */
    public void addLoggedInUser(String userSID);

    /**
     * remove user from list of logged in users
     * 
     * @param userSID
     */
    public void removeLoggedInUser(String userSID);

    /**
     * @return the agent id obtained during registration
     */
    public BigInteger getAgentId();

    /**
     * @return the registration id obtained during registration
     */
    public BigInteger getRegistrationId();

    /**
     * @param action
     * @return true if the passed in action should be tracked.
     */
    public boolean isTrackedAction(ActionEnumType action);

    /**
     * Logs tracking activity
     * 
     * @param action
     *            action being tracked
     * @param userId
     * @param hostId
     * @param applicationId
     * @param fromResource
     * @param toResource
     * @param level
     *            log level
     */
    public void logTrackingActivity(ActionEnumType action, Long userId, String userName,
                                    Long hostId, String hostName, String hostIP,
                                    Long applicationId, String appName,
                                    String fromResource, DynamicAttributes fromAttrs,
                                    String toResource, DynamicAttributes toAttrs,
                                    int level);

    public void logTrackingActivity(ActionEnumType action, Long userId, String userName,
                                    Long hostId, String hostName, String hostIP,
                                    Long applicationId, String appName,
                                    String fromResource, String toResource, int level);
    /**
     * @return the Shared Folder info object
     */
    public FolderTrie getSharedFolderTrie();

    /**
     * returns the host name of the local machine
     */
    public String getHostName();

    /**
     * returns the primary IP address of the local machine
     */
    public String getHostIP();

    /**
     * @return the agent type
     */
    public AgentTypeDTO getAgentType();

    /**
     * Register the user subject types associated with the enforcers utilizing
     * this PDP. A Set of {@see #UserSubjectTypeEnumType} instances is expected
     * 
     * @param userSubjectsToRegister
     *            A Set of user subject types
     */
    public void registerUserSubjectTypes(Set<UserSubjectTypeEnumType> userSubjectsToRegister);

    /**
     * Retrieve the set of registered user subject type as
     * {@see #UserSubjectTypeEnumType} instances
     * 
     * @return the set of registered user subject type as
     *         {@see #UserSubjectTypeEnumType} instances
     */
    public Set<UserSubjectTypeEnumType> getRegisteredUserSubjectTypes();

    /**
     * Return the the custom obligations holder
     */
    public CustomObligationsContainer getCustomObligationsContainer();

    /**
     * Return the time of the last heartbeat
     */
    public Calendar getLastHeartbeat();

    /**
     * Return the time of the last successful heartbeat
     */
    public long getTimeSinceLastSuccessfulHeartbeat();

    /**
     * Return the number of seconds until the next heartbeat
     */
    public long getSecondsUntilNextHeartbeat();

    /**
     * Order the heartbeat record manager to save its internal state to disk
     */
    public void recordHeartbeatStateInformation();

    /**
     * Return the time of the last bundle update
     */
    public Calendar getLastBundleUpdate();

    /**
     * Return a hash of enabled plug-ins associated with their last connection time
     */
    public Map<String, Calendar> getEnabledPlugins();

    /**
     * Return a list of disabled plug-ins
     */
    public List<String> getDisabledPlugins();

    /**
     * Performs a logging action using the serialized log entity and the user decision
     */
    public void logDecision(String logEntity, String userDecision, String[] attributes);
    
    /**
     * Handle logon event
     */
    public void handleLogonEvent(String userSID);
    
    /**
     * Handle logoff event
     */
    public void handleLogoffEvent(String userSID);

    /**
     * Logs additional data provided by a policy assitant.  Connected to the "base" log
     * by the logIdentifier
     */
    public void logAssistantData(String logIdentifier, String assistantName, String assistantOptions, String assistantDescription, String assistantUserActions);

    /**
     * Closes the process token given to PolicyEvaluatorImpl
     */
    public void closeProcessToken(Long processToken);

    /**
     * Get the timeout value to be used by the resolved DNS cache in PolicyEvaluatorImpl
     */
    public int getResolvedDNSCacheTimeout();

    /**
     * Get the sdk cache hint value (the amount of time we will tell the SDK to cache
     * a policy query result)
     */
    public int getSDKCacheHintValue();

    /**
     * Get the trusted process timeout
     */
    public int getTrustedProcessTTL();

    /**
     * Get the trusted process manager
     */
    public ITrustedProcessManager getTrustedProcessManager();

    /**
     * Should we resolve the host name to an IP address or leave it?
     */
    public boolean resolveHostInformation();
}
