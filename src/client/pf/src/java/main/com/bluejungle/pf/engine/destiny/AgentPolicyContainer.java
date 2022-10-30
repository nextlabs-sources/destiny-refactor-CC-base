package com.bluejungle.pf.engine.destiny;

/*
 * Created on Dec 10, 2004
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.destiny.services.policy.types.AgentTypeEnum;
import com.bluejungle.destiny.services.policy.types.DeploymentRequest;
import com.bluejungle.destiny.services.policy.types.SystemUser;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.domain.usersubjecttype.UserSubjectTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.expressions.EvalValue;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IMultivalue;
import com.bluejungle.framework.expressions.Multivalue;
import com.bluejungle.framework.expressions.ValueType;
import com.bluejungle.framework.heartbeat.HeartbeatManagerImpl;
import com.bluejungle.framework.heartbeat.IHeartbeatListener;
import com.bluejungle.framework.heartbeat.IHeartbeatManager;
import com.bluejungle.framework.securestore.ISecureStore;
import com.bluejungle.framework.securestore.SecureFileStore;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.framework.utils.SerializationUtils;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.lib.ClientInformationDTO;
import com.bluejungle.pf.destiny.lib.ClientInformationRequestDTO;
import com.bluejungle.pf.destiny.lib.RegularExpressionDTO;
import com.bluejungle.pf.destiny.lib.RegularExpressionRequestDTO;
import com.bluejungle.pf.domain.destiny.action.IDActionManager;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.destiny.deployment.DeploymentBundleSignatureEnvelope;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundleV2;
import com.bluejungle.pf.domain.destiny.deployment.InvalidBundleException;
import com.bluejungle.pf.domain.destiny.obligation.AgentObligationManager;
import com.bluejungle.pf.domain.destiny.obligation.IDObligationManager;
import com.bluejungle.pf.domain.destiny.resource.AgentResourceManager;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.serviceprovider.IServiceProviderManager;
import com.bluejungle.pf.domain.destiny.serviceprovider.ServiceProviderManager;
import com.bluejungle.pf.domain.destiny.subject.AgentSubjectManager;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectManager;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.resource.IMResource;
import com.bluejungle.pf.domain.epicenter.resource.IResource;
import com.bluejungle.pf.domain.epicenter.resource.IResourceManager;
import com.bluejungle.pf.domain.epicenter.resource.IResourceManager.ResourceInformation;
import com.bluejungle.pf.domain.epicenter.subject.ISubjectType;

import org.apache.commons.logging.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/engine/destiny/AgentPolicyContainer.java#1 $:
 */

public class AgentPolicyContainer implements IAgentPolicyContainer, IInitializable, IManagerEnabled, ILogEnabled, IConfigurable, IHeartbeatListener {
    private static final String DEFAULT_BASE_DIR = ".";
    private IComponentManager manager;
    private IDActionManager actionManager;
    private IDSubjectManager subjectManager;
    private AgentResourceManager resourceManager;
    private IOSWrapper osWrapper;
    private Log log;
    private IConfiguration config;
    private AgentTypeEnumType agentType;
    private EvaluationEngine globalEngine = new EvaluationEngine(ITargetResolver.EMPTY_RESOLVER);
    private AgentPolicyAssembly agentAssembly;
    private IBundleVault bundleVault;
    private Set<String> bundleSubjects = new HashSet<String>();
    private ClientInformationManager clientInfoManager;
    private RegularExpressionInformationManager regularExpressionInfoManager;
    private IHeartbeatManager heartbeatManager = null;
    private String userIdFieldType;
    private IServiceProviderManager serviceProviderManager = null;
    private String baseDir = DEFAULT_BASE_DIR;
    /**
     * This resolver obtains a file-based resource.
     */
    private final ResourceResolver FILE_SYSTEM_RESOLVER = new ResourceResolver() {
        public ResourceInformation<EngineResourceInformation> getResourceInfo(DynamicAttributes attr, Long processToken, AgentTypeEnumType agentType) {
            IEvalValue names = attr.get(SpecAttribute.NAME_ATTR_NAME);
            if (names == null || names == IEvalValue.NULL) {
                throw new IllegalArgumentException("names is null");
            }
            List<String> lowerCaseNames = new ArrayList<String>();
            if (names.getType() == ValueType.MULTIVAL) {
                IMultivalue mval = (IMultivalue)names.getValue();
                if (mval.getType() != ValueType.STRING) {
                    throw new IllegalArgumentException("{names}: unexpected type.");
                }
                for (IEvalValue nameVal : mval) {
                    String name = (String)nameVal.getValue();
                    lowerCaseNames.add(name.toLowerCase());
                }
            } else if (names.getType() == ValueType.STRING) {
                String name = (String)names.getValue();
                lowerCaseNames.add(name.toLowerCase());
            } else {
                throw new IllegalArgumentException("names: unexpected type.");
            }
            return resourceManager.getResource(
                lowerCaseNames.get(0)
            ,   lowerCaseNames
            ,   attr
            ,   processToken
            ,   agentType
            );
        }
    };

    /**
     * This resolver obtains a resource with all attributes
     * specified by the caller.
     */
    private final ResourceResolver GENERIC_RESOLVER = new ResourceResolver() {
        public ResourceInformation<EngineResourceInformation> getResourceInfo(DynamicAttributes attr, Long processToken, AgentTypeEnumType agentType) {
            IEvalValue id = attr.get(SpecAttribute.ID_ATTR_NAME);
            if (id == null || id == IEvalValue.NULL) {
                throw new IllegalArgumentException("id is null");
            }
            IEvalValue nameAliases = attr.get(SpecAttribute.NAME_ATTR_NAME);
            IEvalValue name = attr.get("name");
            if (name == null || name == IEvalValue.NULL) {
                attr.put("name", nameAliases);
            }
            return resourceManager.getResourceInfo((Serializable)id.getValue(), attr);
        }
    };

    private final Map<String,ResourceResolver> resolvers = new HashMap<String,ResourceResolver>(3);

    {
        resolvers.put(ResourceAttribute.FILE_SYSTEM_SUBTYPE, FILE_SYSTEM_RESOLVER);
        resolvers.put(ResourceAttribute.PORTAL_SUBTYPE, GENERIC_RESOLVER);
    }

    private interface ResourceResolver {
        IResourceManager.ResourceInformation<EngineResourceInformation> getResourceInfo(DynamicAttributes attr, Long processToken, AgentTypeEnumType agentType);
    }

    public synchronized void update(DeploymentBundleSignatureEnvelope signedBundle) throws InvalidBundleException {
        try {
            IBundleVault.BundleInfo bundleInfo = bundleVault.validateAndStore(signedBundle);
            loadBundle(bundleInfo);
        } catch (BundleVaultException exception) {
            log.error("Failed to load bundle", exception);
        }
    }
 
    private long noteTime(StringBuilder sb, String name, long enterTime) {
        long currentTime = System.nanoTime();
        sb.append(name + " (" + (currentTime - enterTime)/1000000 + "ms) ");
        return currentTime;
    }


    /**
     * @see IAgentPolicyContainer#evaluate(long, AgentTypeEnumType, Long, Map, long, int, boolean)
     */
    public EvaluationResult evaluate(
        long reqId
    ,   AgentTypeEnumType agentType
    ,   Long processToken
    ,   Map<String,DynamicAttributes> context
    ,   long ts
    ,   int level
    ,   boolean executeObligations) {

        long enterTime = System.nanoTime();
        StringBuilder sb = null;

        if (context == null) {
            throw new NullPointerException("context");
        }
        DynamicAttributes fromAttrs = context.get("from");
        if (fromAttrs == null) {
            throw new IllegalArgumentException("context[from] is missing");
        }
        DynamicAttributes toAttrs = context.get("to");
        if (toAttrs == null) {
            toAttrs = DynamicAttributes.EMPTY;
        }
        DynamicAttributes sendToAttrs = context.get("sendto");
        if (sendToAttrs == null) {
            sendToAttrs = DynamicAttributes.EMPTY;
        }
        DynamicAttributes act = context.get("action");
        if (act == null) {
            throw new IllegalArgumentException("context[action] is missing");
        }
        String actionName = act.getString("name");
        if (actionName == null) {
            throw new IllegalArgumentException("context[action].name is missing");
        }
        DynamicAttributes userCtx = context.get("user");
        if (userCtx == null) {
            throw new IllegalArgumentException("context[user] is missing");
        }
        String userId = userCtx.getString("id");
        if (userId == null) {
            throw new IllegalArgumentException("context[user].id is missing");
        }
        String userName = userCtx.getString("name");
        if (userName == null || userName.equals("")) {
            userName = userId;
        }
        DynamicAttributes loggedInUserCtx = context.get("operating-system-user");
        if (loggedInUserCtx == null) {
            // Take just the info we need from the user
            loggedInUserCtx = new DynamicAttributes();
            loggedInUserCtx.put("id", userId);
        }

        String loggedInUserId = loggedInUserCtx.getString("id");
        if (loggedInUserId == null) {
            throw new IllegalArgumentException("context[operating-system-user].id is missing");
        }

        DynamicAttributes hostCtx = context.get("host");
        if (hostCtx == null) {
            throw new IllegalArgumentException("context[host] is missing");
        }
        String hostName = hostCtx.getString("name");
        String hostAddr = hostCtx.getString("inet_address");
        if (hostName == null) {
            throw new IllegalArgumentException("context[host].name is missing");
        }

        DynamicAttributes envCtx = context.get("environment");
        if (envCtx == null) {
            throw new IllegalArgumentException("context[environment] is missing");
        }

        DynamicAttributes policiesCtx = context.get("policies");
        if (policiesCtx == null) {
            policiesCtx = new DynamicAttributes();
        }
        String additionalPql = policiesCtx.getString("pql");
        boolean ignoreDefault = (policiesCtx.getString("ignoredefault") != null && policiesCtx.getString("ignoredefault").equals("yes")) ? true : false;

        String appFingerprint = null;
        int pid = 0;
        DynamicAttributes appCtx = context.get("application");
        if (appCtx != null) {
            appFingerprint = appCtx.getString("fingerprint");
            try {
                pid = Integer.parseInt(appCtx.getString("pid"));
            } catch (NumberFormatException e) {
            }
        }

        long lastSuccessfulHeartbeatTime = 0;
        DynamicAttributes environmentCtx = context.get("environment");
        if (environmentCtx != null) {
            try {
                lastSuccessfulHeartbeatTime = Long.parseLong(environmentCtx.getString("time_since_last_successful_heartbeat"));
            } catch (NumberFormatException e) {
                // Whatever
            }
        }

        EvaluationEngine engine;

        synchronized (this ) {
            engine = globalEngine;
        }

        if (log.isInfoEnabled()) {
            sb = new StringBuilder("AgentPolicyContainerTime for request " + reqId + ":\n");

            enterTime = noteTime(sb, "unpack args", enterTime);
        }

        IResourceManager.ResourceInformation<EngineResourceInformation> fromRes = getResourceInfo(fromAttrs, processToken, agentType, false);
        if (log.isInfoEnabled()) {
            enterTime = noteTime(sb, "fromRes", enterTime);
        }
        IResourceManager.ResourceInformation<EngineResourceInformation> toRes = getResourceInfo(toAttrs, processToken, agentType, true);
        if (log.isInfoEnabled()) {
            enterTime = noteTime(sb, "toRes", enterTime);
        }
        IContentAnalysisManager contentAnalysisManager = ContentAnalysisManager.build(getRegularExpressionInformationManager()
                                                                                      , fromRes.getMutableResource()
                                                                                      , toRes.getMutableResource()
                                                                                      , pid
                                                                                      , processToken
                                                                                      , loggedInUserId
                                                                                      , level);

        IDSubject user = getUserSubject(userId, userCtx);
        if (log.isInfoEnabled()) {
            enterTime = noteTime(sb, "user", enterTime);
        }
        IDSubject loggedInUser = getSubject(loggedInUserId, SubjectType.USER, loggedInUserCtx);
        if (log.isInfoEnabled()) {
            enterTime = noteTime(sb, "loggedInUser", enterTime);
        }
        IDSubject host = getSubject(hostName.toLowerCase(), SubjectType.HOST, hostCtx);
        if (log.isInfoEnabled()) {
            enterTime = noteTime(sb, "host", enterTime);
        }
        IDSubject app = getSubject(appFingerprint, SubjectType.APP, appCtx);
        if (log.isInfoEnabled()) {
            enterTime = noteTime(sb, "app", enterTime);
        }
        IDSubject[] sendToSubjects = getSentToSubjects(sendToAttrs);
        if (log.isInfoEnabled()) {
            enterTime = noteTime(sb, "sendTo", enterTime);
        }

        long rdpIP = (osWrapper != null) ? osWrapper.getRDPAddress(pid) : 0;

        EvaluationRequest req = new EvaluationRequest(
            (long)reqId
        ,   actionManager.getAction(actionName)
        ,   fromRes.getResource()
        ,   fromRes.getInformation()
        ,   toRes.getResource()
        ,   toRes.getInformation()
        ,   user
        ,   userName
        ,   loggedInUser
        ,   app
        ,   sendToSubjects
        ,   host
        ,   hostAddr
        ,   (rdpIP != 0) ? convertToBinary(rdpIP) : null
        ,   envCtx
        ,   lastSuccessfulHeartbeatTime
        ,   executeObligations
        ,   ts
        ,   level
        ,   additionalPql
        ,   ignoreDefault
        ,   contentAnalysisManager
        ,   agentAssembly
        ,   getClientInformationManager()
        ,   serviceProviderManager
        );

        if (log.isInfoEnabled()) {
            enterTime = noteTime(sb, "setup", enterTime);
        }

        EvaluationResult res = engine.evaluate(req);

        if (log.isInfoEnabled()) {
            noteTime(sb, "query", enterTime);
            sb.append("\n");
            // Also log the subject information that we found
            appendSubjectInfo(sb, "User", user);
            appendSubjectInfo(sb, "Host", host);
            appendSubjectInfo(sb, "App ", app);
            appendSubjectInfo(sb, "Logn", loggedInUser);
            for (IDSubject subj : sendToSubjects) {
                appendSubjectInfo(sb, "Send", subj);
            }

            log.info(sb.toString());
        }

        return res;
    }

    String convertToBinary(long ip) {
        // toBinaryString doesn't pad
        String padded = "00000000000000000000000000000000" + Long.toBinaryString(ip);
        return padded.substring(padded.length()-32);
    }

    private void appendSubjectInfo(StringBuilder sb, String name, IDSubject subj) {
        sb.append(name);
        sb.append(": id(");
        sb.append(subj.getId());
        sb.append(") uid(");
        sb.append(subj.getUid());
        sb.append(") uniqueName(");
        sb.append(subj.getUniqueName());
        sb.append(")\n");
    }

    private IResourceManager.ResourceInformation<EngineResourceInformation> getResourceInfo(DynamicAttributes attr, Long processToken, AgentTypeEnumType agentType, boolean emptyOK) {
        if (attr == null) {
            throw new NullPointerException("attr");
        }
        String typeName = attr.getString(SpecAttribute.DESTINYTYPE_ATTR_NAME);
        if (typeName == null) {
            if (emptyOK) {
                return new ResourceInformation<EngineResourceInformation>() {
                    private static final long serialVersionUID = 1L;
                    private final EngineResourceInformation info = new EngineResourceInformation();
                    public EngineResourceInformation getInformation() {
                        return info;
                    }
                    public IResource getResource() {
                        return null;
                    }
                    public IMResource getMutableResource() {
                        return null;
                    }
                };
            } else {
                throw new IllegalStateException("The call expects a definied resource.");
            }
        }
        // Create a resource based on the resource type.
        ResourceResolver resolver = resolvers.get(typeName);

        if (resolver == null) {
            resolver = GENERIC_RESOLVER;
        }

        return resolver.getResourceInfo(attr, processToken, agentType);
    }


    private IDSubject getSubject(String uid, ISubjectType type, DynamicAttributes attributes) {
        return subjectManager.getSubject(uid, type, attributes);
    }

    private IDSubject getUserSubject(String uid, DynamicAttributes attributes) {
        IEvalValue emailVal = attributes.get("email");
        if (emailVal == null) {
            IEvalValue name = attributes.get("name");
            attributes.put("mail", name);
        } else {
            attributes.put("mail", emailVal);
        }
        return getSubject(uid, SubjectType.USER, attributes);
    }

    private IDSubject[] getSentToSubjects(DynamicAttributes recipientAttrs) {
        if (recipientAttrs == null) {
            return new IDSubject[0];
        }

        if (recipientAttrs.containsKey("id")) {
            // This is a single user with, potentially, other attributes
            String uid = recipientAttrs.getString("id");

            if (uid == null) {
                uid = recipientAttrs.getString("name");
            }

            recipientAttrs.put(SubjectAttribute.USER_EMAIL.getName(), uid);
            return new IDSubject[] {getSubject(uid, SubjectType.RECIPIENT, recipientAttrs)};
        } else if (recipientAttrs.containsKey("email")) {
            IEvalValue emailVal = recipientAttrs.get("email");
            IMultivalue emails;
            if (emailVal.getType() == ValueType.MULTIVAL) {
                emails = (IMultivalue)emailVal.getValue();
            } else {
                emails = Multivalue.create(new Object[] {emailVal.getValue()});
            }
            IDSubject[] subjs = new IDSubject[emails.size()];

            int i = 0;
            for (IEvalValue addrValue : emails) {
                if (addrValue == null || addrValue.getType() != ValueType.STRING) {
                    continue;
                }
                String addrString = ((String)addrValue.getValue()).toLowerCase();
                DynamicAttributes attrs = new DynamicAttributes(1);
                attrs.put(SubjectAttribute.USER_EMAIL.getName(), addrValue);
                subjs[i] = subjectManager.getSubject(
                    addrString
                    ,   SubjectType.USER
                    ,   attrs
                                                     );
                i++;
            }
            return subjs;
        }

        return new IDSubject[0];
    }

    /**
     *
     * @see IAgentPolicyContainer#getDeploymentRequestForSubjectTypes(Set, String)
     */
    public synchronized DeploymentRequest getDeploymentRequestForSubjectTypes(Set<UserSubjectTypeEnumType> userSubjectTypes, String hostName) {
        return getDeploymentRequestForSubjectTypes(userSubjectTypes, hostName, AgentTypeEnum.FILE_SERVER);
    }

    /**
     *
     * @see IAgentPolicyContainer#getDeploymentRequestForSubjectTypes(Set, String, AgentTypeEnum)
     */
    public synchronized DeploymentRequest getDeploymentRequestForSubjectTypes(Set<UserSubjectTypeEnumType> userSubjectTypes, String hostName, AgentTypeEnum agentType) {
        if (userSubjectTypes == null) {
            throw new NullPointerException("userSubjectTypes cannot be null.");
        }

        if (hostName == null) {
            throw new NullPointerException("hostName");
        }
        Calendar ts = (agentAssembly == null) ? null : agentAssembly.getTimestamp();

        String[] userSubjectsTypeIds = new String[userSubjectTypes.size()];
        int i = 0;
        for (UserSubjectTypeEnumType nextUserSubjectType : userSubjectTypes) {
            userSubjectsTypeIds[i++] = nextUserSubjectType.getName();
        }

        // FIXME: This method needs to provide an array of AgentCapability[] to the constructor.
        return new DeploymentRequest(null, userSubjectsTypeIds, null, hostName, agentType, ts);
    }

    /**
     *
     * @see IAgentPolicyContainer#getDeploymentRequestForUsers(Set, String)
     */
    public synchronized DeploymentRequest getDeploymentRequestForUsers(Set<? extends ISystemUser> users, String hostName) {
        if (users == null) {
            throw new NullPointerException("users");
        }
        if (hostName == null) {
            throw new NullPointerException("hostName");
        }

        Calendar ts = (agentAssembly == null) ? null : agentAssembly.getTimestamp();
        SystemUser[] systemUsers = new SystemUser[users.size()];        
        int i = 0;
        for (ISystemUser nextSystemUser : users) {
            systemUsers[i++] = new SystemUser(nextSystemUser.getUserSubjectTypeId(), nextSystemUser.getSystemId());
            if (!bundleSubjects.contains(nextSystemUser.getSystemId().toUpperCase())) {
                // If any of the required subjects is not in the current bundle,
                // request should force a rebuild of the bundle.
                ts = null;
            }
        }
        // FIXME: This method needs to provide an array of AgentCapability[] to the constructor.
        return new DeploymentRequest(systemUsers, null, null, hostName, AgentTypeEnum.DESKTOP, ts);
    }

    synchronized AgentPolicyAssembly getAssembly() {
        return agentAssembly;
    }

    /**
     * @see IInitializable#init()
     */
    public void init() {

        if (this.config != null) {
            baseDir = config.get(BASE_DIR_PROPERTY_NAME, DEFAULT_BASE_DIR);
        }

        // disable DNS caching for correct Inet_Address evaluation
        java.security.Security.setProperty("networkaddress.cache.ttl", "0");

        // force loading of the agent-side DO managers, so that parser gets them
        // automatically
        HashMapConfiguration rmConfig = new HashMapConfiguration();
        rmConfig.setProperty(AgentResourceManager.AGENT_TYPE_CONFIG_PARAM, agentType);
        if(this.config != null){
            rmConfig.setProperty(AgentResourceManager.DISK_CACHE_FLUSH_FREQUENCY_CONFIG_PARAM,
                                 config.get(IAgentPolicyContainer.DISK_CACHE_FLUSH_FREQUENCY_CONFIG_PARAM));
            rmConfig.setProperty(AgentResourceManager.DISK_CACHE_LOCATION_CONFIG_PARAM,
                                 config.get(IAgentPolicyContainer.DISK_CACHE_LOCATION_CONFIG_PARAM));

            if (config.contains(IAgentPolicyContainer.DISK_CACHE_SIZE_CONFIG_PARAM)) {
                rmConfig.setProperty(AgentResourceManager.DISK_CACHE_SIZE_CONFIG_PARAM,
                                     config.get(IAgentPolicyContainer.DISK_CACHE_SIZE_CONFIG_PARAM));
            }

        } else {
            rmConfig.setProperty(AgentResourceManager.DISK_CACHE_LOCATION_CONFIG_PARAM,
                                 baseDir);
        }

        ComponentInfo rmInfo = new ComponentInfo(AgentResourceManager.class.getName(), AgentResourceManager.class.getName(), AgentResourceManager.class.getName(), LifestyleType.SINGLETON_TYPE, rmConfig);
        manager.registerComponent(rmInfo, true);
        resourceManager = (AgentResourceManager) manager.getComponent(rmInfo);

        ComponentInfo omInfo = new ComponentInfo(IDObligationManager.class.getName(), AgentObligationManager.class.getName(), IDObligationManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        manager.registerComponent(omInfo, true);

        ComponentInfo smInfo = new ComponentInfo(IDSubjectManager.class.getName(), AgentSubjectManager.class.getName(), IDSubjectManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        subjectManager = (IDSubjectManager) manager.getComponent(smInfo);
        actionManager = (IDActionManager) manager.getComponent(IDActionManager.COMP_INFO);
        osWrapper = (IOSWrapper) manager.getComponent(OSWrapper.class);

        userIdFieldType = osWrapper.isLinux() ?
            UserSubjectTypeEnumType.LINUX_USER_SUBJECT_TYPE.getName()
        :   UserSubjectTypeEnumType.WINDOWS_USER_SUBJECT_TYPE.getName();


        HashMapConfiguration bundleVaultConfig = new HashMapConfiguration();
        bundleVaultConfig.setProperty(BundleVaultImpl.BASE_DIR_PROPERTY_NAME, baseDir);
        ComponentInfo bundleVaultInfo = new ComponentInfo(IBundleVault.COMPONENT_NAME, BundleVaultImpl.class.getName(), IBundleVault.class.getName(), LifestyleType.SINGLETON_TYPE, bundleVaultConfig);
        bundleVault = (IBundleVault) manager.getComponent(bundleVaultInfo);

        ComponentInfo heartbeatinfo = new ComponentInfo(IHeartbeatManager.class.getName(), HeartbeatManagerImpl.class.getName(), IHeartbeatManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        heartbeatManager = (IHeartbeatManager) manager.getComponent(heartbeatinfo);
        heartbeatManager.register(ClientInformationRequestDTO.CLIENT_INFORMATION_SERVICE, this);
        heartbeatManager.register(RegularExpressionRequestDTO.REGULAR_EXPRESSION_SERVICE, this);

        HashMapConfiguration spConfig = new HashMapConfiguration();
        spConfig.setProperty(IServiceProviderManager.BASE_DIR_PROPERTY_NAME, baseDir);
        serviceProviderManager = (IServiceProviderManager) manager.getComponent(ServiceProviderManager.COMP_INFO, spConfig);
    }

    /**
     * @see IAgentPolicyContainer#loadBundle()
     */
    public synchronized void loadBundle() throws InvalidBundleException {
        try {
            IBundleVault.BundleInfo currentBundleInfo = bundleVault.getBundleInfo();
            loadBundle(currentBundleInfo);
        } catch (BundleVaultException exception) {
            // Think about throwing this in some other form instead
            log.error("Failed to load bundle.", exception);
        }
        try {
            buildClientInformationManager(getClientSecureStore().read());
        } catch (IOException exception) {
            log.error("Failed to load client information.", exception);
        }
        try {
            buildRegularExpressionManager(getRegularExpressionSecureStore().read());
        } catch (IOException exception) {
            log.error("Failed to load regular expression information.", exception);
        }
    }

    /**
     * @see IManagerEnabled#setManager(IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }

    /**
     * @see IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return manager;
    }

    /**
     * @see ILogEnabled#setLog(Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see ILogEnabled#getLog()
     */
    public Log getLog() {
        return log;
    }

    /**
     * @see IAgentPolicyContainer#getPolicyCount()
     */
    public synchronized int getPolicyCount() {
        return agentAssembly.getPolicyCount();
    }

    public void setConfiguration(IConfiguration config) {
        this.config = config;
        if (config != null) {
            agentType = config.get(AGENT_TYPE_CONFIG_PARAM, AgentTypeEnumType.FILE_SERVER);
        } else {
            agentType = AgentTypeEnumType.FILE_SERVER;
        }
    }

    public IConfiguration getConfiguration() {
        return config;
    }

    public boolean isApplicationIgnorable(String appName) {
        EvaluationEngine engine;
        synchronized (this) {
            engine = globalEngine;
        }
        if (engine == null) {
            return false;
        }
        DynamicAttributes attributes = new DynamicAttributes(1);
        attributes.put("name", EvalValue.build(appName));
        return engine.isApplicationIgnorable(subjectManager.getSubject(null, SubjectType.APP, attributes));
    }
    
    public boolean isApplicationTrusted(String appName) {
        EvaluationEngine engine;
        synchronized (this) {
            engine = globalEngine;
        }
        if (engine == null) {
            return false;
        }
        DynamicAttributes attributes = new DynamicAttributes(1);
        attributes.put("name", EvalValue.build(appName));
        return engine.isApplicationTrusted(
            subjectManager.getSubject(null, SubjectType.APP, attributes)
        );
    }

    /**
     * This method is for testing. It lets the tester set the client id resolver.
     *
     * @param resolver the client id resolver to use.
     */
    synchronized void setClientInformationManager(ClientInformationManager clientInfoManager) {
        this.clientInfoManager = clientInfoManager;
    }

    private synchronized IClientInformationManager getClientInformationManager() {
        if (clientInfoManager != null) {
            return clientInfoManager;
        } else {
            return IClientInformationManager.DEFAULT;
        }
    }

    private synchronized IRegularExpressionInformationManager getRegularExpressionInformationManager() {
        if (regularExpressionInfoManager != null) {
            return regularExpressionInfoManager;
        } else {
            return IRegularExpressionInformationManager.DEFAULT;
        }
        
    }

    /**
     * Load the specified policy bundle
     *
     * @param bundle the bundle to load
     */
    private synchronized void loadBundle(IBundleVault.BundleInfo bundleInfo) {
        if (bundleInfo == null) {
            throw new NullPointerException("bundleInfo cannot be null.");
        }
        IDeploymentBundleV2 bundle = bundleInfo.getBundle();

        if (bundle == null) {
            throw new NullPointerException("bundle cannot be null.");
        }
        bundleSubjects.clear();
        String[] uids = bundleInfo.getSubjects();
        if (uids != null) {
            for (String uid : uids) {
                bundleSubjects.add(uid.toUpperCase());
            }
        }

        if (!bundle.isEmpty()) {
            agentAssembly = new AgentPolicyAssembly(bundle, manager);
            globalEngine = new EvaluationEngine(agentAssembly);
            resourceManager.clearCache();
        }
    }

    public Serializable prepareRequest(String id) {
        if (ClientInformationRequestDTO.CLIENT_INFORMATION_SERVICE.equals(id)) {
            Date buildTime = UnmodifiableDate.START_OF_TIME;
            String[] uids = osWrapper.getLoggedInUsers();
            if (clientInfoManager != null && clientInfoManager.includesAllUids(uids)) {
                buildTime = clientInfoManager.getBuildTime();
            }
            return new ClientInformationRequestDTO(
                buildTime
            ,   userIdFieldType
            ,   uids
            );
        } else if (RegularExpressionRequestDTO.REGULAR_EXPRESSION_SERVICE.equals(id)) {
            Date buildTime = UnmodifiableDate.START_OF_TIME;

            if (regularExpressionInfoManager != null) {
                buildTime = regularExpressionInfoManager.getBuildTime();
            }
            return new RegularExpressionRequestDTO(buildTime);
        } else {
            return null;
        }
    }

    public void processResponse(String id, String data) {
        processResponse(id, SerializationUtils.unwrapSerialized(data));
    }

    public void processResponse(String id, Serializable data) {
        if (ClientInformationRequestDTO.CLIENT_INFORMATION_SERVICE.equals(id) && (data instanceof ClientInformationDTO)) {
            ClientInformationDTO dto = (ClientInformationDTO)data;
            buildClientInformationManager(dto);
            try {
                getClientSecureStore().save(dto);
            } catch (IOException exception) {
                log.error("Failed to save client information.", exception);
            }
        } else if (RegularExpressionRequestDTO.REGULAR_EXPRESSION_SERVICE.equals(id) && (data instanceof RegularExpressionDTO)) {
            RegularExpressionDTO dto = (RegularExpressionDTO)data;
            buildRegularExpressionManager(dto);
            try {
                getRegularExpressionSecureStore().save(dto);
            } catch (IOException exception) {
                log.error("Failed to save regular expression information.", exception);
            }
        }
    }

    private void buildRegularExpressionManager(RegularExpressionDTO dto) {
        if (dto == null) {
            return;
        }
        regularExpressionInfoManager = new RegularExpressionInformationManager(dto.getBuildTime(), dto.getMapOfExpressions());
    }

    private void buildClientInformationManager(ClientInformationDTO dto) {
        if (dto == null) {
            return;
        }
        int size = dto.size();
        int uidSize = dto.getPreparedForUids().size();
        clientInfoManager = new ClientInformationManager(
            dto.getBuildTime()
        ,   dto.getPreparedForUids().toArray(new String[uidSize])
        ,   dto.getClientIds().toArray(new String[size])
        ,   dto.getShortNames().toArray(new String[size])
        ,   dto.getLongNames().toArray(new String[size])
        ,   dto.getDomains().toArray(new String[size][])
        ,   dto.getUids().toArray(new String[size][])
            );
        }

    private ISecureStore<ClientInformationDTO> getClientSecureStore() throws IOException {
        return new SecureFileStore<ClientInformationDTO>(
            new File(baseDir + File.separator + "clientinfo.bin").getAbsoluteFile()
        ,   "bundleKey"
        );
    }

    private ISecureStore<RegularExpressionDTO> getRegularExpressionSecureStore() throws IOException {
        return new SecureFileStore<RegularExpressionDTO>(
            new File(baseDir + File.separator + "regexp.bin").getAbsoluteFile()
        ,   "bundleKey"
        );
    }
}
