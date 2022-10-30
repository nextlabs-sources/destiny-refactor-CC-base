/*
 * Created on Nov 12, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.AxisEngine;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.dcc.DCCResourceLocators;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentDO;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentHeartbeatData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentPolicyAssemblyStatusData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentProfileStatusData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentRegistrationData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentShutdownData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStartupConfiguration;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStartupData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentUpdateAcknowledgementData;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentUpdates;
import com.bluejungle.destiny.container.shared.agentmgr.InvalidIDException;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;
import com.bluejungle.destiny.container.shared.agentmgr.service.AgentServiceHelper;
import com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationRelay;
import com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationSource;
import com.bluejungle.destiny.framework.types.CommitFault;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.destiny.framework.types.UnknownEntryFault;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderData;
import com.bluejungle.destiny.server.shared.registration.impl.SharedFolderCookieImpl;
import com.bluejungle.destiny.services.agent.AgentServiceIF;
import com.bluejungle.destiny.services.agent.types.AgentHeartbeatData;
import com.bluejungle.destiny.services.agent.types.AgentPluginData;
import com.bluejungle.destiny.services.agent.types.AgentPluginDataElement;
import com.bluejungle.destiny.services.agent.types.AgentProfileStatusData;
import com.bluejungle.destiny.services.agent.types.AgentRegistrationData;
import com.bluejungle.destiny.services.agent.types.AgentShutdownData;
import com.bluejungle.destiny.services.agent.types.AgentStartupConfiguration;
import com.bluejungle.destiny.services.agent.types.AgentStartupData;
import com.bluejungle.destiny.services.agent.types.AgentUpdateAcknowledgementData;
import com.bluejungle.destiny.services.agent.types.AgentUpdates;
import com.bluejungle.destiny.services.agent.types.UserNotification;
import com.bluejungle.destiny.services.agent.types.UserNotificationBag;
import com.bluejungle.destiny.services.policy.types.DeploymentRequest;
import com.bluejungle.destiny.services.policy.types.SystemUser;
import com.bluejungle.destiny.types.shared_folder.SharedFolderData;
import com.bluejungle.destiny.types.shared_folder.SharedFolderDataCookie;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.framework.crypt.IEncryptor;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.bluejungle.framework.environment.IResourceLocator;
import com.bluejungle.framework.heartbeat.IServerHeartbeatManager;
import com.bluejungle.framework.security.IKeyManager;
import com.bluejungle.framework.utils.IMailHelper;
import com.bluejungle.framework.utils.SerializationUtils;
import com.bluejungle.pf.destiny.lib.DTOUtils;
import com.bluejungle.pf.destiny.lib.axis.IPolicyDeployment;
import com.bluejungle.pf.domain.destiny.deployment.DeploymentBundleSignatureEnvelope;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle;
import com.bluejungle.version.IVersion;
import com.bluejungle.version.types.Version;
import com.bluejungle.versionutil.VersionUtil;

/**
 * This class implements the DABS Agent Web Service
 * 
 * @author ihanen
 */
public class DABSAgentServiceImpl implements AgentServiceIF {

    private AgentServiceHelper agentMgrSvcHelper = AgentServiceHelper.getInstance();
    private ReversibleEncryptor encryptor = new ReversibleEncryptor();
    private static final String AGENT_KEYSTORE_FILE_NAME = "agent-keystore.jks";
    private static final String AGENT_TRUSTSTORE_FILE_NAME = "agent-truststore.jks";
    private static final String SERVER_PRIVATE_KEY_ALIAS = "DCC";

    /**
     * Log object
     */
    private static final Log LOG = LogFactory.getLog(DABSAgentServiceImpl.class.getName());

    /**
     * Constructor
     */
    public DABSAgentServiceImpl() {
        super();
    }

    /**
     * Registers a new agent with DCC
     * 
     * @param regInfo
     *            agent registration info
     * @return registration status
     * @throws RemoteException
     *             if agent registration fails
     */
    public AgentStartupConfiguration registerAgent(AgentRegistrationData registrationData) throws RemoteException {
        // Convert input into appropriate interface object:
        AgentRegistrationDataImpl data = new AgentRegistrationDataImpl(registrationData);
        IAgentManager agentMgr = this.getAgentManager();
        IAgentStartupConfiguration startupConfig = null;

        try {
            startupConfig = agentMgr.registerAgent(data);
        } catch (PersistenceException e) {
            // FIX ME - Should throw a checked persistence exception
            throw new RemoteException("Registration failed.", e);
        }

        // Do post-processing - DTO and keystore attachment creation:
        AgentStartupConfiguration configuration = null;
        if (startupConfig != null) {
            configuration = this.agentMgrSvcHelper.extractStartupConfigurationData(startupConfig);

            // Attach the registered agent's keystore:
            attachSecurityFile("AgentKeyStore", AGENT_KEYSTORE_FILE_NAME);

            // Attach the registered agent's keystore:
            attachSecurityFile("AgentTrustStore", AGENT_TRUSTSTORE_FILE_NAME);

            String password = System.getProperty("nextlabs.javax.net.ssl.keyStorePassword");
            password = "byjqi" + password;
            
            IVersion agentVersion = VersionUtil.convertWSVersionToIVersion(registrationData.getVersion());

            if (agentVersion.getMajor() >= 5 && (agentVersion.getMajor() >= 6 && agentVersion.getMinor() >= 0)) {
                attachString("extra", encryptor.encrypt(password));
            } else {
                // using old logic.
                attachString("extra", encryptor.encrypt(password, "n").substring(1));
            }
        }

        return configuration;
    }

    /**
     * @see com.bluejungle.destiny.services.agent.AgentServiceIF#unregisterAgent(java.math.BigInteger)
     */
    public void unregisterAgent(BigInteger agentId) throws RemoteException, ServiceNotReadyFault, UnauthorizedCallerFault {
        IAgentManager agentManager = getAgentManager();
        try {
            agentManager.unregisterAgent(new Long(agentId.longValue()));
        } catch (PersistenceException exception) {
            getLog().error("Failed to unregister agent with id, " + agentId, exception);
            throw new CommitFault();
        } catch (InvalidIDException exception) {
            getLog().error("Failed to unregister agent with id, " + agentId + ".  No agent with this id found.", exception);
            throw new UnknownEntryFault();
        }
    }

    /**
     * Attach a security files onto the response message only if we are on a
     * secure connection.
     */
    protected void attachSecurityFile(String attachmentId, String fileName) {
        MessageContext messageCtx = AxisEngine.getCurrentMessageContext();
        if (isRequestSecure(messageCtx)) {
            IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
            IResourceLocator securityFileLocator = (IResourceLocator) compMgr.getComponent(DCCResourceLocators.SECURITY_RESOURCE_LOCATOR);
            Message msg = messageCtx.getResponseMessage();
            msg.getAttachmentsImpl().setSendType(org.apache.axis.attachments.Attachments.SEND_TYPE_DIME);

            // Attach the registered agent's keystore:
            InputStream fileStream = securityFileLocator.getResourceAsStream(fileName);
            AttachmentPart fileAttachment = new AttachmentPart();
            fileAttachment.setContent(fileStream, "application/octet-stream");
            fileAttachment.setContentId(attachmentId);
            msg.addAttachmentPart(fileAttachment);
        }
    }

    /**
     * Attach a string to the response message.
     * 
     * @param attachmentId
     *            id of the attachment
     * @param stringToAttach
     *            string to attach
     */
    protected void attachString(String attachmentId, String stringToAttach) {
        MessageContext messageCtx = AxisEngine.getCurrentMessageContext();
        if (isRequestSecure(messageCtx)) {
            Message msg = messageCtx.getResponseMessage();
            msg.getAttachmentsImpl().setSendType(org.apache.axis.attachments.Attachments.SEND_TYPE_DIME);
            InputStream stringStream = new ByteArrayInputStream(stringToAttach.getBytes());
            AttachmentPart fileAttachment = new AttachmentPart();
            fileAttachment.setContent(stringStream, "application/octet-stream");
            fileAttachment.setContentId(attachmentId);
            msg.addAttachmentPart(fileAttachment);
        }
    }

    /**
     * This API is called regularly by all agents to query for updates and
     * notify that they are still online. The response to this call contains
     * notifications about changes that the agent needs to be aware of.
     * 
     * @param heartBeat
     *            heartBeat request (contains the state of the agent)
     * @return a heartbeat response structure
     * @throws RemoteException
     *             if the heartbeat handling fails.
     */
    public AgentUpdates checkUpdates(BigInteger id, AgentHeartbeatData heartbeat) throws RemoteException {
        IAgentManager agentMgr = this.getAgentManager();
        IPolicyDeployment deploymentBundleCreator = this.getDeploymentBundleCreator();

        AgentHeartbeatDataImpl heartbeatData = new AgentHeartbeatDataImpl(heartbeat);
        AgentUpdates updatesData = null;
        try {
            Long agentId = new Long(id.longValue());
            IAgentUpdates updates = agentMgr.checkUpdates(agentId, heartbeatData);
            updatesData = this.agentMgrSvcHelper.extractUpdatesData(updates);
            // Now obtain the policy assembly status updates, if the status data
            // exists:
            IDeploymentBundle bundle = null;
            IAgentDO agent = agentMgr.getAgentById(agentId);

            if (heartbeat.getPolicyAssemblyStatus() != null) {
                String hostName = agent.getHost();
                String agentDomain = null;
                String[] nameAndDomain = hostName.split("[.]", 2);
                if (nameAndDomain.length == 2) {
                    agentDomain = nameAndDomain[1];
                }
                DeploymentRequest req = heartbeat.getPolicyAssemblyStatus();
                bundle = deploymentBundleCreator.getDeploymentBundle(req, agentId, agentDomain, agent.getVersion());
                if (!bundle.isEmpty()) {
                    // FIX ME - Do this once
                    IKeyManager keyManager = getKeyManager();
                    PrivateKey privateKey = keyManager.getPrivateKey(SERVER_PRIVATE_KEY_ALIAS);
                    try {
                        String[] uids;
                        if (req != null) {
                            SystemUser[] users = req.getPolicyUsers();
                            List<String> uidList = new ArrayList<String>();
                            if (users != null) {
                                for (SystemUser user : users) {
                                    if (user != null) {
                                        String uid = user.getSystemId();
                                        if (uid != null) {
                                            uidList.add(uid);
                                        }
                                    }
                                }
                                uids = uidList.toArray(new String[uidList.size()]);
                            } else {
                                uids = new String[0];
                            }
                        } else {
                            uids = new String[0];
                        }
                        DeploymentBundleSignatureEnvelope deploymentBundleEnvelope = new DeploymentBundleSignatureEnvelope(bundle, uids, privateKey);

                        String bundleStr = (bundle == null) ? null : DTOUtils.encodeDeploymentBundle(deploymentBundleEnvelope);
                        updatesData.setPolicyDeploymentBundle(bundleStr);
                    } catch (InvalidKeyException exception) {
                        LOG.error("Failed to deliver bundle", exception);
                    } catch (IOException exception) {
                        LOG.error("Failed to deliver bundle", exception);
                    } catch (SignatureException exception) {
                        LOG.error("Failed to deliver bundle", exception);
                    }
                }
            }

            // Also piggyback the shared folder updates, if any:
            ISharedFolderInformationSource sharedFolderInfoSource = getSharedFolderInformationSource();
            SharedFolderDataCookie wsFolderCookie = heartbeat.getSharedFolderDataCookie();
            Calendar cookieCal = null;
            if (wsFolderCookie != null) {
                cookieCal = wsFolderCookie.getTimestamp();
            }
            ISharedFolderCookie sharedFolderCookie = new SharedFolderCookieImpl(cookieCal);
            ISharedFolderData sharedFolderData = sharedFolderInfoSource.getSharedFolderInformationUpdateSince(sharedFolderCookie);
            SharedFolderData sharedFolderDataUpdate = SharedFolderWebServiceHelper.convertFromSharedFolderData(sharedFolderData);
            updatesData.setSharedFolderData(sharedFolderDataUpdate);

            IVersion agentVersion = agent.getVersion();

            // Yeah, there should be a better way to do this.
            if (agentVersion.getMajor() > 2 || (agentVersion.getMajor() == 2 && agentVersion.getMinor() >= 5)) {
                // Custom obligations are supported in versions > 2.5
                IDestinyConfigurationStore confStore = (IDestinyConfigurationStore) ComponentManagerFactory.getComponentManager().getComponent(DestinyConfigurationStoreImpl.COMP_INFO);
                updatesData.setCustomObligationsData(CustomObligationsWebServiceHelper.convertFromCustomObligations(confStore.retrieveCustomObligationsConfig()));
                
            }

            // 3.2 and greater agents use the Heartbeat Plugin structure
            if (agentVersion.getMajor() > 3 || (agentVersion.getMajor() == 3 && agentVersion.getMinor() >= 2)) {
                IServerHeartbeatManager heartbeatManager = getHeartbeatManager();
                AgentPluginDataElement[] heartbeatPluginDataElements = new AgentPluginDataElement[0];
                if (heartbeat.getPluginData() != null && heartbeat.getPluginData().getElement() != null) {
                    heartbeatPluginDataElements = heartbeat.getPluginData().getElement();
                }

                AgentPluginDataElement[] responseData = new AgentPluginDataElement[heartbeatPluginDataElements.length];

                int i = 0;
                // Provide response for each entry in the heartbeat
                for (AgentPluginDataElement e : heartbeatPluginDataElements) {
                    Serializable response = heartbeatManager.processHeartbeatPluginData(e.getId(), e.get_value());
                    AgentPluginDataElement responseElement = new AgentPluginDataElement(SerializationUtils.wrapSerializable(response));
                    responseElement.setId(e.getId());
                    responseData[i++] = responseElement;
                }

                updatesData.setPluginData(new AgentPluginData(responseData));
            }
        } catch (PersistenceException agentManagerException) {
            // TODO: Throw a service-exception here
            LOG.error("Failed to checkUpdates for agent (" + id.longValue() + ")", agentManagerException);
        } catch (InvalidIDException e) {
            throw new RuntimeException(e);
        }

        return updatesData;
    }

    /**
     * This API is called immediately after a call to checkUpdates if
     * checkUpdates has returned any updates and after the updates have been
     * committed on the agent. Acknowledgement of updated data is used to
     * reflect accurate status data on the management console. It is expected
     * that this data will be sent again on the next heartbeat to the agent and
     * that a call to acnowledgeUpdates ONLY serves as a quicker way to reflect
     * current status than a call to checkUpdates.
     * 
     * @param id
     * @param acknowledgementData
     * @throws RemoteException
     */
    public void acknowledgeUpdates(BigInteger id, AgentUpdateAcknowledgementData acknowledgementData) throws RemoteException {
        AgentUpdateAcknowledgementDataImpl acknowledgement = new AgentUpdateAcknowledgementDataImpl(acknowledgementData);
        try {
            this.getAgentManager().acknowledgeUpdates(new Long(id.longValue()), acknowledgement);
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        } catch (InvalidIDException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if the current request is secure, false otherwise
     * 
     * @return true if the current request is secure, false otherwise
     */
    protected boolean isRequestSecure(MessageContext msgContext) {
        HttpServletRequest req = (HttpServletRequest) msgContext.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        return req.isSecure();
    }

    /**
     * This API is called when the agent comes on line (when its host starts up)
     * The agent sends some information about its state to DCC.
     * 
     * @param agentId
     *            id of the agent
     * @param startupInfo
     *            startup information
     * @return a confirmation message
     * @throws RemoteException
     *             if the startup request fails
     */
    public void startupAgent(BigInteger id, AgentStartupData startupData) throws RemoteException {
        AgentStartupDataImpl startup = new AgentStartupDataImpl(startupData);
        try {
            this.getAgentManager().startupAgent(new Long(id.longValue()), startup);
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        } catch (InvalidIDException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This API is called when the agent goes offline (when its host shuts down)
     * 
     * @param agentId
     *            id of the agent
     * @return a confirmation message
     * @throws RemoteException
     *             if the shutdown request fails.
     */
    public void shutdownAgent(BigInteger id, AgentShutdownData shutdownData) throws RemoteException {
        AgentShutdownDataImpl shutdown = new AgentShutdownDataImpl(shutdownData);
        try {
            this.getAgentManager().shutdownAgent(new Long(id.longValue()), shutdown);
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        } catch (InvalidIDException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see com.bluejungle.destiny.services.agent.AgentServiceIF#sendUserNotifications(com.bluejungle.destiny.services.agent.types.UserNotificationBag)
     */
    public void sendUserNotifications(UserNotificationBag notifications) throws RemoteException, ServiceNotReadyFault, UnauthorizedCallerFault {
        // TODO: make this asynchronous with a DB-based queue
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        if (compMgr.isComponentRegistered(IMailHelper.COMP_NAME)) {
            IMailHelper mh = (IMailHelper) compMgr.getComponent(IMailHelper.COMP_NAME);
            UserNotification[] notifs = notifications.getNotifications();
            for (int i = 0; i < notifs.length; i++) {
                UserNotification notification = notifs[i];
                try {
                    mh.sendMessage(notification.getFrom(), notification.getTo(), notification.getSubject(), notification.getBody());
                    // catch all exceptions because we don't want 1 bad message
                    // to affect the rest
                } catch (Exception e) {
                    Log log = getLog();
                    if (log.isWarnEnabled()) {
                        String msgInfo = "from: " + notification.getFrom() + " to: " + notification.getTo() + "subject: " + notification.getSubject() + " body: " + notification.getBody();
                        log.warn("Unable to send notification: " + msgInfo, e);
                    }
                }
            }
        } else {
            getLog().warn("Email notification was ignored because the mail server settings are not configured");
        }
    }

    /**
     * Returns an instance of the IAgentManager implementation class
     * (AgentManager) after obtaining it from the component manager.
     * 
     * @return IAgentManager
     */
    protected IAgentManager getAgentManager() throws ServiceNotReadyFault {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        if (!compMgr.isComponentRegistered(IAgentManager.COMP_NAME)) {
            throw new ServiceNotReadyFault();
        }

        return (IAgentManager) compMgr.getComponent(IAgentManager.COMP_NAME);
    }

    /**
     * Retrieve the Key Manager {@link IKeyManager}
     * 
     * @return the Key Manager
     */
    private IKeyManager getKeyManager() throws ServiceNotReadyFault {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        if (!compMgr.isComponentRegistered(IKeyManager.COMPONENT_NAME)) {
            throw new ServiceNotReadyFault();
        }

        return (IKeyManager) compMgr.getComponent(IKeyManager.COMPONENT_NAME);
    }

    /**
     * Returns an instance of the IPolicyDeployment implementation class (for
     * policy deployment bundle creation) after obtaining it from the component
     * manager.
     * 
     * @return IAgentManager
     */
    protected IPolicyDeployment getDeploymentBundleCreator() throws ServiceNotReadyFault {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        if (!compMgr.isComponentRegistered(IPolicyDeployment.COMP_NAME)) {
            throw new ServiceNotReadyFault();
        }

        return (IPolicyDeployment) compMgr.getComponent(IPolicyDeployment.COMP_NAME);
    }

    /**
     * Returns the heartbeat manager (@link IServerHeartbeatManager}
     *
     */
    private IServerHeartbeatManager getHeartbeatManager() throws ServiceNotReadyFault {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        if (!compMgr.isComponentRegistered(IServerHeartbeatManager.COMP_NAME)) {
            throw new ServiceNotReadyFault();
        }

        return (IServerHeartbeatManager) compMgr.getComponent(IServerHeartbeatManager.COMP_NAME);
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    protected Log getLog() {
        return LOG;
    }

    /**
     * Returns an instance of the ISharedFolderInformationSource implementation
     * class after obtaining it from the component manager.
     * 
     * @return ISharedFolderInformationSource
     */
    protected ISharedFolderInformationSource getSharedFolderInformationSource() throws ServiceNotReadyFault {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        if (!compMgr.isComponentRegistered(ISharedFolderInformationRelay.COMP_NAME)) {
            throw new ServiceNotReadyFault();
        }

        return (ISharedFolderInformationSource) compMgr.getComponent(ISharedFolderInformationRelay.COMP_NAME);
    }

    /**
     * Wrapper class to create registration data from the registration DTO
     * 
     * @author safdar
     */
    private class AgentRegistrationDataImpl implements IAgentRegistrationData {

        /*
         * Delegate data
         */
        private AgentRegistrationData registrationData;
        private IAgentType agentType;
        
        /**
         * 
         * Constructor
         * 
         * @param registrationData
         */
        public AgentRegistrationDataImpl(AgentRegistrationData registrationData) throws ServiceNotReadyFault {
            this.registrationData = registrationData;
            this.agentType = getAgentManager().getAgentType(this.registrationData.getType().getValue());
        }

        /**
         * 
         * @see com.bluejungle.destiny.container.dms.components.agentmgr.IAgentRegistrationData#getHost()
         */
        public String getHost() {
            return this.registrationData.getHost();
        }

        /**
         * 
         * @see com.bluejungle.destiny.container.dms.components.agentmgr.IAgentRegistrationData#getType()
         */
        public IAgentType getType() {
            return this.agentType;
        }

        /**
         * 
         * @see com.bluejungle.destiny.container.dms.components.agentmgr.IAgentRegistrationData#getVersion()
         */
        public IVersion getVersion() {
            return VersionUtil.convertWSVersionToIVersion(this.registrationData.getVersion());
        }
    }

    /**
     * Wrapper class to create startup data from the startup DTO
     * 
     * @author safdar
     */
    private class AgentStartupDataImpl implements IAgentStartupData {

        /*
         * Delegate data
         */
        private AgentStartupData startupData;

        /**
         * 
         * Constructor
         * 
         * @param startupData
         */
        public AgentStartupDataImpl(AgentStartupData startupData) {
            this.startupData = startupData;
        }

        /**
         * 
         * @see com.bluejungle.destiny.container.dms.components.agentmgr.IAgentStartupData#getPushPort()
         */
        public Integer getPushPort() {
            Integer result = null;
            if (startupData.getPushPort() != null) {
                result = new Integer(startupData.getPushPort().intValue());
            }
            return result;
        }
    }

    /**
     * Wrapper class to create heartbeat data from the heartbeat DTO
     * 
     * @author safdar
     */
    private class AgentHeartbeatDataImpl implements IAgentHeartbeatData {

        /*
         * Delegate data
         */
        private AgentHeartbeatData heartbeatData;

        /**
         * 
         * Constructor
         * 
         * @param data
         */
        public AgentHeartbeatDataImpl(AgentHeartbeatData data) {
            this.heartbeatData = data;
        }

        /**
         * 
         * @see com.bluejungle.destiny.container.dms.components.agentmgr.IAgentHeartbeatData#getProfileStatus()
         */
        public IAgentProfileStatusData getProfileStatus() {
            if (this.heartbeatData.getProfileStatus() != null) {
                return new AgentProfileStatusDataImpl(this.heartbeatData.getProfileStatus());
            } else
                return null;
        }

        /**
         * 
         * @see com.bluejungle.destiny.container.dms.components.agentmgr.IAgentHeartbeatData#getPolicyAssemblyStatus()
         */
        public IAgentPolicyAssemblyStatusData getPolicyAssemblyStatus() {
            if (this.heartbeatData.getPolicyAssemblyStatus() != null) {
                return new AgentPolicyAssemblyStatusDataImpl(this.heartbeatData.getPolicyAssemblyStatus());
            } else
                return null;
        }
    }

    /**
     * Wrapper class to create update acknowldgement data from the update
     * acknowledgement DTO
     * 
     * @author safdar
     */
    private class AgentUpdateAcknowledgementDataImpl implements IAgentUpdateAcknowledgementData {

        /*
         * Delegate data
         */
        private AgentUpdateAcknowledgementData acknowledgementData;

        /**
         * 
         * Constructor
         * 
         * @param data
         */
        public AgentUpdateAcknowledgementDataImpl(AgentUpdateAcknowledgementData data) {
            this.acknowledgementData = data;
        }

        /**
         * 
         * @see com.bluejungle.destiny.container.dms.components.agentmgr.IAgentUpdateAcknowledgementData#getProfileStatus()
         */
        public IAgentProfileStatusData getProfileStatus() {
            if (this.acknowledgementData.getProfileStatus() != null) {
                return new AgentProfileStatusDataImpl(this.acknowledgementData.getProfileStatus());
            } else
                return null;
        }

        /**
         * 
         * @see com.bluejungle.destiny.container.dms.components.agentmgr.IAgentAcknowledgementData#getPolicyAssemblyStatus()
         */
        public IAgentPolicyAssemblyStatusData getPolicyAssemblyStatus() {
            if (this.acknowledgementData.getPolicyAssemblyStatus() != null) {
                return new AgentPolicyAssemblyStatusDataImpl(this.acknowledgementData.getPolicyAssemblyStatus());
            } else
                return null;
        }
    }

    /**
     * Wrapper class to create profile status data from the profile status DTO
     * 
     * @author safdar
     */
    private class AgentProfileStatusDataImpl implements IAgentProfileStatusData {

        /*
         * Delegate data
         */
        private AgentProfileStatusData profileStatusData;

        /**
         * 
         * Constructor
         * 
         * @param data
         */
        public AgentProfileStatusDataImpl(AgentProfileStatusData data) {
            this.profileStatusData = data;
        }

        /**
         * 
         * @see com.bluejungle.destiny.container.dms.components.agentmgr.IAgentProfileStatusData#getLastCommittedAgentProfileName()
         */
        public String getLastCommittedAgentProfileName() {
            return this.profileStatusData.getLastCommittedAgentProfileName();
        }

        /**
         * 
         * @see com.bluejungle.destiny.container.dms.components.agentmgr.IAgentProfileStatusData#getLastCommittedAgentProfileTimestamp()
         */
        public Calendar getLastCommittedAgentProfileTimestamp() {
            return this.profileStatusData.getLastCommittedAgentProfileTimestamp();
        }

        /**
         * 
         * @see com.bluejungle.destiny.container.dms.components.agentmgr.IAgentProfileStatusData#getLastCommittedCommProfileName()
         */
        public String getLastCommittedCommProfileName() {
            return this.profileStatusData.getLastCommittedCommProfileName();
        }

        /**
         * 
         * @see com.bluejungle.destiny.container.dms.components.agentmgr.IAgentProfileStatusData#getLastCommittedCommProfileTimestamp()
         */
        public Calendar getLastCommittedCommProfileTimestamp() {
            return this.profileStatusData.getLastCommittedCommProfileTimestamp();
        }
    }

    /**
     * Wrapper class to create policy assembly status data from the policy
     * assembly status DTO
     * 
     * @author safdar
     */
    private class AgentPolicyAssemblyStatusDataImpl implements IAgentPolicyAssemblyStatusData {

        /*
         * Delegate data
         */
        private DeploymentRequest policyAssemblyStatus;

        /**
         * 
         * Constructor
         * 
         * @param data
         */
        public AgentPolicyAssemblyStatusDataImpl(DeploymentRequest data) {
            this.policyAssemblyStatus = data;
        }

        /**
         * 
         * @see com.bluejungle.destiny.container.dms.components.agentmgr.IAgentPolicyAssemblyStatusData#getLastCommittedDeploymentBundleTimestamp()
         */
        public Calendar getLastCommittedDeploymentBundleTimestamp() {
            return this.policyAssemblyStatus.getTimestamp();
        }
    }

    /**
     * Wrapper class to create shutdown data from the shutdown DTO.
     * 
     * @author safdar
     */
    private class AgentShutdownDataImpl implements IAgentShutdownData {

        /*
         * Delegate data
         */
        private AgentShutdownData shutdownData;

        /**
         * 
         * Constructor
         * 
         * @param data
         */
        public AgentShutdownDataImpl(AgentShutdownData data) {
            this.shutdownData = data;
        }
    }

}
