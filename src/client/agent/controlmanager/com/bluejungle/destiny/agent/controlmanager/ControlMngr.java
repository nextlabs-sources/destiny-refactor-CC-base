/*
 * Created on Nov 2, 2004
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author hfriedland
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/ControlMngr.java#1 $:
 */
package com.bluejungle.destiny.agent.controlmanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.types.UnsignedShort;
import org.apache.commons.logging.Log;

import com.bluejungle.destiny.agent.activityjournal.ActivityJournal;
import com.bluejungle.destiny.agent.activityjournal.IActivityJournal;
import com.bluejungle.destiny.agent.commandengine.CommandExecutor;
import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.destiny.agent.communication.CommunicationManager;
import com.bluejungle.destiny.agent.communication.ICommunicationManager;
import com.bluejungle.destiny.agent.ipc.IIPCProxy;
import com.bluejungle.destiny.agent.ipc.IIPCStub;
import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.IPCKernelStub;
import com.bluejungle.destiny.agent.ipc.IPCLinuxStub;
import com.bluejungle.destiny.agent.ipc.IPCLinuxUserModeStub;
import com.bluejungle.destiny.agent.ipc.IPCProxy;
import com.bluejungle.destiny.agent.ipc.IPCStubBase;
import com.bluejungle.destiny.agent.ipc.IPCUserModeStub;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.destiny.agent.notification.INotificationManager;
import com.bluejungle.destiny.agent.notification.NotificationManager;
import com.bluejungle.destiny.agent.profile.IProfileManager;
import com.bluejungle.destiny.agent.profile.ProfileManager;
import com.bluejungle.destiny.agent.scheduling.Scheduler;
import com.bluejungle.destiny.agent.security.AxisSocketFactory;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.destiny.services.agent.AgentServiceIF;
import com.bluejungle.destiny.services.agent.AgentServiceIFBindingStub;
import com.bluejungle.destiny.services.agent.types.AgentHeartbeatData;
import com.bluejungle.destiny.services.agent.types.AgentPluginData;
import com.bluejungle.destiny.services.agent.types.AgentPluginDataElement;
import com.bluejungle.destiny.services.agent.types.AgentProfileStatusData;
import com.bluejungle.destiny.services.agent.types.AgentRegistrationData;
import com.bluejungle.destiny.services.agent.types.AgentStartupConfiguration;
import com.bluejungle.destiny.services.agent.types.AgentStartupData;
import com.bluejungle.destiny.services.agent.types.AgentUpdateAcknowledgementData;
import com.bluejungle.destiny.services.agent.types.AgentUpdates;
import com.bluejungle.destiny.services.management.types.ActivityJournalingSettingsDTO;
import com.bluejungle.destiny.services.management.types.AgentProfileDTO;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.destiny.services.policy.types.AgentTypeEnum;
import com.bluejungle.destiny.services.policy.types.DeploymentRequest;
import com.bluejungle.destiny.types.custom_obligations.CustomObligation;
import com.bluejungle.destiny.types.custom_obligations.CustomObligationsData;
import com.bluejungle.destiny.types.shared_folder.SharedFolderAliasList;
import com.bluejungle.destiny.types.shared_folder.SharedFolderAliases;
import com.bluejungle.destiny.types.shared_folder.SharedFolderAliasesAlias;
import com.bluejungle.destiny.types.shared_folder.SharedFolderData;
import com.bluejungle.destiny.types.shared_folder.SharedFolderDataCookie;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.ToResourceInformation;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.domain.types.ActionTypeDTO;
import com.bluejungle.domain.types.AgentTypeDTO;
import com.bluejungle.domain.usersubjecttype.UserSubjectTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.ValueType;
import com.bluejungle.framework.heartbeat.HeartbeatManagerImpl;
import com.bluejungle.framework.heartbeat.IHeartbeatManager;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.framework.utils.Pair;
import com.bluejungle.framework.utils.SerializationUtils;
import com.bluejungle.framework.utils.TimeUnitsUtils;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.lib.DTOUtils;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.destiny.deployment.DeploymentBundleSignatureEnvelope;
import com.bluejungle.pf.domain.destiny.deployment.InvalidBundleException;
import com.bluejungle.pf.domain.destiny.serviceprovider.IServiceProviderManager;
import com.bluejungle.pf.domain.destiny.serviceprovider.ServiceProviderManager;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectManager;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.subject.ISubject;
import com.bluejungle.pf.engine.destiny.IAgentPolicyContainer;
import com.bluejungle.pf.engine.destiny.ISystemUser;
import com.bluejungle.pf.engine.destiny.EvaluationResult;
import com.bluejungle.version.IVersion;
import com.bluejungle.version.types.Version;
import com.bluejungle.versionexception.InvalidVersionException;
import com.bluejungle.versionfactory.VersionFactory;
import com.bluejungle.versionutil.VersionUtil;
import com.nextlabs.domain.log.PolicyActivityLogEntryV5;
import com.nextlabs.domain.log.TrackingLogEntryV3;

public class ControlMngr implements ISCMEventListener, IControlManager, IHasComponentInfo<IControlManager>, IDisposable, ILogEnabled {
    public static final ComponentInfo<IControlManager> COMP_INFO = new ComponentInfo<IControlManager>(IControlManager.NAME,
                                                                                                      ControlMngr.class, 
                                                                                                      IControlManager.class, 
                                                                                                      LifestyleType.SINGLETON_TYPE);

    private static final String NOTIFICATION_PROXY_SUFFIX = "DestinyNotifyRequestHandler";
    private static final String SHOWNOTIFICATION = "ShowNotification";
    private static final String CUSTOMOBLIGATION = "CustomObligation";
    private static final String REGISTRATION_FILE = "config/registration.info";
    private static final String SHARED_FOLDER_FILE = "config/sharedfolder.info";
    private static final String CUSTOM_OBLIGATIONS_FILE = "config/customobligations.info";
    private static final String AGENT_KEYSTORE_ATTACHMENT = "AgentKeyStore";
    private static final String AGENT_TRUSTSTORE_ATTACHEMENT = "AgentTrustStore";
    private static final String AGENT_EXTRA_ATTACHMENT = "extra";
    private static final String NO_SERVICE_ARG = "NoService";
    private static final String SDK_OPT = "SDK";
    private static final String SDK_CACHE_HINT_VALUE = "SDKCacheHintValue";
    private static final String TRUSTED_PROCESS_TIMEOUT = "TrustedProcessTimeout";
    private static final String ROLLBACK_DETECTION_OPT = "DetectSysclockRollback";
    private static final String RESOLVED_CACHE_TIMEOUT = "ResolvedCacheTimeout";
    private static final String KERNEL_IPC_THREAD_COUNT = "KernelIPCThreads";
    private static final String RESOLVE_HOST_INFORMATION = "ResolveHostInformation";
    private static final String ROOT_DIRECTORY = "RootDirectory";
    private static final String EVAL_CACHE_SIZE = "EvalCacheSize";

    private static IOSWrapper osWrapper = ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);
    private IPCStubBase userModeStub = null;
    private IPCStubBase kernelModeStub = null;
    private Thread userModeStubThread = null;
    private Thread kernelModeStubThread = null;
    
    private IPCUserModeStub ipcUserModeStub = null;
    private Thread ipcUserModeStubThread = null;

    private Log log = null;
    private boolean isInitialized = false;
    private IProfileManager profileManager = null;
    private ICommandExecutor commandExecutor = null;
    private ICommunicationManager communicationManager = null;
    private IHeartbeatManager heartbeatManager = null;
    private Scheduler scheduler = null;
    private BigInteger agentId = null;
    private BigInteger registrationId = null;
    private Map<String, IIPCProxy> userNotificationProxyMap = Collections.synchronizedMap(new HashMap<String, IIPCProxy>());
    private IAgentPolicyContainer policyContainer = null;
    private IAgentKeyManagerComponentBuilder keyManagerBuilder = null;
    private String hostName = null;
    private String hostIP = null;
    private AgentTypeDTO agentType = null;
    private IActivityJournal activityJournal;
    private INotificationManager notificationManager = null;
    private Set<ActionEnumType> trackedActions = null;
    private FolderTrie sharedFolderTrie = null;
    private SharedFolderDataCookie sharedFolderCookie = null;
    private boolean startupRequestSent = false;
    private IVersion version;
    private Set<UserSubjectTypeEnumType> registeredUserSubjectTypes = new HashSet<UserSubjectTypeEnumType>();
    private CustomObligationsContainer customObligations = new CustomObligationsContainer();
    private static boolean isSDK=false;
    private static int sdkCacheHintValue = 300;
    private static boolean doRollbackDetection=false;
    private static boolean resolveHostInfo = true;
    private static int trustedProcessTimeout=10*60; // 10 minutes
    private static int resolvedDNSCacheTimeout=2*60*60; // 2 hours
    private static int kernelIPCThreadCount = 15;
    private static int agentResourceEvalCacheSize = -1;
    private static String policyControllerRootDirectory = ".";
    private Calendar lastHeartbeatSent=null;
    private Calendar lastSuccessfulHeartbeatTime=null;
    private IHeartbeatRecordManager heartbeatRecordManager=null;
    private long heartbeatRateInSeconds = 0;
    private ITrustedProcessManager trustedProcessManager = null;

    /**
     * Sets up the IPC stubs. For desktop agent, we only have a user mode stub.
     * For file server agent, another stub is used for kernel to user mode
     * communication.
     * 
     */
    private void initIPCStub() {
        getLog().info("Creating stub objects...");

        // //////[jzhang 092206] Now both kernel and user mode stubs are using
        // socket, therefore we use a general stub class
        // for user mode, also use CMKernelRequestHandler for request handling
        final HashMapConfiguration userModeStubConfig = new HashMapConfiguration();
        userModeStubConfig.setProperty(IIPCStub.REQUEST_HANDLER_CLASS_NAME_CONFIG_PARAM, CMKernelRequestHandler.class.getName());

        // For file server, only 2 user mode threads
        if (agentType == AgentTypeDTO.FILE_SERVER) {
            userModeStubConfig.setProperty(IIPCStub.THREAD_POOL_SIZE_CONFIG_PARAM, new Integer(2));
        } else {
            userModeStubConfig.setProperty(IIPCStub.THREAD_POOL_SIZE_CONFIG_PARAM, new Integer(10));
        }

        userModeStubConfig.setProperty(IControlManager.NAME, this);

        getLog().info("About to instantiate the user mode IPC stub...");
         
        if (osWrapper.isLinux()) {
            userModeStubConfig.setProperty(IIPCStub.REQUEST_HANDLER_CLASS_NAME_CONFIG_PARAM, CMKernelRequestHandler.class.getName());
            userModeStubConfig.setProperty(IIPCStub.STUB_TYPE, new Integer(0)); // user

            String userModeStubCompName = IPCLinuxStub.class.getName() + "User";
            ComponentInfo userModeStubCompInfo = new ComponentInfo(userModeStubCompName, IPCLinuxStub.class.getName(), null, LifestyleType.TRANSIENT_TYPE, userModeStubConfig);
            ComponentManagerFactory.getComponentManager().registerComponent(userModeStubCompInfo, true);
            userModeStub = (IPCLinuxStub) ComponentManagerFactory.getComponentManager().getComponent(userModeStubCompName);

            // [jzhang 102306] IPC Shared memory channel stub (will be
            // depreciated)
            userModeStubConfig.setProperty(IIPCStub.REQUEST_HANDLER_CLASS_NAME_CONFIG_PARAM, CMRequestHandler.class.getName());
            ipcUserModeStub = (IPCUserModeStub) ComponentManagerFactory.getComponentManager().getComponent(IPCLinuxUserModeStub.class, userModeStubConfig);
            getLog().info("IPC User mode IPC stub instantiated");
            ipcUserModeStubThread = new Thread(ipcUserModeStub);
            ipcUserModeStubThread.start();
            getLog().info("Started IPC user mode stub thread");

        } else {
            userModeStubConfig.setProperty(IIPCStub.REQUEST_HANDLER_CLASS_NAME_CONFIG_PARAM, CMRequestHandler.class.getName());
            String userModeStubCompName = IPCUserModeStub.class.getName();
            ComponentInfo userModeStubCompInfo = new ComponentInfo(userModeStubCompName, IPCUserModeStub.class.getName(), null, LifestyleType.TRANSIENT_TYPE, userModeStubConfig);
            ComponentManagerFactory.getComponentManager().registerComponent(userModeStubCompInfo, true);
            userModeStub = (IPCUserModeStub) ComponentManagerFactory.getComponentManager().getComponent(userModeStubCompName);
        }

        getLog().info("User mode IPC stub instantiated");
        userModeStubThread = new Thread(userModeStub);
        userModeStubThread.start();
        getLog().info("Started user mode stub thread");

        // For file server agent, add extra IPC stub for kernel

        //For SDK, temporarily obsolete kernel mode stub
        // if (false && AgentTypeDTO.FILE_SERVER == getAgentType()) {
        getLog().info("About to instantiate the kernel mode IPC stub...");
        final HashMapConfiguration kernelModeStubConfig = new HashMapConfiguration();
        kernelModeStubConfig.setProperty(IIPCStub.THREAD_POOL_SIZE_CONFIG_PARAM, kernelIPCThreadCount);
        kernelModeStubConfig.setProperty(IIPCStub.REQUEST_HANDLER_CLASS_NAME_CONFIG_PARAM, CMKernelRequestHandler.class.getName());

        kernelModeStubConfig.setProperty(IIPCStub.STUB_TYPE, new Integer(1)); // kernel
        // mode

        // [jzhang 092506] using different componentInfo for two stubs
        String kernelModeStubCompName = IPCLinuxStub.class.getName() + "Kernel";
        ComponentInfo kernelModeStubCompInfo = new ComponentInfo(kernelModeStubCompName, IPCLinuxStub.class.getName(), null, LifestyleType.TRANSIENT_TYPE, kernelModeStubConfig);
        ComponentManagerFactory.getComponentManager().registerComponent(kernelModeStubCompInfo, true);

        try {
            
            if (osWrapper.isLinux()) {
                kernelModeStub = (IPCStubBase) ComponentManagerFactory.getComponentManager().getComponent(kernelModeStubCompInfo);
            } else {
                kernelModeStub = (IPCStubBase) ComponentManagerFactory.getComponentManager().getComponent(IPCKernelStub.class, kernelModeStubConfig);
            }
            getLog().info("Kernel mode IPC stub instantiated");
            kernelModeStubThread = new Thread(kernelModeStub);
            kernelModeStubThread.setPriority(java.lang.Thread.MAX_PRIORITY);
            kernelModeStubThread.start();
            getLog().info("Started kernel mode stub thread");


            
            /*
             * There is a CM method handling there in the CMRequestHandler.
             * However, we can't use it because in the kernel mode, we don't
             */
            /*
             * even call the invoke function, but we "doWork" and call the
             * queryEngine directly from the CMKernelRequestHandler
             */
            /*
             * Therefore, we will have to do it from this initialization place
             * for the kernel.
             */

            Set<UserSubjectTypeEnumType> userSubjectTypeSet = new HashSet<UserSubjectTypeEnumType>();
            UserSubjectTypeEnumType userSubjectType = osWrapper.isLinux() ? UserSubjectTypeEnumType.LINUX_USER_SUBJECT_TYPE : UserSubjectTypeEnumType.WINDOWS_USER_SUBJECT_TYPE;

            userSubjectTypeSet.add(userSubjectType);
            registerUserSubjectTypes(userSubjectTypeSet);

        } catch (Exception e) {
            getLog().info("Kernel stub (driver) doesn't exist");
        }
    }

    /**
     * Shuts down the IPC stubs
     */
    private void uninitIPCStub() {
        // stop the IPC stub
        userModeStubThread.interrupt();
        userModeStub.stop();

        // [jzhang 102306]
        if (osWrapper.isLinux()) {
            ipcUserModeStubThread.interrupt();
            ipcUserModeStub.stop();
        }

        getLog().info("Stopped user mode IPC Stub...");
        if (AgentTypeDTO.FILE_SERVER == getAgentType()) {
            kernelModeStubThread.interrupt();
            kernelModeStub.stop();
            getLog().info("Stopped kernel mode IPC Stub...");
        }
    }

    public ControlMngr() {
        SCMEventManager scm = SCMEventManager.getInstance();
        scm.addSCMEventListener(this);
    }

    public void handleSCMEvent(SCMEvent event) {
        int eventID = event.getID();
        if (eventID == SCMEvent.SERVICE_STOPPED) {
            getLog().info("Received SERVICE_STOPPED event");
            uninit();
        } else if (eventID == SCMEvent.SERVICE_STARTED) {
            getLog().info("Received SERVICE_STARTED event");
            init();
        } else if (eventID == SCMEvent.SERVICE_CONTROL_DEVICEEVENT) {
            // retrieve and update device information
            getLog().info("Received SERVICE_CONTROL_DEVICEEVENT event");
        } else if (eventID == SCMEvent.ABNORMAL_SHUTDOWN) {
            IComponentManager manager = ComponentManagerFactory.getComponentManager();
            IDSubjectManager subjectManager = (IDSubjectManager) manager.getComponent(IDSubjectManager.class.getName());
            IDSubject host = subjectManager.getSubject(getHostName(), SubjectType.HOST);
            Long hostId = host != null ? host.getId() : IDSubject.UNKNOWN_ID;
            logTrackingActivity(ActionEnumType.ACTION_ABNORMAL_AGENT_SHUTDOWN, IHasId.UNKNOWN_ID, IHasId.UNKNOWN_NAME, hostId, getHostName(), getHostIP(), null, "", CMRequestHandler.DUMMY_AGENT_FROM_STR, null, 3);
        }
    }
    
    public void init() {
        try {
            // Initialize all components
            getLog().info("Initializing all components.");
            if (!isInitialized) {
                HashMapConfiguration config = new HashMapConfiguration();
                IComponentManager manager = ComponentManagerFactory.getComponentManager();

                setupHostName();
                setupVersion();

                getLog().info("Initializing ProfileManager.");
                // Initialize profile manager
                config = new HashMapConfiguration();
                config.setProperty(ProfileManager.BASE_DIR_PROPERTY_NAME, policyControllerRootDirectory);
                ComponentInfo info = new ComponentInfo(IProfileManager.NAME, ProfileManager.class.getName(), IProfileManager.class.getName(), LifestyleType.SINGLETON_TYPE, config);
                profileManager = (IProfileManager) manager.getComponent(info);

                setupHeartbeatRate();
                setupTrackedActions();

                // Initialize Command Executor
                getLog().info("Initializing CommandExecutor.");
                info = new ComponentInfo(ICommandExecutor.NAME, CommandExecutor.class.getName(), ICommandExecutor.class.getName(), LifestyleType.SINGLETON_TYPE);
                commandExecutor = (ICommandExecutor) manager.getComponent(info);

                getLog().info("Initializing HeartbeatManager.");
                info = new ComponentInfo(IHeartbeatManager.class.getName(), HeartbeatManagerImpl.class.getName(), IHeartbeatManager.class.getName(), LifestyleType.SINGLETON_TYPE);
                heartbeatManager = (IHeartbeatManager) manager.getComponent(info);
            
                config = new HashMapConfiguration();
                config.setProperty(AgentKeyManagerComponentBuilder.AGENT_HOME_DIRECTORY_PROPERTY_NAME, policyControllerRootDirectory);
                info  = new ComponentInfo(IAgentKeyManagerComponentBuilder.COMPONENT_NAME, AgentKeyManagerComponentBuilder.class.getName(), IAgentKeyManagerComponentBuilder.class.getName(), LifestyleType.SINGLETON_TYPE, config);
                keyManagerBuilder = (IAgentKeyManagerComponentBuilder) manager.getComponent(info);
                if (!readRegistrationInfo()) {
                    keyManagerBuilder.buildNonregisteredKeyManager();
                } else {
                    keyManagerBuilder.buildRegisteredKeyManager();
                }

                // Initialize Communication Manager - change mock to real
                getLog().info("Initializing CommunicationManager.");
                config = new HashMapConfiguration();
                config.setProperty(CommunicationManager.COMM_PROFILE_CONFIG_KEY, getCommunicationProfile());
                communicationManager = (ICommunicationManager) manager.getComponent(CommunicationManager.class, config);

                // Load from file
                getLog().info("Loading custom obligations and shared folder info.");
                updateCustomObligationsInfo(null);
                updateSharedFolderInfo(null);

                // All required components are ready. Log

                // Initialize Activity Journal
                getLog().info("Initializing ActivityJournal.");
                config = new HashMapConfiguration();
                config.setProperty(IControlManager.NAME, this);
                config.setProperty(ActivityJournal.BASE_DIR_PROPERTY_NAME, policyControllerRootDirectory);
                info = new ComponentInfo(IActivityJournal.NAME, ActivityJournal.class.getName(), IActivityJournal.class.getName(), LifestyleType.SINGLETON_TYPE, config);
                activityJournal = (IActivityJournal) manager.getComponent(info);

                getLog().info("Initializing NotificationManager.");
                config = new HashMapConfiguration();
                config.setProperty(NotificationManager.BASE_DIR_PROPERTY_NAME, policyControllerRootDirectory);
                info = new ComponentInfo(INotificationManager.NAME, NotificationManager.class.getName(), INotificationManager.class.getName(), LifestyleType.SINGLETON_TYPE, config);
                notificationManager = (INotificationManager) manager.getComponent(info);

                getLog().info("Initializing HeartbeatRecordManager.");
                config = new HashMapConfiguration();
                config.setProperty(HeartbeatRecordManager.BASE_DIR_PROPERTY_NAME, policyControllerRootDirectory);
                info = new ComponentInfo(IHeartbeatRecordManager.NAME, HeartbeatRecordManager.class.getName(), IHeartbeatRecordManager.class.getName(), LifestyleType.SINGLETON_TYPE);
                heartbeatRecordManager = (IHeartbeatRecordManager) manager.getComponent(info, config);
                heartbeatRecordManager.detectRollback(doRollbackDetection);

                if (agentId == null) {
                    // if agent id is not available, register with server.
                    getLog().info("agent id was null, registering with server.");
                    if (!registerAgent()) {
                        getLog().error("Unable to register with server. Registration is required for application to continue. Registration will be retried.");
                    }
                }

                // Initialize Policy Container
                getLog().info("Initializing AgentPolicyContainer.");
                config = new HashMapConfiguration();
                config.setProperty(IAgentPolicyContainer.AGENT_TYPE_CONFIG_PARAM, AgentTypeEnumType.getAgentType(agentType.getValue()));
                config.setProperty(IAgentPolicyContainer.BASE_DIR_PROPERTY_NAME, policyControllerRootDirectory);
                config.setProperty(IAgentPolicyContainer.DISK_CACHE_LOCATION_CONFIG_PARAM, policyControllerRootDirectory);
                if (agentResourceEvalCacheSize != -1) {
                    config.setProperty(IAgentPolicyContainer.DISK_CACHE_SIZE_CONFIG_PARAM, agentResourceEvalCacheSize);
                }

                policyContainer = (IAgentPolicyContainer) manager.getComponent(IAgentPolicyContainer.COMP_INFO, config);
                if (agentId != null) {
                    try {
                        policyContainer.loadBundle();
                    } catch (InvalidBundleException exception) {
                        reportInvalidBundle();
                    }
                }

                getLog().info("Initializing TrustedProcessManager.");
                // Trusted process manager
                trustedProcessManager = (ITrustedProcessManager) manager.getComponent(TrustedProcessManager.COMP_INFO);
                
                // The PC is trusted forever
                trustedProcessManager.addPermanentTrustedProcess(osWrapper.getProcessId());


                //In SDK, IPCStub has been obsolete
                if(!isSDK) {
                    getLog().info("Initializing IPCStub.");
                    initIPCStub();
                }

                // Initialize Scheduler
                getLog().info("Initializing Scheduler.");
                config = new HashMapConfiguration();
                config.setProperty(IControlManager.NAME, this);
                scheduler = (Scheduler) manager.getComponent(Scheduler.class, config);

                getLog().info("Sending heartbeat.");
                commandExecutor.start();
                commandExecutor.sendHeartBeat();

                getLog().info("Initializing SubjectManager.");
                IDSubjectManager subjectManager = (IDSubjectManager) manager.getComponent(IDSubjectManager.class.getName());

                if (subjectManager != null) {
                    try {
                        IDSubject host = subjectManager.getSubject(getHostName(), SubjectType.HOST);
                        Long hostId = host != null ? host.getId() : IDSubject.UNKNOWN_ID;

                        logTrackingActivity(ActionEnumType.ACTION_START_AGENT, IHasId.SYSTEM_USER_ID, IHasId.SYSTEM_USER_NAME, hostId, getHostName(), getHostIP(), null, "", CMRequestHandler.DUMMY_AGENT_FROM_STR, null, 3);
                    } catch (NullPointerException e) {
                        // This will happen when we have no bundle. Ignore.
                        getLog().trace("NPE while getting hostid because we dont have a bundle yet.");
                    }
                }
            
                getLog().info("Initializing ServiceProviderManager.");
                config = new HashMapConfiguration();
                config.setProperty(IServiceProviderManager.BASE_DIR_PROPERTY_NAME, policyControllerRootDirectory);
                manager.getComponent(ServiceProviderManager.class, config);
            
                isInitialized = true;
            }
        } catch (Throwable e) {
            getLog().error("ControlMngr.init() failed: " + e);
        }
    }

    /**
     * Gets tracked actions from the profile and adds them to trackedActionMap
     */
    private void setupTrackedActions() {
        trackedActions = new HashSet<ActionEnumType>();
        trackedActions.add(ActionEnumType.ACTION_STOP_AGENT);
        ActivityJournalingSettingsDTO activityJournalSettings = profileManager.getCommunicationProfile().getCurrentActivityJournalingSettings();
        if (activityJournalSettings != null) {
            ActionTypeDTO[] trackedActionList = activityJournalSettings.getLoggedActivities().getAction();
            if (trackedActionList != null) {
                for (int i = 0; i < trackedActionList.length; i++) {
                    ActionTypeDTO actionTypeDTO = trackedActionList[i];
                    trackedActions.add(ActionEnumType.getActionEnum(actionTypeDTO.getValue()));
                }
            }
        }
    }

    /**
     * Gets the heartbeat rate from the profile (used by getSecondsUntilNextHeartbeat)
     */
    private void setupHeartbeatRate() {
        long unitMultiplier = TimeUnitsUtils.getMultiplier(getCommunicationProfile().getHeartBeatFrequency().getTimeUnit());
        heartbeatRateInSeconds = (getCommunicationProfile().getHeartBeatFrequency().getTime().intValue() * unitMultiplier)/1000;
    }


    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#logDecision()
     */
    public void logDecision(String logEntity, String userDecision, String[] attributes) {
        PolicyActivityLogEntryV5 entry = new PolicyActivityLogEntryV5();

        SerializationUtils.unwrapExternalized(logEntity, entry);
        // Change user supplied information
        entry.setPolicyDecision(PolicyDecisionEnumType.getPolicyDecisionEnum(userDecision.toLowerCase()));
        addAttributes(entry.getCustomAttributes(), attributes);
        commandExecutor.logActivity(entry);
        
        return;
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#handleLogonEvent()
     * This code was copied from CMRequestHandler
     */
    public void handleLogonEvent(String userSID) {
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        IDSubjectManager subjectManager = (IDSubjectManager) manager.getComponent(IDSubjectManager.class.getName());
        IDSubject user = subjectManager.getSubject(userSID, SubjectType.USER);
        Long userId = user != null ? user.getId() : IDSubject.UNKNOWN_ID;
        
        IDSubject host = subjectManager.getSubject(getHostName(), SubjectType.HOST);
        Long hostId = host != null ? host.getId() : IDSubject.UNKNOWN_ID;

        String userName = UserNameCache.getUserName(userSID);
        logTrackingActivity(ActionEnumType.ACTION_AGENT_USER_LOGIN, userId, userName, hostId, getHostName(), "", null, "", CMRequestHandler.DUMMY_AGENT_FROM_STR, null, 3);
        ICommandExecutor commandExecutor = (ICommandExecutor) ComponentManagerFactory.getComponentManager().getComponent(ICommandExecutor.NAME);
        getLog().debug("Reported user logon: " + userName);
        
        // force heartbeat to get bundle
        commandExecutor.sendHeartBeat();
        
        return;
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#handleLogoffEvent()
     * This code was copied from CMRequestHandler
     */
    public void handleLogoffEvent(String userSID) {
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        IDSubjectManager subjectManager = (IDSubjectManager) manager.getComponent(IDSubjectManager.class.getName());
        IDSubject user = subjectManager.getSubject(userSID, SubjectType.USER);
        Long userId = user != null ? user.getId() : IDSubject.UNKNOWN_ID;
        
        IDSubject host = subjectManager.getSubject(getHostName(), SubjectType.HOST);
        Long hostId = host != null ? host.getId() : IDSubject.UNKNOWN_ID;

        String userName = UserNameCache.getUserName(userSID);
        logTrackingActivity(ActionEnumType.ACTION_AGENT_USER_LOGOUT, userId, userName, hostId, getHostName(), "", null, "", CMRequestHandler.DUMMY_AGENT_FROM_STR, null, 3);
        ICommandExecutor commandExecutor = (ICommandExecutor) ComponentManagerFactory.getComponentManager().getComponent(ICommandExecutor.NAME);
        
        // NI 3/30/2010: PC has been sending heartbeat when user logs out, but we do not think it is necessary
        
        getLog().debug("Reported user logoff: " + userName);

        return;
    }

    
    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#logAssistantData()
     */
    public void logAssistantData(String logIdentifier,
                                 String assistantName,
                                 String assistantOptions,
                                 String assistantDescription,
                                 String assistantUserActions) {

        if (getLog().isTraceEnabled()) {
            String s = "Log Assistant Data:\nID:    " + logIdentifier + 
                       "\nName:  " + assistantName + 
                       "\nAttr1: " + assistantOptions + 
                       "\nAttr2: " + assistantDescription + 
                       "\nAttr3: " + assistantUserActions;
            getLog().trace(s);
        }

        PolicyAssistantLogEntry entry = new PolicyAssistantLogEntry(logIdentifier,
                                                                    assistantName,
                                                                    assistantOptions,
                                                                    assistantDescription,
                                                                    assistantUserActions);
        entry.setTimestamp(System.currentTimeMillis());
        commandExecutor.logActivity(entry);
        return;
    }

    private void addAttributes(DynamicAttributes base, String[] additionalAttributes) {
        if (additionalAttributes == null || base == null) {
            return;
        }
        
        for (int i = 0; i < additionalAttributes.length; i+=2) {
            String key = additionalAttributes[i];
            try {
                String value = additionalAttributes[i+1];
                base.add(key, value);
            } catch(ArrayIndexOutOfBoundsException e) {
            }
        }
    }

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
     */
    public void logTrackingActivity(ActionEnumType action, Long userId, String userName,
                                    Long hostId, String hostName, String hostIP,
                                    Long applicationId, String appName,
                                    String fromResource, String toResource,
                                    int level) {
        logTrackingActivity(action, userId, userName, hostId, hostName, hostIP, applicationId, appName, fromResource, null, toResource, null, level);
    }

    public void logTrackingActivity(ActionEnumType action, Long userId, String userName,
                                    Long hostId, String hostName, String hostIP,
                                    Long applicationId, String appName,
                                    String fromResource, DynamicAttributes fromAttrs,
                                    String toResource, DynamicAttributes toAttrs,
                                    int level) {
        TrackingLogEntryV3 trackingLogEntry = new TrackingLogEntryV3();
        trackingLogEntry.setTimestamp(System.currentTimeMillis());
        trackingLogEntry.setAction(action.getName());
        if (userId != null) {
            trackingLogEntry.setUserId(userId.longValue());
        } else {
            trackingLogEntry.setUserId(IHasId.UNKNOWN_ID.longValue());
        }
        if (hostId != null) {
            trackingLogEntry.setHostId(hostId.longValue());
        } else {
            trackingLogEntry.setHostId(IHasId.UNKNOWN_ID.longValue());
        }
        if (applicationId != null) {
            trackingLogEntry.setApplicationId(applicationId.longValue());
        } else {
            trackingLogEntry.setApplicationId(IHasId.UNKNOWN_ID.longValue());
        }

        trackingLogEntry.setUserName(userName);
        trackingLogEntry.setHostName(hostName);
        trackingLogEntry.setHostIP(hostIP);
        trackingLogEntry.setApplicationName(appName);
        trackingLogEntry.setLevel(level);
        trackingLogEntry.setCustomAttr(fromAttrs);

        if (fromResource != null) {
            trackingLogEntry.setFromResourceInfo(makeFromResourceInfo(fromResource, fromAttrs));
        }
        if (toResource != null) {
            trackingLogEntry.setToResourceInfo(makeToResourceInfo(toResource));
        }

        commandExecutor.logActivity(trackingLogEntry);
    }

    private long readAttr(DynamicAttributes attr, String key, long defaultVal) {
        IEvalValue val = attr.get(key);
        
        if (val != null ) {
            if (val.getType() != ValueType.LONG &&
                val.getType() != ValueType.DATE) {
                long asLong = defaultVal;
                try {
                    asLong = Long.parseLong((String)val.getValue());
                } catch (NumberFormatException e) {
                }
                return asLong;
            } else {
                return (Long)val.getValue();
            }
        }

        return defaultVal;
    }


    /**
     * Returns a "From resource" information
     * 
     * @param resource
     *            "from resource" name
     * @return a "From resource" information
     */
    private FromResourceInformation makeFromResourceInfo(String resource, DynamicAttributes attrs) {
        FromResourceInformation res = new FromResourceInformation();
        res.setName(resource);

        String ownerId = "-1";
        long createdDate = -1;
        long modifiedDate = -1;
        long size = -1;

        if (attrs != null) {
            ownerId = attrs.getString("owner");
            if (ownerId == null) {
                ownerId = "-1";
            }

            createdDate = readAttr(attrs, "created_date", createdDate);
            modifiedDate = readAttr(attrs, "modified_date", modifiedDate);
            size = readAttr(attrs, "size", size);    

            // If we didn't get good values then try to read them from the file
            if (createdDate == -1 || modifiedDate == -1 || size == -1 || ownerId.equals("-1")) {
                String destinyType = attrs.getString(SpecAttribute.DESTINYTYPE_ATTR_NAME);
                String canDoFsCheckStr = attrs.getString(SpecAttribute.FSCHECK_ATTR_NAME);
                boolean canDoFsCheck = canDoFsCheckStr == null || !canDoFsCheckStr.equals("no");

                if (destinyType != null && destinyType.equals("fso") && canDoFsCheck) {
                    String fileName = attrs.getString(SpecAttribute.ID_ATTR_NAME);
                    File file = new File(fileName);
                    if (file.exists()) {
                        if (createdDate == -1) {
                            createdDate = osWrapper.getFileCreateTime(fileName, null);
                        }
                        if (modifiedDate == -1) {
                            modifiedDate = osWrapper.getFileModifiedTime(fileName, null);
                        }
                        if (size == -1) {
                            size = file.length();
                        }
                        if (ownerId.equals("-1")) {
                            ownerId = osWrapper.getOwnerSID(fileName, agentType == AgentTypeDTO.DESKTOP ? 0 : 1, null);
                        }
                    }
                }
            }
        }

        res.setCreatedDate(createdDate);
        res.setModifiedDate(modifiedDate);
        res.setSize(size);
        res.setOwnerId(ownerId);
        return res;
    }

    /**
     * Create a "to resource" information
     * 
     * @param resource
     *            "to resource" name
     * @return a "to resource" information
     */
    private ToResourceInformation makeToResourceInfo(String resource) {
        ToResourceInformation res = new ToResourceInformation();
        res.setName(resource);
        return res;
    }

    /**
     * sends a startup request to the server.
     * 
     * @return true if sent successfully.
     */
    private boolean sendStartupRequest() {
        AgentStartupData startupData = new AgentStartupData();

        try {
            int pushPortInt = communicationManager.getPort();
            UnsignedShort port = null;
            if (pushPortInt >= 0) {
                port = new UnsignedShort(pushPortInt);
            }
            startupData.setPushPort(port);
            AgentServiceIF agentServiceIF = communicationManager.getAgentServiceIF();
            agentServiceIF.startupAgent(agentId, startupData);
            startupRequestSent = true;
        } catch (ServiceNotReadyFault e) {
            getLog().info("Startup Message: Unable to send request to server. May be because machine is disconnected.", e);
        } catch (UnauthorizedCallerFault e) {
            getLog().error("Server denied access. Registration/KeyGeneration may have been unsuccessful", e);
        } catch (RemoteException e) {
            getLog().error("Startup Message. Server returned an error.", e);
        } catch (ServiceException e) {
            getLog().error("Agent Startup. Could not get Agent service interface.", e);
        }

        return startupRequestSent;

    }

    /**
     * Reads the registration info file if available and populates AgentId
     */
    private boolean readRegistrationInfo() {
        boolean successful = false;
        try {
            File registrationFile = new File(policyControllerRootDirectory + File.separator + ControlMngr.REGISTRATION_FILE);
            String fullPath = registrationFile.getAbsolutePath();
            getLog().info("The path is " + fullPath);
            if (registrationFile.exists()) {
                ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(registrationFile)));
                agentId = (BigInteger) in.readObject();
                registrationId = (BigInteger) in.readObject();
                successful = true;
            }
        } catch (IOException e) {
            getLog().error("Unable to read registration file.", e);
        } catch (ClassNotFoundException e) {
            getLog().error("Unable to read registration file.", e);
        }

        return successful;
    }

    /**
     * send registration info to server. Extract agent ID and profiles from
     * response.
     */
    private boolean registerAgent() {
        AgentRegistrationData registrationData = new AgentRegistrationData();

        registrationData.setHost(hostName);
        registrationData.setType(agentType);
        Version wsVersion = VersionUtil.convertIVersionToWSVersion(version);
        registrationData.setVersion(wsVersion);

        AgentStartupConfiguration startupConfiguration = null;
        AgentServiceIF agentServiceIF = null;
        try {
            agentServiceIF = communicationManager.getAgentServiceIF();
            getLog().info("Sending registration request");
            startupConfiguration = agentServiceIF.registerAgent(registrationData);
            getLog().info("Received registration response");
        } catch (ServiceNotReadyFault e) {
            getLog().info("Agent Registration: Unable to send request to server. May be because machine is disconnected.", e);
        } catch (UnauthorizedCallerFault e) {
            getLog().error("Server denied access. Agent Registration unsuccessful", e);
        } catch (RemoteException e) {
            getLog().error("Agent Registration: Server returned an error.", e);
        } catch (ServiceException e) {
            getLog().error("Agent Registration: Could not get Agent Service interface.", e);
        }

        if (startupConfiguration != null) {

            agentId = startupConfiguration.getId();
            registrationId = startupConfiguration.getRegistrationId();

            // get keystores from attachments.
            AgentServiceIFBindingStub agentSvcStub = (AgentServiceIFBindingStub) agentServiceIF;

            Object[] attachments = null;
            attachments = agentSvcStub.getAttachments();
            for (int i = 0; i < attachments.length; i++) {
                AttachmentPart attachment = (AttachmentPart) attachments[i];
                if (attachment.getContentId().equals(AGENT_KEYSTORE_ATTACHMENT)) {
                    try {
                        InputStream is = (InputStream) attachment.getContent();
                        keyManagerBuilder.updateRegisteredKeystore(is);                                  
                    } catch (SOAPException e2) {
                        getLog().error("Unable to read key store attachment", e2);
                    } catch (IOException e) {
                        getLog().error("Unable to save key store attachment", e);
                    }
                } else if (attachment.getContentId().equals(AGENT_TRUSTSTORE_ATTACHEMENT)) {
                    try {
                        InputStream is = (InputStream) attachment.getContent();
                        keyManagerBuilder.updateRegisteredTruststore(is);      
                    } catch (SOAPException e2) {
                        getLog().error("Unable to read trust store attachment", e2);
                    } catch (IOException e) {
                        getLog().error("Unable to save trust store attachment", e);
                    }
                } else if (attachment.getContentId().equals(AGENT_EXTRA_ATTACHMENT)) {
                    try {
                        InputStream is = (InputStream) attachment.getContent();
                        int len = is.available();
                        byte[] bytes = new byte[len];
                        is.read(bytes, 0, len);
                        String password = new String(bytes);
                        ReversibleEncryptor decryptor = new ReversibleEncryptor();
                        try {
                            password = decryptor.decrypt(password);
                            password = password.substring(5);
                        } catch (Exception e) {
                            // TODO we should change the encryptor to throw an
                            // exception if the input is bad.
                            // getLog().error ("Invalid password", e);
                        }

                        keyManagerBuilder.updateRegisteredKeystorePassword(password);
                    } catch (SOAPException e) {
                        getLog().error("Unable to read extra registration info", e);
                    } catch (IOException e) {
                        getLog().error("Unable to read extra registration info", e);
                    }
                }
            }
            keyManagerBuilder.buildRegisteredKeyManager();

            if (AxisSocketFactory.getInstance() != null) {
                AxisSocketFactory.getInstance().reloadKeyStore();
            }            

            ObjectOutputStream out;
            try {
                out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(policyControllerRootDirectory + File.separator + ControlMngr.REGISTRATION_FILE)));
                out.writeObject(agentId);
                out.writeObject(registrationId);
                out.close();
            } catch (FileNotFoundException e1) {
                getLog().error("Unable to write registration info file", e1);
            } catch (IOException e1) {
                getLog().error("Unable to write registration info file", e1);
            }

            profileManager.setAgentProfile(startupConfiguration.getAgentProfile());
            profileManager.setCommunicationProfile(startupConfiguration.getCommProfile());

            // reinitialize communication manager new profiles
            HashMapConfiguration config = new HashMapConfiguration();
            config.setProperty(CommunicationManager.COMM_PROFILE_CONFIG_KEY, startupConfiguration.getCommProfile());
            ((IConfigurable)communicationManager).setConfiguration(config);
            communicationManager.reinit();
            if (activityJournal != null) {
                activityJournal.setEnabled();
            }

            setupHeartbeatRate();
            setupTrackedActions();

            return (true);
        }

        return (false);
    }

    /**
     * Gets the host name of the local machine and assigns it to the hostname
     * member variable.
     */
    private void setupHostName() {
        // Get hostname
        hostName = osWrapper.getFQDN().toLowerCase();

        if (getLog().isInfoEnabled()) {
            getLog().info("Host name: " + hostName);
        }

        try {
            hostIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            hostIP = "127.0.0.1";
        }
    }

    /**
     * Gets the host name of the local machine and assigns it to the hostname
     * member variable.
     */
    private void setupVersion() {
        // Get hostname
        try {
            version = (new VersionFactory()).getVersion();
        } catch (IOException e) {
            getLog().fatal("Version file read error in");
            throw new RuntimeException(e);
        } catch (InvalidVersionException e) {
            getLog().fatal("Invalid version");
            throw new RuntimeException(e);
        }

        if (getLog().isInfoEnabled()) {
            getLog().info("Version: " + version);
        }
    }

    public void uninit() {
        // uninitialize all components
        // TODO logTrackingActivity();
        getLog().info("un-initializing all components");
        if (isInitialized) {
            //In SDK, IPCStub has been obsolete
            if(!isSDK) {
                uninitIPCStub();
            }
            activityJournal.stop();
            isInitialized = false;
        }
        commandExecutor.stop();
    }

    /**
     * @return true if the agent is initialized.
     */
    public static boolean isInitialized() {
        ControlMngr controlManager = (ControlMngr) ComponentManagerFactory.getComponentManager().getComponent(IControlManager.NAME);
        if (controlManager != null) {
            return controlManager.isInitialized;
        }

        return false;
    }

    /*
     * Extract the integer part of command line arguments of the form
     * Blahblah=integer Return a default if there is nothing to the
     * right of the = sign or if it isn't an integer, etc.
     */
    private static int getIntegerArgument(String s, int defaultVal) {
        int val = defaultVal;
        try {
            // We assume this is key=intvalue
            int keylen = s.indexOf('=');
            val = Integer.parseInt(s.substring(keylen+1, s.length()));
        } catch (IndexOutOfBoundsException ex) {
        } catch (NumberFormatException ex) {
        }
        return val;
    }

    public static void main(String[] args) {
        ControlMngr obj = (ControlMngr)ComponentManagerFactory.getComponentManager().getComponent(ControlMngr.COMP_INFO);

        if (args.length > 0) {
            try {
                obj.agentType = AgentTypeDTO.fromValue(args[0]);
            } catch (IllegalArgumentException e) {
                obj.agentType = AgentTypeDTO.FILE_SERVER;
            }
        }
        else {
            obj.agentType = AgentTypeDTO.DESKTOP;
        }

        boolean isNotService = false;
        if (args.length > 1 && args[1].compareToIgnoreCase(NO_SERVICE_ARG) == 0) {
            isNotService = true;
        } 
        
        // check other options
        for(int i=0; i<args.length; i++) {
            if(args[i].compareToIgnoreCase(SDK_OPT) == 0) {
                isSDK = true;
            } else if(args[i].compareToIgnoreCase(ROLLBACK_DETECTION_OPT) == 0) {
                doRollbackDetection = true;
            } else if(args[i].startsWith(RESOLVED_CACHE_TIMEOUT+"=")) {
                resolvedDNSCacheTimeout = getIntegerArgument(args[i], resolvedDNSCacheTimeout);
            } else if(args[i].startsWith(TRUSTED_PROCESS_TIMEOUT+"=")) {
                trustedProcessTimeout = getIntegerArgument(args[i], trustedProcessTimeout);
            } else if(args[i].startsWith(SDK_CACHE_HINT_VALUE+"=")) {
                int ret = getIntegerArgument(args[i], -1);

                if (ret >= 0) {
                    sdkCacheHintValue = ret;
                }
            } else if(args[i].startsWith(KERNEL_IPC_THREAD_COUNT+"=")) {
                kernelIPCThreadCount = getIntegerArgument(args[i], kernelIPCThreadCount);
            } else if(args[i].startsWith(RESOLVE_HOST_INFORMATION+"=")) {
                String resolveYesNo = args[i].substring(args[i].indexOf('=')+1);
                resolveHostInfo = resolveYesNo.equals("yes");
            } else if(args[i].startsWith(ROOT_DIRECTORY+"=")) {
                policyControllerRootDirectory = args[i].substring(args[i].indexOf('=')+1);
            } else if(args[i].startsWith(EVAL_CACHE_SIZE+"=")) {
                agentResourceEvalCacheSize = getIntegerArgument(args[i], agentResourceEvalCacheSize);
            }
        }
        
        // new ControlMngr();
        obj.init();
        Logger SystemLogger = Logger.getLogger("com.bluejungle.destiny.agent.controlmanager");
        SystemLogger.info("Starting the Control Mngr.");
        if (isNotService && obj.agentType == AgentTypeDTO.FILE_SERVER) {
            InputStreamReader unbuffered = new InputStreamReader(System.in);
            BufferedReader keyboard = new BufferedReader(unbuffered);

            while (true) {
                try {
                    if (keyboard.readLine().equalsIgnoreCase("X")) {
                        obj.uninit();
                        System.out.println("IPC Stub Stopped.");
                        System.exit(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getAgentProfile()
     */
    public AgentProfileDTO getAgentProfile() {
        return profileManager.getAgentProfile();
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getCommunicationProfile()
     */
    public CommProfileDTO getCommunicationProfile() {
        return profileManager.getCommunicationProfile();
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getAgentPluginData()
     */
    private AgentPluginData getAgentPluginData(AgentHeartbeatData request) {
        Map<String, Serializable> data = heartbeatManager.getHeartbeatPluginData();
        AgentPluginDataElement[] elements = new AgentPluginDataElement[data.size()];
        
        int i = 0;
        for (Map.Entry<String, Serializable> entry : data.entrySet()) {
            AgentPluginDataElement element = new AgentPluginDataElement(SerializationUtils.wrapSerializable(entry.getValue()));
            element.setId(entry.getKey());
            elements[i++] = element;
        }

        return new AgentPluginData(elements);
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#sendHeartBeat()
     */
    public void sendHeartBeat() {

        if (agentId == null) {
            if (!registerAgent()) {
                getLog().error("Unable to register with server. Registration is required for application to continue. Registration will be retried.");
                return;
            }
        }

        if (!startupRequestSent) {
            // we need to continue to try sending the startup request till it is
            // successful
            sendStartupRequest();
        }

        AgentHeartbeatData request = new AgentHeartbeatData();
        AgentProfileStatusData profileStatus = new AgentProfileStatusData();
        profileStatus.setLastCommittedAgentProfileName(getAgentProfile().getName().toString());
        profileStatus.setLastCommittedAgentProfileTimestamp(getAgentProfile().getModifiedDate());
        profileStatus.setLastCommittedCommProfileName(getCommunicationProfile().getName().toString());
        profileStatus.setLastCommittedCommProfileTimestamp(getCommunicationProfile().getModifiedDate());

        request.setProfileStatus(profileStatus);

        DeploymentRequest deploymentRequest;
        if (agentType == AgentTypeDTO.DESKTOP) {
            deploymentRequest = policyContainer.getDeploymentRequestForUsers(getLoggedInUsers(), hostName);
        } else if (agentType == AgentTypeDTO.PORTAL) {
            deploymentRequest = policyContainer.getDeploymentRequestForSubjectTypes(getRegisteredUserSubjectTypes(), hostName, AgentTypeEnum.PORTAL);
        } else if (agentType == AgentTypeDTO.FILE_SERVER) {
            deploymentRequest = policyContainer.getDeploymentRequestForSubjectTypes(getRegisteredUserSubjectTypes(), hostName, AgentTypeEnum.FILE_SERVER);
        } else {
            getLog().error("Unknown agent type: " + agentType);
            return;
        }

        request.setPolicyAssemblyStatus(deploymentRequest);

        request.setSharedFolderDataCookie(sharedFolderCookie);
        request.setPluginData(getAgentPluginData(request));

        boolean needConfirmation = false;
        try {
            AgentServiceIF agentServiceIF = communicationManager.getAgentServiceIF();

            updateLastHeartbeat();

            if (getLog().isInfoEnabled()) {
                getLog().info("Heartbeat request sent...");
            }
            AgentUpdates response = agentServiceIF.checkUpdates(agentId, request);
            if (getLog().isInfoEnabled()) {
                osWrapper.logEvent(EventMessages.EVENTLOG_INFORMATION_TYPE, EventMessages.MSG_HEARTBEAT, new String[0]);
            }

            updateLastSuccessfulHeartbeat(getLastHeartbeat());

            getLog().info("Heartbeat response received...");

            if (response.getAgentProfile() != null) {
                profileManager.setAgentProfile(response.getAgentProfile());
                needConfirmation = true;
            }
            if (response.getCommProfile() != null) {
                if (getLog().isInfoEnabled()) {
                    getLog().info("Received profile updates from server.");
                    osWrapper.logEvent(EventMessages.EVENTLOG_INFORMATION_TYPE, EventMessages.MSG_PROFILE_UPDATE, new String[0]);
                }
                profileManager.setCommunicationProfile(response.getCommProfile());
                HashMapConfiguration config = new HashMapConfiguration();
                config.setProperty(CommunicationManager.COMM_PROFILE_CONFIG_KEY, response.getCommProfile());
                ((IConfigurable)communicationManager).setConfiguration(config);
                communicationManager.reinit();
                if (scheduler != null) {
                    scheduler.init();
                }
                if (activityJournal != null) {
                    synchronized (activityJournal) {
                        activityJournal.stop();
                        activityJournal.init();
                    }
                }

                setupHeartbeatRate();
                setupTrackedActions();

                needConfirmation = true;
            }

            String bundleStr = response.getPolicyDeploymentBundle();
            DeploymentBundleSignatureEnvelope bundle = (bundleStr == null) ? null : DTOUtils.decodeDeploymentBundle(bundleStr);
            if (bundle != null) {
                getLog().info("Received policy updates from server.");

                // sent policy updates to policy framework.
                try {
                    policyContainer.update(bundle);
                    reportBundleRecieved();
                    needConfirmation = true;
                } catch (InvalidBundleException exception) {
                    reportInvalidBundle();
                }
            }

            CustomObligationsData receivedCustomObligationsData = response.getCustomObligationsData();
            if (receivedCustomObligationsData != null) {
                getLog().info("Received custom obligations info updates from server.");
                updateCustomObligationsInfo(receivedCustomObligationsData);
            }

            SharedFolderData sharedFolderData = response.getSharedFolderData();
            if (sharedFolderData != null) {
                getLog().info("Received shared folder info updates from server.");
                updateSharedFolderInfo(sharedFolderData);
            }

            AgentPluginData pluginData = response.getPluginData();
            if (pluginData != null) {
                getLog().info("Received plugin data from server.");
                updatePluginDataInfo(pluginData);
            }

            if (response.getServerBusy().booleanValue()) {
                // slow down the heartbeat
            }

            if (needConfirmation) {
                AgentUpdateAcknowledgementData ackData = new AgentUpdateAcknowledgementData();
                profileStatus = new AgentProfileStatusData();
                profileStatus.setLastCommittedAgentProfileName(getAgentProfile().getName().toString());
                profileStatus.setLastCommittedAgentProfileTimestamp(getAgentProfile().getModifiedDate());
                profileStatus.setLastCommittedCommProfileName(getCommunicationProfile().getName().toString());
                profileStatus.setLastCommittedCommProfileTimestamp(getCommunicationProfile().getModifiedDate());
                ackData.setProfileStatus(profileStatus);

                // FIXME change back to call the right api for desktop agent
                // once keni fixes timestamp issue.
                // LATER: We have absolutely no idea why we are doing this or what the 'timestamp issue' is.
                // Presumably this call should either be removed altogether or changed to the 'agent appropriate'
                // version.  Any guesses?
                deploymentRequest = policyContainer.getDeploymentRequestForSubjectTypes(getRegisteredUserSubjectTypes(), hostName, AgentTypeEnum.FILE_SERVER);

                ackData.setPolicyAssemblyStatus(deploymentRequest);

                getLog().info("Sending acknowledgement to server.");
                agentServiceIF.acknowledgeUpdates(agentId, ackData);
                getLog().info("Received response for acknowledgement request.");
            }
        } catch (ServiceNotReadyFault e) {
            if (getLog().isInfoEnabled()) {
                getLog().info("Heartbeat failed: Could not connect to server.");
                osWrapper.logEvent(EventMessages.EVENTLOG_WARNING_TYPE, EventMessages.MSG_HEARTBEAT_FAILED, new String[] { "ICENet Service Not Ready" });
            }
        } catch (UnauthorizedCallerFault e) {
            if (getLog().isErrorEnabled()) {
                getLog().error("Heartbeat failed: Access denied by server.");
                osWrapper.logEvent(EventMessages.EVENTLOG_WARNING_TYPE, EventMessages.MSG_HEARTBEAT_FAILED, new String[] { "Access Denied" });
            }
        } catch (RemoteException e) {
            if (e.detail instanceof SocketTimeoutException) {
                if (getLog().isWarnEnabled()) {
                    getLog().info("Heartbeat request failed. Socket timeout.", e);
                    osWrapper.logEvent(EventMessages.EVENTLOG_WARNING_TYPE, EventMessages.MSG_HEARTBEAT_FAILED, new String[] { "Socket timeout. The ICENet server did not respond" });
                }
            } else {
                if (getLog().isWarnEnabled()) {
                    getLog().info("Heartbeat request failed.", e);
                    osWrapper.logEvent(EventMessages.EVENTLOG_WARNING_TYPE, EventMessages.MSG_HEARTBEAT_FAILED, new String[] { "Could not connect to ICENet server" });
                }
            }
        } catch (ServiceException e) {
            if (getLog().isErrorEnabled()) {
                getLog().error("Heartbeat failed: Could not get Agent Service interface.");
                osWrapper.logEvent(EventMessages.EVENTLOG_WARNING_TYPE, EventMessages.MSG_HEARTBEAT_FAILED, new String[] { "Service Exception" });
            }
        }

        // send notifications if there are any along with the heartbeat
        notificationManager.sendNotifications();
    }

    /**
     * 
     */
    private void reportBundleRecieved() {
        osWrapper.logEvent(EventMessages.EVENTLOG_INFORMATION_TYPE, EventMessages.MSG_POLICY_UPDATE, new String[] { Integer.toString(policyContainer.getPolicyCount()) });
        String hostName = getHostName();
        Long hostId = getHostId();
        String hostIP = getHostIP();
        logTrackingActivity(ActionEnumType.ACTION_BUNDLE_RECEIVED, IHasId.UNKNOWN_ID, IHasId.UNKNOWN_NAME, hostId, hostName, hostIP, null, "", CMRequestHandler.DUMMY_AGENT_FROM_STR, null, 3);        
    }

    /**
     * Report an invalid bundle
     */
    private void reportInvalidBundle() {
        osWrapper.logEvent(EventMessages.EVENTLOG_INFORMATION_TYPE, EventMessages.MSG_INVALID_BUNDLE, new String[] {});
        String hostName = getHostName();
        Long hostId = getHostId();
        String hostIP = getHostIP();
        logTrackingActivity(ActionEnumType.ACTION_INVALID_BUNDLE, IHasId.UNKNOWN_ID, IHasId.UNKNOWN_NAME, hostId, hostName, hostIP, null, "", CMRequestHandler.DUMMY_AGENT_FROM_STR, null, 3);
    }

    /**
     * Retrieve the host id of this host. 
     * @return the host id of this host.
     */
    private Long getHostId() {
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        IDSubjectManager subjectManager = (IDSubjectManager) manager.getComponent(IDSubjectManager.class.getName());
        ISubject host = subjectManager.getSubject(getHostName(), SubjectType.HOST);
        return (host != null) ? host.getId() : IDSubject.UNKNOWN_ID;
    }

    /**
     * unpacks the plugin data and processes each element with registered listener
     *
     * @param pluginData
     */
    private void updatePluginDataInfo(AgentPluginData pluginData) {
        AgentPluginDataElement[] elements = pluginData.getElement();

        if (elements != null) {
            for (AgentPluginDataElement element : elements) {
                heartbeatManager.processHeartbeatPluginData(element.getId(), element.get_value());
            }
        }
    }


    /**
     * transforms SharedFolderData data structure into a FolderTrie and stores
     * it to disk
     * 
     * @param sharedFolderData
     */
    private void updateSharedFolderInfo(SharedFolderData sharedFolderData) {
        if (sharedFolderData != null) {
            if (sharedFolderCookie == null || !sharedFolderData.getCookie().getTimestamp().equals(sharedFolderCookie.getTimestamp())) {
                boolean skipFolder = false;
                int index = hostName.indexOf('.');
                String host = index != -1 ? hostName.substring(0, index) : hostName;
                String serverPrefix = "\\\\" + host;
                serverPrefix = serverPrefix.toLowerCase();
                FolderTrie trie = new FolderTrie();
                SharedFolderAliasList sharedFolderAliasList = sharedFolderData.getAliasList();
                SharedFolderAliases[] sharedFolderAliasesArray = sharedFolderAliasList.getAliases();

                if (sharedFolderAliasesArray != null) {
                    for (int i = 0; i < sharedFolderAliasesArray.length; i++) {
                        skipFolder = true;
                        SharedFolderAliases sharedFolderAliases = sharedFolderAliasesArray[i];
                        SharedFolderAliasesAlias[] aliasArray = sharedFolderAliases.getAlias();
                        String[] folderList;
                        if (agentType == AgentTypeDTO.DESKTOP) {
                            // desktop does not get the physical location.
                            folderList = new String[aliasArray.length - 1];
                        } else {
                            folderList = new String[aliasArray.length];
                        }
                        int k = 0;
                        for (int j = 0; j < aliasArray.length; j++) {
                            String folderName = aliasArray[j].getName();
                            boolean isPhysicalAddress;
                            // [jzhang 101706] We don't need to distinguish it's a
                            // Linux or Windows physical address
                            // Instead, we only care whether it is a physical
                            // address or not for both
                            isPhysicalAddress = (folderName.charAt(1) == ':') || (folderName.startsWith("/") && (folderName.charAt(1) != '/'));

                            if (agentType == AgentTypeDTO.DESKTOP && !isPhysicalAddress) {
                                // skip c:\ type folder names for desktop agents.
                                // This
                                // is only for remote folders.
                                folderList[k++] = folderName;
                                skipFolder = false;
                            } else if (agentType == AgentTypeDTO.FILE_SERVER) {
                                if (!isPhysicalAddress && skipFolder && folderName.toLowerCase().startsWith(serverPrefix)) {
                                    // This folder is for some other machine.
                                    skipFolder = false;
                                }

                                if (isPhysicalAddress) {
                                    folderList[0] = folderName; // physical address
                                    // needs to be first
                                    // for FSA
                                } else {
                                    folderList[++k] = folderName;
                                }

                            }
                        }

                        if (skipFolder) {
                            continue;
                        }

                        for (int j = 0; j < folderList.length; j++) {
                            if (folderList[j] != null) {
                                trie.addString(folderList[j], folderList);
                            }
                        }
                    }
                }
                sharedFolderCookie = sharedFolderData.getCookie();
                setSharedFolderTrie(trie);

                ObjectOutputStream out;
                try {
                    out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(policyControllerRootDirectory + File.separator + ControlMngr.SHARED_FOLDER_FILE)));
                    // At some point, we agreed to not persist the
                    // sharedfoldercookie. Should we reconsider?
                    out.writeObject(sharedFolderTrie);
                    out.close();
                } catch (FileNotFoundException e1) {
                    getLog().error("Unable to write shared folder info file", e1);
                } catch (IOException e1) {
                    getLog().error("Unable to write shared folder info file", e1);
                }
            } else {
                // Got back the same timestamp we sent. No data changed
            }
        } else { // read from file.
            try {
                ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(policyControllerRootDirectory + File.separator + ControlMngr.SHARED_FOLDER_FILE)));
                setSharedFolderTrie((FolderTrie) in.readObject());
            } catch (FileNotFoundException e) {
                // This is not an error condition. No file was found.
                return;
            } catch (Exception e) {
                getLog().error("Unable to read shared folder file.", e);
            }
        }

    }

    private void updateCustomObligationsInfo(CustomObligationsData custOblData) {
        if (custOblData != null)
        {
            CustomObligation[] obligations = custOblData.getCustomObligation();

            if (obligations == null) {
                return;
            }

            for (CustomObligation obl : obligations) {
                customObligations.put(obl.getDisplayName(), obl.getRunAt(), obl.getRunBy(), obl.getInvocationString());
            }

            ObjectOutputStream out = null;
            try {
                out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(policyControllerRootDirectory + File.separator + ControlMngr.CUSTOM_OBLIGATIONS_FILE)));
                customObligations.writeExternal(out);
            } catch (FileNotFoundException e) {
                getLog().error("Unable to write custom obligation info file", e);
            } catch (IOException e) {
                getLog().error("Unable to write custom obligation info file", e);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        getLog().error("Unable to close custom obligation info file", e);
                    }
                }
            }
        } else {
            ObjectInputStream ois = null;
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(policyControllerRootDirectory + File.separator + ControlMngr.CUSTOM_OBLIGATIONS_FILE);
                ois = new ObjectInputStream(new BufferedInputStream(fis));
                customObligations.readExternal(ois);
            } catch (EOFException e) {
                // This is not an error condition.
            } catch (FileNotFoundException e) {
                // This is not an error condition.
            } catch (ClassNotFoundException e) {
                getLog().error("Unable to read custom obligation info file", e);
            } catch (IOException e) {
                getLog().error("Unable to read custom obligation info file", e);
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                        getLog().warn("Could not close " + ControlMngr.CUSTOM_OBLIGATIONS_FILE, e);
                    }
                } else if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        getLog().warn("Could not close " + ControlMngr.CUSTOM_OBLIGATIONS_FILE, e);
                    }
                }
            }
        }
    }

    public CustomObligationsContainer getCustomObligationsContainer() {
        return customObligations;
    }

    public void setCustomObligationsContainer(CustomObligationsContainer customObligations) {
        this.customObligations = customObligations;
    }

    /**
     * add user to list of logged in users
     * 
     * @param userSID
     */
    public void addLoggedInUser(String userSID) {
        if (getLog().isInfoEnabled()) {
            getLog().info("User logged on: " + userSID);
        }
    }

    /**
     * remove user from list of logged in users
     * 
     * @param userSID
     */
    public void removeLoggedInUser(String userSID) {
        if (getLog().isInfoEnabled()) {
            getLog().info("User logged off: " + userSID);
        }
    }

    /**
     * 
     * @return list of all logged in users and ISystemUser instances
     */
    private Set<ISystemUser> getLoggedInUsers() {
        String type = osWrapper.isLinux() ? UserSubjectTypeEnumType.LINUX_USER_SUBJECT_TYPE.getName() : UserSubjectTypeEnumType.WINDOWS_USER_SUBJECT_TYPE.getName();

        Set<ISystemUser> ret = new HashSet<ISystemUser>();
        String[] users = osWrapper.getLoggedInUsers();
        for (int i = 0; i < users.length; i++) {
            ISystemUser userToAdd = new SystemUserImpl(users[i], type);
            ret.add(userToAdd);
        }
        if (getLog().isInfoEnabled()) {
            getLog().info("Logged in users: " + ret);
        }
        return ret;
    }

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
     *            result of the policy
     * @param resource
     *            file name
     * @param message
     *            message to be sent to the user
     */
    public void notifyUser(String userId, String time, String action, String enforcement, String resource, String message) {
        ArrayList<String> messages = new ArrayList<String>();

        messages.add(message);

        notifyUser(userId, time, action, enforcement, resource, messages);
    }

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
     *            result of the policy
     * @param resource
     *            file name
     * @param messageList
     *            array of notification messages to be displayed to the user.
     */
    public void notifyUser(String userId, String time, String action, String enforcement, String resource, Collection messageList) {

        if (agentType == AgentTypeDTO.FILE_SERVER) {
            return;
        }

        ArrayList inputArgs = new ArrayList();
        inputArgs.add(time);

        // This is done to avoid the change in the IPC structure because all
        // the communication is depending on it, and it will not be a good
        // idea to mess with that.
        String result_action = action;

        if (enforcement.equals(EvaluationResult.DENY)) {
            result_action = "D" + action;
        } else if (enforcement.equals(EvaluationResult.ALLOW)) {
            result_action = "A" + action;
        }

        inputArgs.add(result_action);
        inputArgs.add(resource);
        inputArgs.addAll(messageList);

        invokeIPC(userId, SHOWNOTIFICATION, inputArgs, true);
    }

    public void executeCustomObligations(String userId, String commandLine) {
        ArrayList inputParams = new ArrayList();
        
        // Break command line into chunks
        int commandLineLen = commandLine.length();
        int startAt = 0;
        int endAt = IPCProxy.paramLen;
        while(true) {
            if (endAt >= commandLineLen) {
                endAt = commandLineLen;
            }

            inputParams.add(commandLine.substring(startAt, endAt));

            if (endAt == commandLineLen) {
                break;
            }

            startAt = endAt;
            endAt += IPCProxy.paramLen;
        }

        invokeIPC(userId, CUSTOMOBLIGATION, inputParams, false);
    }

    private boolean callFailed(ArrayList result) {
        return result != null && !((String)result.get(0)).equals("SUCCESS");
    }

    private void invokeIPC(String userId, String methodName, ArrayList inputArgs, boolean checkResult) {
        IIPCProxy notificationProxy = getUserNotificationProxy(userId);

        ArrayList result = null;
        if (checkResult) {
            result = new ArrayList();
        }

        if (!notificationProxy.invoke(methodName, inputArgs, result) || callFailed(result)) {
            getLog().trace("No response from Notifier. Uninit Proxy.");
            notificationProxy.uninit();
            synchronized (userNotificationProxyMap) {
                userNotificationProxyMap.remove(userId);
            }
            // Try getting a new proxy. Notifier may have died.
            notificationProxy = getUserNotificationProxy(userId);
            if (!notificationProxy.invoke(methodName, inputArgs, result) || callFailed(result)) {
                getLog().trace("No response from Notifier. Uninit Proxy.");
                notificationProxy.uninit();
                synchronized (userNotificationProxyMap) {
                    userNotificationProxyMap.remove(userId);
                }
            }
        }

    }
    /**
     * @param userId
     * @return
     */
    private IIPCProxy getUserNotificationProxy(String userId) {
        IIPCProxy notificationProxy;
        synchronized (userNotificationProxyMap) {
            notificationProxy = userNotificationProxyMap.get(userId);

            if (notificationProxy == null) {
                HashMapConfiguration config = new HashMapConfiguration();
                config.setProperty(IIPCProxy.REQUEST_HANDLER_CLASS_NAME, userId + NOTIFICATION_PROXY_SUFFIX);
                ComponentInfo info = new ComponentInfo(IPCProxy.class.getName(), IPCProxy.class.getName(), IIPCProxy.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
                IComponentManager cm = ComponentManagerFactory.getComponentManager();
                cm.registerComponent(info, true);
                notificationProxy = (IIPCProxy) cm.getComponent(info);
                userNotificationProxyMap.put(userId, notificationProxy);
            }
        }
        return notificationProxy;
    }

    /**
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo<IControlManager> getComponentInfo() {
        return COMP_INFO;
    }
    
    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return log;
    }

    /**
     * @see com.bluejungle.framework.comp.IDisposable
     */
    public void dispose() {
        uninit();
        ControlManagerStub.getInstance().stop();
    }

    public BigInteger getAgentId() {
        return agentId;
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#isTrackedAction(com.bluejungle.destiny.services.getLog().types.ActionType)
     * @param action
     * @return true if the passed in action should be tracked.
     */
    public boolean isTrackedAction(ActionEnumType action) {
        return trackedActions.contains(action);
    }

    public FolderTrie getSharedFolderTrie() {
        FolderTrie ret = null;
        if (sharedFolderTrie != null) {
            synchronized (sharedFolderTrie) {
                ret = sharedFolderTrie;
            }
        }
        return ret;
    }

    public void setSharedFolderTrie(FolderTrie sharedFolderTrie) {
        // make sure we dont need a separate object for locking
        if (this.sharedFolderTrie != null) {
            synchronized (this.sharedFolderTrie) {
                this.sharedFolderTrie = sharedFolderTrie;
            }
        } else {
            this.sharedFolderTrie = sharedFolderTrie;
        }
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getHostName()
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getHostIP()
     */
    public String getHostIP() {
        return hostIP;
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getAgentType()
     */
    public AgentTypeDTO getAgentType() {
        return agentType;
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getRegistrationId()
     */
    public BigInteger getRegistrationId() {
        return registrationId;
    }

    /**
     * Remember most recent heartbeat
     */
    private void updateLastHeartbeat() {
        if (lastHeartbeatSent != null) {
            synchronized (lastHeartbeatSent) {
                lastHeartbeatSent = Calendar.getInstance();
            }
        } else {
            lastHeartbeatSent = Calendar.getInstance();
        }
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getLastHeartbeat()
     */
    public Calendar getLastHeartbeat() {
        Calendar ret = null;

        if (lastHeartbeatSent != null) {
            synchronized(lastHeartbeatSent) {
                ret = lastHeartbeatSent;
            }
        }
        
        return ret;
    }

    private void updateLastSuccessfulHeartbeat(Calendar when) {
        heartbeatRecordManager.updateLastSuccessfulHeartbeat(when);
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getTimeSinceLastSuccessfulHeartbeat()
     */
    public long getTimeSinceLastSuccessfulHeartbeat() {
        return heartbeatRecordManager.getTimeSinceLastSuccessfulHeartbeat();
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getSecondsUntilNextHeartbeat()
     */
    public long getSecondsUntilNextHeartbeat() {
        Calendar lastHeartbeat = getLastHeartbeat();

        if (lastHeartbeat == null) {
            return 0;
        }

        // When trying to figure out the next heartbeat time, we aren't interested in the last
        // time we succeeded in heartbeating, just the last time we tried
        long delta = heartbeatRateInSeconds - (System.currentTimeMillis() - lastHeartbeat.getTimeInMillis())/1000;

        return (delta < 0) ? 0 : delta;
    }

    public void recordHeartbeatStateInformation() {
        heartbeatRecordManager.recordState();
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getLastBundle()
     */
    public Calendar getLastBundleUpdate() {
        DeploymentRequest request = policyContainer.getDeploymentRequestForSubjectTypes(getRegisteredUserSubjectTypes(), getHostName());
        return request.getTimestamp();
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getEnabledPlugins()
     */
    public Map<String, Calendar> getEnabledPlugins() {
        Map<String, Calendar> cannedData = new HashMap<String, Calendar>();
        return cannedData;
    }
    
    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getDisabledPlugins()
     */
    public List<String> getDisabledPlugins() {
        List<String> cannedData = new ArrayList<String>();
        
        return cannedData;
    }
    /**
     * 
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#registerUserSubjectTypes(java.util.Set)
     */
    public void registerUserSubjectTypes(Set<UserSubjectTypeEnumType> userSubjectsToRegister) {
        if (userSubjectsToRegister == null) {
            throw new NullPointerException("userSubjectsToRegister cannot be null.");
        }

        registeredUserSubjectTypes.addAll(userSubjectsToRegister);
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getRegisteredUserSubjectTypes()
     */
    public Set<UserSubjectTypeEnumType> getRegisteredUserSubjectTypes() {
        return registeredUserSubjectTypes;
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#closeProcessToken()
     */
    public void closeProcessToken(Long processToken) {
        if (processToken == null) {
            return;
        }

        osWrapper.closeProcessToken(processToken);
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getResolvedDNSCacheTimeout()
     */
    public int getResolvedDNSCacheTimeout() {
        return resolvedDNSCacheTimeout;
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getTrustedProcessManager()
     */
    public ITrustedProcessManager getTrustedProcessManager() {
        return trustedProcessManager;
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getTrustedProcessTTL()
     */
    public int getTrustedProcessTTL() {
        return trustedProcessTimeout;
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#getSDKCacheHintValue()
     */
    public int getSDKCacheHintValue() {
        return sdkCacheHintValue;
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IControlManager#resolveHostInformation()
     */
    public boolean resolveHostInformation()
    {
        return resolveHostInfo;
    }

    private class SystemUserImpl implements ISystemUser {

        private String systemId;
        private String userSubjectType;

        /**
         * Create an instance of SystemUserImpl
         * 
         * @param systemId
         * @param userSubjectType
         */
        public SystemUserImpl(String systemId, String userSubjectType) {
            if (systemId == null) {
                throw new NullPointerException("systemId cannot be null.");
            }

            if (userSubjectType == null) {
                throw new NullPointerException("userSubjectType cannot be null.");
            }

            this.systemId = systemId;
            this.userSubjectType = userSubjectType;
        }

        /**
         * @see com.bluejungle.destiny.agent.controlmanager.ISystemUser#getSystemId()
         */
        public String getSystemId() {
            return systemId;
        }

        /**
         * @see com.bluejungle.destiny.agent.controlmanager.ISystemUser#getUserSubjectTypeId()
         */
        public String getUserSubjectTypeId() {
            return userSubjectType;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            boolean valueToReturn = false;
            if ((obj != null) && (obj instanceof SystemUserImpl)) {
                SystemUserImpl userToTest = (SystemUserImpl) obj;
                valueToReturn = userToTest.getSystemId().equals(getSystemId());
                valueToReturn &= userToTest.getUserSubjectTypeId().equals(getUserSubjectTypeId());
            }

            return valueToReturn;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return getSystemId().hashCode() + getUserSubjectTypeId().hashCode();
        }
    }
}
