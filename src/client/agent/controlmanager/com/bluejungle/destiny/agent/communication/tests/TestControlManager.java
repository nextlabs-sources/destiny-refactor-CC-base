/*
 * Created on Dec 14, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.communication.tests;

import java.math.BigInteger;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.axis.types.URI;
import org.apache.axis.types.UnsignedShort;
import org.apache.axis.types.URI.MalformedURIException;

import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.destiny.agent.commandengine.tests.TestCommandExecutor;
import com.bluejungle.destiny.agent.controlmanager.CustomObligationsContainer;
import com.bluejungle.destiny.agent.controlmanager.FolderTrie;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.agent.controlmanager.ITrustedProcessManager;
import com.bluejungle.destiny.framework.types.TimeIntervalDTO;
import com.bluejungle.destiny.framework.types.TimeUnits;
import com.bluejungle.destiny.services.management.types.AgentProfileDTO;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.types.AgentTypeDTO;
import com.bluejungle.domain.usersubjecttype.UserSubjectTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.utils.DynamicAttributes;

/**
 * @author fuad
 * @version $Id:
 *          //depot/personal/sasha/main/Destiny/src/client/agent/controlmanager/com/bluejungle/destiny/agent/communication/tests/TestControlManager.java#21 $:
 */

/**
 * @author fuad
 */
public class TestControlManager implements IControlManager {

    public static final int DEFAULT_PORT = 8081;
    public static final int CHANGE_INCREMENT = 20;
    public static final String HOST_NAME = "http://localhost";
    public static final String HOST_NAME_2 = "http://nonlocalhost";
    public static final String INTERVAL = "100";
    public static final String INTERVAL_2 = "50";
    public static final int LOG_LIMIT = 5; //5 MB

    private CommProfileDTO communicationProfile = null;

    public TestControlManager() {
        this.communicationProfile = new CommProfileDTO();
        try {
            getCommunicationProfile().setDABSLocation(new URI(HOST_NAME));
        } catch (MalformedURIException e) {
            e.printStackTrace();
        }

        getCommunicationProfile().setHeartBeatFrequency(new TimeIntervalDTO(new UnsignedShort(INTERVAL), TimeUnits.milliseconds));
        getCommunicationProfile().setLogFrequency(new TimeIntervalDTO(new UnsignedShort(INTERVAL_2), TimeUnits.milliseconds));
        getCommunicationProfile().setPushEnabled(true);
        getCommunicationProfile().setDefaultPushPort(new UnsignedShort(DEFAULT_PORT));
        getCommunicationProfile().setLogLimit(new UnsignedShort(LOG_LIMIT));

        //Start Command Executor
        ComponentInfo<TestCommandExecutor> info = new ComponentInfo<TestCommandExecutor>(
                ICommandExecutor.NAME, 
                TestCommandExecutor.class, 
                ICommandExecutor.class,
                LifestyleType.SINGLETON_TYPE);
        ComponentManagerFactory.getComponentManager().getComponent(info);

    }

    public void changeConfig() {
        this.communicationProfile = new CommProfileDTO();
        try {
            communicationProfile.setDABSLocation(new URI(HOST_NAME_2));
        } catch (MalformedURIException e) {
            e.printStackTrace();
        }

        communicationProfile.setHeartBeatFrequency(new TimeIntervalDTO(new UnsignedShort(INTERVAL_2), TimeUnits.milliseconds));
        communicationProfile.setLogFrequency(new TimeIntervalDTO(new UnsignedShort(INTERVAL), TimeUnits.milliseconds));
        communicationProfile.setPushEnabled(true);
        communicationProfile.setDefaultPushPort(new UnsignedShort(DEFAULT_PORT + CHANGE_INCREMENT));
        getCommunicationProfile().setLogLimit(new UnsignedShort(LOG_LIMIT));
    }

    public void removePushConfig() {
        communicationProfile.setPushEnabled(false);
    }

    /**
     * @see IControlManager#getAgentProfile()
     */
    public AgentProfileDTO getAgentProfile() {
        return null;
    }

    /**
     * @see IControlManager#getCommunicationProfile()
     */
    public CommProfileDTO getCommunicationProfile() {
        return communicationProfile;
    }

    /**
     * @see IControlManager#sendHeartBeat()
     */
    public void sendHeartBeat() {
    }

    /**
     * @see IControlManager#notifyUser(String,
     *      String, String, String,
     *      java.util.ArrayList)
     */
    public void notifyUser(String userId, String time, String action, String resource, Collection messageList) {
    }

    /**
     * @see IControlManager#executeCustomObligations(String, String)
     */
    public void executeCustomObligations(String userId, String commandLine) {
    }

    /**
     * @see IControlManager#addLoggedInUser(String)
     */
    public void addLoggedInUser(String userSID) {

    }

    /**
     * @see IControlManager#removeLoggedInUser(String)
     */
    public void removeLoggedInUser(String userSID) {

    }

    /**
     * @see IControlManager#getAgentId()
     */
    public BigInteger getAgentId() {
        return BigInteger.valueOf(43579);
    }

    /**
     * @see IControlManager#getSharedFolderTrie()
     */
    public FolderTrie getSharedFolderTrie() {
        return null;
    }

    /**
     * @see IControlManager#getHostName()
     */
    public String getHostName() {
        return "localhost";
    }

    /**
     * @see IControlManager#getHostName()
     */
    public String getHostIP() {
        return "127.0.0.1";
    }

    /**
     * @see IControlManager#getAgentType()
     */
    public AgentTypeDTO getAgentType() {
        return null;
    }

    /**
     * @see IControlManager#getRegistrationId()
     */
    public BigInteger getRegistrationId() {
        return BigInteger.ONE;
    }

    /**
     * @see IControlManager#isTrackedAction(ActionEnumType)
     */
    public boolean isTrackedAction(ActionEnumType action) {
        return true;
    }

    /**
     * @see IControlManager#logTrackingActivity(ActionEnumType,
     *      Long, String, Long, String,
     *      String, Long, String,
     *      String, String, String)
     */
    public void logTrackingActivity(ActionEnumType arg0, Long arg1, String arg2, Long arg3, String arg4, String arg5, Long arg6, String arg7, String arg8, String arg9, int logLevel) {
    }

    public void logTrackingActivity(ActionEnumType arg0, Long arg1, String arg2, Long arg3, String arg4, String arg5, Long arg6, String arg7, String arg8, DynamicAttributes arg9, String arg10, DynamicAttributes arg11, int logLevel) {
    }

    public void logAssistantData(String arg0, String arg1, String arg2, String arg3, String arg4) {
    }

    /**
     * @see IControlManager#registerUserSubjectTypes(java.util.Collection)
     */
    public void registerUserSubjectTypes(Set<UserSubjectTypeEnumType> userSubjectsToRegister) {
    }

    /**
     * @see IControlManager#getRegisteredUserSubjectTypes()
     */
    public Set<UserSubjectTypeEnumType> getRegisteredUserSubjectTypes() {
        return null;
    }

    public void notifyUser(String userId, String time, String action, String enforcement, String resource, String message) {
    }                

    public void notifyUser(String userId, String time, String action, String enforcement, String resource, Collection messageList) {
    }                

    public CustomObligationsContainer getCustomObligationsContainer() {
        return null;
    }

    public Calendar getLastHeartbeat() {
        return null;
    }

    public long getTimeSinceLastSuccessfulHeartbeat() {
        return 0;
    }

    public long getSecondsUntilNextHeartbeat() {
        return 0;
    }

    public void recordHeartbeatStateInformation() {
    }

    public Calendar getLastBundleUpdate() {
        return null;
    }

    public Map<String, Calendar> getEnabledPlugins() {
        return null;
    }

    public List<String> getDisabledPlugins() {
        return null;
    }

    public void logDecision(String a, String b, String[] c) {
        return;
    }
    
    public void handleLogonEvent(String userSID) {
    	return;
    }
    
    public void handleLogoffEvent(String userSID) {
    	return;
    }


    public void closeProcessToken(Long processToken) {
        return;
    }

    public int getResolvedDNSCacheTimeout() {
        return 0;
    }

    public int getSDKCacheHintValue() {
        return 0;
    }

    public int getTrustedProcessTTL() {
        return 0;
    }
    
    public ITrustedProcessManager getTrustedProcessManager() {
        return null;
    }

    public boolean resolveHostInformation() {
        return true;
    }
}
