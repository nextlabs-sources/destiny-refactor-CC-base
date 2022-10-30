/*
 * Created on Jun 21, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.datasync;

import static com.nextlabs.destiny.tools.datasync.DatasyncConsoleOptionDescriptorEnum.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.sf.hibernate.HibernateException;

import com.bluejungle.destiny.server.shared.configuration.IDACComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IActivityJournalSettingConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IRepositoryConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.AbstractConfigurableHibernateRepositoryImpl;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.PoolBackedHibernateRepositoryImpl;
import com.bluejungle.framework.sharedcontext.DestinySharedContextLocatorImpl;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.framework.utils.CollectionUtils;
import com.nextlabs.destiny.container.dac.datasync.IDataSyncManager;
import com.nextlabs.destiny.container.dac.datasync.IDataSyncTask.SyncType;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl.ConfigurationFileParser;
import com.nextlabs.destiny.container.shared.inquirymgr.ReportDataHolderManager;
import com.nextlabs.shared.tools.ConsoleApplicationBase;
import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/datasync/src/java/main/com/nextlabs/destiny/tools/datasync/DatasyncConsole.java#1 $
 */

public class DatasyncConsole extends ConsoleApplicationBase {
    public static void main(String[] args) {
        try {
            new DatasyncConsole().parseAndExecute(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private final IConsoleApplicationDescriptor options;

    private DatasyncConsole() throws InvalidOptionDescriptorException {
        options = new DatasyncConsoleOptionDescriptorEnum();
    }
    
    @Override
    protected IConsoleApplicationDescriptor getDescriptor() {
        return options;
    }
    
    @Override
    protected void execute(ICommandLine commandLine) {
        final SyncType type = getValue(commandLine, ACTION_OPTION_ID);
        final File configFolder = getValue(commandLine, SERVER_CONFIGURATION_FOLDER_OPTION_ID);
        //final String managerId = getValue(commandLine, MANAGER_ID_OPTION_ID);
        final boolean force = false; //(Boolean)getValue(commandLine, FORCE
        
        
        final ConfigurationFileParser configurationFileParser = parseServerConfigFile(configFolder);
        if (configurationFileParser == null) {
            return;
        }
        
        String defaultId;
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            defaultId = localhost.getCanonicalHostName() + " - " + localhost.getHostAddress();
        } catch (UnknownHostException e) {
            defaultId = "unknown";
        }
        
        String workingFolder = System.getProperty("user.dir");
        String suffix;
        if(workingFolder != null){
            suffix = Integer.toString(new File(workingFolder).getAbsolutePath().hashCode());
        }else{
            suffix = "console";
        }
        
        String managerId = defaultId + " - " + suffix;
        
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        DataSyncManagerConsole manager = getDataSyncManager(compMgr, configurationFileParser, managerId);

        if(force){
            try {
                manager.clear();
            } catch (HibernateException e) {
                System.err.println("fail to force execute action. " + e.getMessage());
            }
        }
        
        System.out.println("manager id = " + manager.getId());
        System.out.println(type.name() + " is starting.");
        
        switch (type) {
        case SYNC:
            manager.sync();
            break;
        case INDEXES_REBUILD:
            manager.indexRebuild();
            break;
        case ARCHIVE:
            manager.archive();
            break;
        default:
            throw new IllegalArgumentException("unknown syntype: " + type);
        }
        
        // the manager is holding the main thread because of the executor 
        // after the task is done, it will call manager.register() to reschedule next task.
        // we override the register() to shutdown the manager.
        // Once the manager shutdown. The lock will be release and the program will exit. 
    }
    
    private ConfigurationFileParser parseServerConfigFile(File configurationFolder) {
        ConfigurationFileParser configurationFileParser = new ConfigurationFileParser();
        Collection<String> errors = configurationFileParser.validateConfig(configurationFolder);
        
        if (errors != null && errors.size() > 0) {
            System.err.println(CollectionUtils.asString(errors, ConsoleDisplayHelper.NEWLINE));
            return null;
        }
        
        configurationFileParser.parseConfig(configurationFolder);
        
        return configurationFileParser;
    }
    
    private DataSyncManagerConsole getDataSyncManager(IComponentManager compMgr,
            ConfigurationFileParser configurationFileParser, String managerId) {
        final IActivityJournalSettingConfigurationDO syncConfig = getSyncConfig(configurationFileParser);
        final IRepositoryConfigurationDO activityRepositoryConfig = getActivityRepositoryConfiguration(configurationFileParser);
        
        createDestinySharedContextLocator(compMgr);
        IHibernateRepository activityDataSrc = createHiberateRepository(compMgr, activityRepositoryConfig);
        
        
        HashMapConfiguration reportDataHolderManagerConfig = new HashMapConfiguration();
        reportDataHolderManagerConfig.setProperty(ReportDataHolderManager.DATA_SOURCE_PARAM, activityDataSrc);
        ReportDataHolderManager reportDataHolderManager = compMgr.getComponent(
                ReportDataHolderManager.class, reportDataHolderManagerConfig);
        
//        return createDataSyncManager(compMgr, activityDataSrc, syncConfig, managerId, reportDataHolderManager);
        
        HashMapConfiguration conf = new HashMapConfiguration();
        conf.setProperty(IDataSyncManager.DATASOURCE_CONFIG_PARAM, activityDataSrc);
        
        conf.setProperty(IDataSyncManager.SYNC_TIME_INTERVAL_PARAM,             0L);
        conf.setProperty(IDataSyncManager.SYNC_TIME_OF_DAY_PARAM,               null);
        conf.setProperty(IDataSyncManager.SYNC_TIMEOUT_PARAM,                   syncConfig.getSyncTimeoutInMinutes() * 60 * 1000L);
        
        conf.setProperty(IDataSyncManager.INDEXES_REBUILD_TIME_OF_DAY,          syncConfig.getIndexRebuildTimeOfDay());
        conf.setProperty(IDataSyncManager.INDEXES_REBUILD_DAY_OF_WEEK,          new BitSet(7));
        conf.setProperty(IDataSyncManager.INDEXES_REBUILD_DAY_OF_MONTH,         null);
        conf.setProperty(IDataSyncManager.INDEXES_REBUILD_AUTO_REBUILD_INDEXES, true);
        conf.setProperty(IDataSyncManager.INDEXES_REBUILD_TIMEOUT_PARAM,        syncConfig.getIndexRebuildTimeoutInMinutes() * 60 * 1000L);

        conf.setProperty(IDataSyncManager.ARCHIVE_TIME_OF_DAY,                  syncConfig.getArchiveTimeOfDay());
        conf.setProperty(IDataSyncManager.ARCHIVE_DAY_OF_WEEK,                  new BitSet(7));
        conf.setProperty(IDataSyncManager.ARCHIVE_DAY_OF_MONTH,                 null);
        conf.setProperty(IDataSyncManager.ARCHIVE_DAYS_OF_DATA_TO_KEEP,         syncConfig.getArchiveDaysOfDataToKeep());
        conf.setProperty(IDataSyncManager.ARCHIVE_AUTO_ARCHIVE,                 true);
        conf.setProperty(IDataSyncManager.ARCHIVE_TIMEOUT_PARAM,                syncConfig.getArchiveTimeoutInMinutes() * 60 * 1000L);
        conf.setProperty(IDataSyncManager.MANAGER_ID_PARAM,                     managerId);
        conf.setProperty(IDataSyncManager.REPORT_DATA_HOLDER_MANAGER_PARAM,     reportDataHolderManager);
        
        DataSyncManagerConsole manager = compMgr.getComponent(
                new ComponentInfo<DataSyncManagerConsole>(
                    DataSyncManagerConsole.class.getName(), 
                    DataSyncManagerConsole.class, 
                    IDataSyncManager.class, 
                    LifestyleType.SINGLETON_TYPE, 
                    conf));
        return manager;
    }

    private IDestinySharedContextLocator createDestinySharedContextLocator(IComponentManager compMgr) {
        ComponentInfo<IDestinySharedContextLocator> locatorInfo =
                new ComponentInfo<IDestinySharedContextLocator>
                    (IDestinySharedContextLocator.COMP_NAME,
                    DestinySharedContextLocatorImpl.class,
                    IDestinySharedContextLocator.class, 
                    LifestyleType.TRANSIENT_TYPE);
       return compMgr.getComponent(locatorInfo);
    }
    
    private IHibernateRepository createHiberateRepository(IComponentManager compMgr,
            IRepositoryConfigurationDO activityRepositoryConfig) {
        HashMapConfiguration conf = new HashMapConfiguration();
        InputStream commonPropIs = this.getClass().getResourceAsStream("/conf/" + DestinyRepository.COMMON_REPOSITORY.getConfigFileName());
        InputStream activityXmlIs = this.getClass().getResourceAsStream("/conf/activity.repository.xml");
            
        if( commonPropIs == null || activityXmlIs == null){
            System.err.println("Fail to load property files. Are you running from eclipse?");
            // I am in eclipse, not running from jar
            String srcRoot = System.getProperty("src.root.dir");
            if (srcRoot == null) {
                throw new NullPointerException("please set the src.root.dir");
            }
            try {
                commonPropIs = new FileInputStream(srcRoot + "/server/base/resource/main/common.repository.properties");
                activityXmlIs = new FileInputStream(srcRoot + "/server/container/dac/src/hibernate/activity.repository.xml");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        
        conf.setProperty(AbstractConfigurableHibernateRepositoryImpl.COMMON_CONFIG, commonPropIs );
        conf.setProperty(AbstractConfigurableHibernateRepositoryImpl.REPOSITORY_CONFIG, activityXmlIs);
        conf.setProperty(PoolBackedHibernateRepositoryImpl.REPOSITORY_DEFINITION, activityRepositoryConfig);
        IHibernateRepository activityDataSrc = compMgr.getComponent(
                new ComponentInfo<IHibernateRepository>(
                    DestinyRepository.ACTIVITY_REPOSITORY.getName(), 
                    PoolBackedHibernateRepositoryImpl.class, 
                    IHibernateRepository.class, 
                    LifestyleType.SINGLETON_TYPE, 
                    conf));
        return activityDataSrc;
    }
    
    private IActivityJournalSettingConfigurationDO getSyncConfig(
            ConfigurationFileParser configurationFileParser) throws IllegalArgumentException {
        IActivityJournalSettingConfigurationDO syncConfig = null;
        Map<ServerComponentType, IDCCComponentConfigurationDO> map = configurationFileParser.getDCCConfigs();
        if (map != null) {
            IDACComponentConfigurationDO dacConfig = (IDACComponentConfigurationDO)map.get(ServerComponentType.DAC);
            if(dacConfig != null){
                syncConfig = dacConfig.getActivityJournalSettingConfiguration();
            }
        }
        
        if(syncConfig == null){
            throw new IllegalArgumentException("missing dac sync config.");
        }
        return syncConfig;
    }
    
    private IRepositoryConfigurationDO getActivityRepositoryConfiguration(
            ConfigurationFileParser configurationFileParser) throws IllegalArgumentException {
        IRepositoryConfigurationDO activityRepositoryConfig = null;
        RepositoryConfigurationList repositoryConfigurationList = configurationFileParser.getRepositories();
        if(repositoryConfigurationList != null){
            Set<? extends IRepositoryConfigurationDO> repositoryConfigs = repositoryConfigurationList.getRepositoriesAsSet();
            if(repositoryConfigs != null){
                for(IRepositoryConfigurationDO repositoryConfig : repositoryConfigs){
                    if (DestinyRepository.ACTIVITY_REPOSITORY.getName().equals(repositoryConfig.getName())) {
                        activityRepositoryConfig = repositoryConfig;
                    }
                }
            }
        }
        
        if(activityRepositoryConfig == null){
            throw new IllegalArgumentException("missing activity repository config.");
        }
        return activityRepositoryConfig;
    }
    
}
