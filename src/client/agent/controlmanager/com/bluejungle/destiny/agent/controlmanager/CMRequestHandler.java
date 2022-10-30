/*
 * Created on Dec 2, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager;

import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.IPCMessageChannel;
import com.bluejungle.destiny.agent.ipc.IPCProxy;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.destiny.agent.ipc.RequestHandlerBase;
import com.bluejungle.destiny.services.policy.types.DeploymentRequest;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.usersubjecttype.UserSubjectTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.utils.CryptUtils;
import com.bluejungle.framework.utils.StringUtils;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.domain.destiny.resource.AgentResourceManager;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectManager;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.engine.destiny.IAgentPolicyContainer;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author hfriedland
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/CMRequestHandler.java#67 $
 */

/**
 * @author fuad
 */
public class CMRequestHandler extends RequestHandlerBase {

    public static final String QUERY_DECISION_ENGINE = "queryDecisionEngine";
    public static final String SET_DESKTOP_FOLDERS = "setDesktopFolders";
    public static final String HANDLE_LOGON_EVENT = "handleLogonEvent";
    public static final String HANDLE_LOGOFF_EVENT = "handleLogoffEvent";
    public static final String STOP_AGENT_SERVICE = "stopAgentService";
    public static final String CLOSE_HANDLE = "closeHandle";
    public static final String GET_AGENT_INFO = "getAgentInfo";
    public static final String GET_BUNDLE_INFO = "getBundleAndHeartbeatStatus";
    public static final String GET_ENABLED_PLUGINS = "getEnabledPlugins";
    public static final String GET_DISABLED_PLUGINS = "getDisabledPlugins";
    public static final String GET_SHUTDOWN_CHALLENGE = "getShutdownChallenge";
    public static final String REGISTER_USER_SUBJECT_TYPES = "registerUserSubjectTypes";
    public static final String IS_APP_IGNORED = "IsAppIgnored";

    public static final String AGENT_STOP_EVENT = "d65e9670-b7fd-4f91-8228-0f5e4e1cd9a2";

    public static final String DUMMY_AGENT_FROM_STR = "cepdpman.exe";

    private static final int SID_ARG_INDEX = 0;
    private static final int MY_DOCUMENTS_ARG_INDEX = 1;
    private static final int MY_DESKTOP_ARG_INDEX = 2;

    private static final int STOP_PASSWORD_ARG_INDEX = 0;
    private static final int STOP_USER_ARG_INDEX = 1;
    private static final int STOP_HOST_ARG_INDEX = 2;

    private static final int IGNORED_APP_ARG_INDEX = 0;

    private IAgentPolicyContainer policyContainer = null;
    private IChallengeMgr challengeMgr;

    private static IOSWrapper osWrapper = (IOSWrapper) ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);
    private IControlManager controlManager;
    private IDSubjectManager subjectManager;
    private IPolicyEvaluator policyEvaluator;

    // using Hashtable b/c it's the fastest synchronized map
    public static Hashtable DESKTOP_FOLDER_MAP = new Hashtable();

    AgentTypeEnum agentType = null;

    /**
     * @see com.bluejungle.destiny.agent.ipc.IRequestHandler#invoke(java.lang.String,
     *      java.util.ArrayList, java.util.ArrayList)
     */
    public boolean invoke(String methodName, ArrayList paramArray, ArrayList resultParamArray) {

        if (methodName.equals(QUERY_DECISION_ENGINE)) {
            // This API is going away, but we have to shore it up for a bit so that the unit
            // tests will pass
            long inTime = System.currentTimeMillis();

            Object[] newStyleArray = new Object[14];

            APIConverter.convertOldToNew(newStyleArray, paramArray);

            ArrayList newResultParamArray = new ArrayList(3);
            
            boolean ret = this.policyEvaluator.queryDecisionEngine(newStyleArray, newResultParamArray);
            resultParamArray.add(0, newResultParamArray.get(0));
            resultParamArray.add(1, Boolean.toString(true));

            if (System.currentTimeMillis() - inTime > 5 * 1000) {
                getLog().error("queryDecisionEngine took over 5 seconds");
            }

            return ret;
        } else if (methodName.equals(SET_DESKTOP_FOLDERS)) {
            return this.setDesktopFolders(paramArray);
        } else if (methodName.equals(HANDLE_LOGON_EVENT)) {
            return this.handleLogonEvent((String) paramArray.get(0));
        } else if (methodName.equals(HANDLE_LOGOFF_EVENT)) {
            return this.handleLogoffEvent((String) paramArray.get(0));
        } else if (methodName.equals(STOP_AGENT_SERVICE)) {
            resultParamArray.add(Boolean.toString(this.stopAgentService(paramArray)));
            return true;
        } else if (methodName.equals(GET_AGENT_INFO)) {
            this.getAgentInfo(resultParamArray);
            return true;
        } else if (methodName.equals(GET_BUNDLE_INFO)) {
            this.getBundleAndHeartbeatStatus(resultParamArray);
            return true;
        } else if (methodName.equals(GET_ENABLED_PLUGINS)) {
            this.getEnabledPlugins(resultParamArray);
            return true;
        } else if (methodName.equals(GET_DISABLED_PLUGINS)) {
            this.getDisabledPlugins(resultParamArray);
            return true;
        } else if (GET_SHUTDOWN_CHALLENGE.equals(methodName)) {
            this.handleChallengeCreation(resultParamArray);
            return true;
        } else if (methodName.equals(CLOSE_HANDLE)) {
            Integer processToken = null;
            try {
                processToken = Integer.decode(((String) paramArray.get(0)));
                osWrapper.closeHandle(processToken.longValue());
            } catch (Exception e) {
            }
        } else if (methodName.equals(IS_APP_IGNORED)) {
            resultParamArray.add(Boolean.toString(this.isAppIgnored((String) paramArray.get(IGNORED_APP_ARG_INDEX))));
            return true;
        } else if (methodName.equals(REGISTER_USER_SUBJECT_TYPES)) {
            Set<UserSubjectTypeEnumType> userSubjectTypesToRegister = new HashSet();
            Iterator paramArrayIterator = paramArray.iterator();
            while (paramArrayIterator.hasNext()) {
                String nextUserSubjectId = (String) paramArrayIterator.next();
                UserSubjectTypeEnumType nextUserSubjectType = UserSubjectTypeEnumType.getUserSubjectType(nextUserSubjectId);
                userSubjectTypesToRegister.add(nextUserSubjectType);
            }

            this.controlManager.registerUserSubjectTypes(userSubjectTypesToRegister);
        } else {
            getLog().error("CMRequestHandle. Unknown method name " + methodName);
        }

        return true;
    }

    /**
     * returns true if the specified application should not be hooked. This
     * method is called when deciding whether or not to hook a newly started
     * app.
     * 
     * @param appPath
     *            path of application to be evaluated
     * @return true if application should be ignored
     */
    private boolean isAppIgnored(String appPath) {
        boolean ignore = this.policyContainer.isApplicationIgnorable(appPath);
        getLog().trace((ignore ? "NOT " : "") + "Hooking application :" + appPath);
        return ignore;
    }

    /**
     * Adds the last policy update time to the resultparamArray
     * 
     * @param resultParamArray
     *            array to return results in
     */
    private void getAgentInfo(ArrayList resultParamArray) {
        DeploymentRequest request = 
            this.policyContainer.getDeploymentRequestForSubjectTypes(this.controlManager.getRegisteredUserSubjectTypes(),
                                                                     this.controlManager.getHostName());
        Calendar timeStamp = request.getTimestamp();
        String lastUpdateTime = calendarAsString(request.getTimestamp());

        resultParamArray.add(lastUpdateTime);
    }

    private String calendarAsString(Calendar ts) {
        if (ts == null || ts.getTime().equals(UnmodifiableDate.START_OF_TIME)) {
            return "Never";
        } else {
            return SimpleDateFormat.getDateTimeInstance().format(ts.getTime());
        }
    }

    /**
     * Adds the last policy update time and last heartbeat time to the resultparamarray
     *
     * @param resultParamArray
     *          array for result string
     */
    private void getBundleAndHeartbeatStatus(ArrayList resultParamArray) {
        String lastHeartbeat = calendarAsString(this.controlManager.getLastHeartbeat());
        String lastBundleUpdate = calendarAsString(this.controlManager.getLastBundleUpdate());

        resultParamArray.add(lastHeartbeat + "\n" + lastBundleUpdate);
    }

    // See almost identical code in ControlMngr.java
    private void splitLine(String line, List res) {
        int lineLen = line.length();
        int startAt = 0;
        int endAt = IPCProxy.paramLen;
        while(true) {
            if (endAt >= lineLen) {
                endAt = lineLen;
            }

            res.add(line.substring(startAt, endAt));

            if (endAt == lineLen) {
                break;
            }

            startAt = endAt;
            endAt += IPCProxy.paramLen;
        }
    }

    /**
     * Adds all the enabled plugins to the resultParamArray
     *
     * @param resultParamArray
     *          array for result string
     */
    private void getEnabledPlugins(ArrayList resultParamArray) {
        Map<String, Calendar> enabledPlugins = controlManager.getEnabledPlugins();
        
        if (enabledPlugins != null) {
            boolean needsLeadingCR = false;
            StringBuffer enabledPluginsStr = new StringBuffer("");
            Set<Map.Entry<String,Calendar>> plugins = enabledPlugins.entrySet();
            for (Map.Entry<String,Calendar> p : plugins) {
                if (needsLeadingCR) {
                    enabledPluginsStr.append("\n");
                }
                enabledPluginsStr.append(p.getKey());
                enabledPluginsStr.append("\n");
                enabledPluginsStr.append(calendarAsString(p.getValue()));
                needsLeadingCR = true;
            }

            splitLine(enabledPluginsStr.toString(), resultParamArray);
        }
    }

    /**
     * Adds all the disabled plugins to the resultParamArray
     *
     * @param resultParamArray
     *          array for result string
     */
    private void getDisabledPlugins(ArrayList resultParamArray) {
        List<String> disabledPlugins = controlManager.getDisabledPlugins();

        splitLine(StringUtils.join(disabledPlugins, "\n"), resultParamArray);
    }

    /**
     * 
     * @param passwordHash
     *            password entered by user
     * @param profilePasswordHash
     *            password from profile.
     * 
     * @return true if password matches.
     */
    private static boolean isPasswordCorrect(byte[] passwordHash, byte[] profilePasswordHash) {
        boolean isPasswordCorrect = false;
        if (profilePasswordHash != null && passwordHash.length == profilePasswordHash.length) {
            isPasswordCorrect = true;
            for (int i = 0; i < passwordHash.length; i++) {
                if (passwordHash[i] != profilePasswordHash[i]) {
                    isPasswordCorrect = false;
                    break;
                }
            }
        }
        return isPasswordCorrect;
    }

    /**
     * Stops the agent service if the password provided is correct.
     * 
     * @param password
     *            password provided to stop the service
     * @return true if password is correct
     */
    private boolean stopAgentService(ArrayList paramArray) {
        try {
            boolean isPasswordCorrect = false;
            String password = (String) paramArray.get(STOP_PASSWORD_ARG_INDEX);
            String userId = (String) paramArray.get(STOP_USER_ARG_INDEX);
            String userName = UserNameCache.getUserName(userId);
            String hostName = (String) paramArray.get(STOP_HOST_ARG_INDEX);
            IDSubject user = null;
            IDSubject host = null;
            try {
                user = this.subjectManager.getSubject(userId, SubjectType.USER);
                host = this.subjectManager.getSubject(hostName, SubjectType.HOST);
            } catch (Exception e) {
                // We cannot afford to crash here. Proceed without user/host if
                // there is an exception.
                getLog().error("StopAgent: Could not determine user/host.", e);
            }
            Long userSubjectId = user != null ? user.getId() : IDSubject.UNKNOWN_ID;
            Long hostSubjectId = host != null ? host.getId() : IDSubject.UNKNOWN_ID;

            // Verify first if the password is given as an answer to a challenge
            if (this.challengeMgr.hasPendingChallenge() && this.challengeMgr.answerChallenge(password)) {
                isPasswordCorrect = true;
            } else {
                byte[] passwordHash = CryptUtils.digest(password, "SHA1", 0);
                byte[] profilePasswordHash = this.controlManager.getCommunicationProfile().getPasswordHash();
                isPasswordCorrect = CMRequestHandler.isPasswordCorrect(passwordHash, profilePasswordHash);
            }

            if (isPasswordCorrect) {
                long stopEvent = osWrapper.openEvent(AGENT_STOP_EVENT);
                osWrapper.setEvent(stopEvent);

                this.controlManager.logTrackingActivity(ActionEnumType.ACTION_STOP_AGENT, userSubjectId, userName, hostSubjectId, hostName, "", null, "", DUMMY_AGENT_FROM_STR, null, 3);
                getLog().info("Agent Stopped. Password Authenticated.");
                return true;
            } else {
                // TODO Should have a separate action for unsuccessful stop.
                this.controlManager.logTrackingActivity(ActionEnumType.ACTION_STOP_AGENT, userSubjectId, userName, hostSubjectId, hostName, "", null, "", DUMMY_AGENT_FROM_STR, null, 3);
                getLog().error("Agent Stop Attempted. Incorrect Password!");
            }
        } catch (NoSuchAlgorithmException e) {
            getLog().error("NoSuchAlgorithmException", e);
        }
        return false;
    }

    /**
     * @param paramArray
     * @return
     */
    private boolean setDesktopFolders(ArrayList paramArray) {

        CMRequestHandler.DESKTOP_FOLDER_MAP.put(paramArray.get(SID_ARG_INDEX), new String[] { (String) paramArray.get(MY_DOCUMENTS_ARG_INDEX), (String) paramArray.get(MY_DESKTOP_ARG_INDEX) });
        return true;
    }

    /**
     * Returns the new challenge string
     * 
     * @return the new challenge string
     */
    private void handleChallengeCreation(ArrayList resultParamArray) {
        resultParamArray.add(this.challengeMgr.getNewChallenge());
    }

    /**
     * @param string
     *            SID for user logging off
     * @return
     */
    private boolean handleLogoffEvent(String userSID) {
        if (getLog().isInfoEnabled()) {
            getLog().info("User logged off: " + userSID);
        }
        this.controlManager.removeLoggedInUser(userSID);
        IDSubject user = this.subjectManager.getSubject(userSID, SubjectType.USER);
        Long userId = user != null ? user.getId() : IDSubject.UNKNOWN_ID;
        IDSubject host = this.subjectManager.getSubject(this.controlManager.getHostName(), SubjectType.HOST);
        Long hostId = host != null ? host.getId() : IDSubject.UNKNOWN_ID;
        String userName = UserNameCache.getUserName(userSID);
        this.controlManager.logTrackingActivity(ActionEnumType.ACTION_AGENT_USER_LOGOUT, userId, userName, hostId, this.controlManager.getHostName(), "", null, "", DUMMY_AGENT_FROM_STR, null, 3);

        ICommandExecutor commandExecutor = (ICommandExecutor) ComponentManagerFactory.getComponentManager().getComponent(ICommandExecutor.NAME);
        commandExecutor.sendHeartBeat();
        return true;
    }

    /**
     * @param string
     *            SID for user logging on
     * @return
     */
    private boolean handleLogonEvent(String userSID) {
        if (getLog().isInfoEnabled()) {
            getLog().info("User logged on: " + userSID);
        }

        this.controlManager.addLoggedInUser(userSID);
        IDSubject user = this.subjectManager.getSubject(userSID, SubjectType.USER);
        Long userId = user != null ? user.getId() : IDSubject.UNKNOWN_ID;
        IDSubject host = this.subjectManager.getSubject(this.controlManager.getHostName(), SubjectType.HOST);
        Long hostId = host != null ? host.getId() : IDSubject.UNKNOWN_ID;

        String userName = UserNameCache.getUserName(userSID);
        this.controlManager.logTrackingActivity(ActionEnumType.ACTION_AGENT_USER_LOGIN, userId, userName, hostId, this.controlManager.getHostName(), "", null, "", DUMMY_AGENT_FROM_STR, null, 3);
        ICommandExecutor commandExecutor = (ICommandExecutor) ComponentManagerFactory.getComponentManager().getComponent(ICommandExecutor.NAME);
        commandExecutor.sendHeartBeat();
        return true;
    }

    /**
     * Sets up the evaluation engine.
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        super.init();
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        this.policyContainer = manager.getComponent(IAgentPolicyContainer.COMP_INFO);
        this.subjectManager = (IDSubjectManager) manager.getComponent(IDSubjectManager.class.getName());
        this.challengeMgr = (IChallengeMgr) manager.getComponent(ChallengeMgrImpl.COMP_INFO);
        this.agentType = AgentTypeEnum.getAgentTypeEnum(this.controlManager.getAgentType().getValue());

        // Initializes the policy evaluator
        HashMapConfiguration policyEvalConfig = new HashMapConfiguration();
        policyEvalConfig.setProperty(IPolicyEvaluator.AGENT_POLICY_CONTAINER_CONFIG_PARAM, manager.getComponent(IAgentPolicyContainer.COMP_INFO));
        policyEvalConfig.setProperty(IPolicyEvaluator.CONTROL_MANAGER_CONFIG_PARAM, (IControlManager)manager.getComponent(IControlManager.NAME));
        policyEvalConfig.setProperty(IPolicyEvaluator.RESOURCE_MANAGER_CONFIG_PARAM, manager.getComponent(AgentResourceManager.COMP_INFO));
        policyEvalConfig.setProperty(IPolicyEvaluator.SUBJECT_MANAGER_CONFIG_PARAM, manager.getComponent(IDSubjectManager.COMP_INFO));
        ComponentInfo<PolicyEvaluatorImpl> evaluatorCompInfo = new ComponentInfo<PolicyEvaluatorImpl>(
        		"policyEvaluator", 
        		PolicyEvaluatorImpl.class, 
        		IPolicyEvaluator.class, 
        		LifestyleType.SINGLETON_TYPE, policyEvalConfig);
        this.policyEvaluator = manager.getComponent(evaluatorCompInfo);
    }

    /**
     * @see com.bluejungle.destiny.agent.ipc.RequestHandlerBase#readIPCRequest(com.bluejungle.destiny.agent.ipc.IPCMessageChannel)
     */
    protected String[] readIPCRequest(IPCMessageChannel messageChannel) {
        synchronized (messageChannel) {
            if (messageChannel.isValid()) {
                return osWrapper.readPolicyIPCRequest(messageChannel.getSharedMemoryHandle());
            } else {
                getLog().error("Tried to read from invalid IPC slot. Caller may be dead. Ignoring to avoid crash.");
            }
        }
        return null;
    }

    /**
     * @see com.bluejungle.destiny.agent.ipc.RequestHandlerBase#writeIPCResponse(com.bluejungle.destiny.agent.ipc.IPCMessageChannel,
     *      java.util.ArrayList)
     */
    protected void writeIPCResponse(IPCMessageChannel messageChannel, ArrayList resultParams) {
        synchronized (messageChannel) {
            if (messageChannel.isValid()) {
                osWrapper.writePolicyIPCResponse(messageChannel.getSharedMemoryHandle(), resultParams.toArray());
                osWrapper.setEvent(messageChannel.getSendEvent());
            } else {
                getLog().error("Tried to write to invalid IPC slot. Caller may be dead. Ignoring to avoid crash.");
            }
        }
    }

    /**
     * Retrieves the control manager from the configuration
     * 
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        super.setConfiguration(config);
        this.controlManager = (IControlManager) ComponentManagerFactory.getComponentManager().getComponent(IControlManager.NAME);
    }
}
