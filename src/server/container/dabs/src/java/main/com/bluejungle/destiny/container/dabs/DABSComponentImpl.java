/*
 * Created on Nov 12, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.Address;

import net.sf.hibernate.Session;

import com.bluejungle.destiny.appframework.appsecurity.axis.ISecureSessionVault;
import com.bluejungle.destiny.appframework.appsecurity.axis.SecureSessionVaultGateway;
import com.bluejungle.destiny.container.dabs.components.deployment.IPolicyDeploymentManager;
import com.bluejungle.destiny.container.dabs.components.deployment.PolicyDeploymentManagerImpl;
import com.bluejungle.destiny.container.dabs.components.log.ILogWriter;
import com.bluejungle.destiny.container.dabs.components.log.hibernateimpl.HibernateLogWriter;
import com.bluejungle.destiny.container.dabs.components.sharedfolder.defaultimpl.DABSSharedFolderInformationRelayImpl;
import com.bluejungle.destiny.container.dcc.BaseDCCComponentImpl;
import com.bluejungle.destiny.container.dcc.DCCResourceLocators;
import com.bluejungle.destiny.container.dcc.IDCCContainer;
import com.bluejungle.destiny.container.dcc.INamedResourceLocator;
import com.bluejungle.destiny.container.dcc.ServerRelativeFolders;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentManager;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.HibernateProfileManager;
import com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationRelay;
import com.bluejungle.destiny.server.shared.configuration.IDABSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IFileSystemLogConfigurationDO;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.destiny.types.secure_session.v1.SecureSession;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryEventManager;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.heartbeat.IServerHeartbeatManager;
import com.bluejungle.framework.heartbeat.ServerHeartbeatManagerImpl;
import com.bluejungle.framework.utils.IMailHelper;
import com.bluejungle.framework.utils.MailHelper;
import com.bluejungle.pf.destiny.lib.ClientInformationRequestDTO;
import com.bluejungle.pf.destiny.lib.KeyRequestDTO;
import com.bluejungle.pf.destiny.lib.RegularExpressionRequestDTO;
import com.bluejungle.pf.destiny.lib.axis.IPolicyDeployment;
import com.bluejungle.pf.destiny.lib.axis.PolicyDeploymentImpl;
import com.bluejungle.pf.destiny.lib.client.ClientInformationProvider;
import com.nextlabs.destiny.container.dabs.components.log.ILogQueueMgr;
import com.nextlabs.destiny.container.dabs.components.log.IThreadPool;
import com.nextlabs.destiny.container.dabs.components.log.filesystemimpl.FileSystemBufferLogWriter;
import com.nextlabs.destiny.container.dabs.components.log.filesystemimpl.FileSystemLogQueueMgr;
import com.nextlabs.destiny.container.dabs.components.log.filesystemimpl.FileSystemThreadPool;
import com.nextlabs.destiny.container.dabs.components.log.filesystemimpl.LogInsertTaskFactory;
import com.nextlabs.framework.messaging.IMessageHandler;
import com.nextlabs.framework.messaging.IMessageHandlerManager;
import com.nextlabs.framework.messaging.handlers.EmailMessageHandler;
import com.nextlabs.framework.messaging.impl.IEmailMessageHandlerConfig;
import com.nextlabs.framework.messaging.impl.MessageHandlerManagerImpl;

/**
 * This is the DABS component implementation class
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dabs/com/bluejungle/destiny/container/dabs/DABSComponentImpl.java#5 $
 */
public class DABSComponentImpl extends BaseDCCComponentImpl {
    private static PropertyKey<String> DABS_LOG_QUEUE_FOLDER_LOCATION = new PropertyKey<String>("LogQueueFolder");
    
    /**
     * DABS container initialization
     */
    public void init() {
        setComponentType(ServerComponentType.DABS);
        super.init();

        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();

        // Ensure that the required data source(s) exist(s). These should have
        // been initialized by the BaseDCCComponentImpl based on the
        // configuration file for this component.
        IHibernateRepository mgmtDataSrc = (IHibernateRepository) compMgr.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());
        if (mgmtDataSrc == null) {
            throw new RuntimeException("Data source " + DestinyRepository.MANAGEMENT_REPOSITORY + " is not correctly setup for the DABS component.");
        }
        IHibernateRepository activityDataSrc = (IHibernateRepository) compMgr.getComponent(DestinyRepository.ACTIVITY_REPOSITORY.getName());
        if (activityDataSrc == null) {
            throw new RuntimeException("Data source " + DestinyRepository.ACTIVITY_REPOSITORY + " is not correctly setup for the DABS component.");
        }

        final IDestinyConfigurationStore configMgr = getManager().getComponent(DestinyConfigurationStoreImpl.COMP_INFO);

        // Initialize dictionary
        Dictionary dictionary = compMgr.getComponent(Dictionary.COMP_INFO);

        // Policy Bundle initialization:
        ComponentInfo<PolicyDeploymentImpl> policyDeploymentImplCompInfo = 
        	new ComponentInfo<PolicyDeploymentImpl>(
        		IPolicyDeployment.COMP_NAME, 
        		PolicyDeploymentImpl.class, 
        		IPolicyDeployment.class, 
        		LifestyleType.SINGLETON_TYPE);
        IPolicyDeployment policyDeployment = compMgr.getComponent(policyDeploymentImplCompInfo);

        // Initialize the Agent Manager - depends on the profile manager and the
        // policy deployment module being initialized before-hand        
        ComponentInfo<AgentManager> agentMgrCompInfo = 
        	new ComponentInfo<AgentManager>(
        		IAgentManager.COMP_NAME, 
        		AgentManager.class, 
        		IAgentManager.class, 
        		LifestyleType.SINGLETON_TYPE);
        IAgentManager agentMgr = compMgr.getComponent(agentMgrCompInfo);
        
        //  Initialize the Profile Manager:
        /*
         * FIX ME - We shouldn't be directly accessing the IProfileManager
         * implementation here (i.e. HibernateProfileManager.class.getName()).
         * Though, I can't think of the negative consequences at the moment, so
         * I'm going to do it for consistency
         * 
         * Ideally, we should have a declaritive model, in which the required
         * components and their implementions are specified in a config file+
         */
        ComponentInfo<HibernateProfileManager> profileMgrCompInfo = 
        	new ComponentInfo<HibernateProfileManager>(
        		IProfileManager.COMP_NAME, 
        		HibernateProfileManager.class, 
        		IProfileManager.class, 
        		LifestyleType.SINGLETON_TYPE);
        compMgr.getComponent(profileMgrCompInfo);

        final IDABSComponentConfigurationDO dabsCfg = (IDABSComponentConfigurationDO) configMgr
				.retrieveComponentConfiguration(getComponentType().getName());
        
        final IFileSystemLogConfigurationDO fileSystemLogCfg = dabsCfg.getFileSystemLogConfiguration();
        
        //the length is 4000 now, it used to be 2000 because of a workaround. However, it is not easy
        // to upgrade postgresql database encoding. We will keep the old value, 2000, if the database
        // is not using UNICODE.
        int customAttrFieldLength = getCustomAttrFieldLength(activityDataSrc);
        
        // initialize the persistence log writer
        /*
         * This is the log writer that gets invoked by the real log writer 
         * to write logs into persistence.
         */
        HashMapConfiguration persistenceLogWriterConfig = new HashMapConfiguration();
        persistenceLogWriterConfig.setProperty(HibernateLogWriter.CUSTOM_ATTR_FIELD_LENGTH_KEY, customAttrFieldLength);
        ComponentInfo<HibernateLogWriter> persistenceLogWriterCompInfo = 
        	new ComponentInfo<HibernateLogWriter>(
        		HibernateLogWriter.COMP_NAME, 
        		HibernateLogWriter.class, 
        		ILogWriter.class, 
        		LifestyleType.SINGLETON_TYPE,
        		persistenceLogWriterConfig);
        ILogWriter persistenceLogWriter = compMgr.getComponent(persistenceLogWriterCompInfo);
        
        
        HashMapConfiguration fileSystemThreadPoolConfig = new HashMapConfiguration();
        fileSystemThreadPoolConfig.setProperty(IThreadPool.KEEP_ALIVE_TIME_PARAM, fileSystemLogCfg.getThreadPoolKeepAliveTime());
        fileSystemThreadPoolConfig.setProperty(IThreadPool.MAXIMUM_POOL_SIZE_PARAM, fileSystemLogCfg.getThreadPoolMaximumSize());
        IThreadPool fileSystemThreadPool = compMgr.getComponent(
				FileSystemThreadPool.COMP_INFO, fileSystemThreadPoolConfig);

        // Default log queue location
        INamedResourceLocator serverResourceLocator = (INamedResourceLocator) compMgr.getComponent(DCCResourceLocators.SERVER_HOME_RESOURCE_LOCATOR);
        String logQueueDirRelativePath = ServerRelativeFolders.LOG_QUEUE_FOLDER.getPath();
        String logQueueDirFullPath = serverResourceLocator.getFullyQualifiedName(logQueueDirRelativePath);

        // Optionally override by server.xml
        String loggingQueue = getConfiguration().get(DABS_LOG_QUEUE_FOLDER_LOCATION, logQueueDirFullPath);
        
        // initialize the log queue manager
        HashMapConfiguration logQueueMgrConfig = new HashMapConfiguration();
        logQueueMgrConfig.setProperty(FileSystemLogQueueMgr.LOG_QUEUE_LOCATION, loggingQueue);
        logQueueMgrConfig.setProperty(FileSystemLogQueueMgr.TIMEOUT_CHECKER_FREQUENCY, fileSystemLogCfg.getTimeoutCheckerFrequency());
        logQueueMgrConfig.setProperty(FileSystemLogQueueMgr.LOG_TIMEOUT, fileSystemLogCfg.getLogTimeout());
        logQueueMgrConfig.setProperty(FileSystemLogQueueMgr.MAX_BYTES_UPLOAD_SIZE_PARAM, fileSystemLogCfg.getQueueManagerUploadSize());
        logQueueMgrConfig.setProperty(FileSystemLogQueueMgr.MAX_HANDLE_FILE_SIZE_PER_THREAD_PARAM, fileSystemLogCfg.getMaxHandleFileSizePerThread());
        logQueueMgrConfig.setProperty(FileSystemLogQueueMgr.FILE_SYSTEM_THREAD_POOL_PARAM, fileSystemThreadPool);
        
        logQueueMgrConfig.setProperty(LogInsertTaskFactory.LOG_INSERT_TASK_IDLE_TIME, fileSystemLogCfg.getLogInsertTaskIdleTime());
        logQueueMgrConfig.setProperty(LogInsertTaskFactory.PERSISTENCE_LOG_WRITER_PARAM, persistenceLogWriter);
        ComponentInfo<FileSystemLogQueueMgr> logQueueMgrCompInfo = 
        	new ComponentInfo<FileSystemLogQueueMgr>(ILogQueueMgr.COMP_NAME, 
        			FileSystemLogQueueMgr.class, 
        			ILogQueueMgr.class, 
        			LifestyleType.SINGLETON_TYPE, 
        			logQueueMgrConfig);
        ILogQueueMgr logQueueMgr = compMgr.getComponent(logQueueMgrCompInfo);
        
        // Initialize the real Log Writer
        /*
         * Currently this is the default log writer being used.  This log
         * writer does not write to persistence directly, it uses the 
         * HibernateLogWriter initialized above to write to persistence. 
         * This particular log writer will queue the logs onto the file
         * system.  
         */
        HashMapConfiguration logWriterConfig = new HashMapConfiguration();
        logWriterConfig.setProperty(FileSystemBufferLogWriter.LOG_QUEUE_MGR_PARAM, logQueueMgr);
        ComponentInfo<FileSystemBufferLogWriter> logWriterCompInfo = 
        	new ComponentInfo<FileSystemBufferLogWriter>(
        		ILogWriter.COMP_NAME, 
        		FileSystemBufferLogWriter.class, 
        		ILogWriter.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		logWriterConfig);
        ILogWriter logWriter = compMgr.getComponent(logWriterCompInfo);

        // Initialize heartbeat manager
        ComponentInfo<ServerHeartbeatManagerImpl> heartbeatMgrCompInfo = 
        	new ComponentInfo<ServerHeartbeatManagerImpl>(
        		IServerHeartbeatManager.COMP_NAME, 
        		ServerHeartbeatManagerImpl.class, 
        		IServerHeartbeatManager.class, 
        		LifestyleType.SINGLETON_TYPE);
        IServerHeartbeatManager heartbeatMgr = compMgr.getComponent(heartbeatMgrCompInfo);

        // Initialize the client information provider
        try {
            heartbeatMgr.register(ClientInformationRequestDTO.CLIENT_INFORMATION_SERVICE, new ClientInformationProvider());
        } catch (Exception ex) {
            // Too bad
        }

        // Initialize the regular expression provider
        try {
            heartbeatMgr.register(RegularExpressionRequestDTO.REGULAR_EXPRESSION_SERVICE, new RegularExpressionProvider(dabsCfg.getRegularExpressions()));
        } catch (Exception ex) {
            // Too bad
        }

        try {
            heartbeatMgr.register(KeyRequestDTO.SERVICE_NAME, new ServerKeyProvider());
        } catch (Exception ex) {
            // Too bad
        }

        // Initialize the Policy Deployment manager
        HashMapConfiguration policyDeployConfig = new HashMapConfiguration();
        policyDeployConfig.setProperty(IPolicyDeploymentManager.MGMT_DATA_SOURCE_CONFIG_PARAM, mgmtDataSrc);
        policyDeployConfig.setProperty(IPolicyDeploymentManager.DABS_COMPONENT_NAME, getComponentName());
        ComponentInfo<PolicyDeploymentManagerImpl> policyDeploymentCompInfo = 
        	new ComponentInfo<PolicyDeploymentManagerImpl>(
        			IPolicyDeploymentManager.COMP_NAME, 
        			PolicyDeploymentManagerImpl.class, 
        			IPolicyDeploymentManager.class, 
        			LifestyleType.SINGLETON_TYPE, 
        			policyDeployConfig);
        IPolicyDeploymentManager policyDeploymentMgr = compMgr.getComponent(policyDeploymentCompInfo);

        
        // Initialize the SharedFolderInformation relay for DABS:
        ComponentInfo<DABSSharedFolderInformationRelayImpl> sharedFolderInfoRelayCompInfo = 
        	new ComponentInfo<DABSSharedFolderInformationRelayImpl>(
        			ISharedFolderInformationRelay.COMP_NAME, 
        			DABSSharedFolderInformationRelayImpl.class, 
        			ISharedFolderInformationRelay.class, 
        			LifestyleType.SINGLETON_TYPE);
        ISharedFolderInformationRelay sharedFolderInfoRelay = compMgr.getComponent(sharedFolderInfoRelayCompInfo);

        // Initialize Mail Helper -- Make sure the configuration is ready before
        // instantiating
        
        IMessageHandlerManager messageHandlerManager = compMgr.getComponent(MessageHandlerManagerImpl.class);
        IMessageHandler messageHandler =
                messageHandlerManager.getMessageHandler(EmailMessageHandler.DEFAULT_HANDLER_NAME);
        
        
        if (messageHandler != null) {
            String mailServerHost   = messageHandler.getProperty(IEmailMessageHandlerConfig.SERVER);
            Integer mailServerPort  = messageHandler.getProperty(IEmailMessageHandlerConfig.PORT);
            String username         = messageHandler.getProperty(IEmailMessageHandlerConfig.USER);
            String password         = messageHandler.getProperty(IEmailMessageHandlerConfig.PASSWORD);
            Address from            = messageHandler.getProperty(IEmailMessageHandlerConfig.DEFAULT_FROM);
            
            HashMapConfiguration mailConfiguration = new HashMapConfiguration();
            mailConfiguration.setProperty(IMailHelper.SERVER_CFG_KEY, mailServerHost);
            mailConfiguration.setProperty(IMailHelper.PORT_CFG_KEY, mailServerPort);
            mailConfiguration.setProperty(IMailHelper.USER_CFG_KEY, username);
            mailConfiguration.setProperty(IMailHelper.PASSWORD_CFG_KEY, password);
            mailConfiguration.setProperty(IMailHelper.HEADER_FROM_CFG_KEY, from.toString());
            MailHelper.COMP_INFO.overrideConfiguration(mailConfiguration);
            compMgr.registerComponent(MailHelper.COMP_INFO, true);
        } else {
            getLog().warn("Mail server was not configured. Policy notifications will be disabled.");
        }          


        // Launch thread that will cause the dictionary "lastConsistentTime" to reset every hour.  This will force us to hit the database
        // when a new bundle request comes in
        final DictionaryEventManager dictEventManager = new DictionaryEventManager(dictionary, compMgr);

        final int DELAY_BETWEEN_DICTIONARY_RESET =  15 * 60 * 1000;
        Timer dabsTimer = new Timer("DictionaryChangeChecker", true);
        dabsTimer.schedule( new TimerTask() {
            public void run() {
                dictEventManager.fireRemoteDictionaryChangeEvent();
            }
        }, DELAY_BETWEEN_DICTIONARY_RESET, DELAY_BETWEEN_DICTIONARY_RESET);

        // for DABS connect to DKMS
        SecureSessionVaultGateway.setSecureSessionVault(new ISecureSessionVault(){
            private SecureSession secureSession;

            public void storeSecureSession(SecureSession secureSession) {
                this.secureSession = secureSession;
            }

            public SecureSession getSecureSession() {
                return secureSession;
            }

            public void clearSecureSession() {
                secureSession = null;
            }
        });

    }

    /**
     * this is temporary needed. After 3 release of OrionM1, we will remove this method
     * @param activityDataSrc
     * @return
     */
    private int getCustomAttrFieldLength(IHibernateRepository activityDataSrc) {
        int customAttrFieldLength = -1;

        Session s = null;
        try {
            s = activityDataSrc.getCountedSession();
            Connection connection = s.connection();
            DatabaseMetaData metadata = connection.getMetaData();
            String url = metadata.getURL();
            String loweredUrl = url.toLowerCase();
            
            /**
             * all possible format
             * 
             * jdbc:postgresql:database
             * jdbc:postgresql://host/database
             * jdbc:postgresql://host:port/database
             */
            
            final String prefix = "jdbc:postgresql:";
            if(loweredUrl.startsWith(prefix)){
                //this is postgresql, check encoding
                
                int start;
                int end;
                
                int indexOfParameter = loweredUrl.lastIndexOf('?');
                if (indexOfParameter == -1) {
                    start = loweredUrl.lastIndexOf('/');
                    if (start == -1) {
                        start = loweredUrl.lastIndexOf(':');
                    }
                    end = loweredUrl.length();
                } else {
                    start = loweredUrl.lastIndexOf('/', indexOfParameter);
                    end = indexOfParameter;
                }
                String databaseName = url.substring(start + 1,end);
                
                PreparedStatement statement = null;
                ResultSet resultSet = null;
                
                try {
                    statement = connection.prepareStatement("select encoding from pg_database where datname = ?");
                    statement.setString(1, databaseName);
                    resultSet = statement.executeQuery();
                    
                    if (resultSet.next()) {
                        int encoding = resultSet.getInt(1);
                        //6 is unicode
                        //0 is sql_ascii
                        if (encoding != 6) {
                            customAttrFieldLength = 2000;
                        }
                    }
                } finally {
                    if (resultSet != null) {
                        try {
                            resultSet.close();
                        } catch (SQLException e) {
                            getLog().error("unable to close resultset", e);
                        }
                    }
                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (SQLException e) {
                            getLog().error("unable to close statement", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            getLog().error("unable to determine database encoding type. Default value, " 
                    + HibernateLogWriter.DEFAULT_CUSTOM_ATTR_FIELD_LENGTH + ", will be used." , e);
        } finally {
            if (s != null) {
                HibernateUtils.closeSession(activityDataSrc, getLog());
            }
        }

        if(customAttrFieldLength == -1){
            customAttrFieldLength = HibernateLogWriter.DEFAULT_CUSTOM_ATTR_FIELD_LENGTH;
        }
        return customAttrFieldLength;
    }
}
