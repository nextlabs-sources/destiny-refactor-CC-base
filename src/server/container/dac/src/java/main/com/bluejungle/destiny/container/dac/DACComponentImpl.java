/*
 * Created on Feb 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac;

import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.IHostMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.hostmgr.hibernateimpl.HostMgrImpl;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.IPolicyMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.policymgr.hibernateimpl.PolicyMgrImpl;
import com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.IResourceClassMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr.memoryimpl.ResourceClassMgrImpl;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.hibernateimpl.UserMgrImpl;
import com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.IUserReportMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.userreportmgr.hibernateimpl.UserReportMgrImpl;
import com.bluejungle.destiny.container.dcc.BaseDCCComponentImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.PersistentReportMgrImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ReportExecutionMgrStatelessImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ReportMgrImpl;
import com.bluejungle.destiny.container.shared.storedresults.IStoredQueryMgr;
import com.bluejungle.destiny.container.shared.storedresults.hibernateimpl.StoredQueryMgrImpl;
import com.bluejungle.destiny.server.shared.configuration.IActivityJournalSettingConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDACComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.pf.domain.destiny.common.ServerSpecManager;
import com.nextlabs.destiny.container.dac.datasync.Constants;
import com.nextlabs.destiny.container.dac.datasync.DataSyncManager;
import com.nextlabs.destiny.container.dac.datasync.IDataSyncManager;
import com.nextlabs.destiny.container.shared.inquirymgr.ReportDataHolderManager;
import com.nextlabs.destiny.container.shared.utils.DCCComponentHelper;

/**
 * This is the DAC component implementation class.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/shared/com/bluejungle/destiny/container/dac/DACComponentImpl.java#1 $
 */

public class DACComponentImpl extends BaseDCCComponentImpl {

    private IDisposable storedQueryMgr;

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        if (this.storedQueryMgr != null) {
            this.storedQueryMgr.dispose();
        }
        super.dispose();
    }

    /**
     * Returns the component type. For this component, the component type is
     * DAC.
     * 
     * @return the component type.
     */
    public ServerComponentType getComponentType() {
        return ServerComponentType.DAC;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        super.init();

        IComponentManager compMgr = getManager();
        compMgr.registerComponent(ServerSpecManager.COMP_INFO, true);

        DCCComponentHelper.initSecurityComponents(getManager(), getLog());
        initReportComponents();
    }

    /**
     * This function initializes all components related to inquiry center.
     */
    protected void initReportComponents() {
        IComponentManager compMgr = getManager();
        IHibernateRepository activityDataSrc = (IHibernateRepository) compMgr.getComponent(DestinyRepository.ACTIVITY_REPOSITORY.getName());

        final IDestinyConfigurationStore confStore = getManager().getComponent(DestinyConfigurationStoreImpl.COMP_INFO);
        
        //Initializes the stored query manager
        getLog().debug("Initializing the stored query manager in DAC component.");
        HashMapConfiguration storedQueryMgrConfig = new HashMapConfiguration();
        storedQueryMgrConfig.setProperty(IStoredQueryMgr.ACTIVITY_DATA_SOURCE_CONFIG_PARAM, activityDataSrc);
        ComponentInfo<StoredQueryMgrImpl> storedQueryMgrImplCompInfo = new ComponentInfo<StoredQueryMgrImpl>(
        		"storedQueryMgr", 
        		StoredQueryMgrImpl.class, 
        		IStoredQueryMgr.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		storedQueryMgrConfig);
        StoredQueryMgrImpl storedQueryManager = compMgr.getComponent(storedQueryMgrImplCompInfo);
        if (storedQueryManager != null && storedQueryManager instanceof IDisposable) {
            this.storedQueryMgr = storedQueryManager;
        } else {
            getLog().warn("Stored query manager does not appear to be disposable...");
        }
        getLog().debug("Initialized the stored query manager in DAC component.");



        //Initializes the policy manager component
        getLog().debug("Initializing the policy manager in DAC component.");
        HashMapConfiguration policyMgrConfig = new HashMapConfiguration();
        policyMgrConfig.setProperty(IPolicyMgr.DATASOURCE_CONFIG_PARAM, activityDataSrc);
        ComponentInfo<PolicyMgrImpl> policyMgrImplCompInfo = 
        	new ComponentInfo<PolicyMgrImpl>(
        		IPolicyMgr.COMP_NAME, 
        		PolicyMgrImpl.class, 
        		IPolicyMgr.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		policyMgrConfig);
        compMgr.registerComponent(policyMgrImplCompInfo, true);
        //Prime the component here
        compMgr.getComponent(policyMgrImplCompInfo);
        getLog().debug("Initialized the policy manager in DAC component.");

        //Initializes the resource class manager component
        getLog().debug("Initializing the resource class manager in DAC component.");
        HashMapConfiguration resourceClassMgrConfig = new HashMapConfiguration();
        ComponentInfo<ResourceClassMgrImpl> resourceClassMgrImplCompInfo = 
        	new ComponentInfo<ResourceClassMgrImpl>(
        		IResourceClassMgr.COMP_NAME, 
        		ResourceClassMgrImpl.class, 
        		IResourceClassMgr.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		resourceClassMgrConfig);
        compMgr.registerComponent(resourceClassMgrImplCompInfo, true);
        //Prime the component here
        compMgr.getComponent(resourceClassMgrImplCompInfo);
        getLog().debug("Initialized the resource class manager in DAC component.");

        //Initializes the host manager component
        getLog().debug("Initializing the host manager in DAC component.");
        HashMapConfiguration hostMgrConfig = new HashMapConfiguration();
        hostMgrConfig.setProperty(IHostMgr.DATASOURCE_CONFIG_PARAM, activityDataSrc);
        ComponentInfo<HostMgrImpl> hostMgrImplCompInfo = new ComponentInfo<HostMgrImpl>(
        		IHostMgr.COMP_NAME, 
        		HostMgrImpl.class, 
        		IHostMgr.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		hostMgrConfig);
        compMgr.registerComponent(hostMgrImplCompInfo, true);
        //Prime the component here
        compMgr.getComponent(hostMgrImplCompInfo);
        getLog().debug("Initialized the host manager in DAC component.");

        //Initializes the user manager component
        getLog().debug("Initializing the user manager in DAC component.");
        HashMapConfiguration userMgrConfig = new HashMapConfiguration();
        userMgrConfig.setProperty(IUserMgr.DATASOURCE_CONFIG_PARAM, activityDataSrc);
        ComponentInfo<UserMgrImpl> userMgrImplCompInfo = 
        	new ComponentInfo<UserMgrImpl>(
        		IUserMgr.COMP_NAME, 
        		UserMgrImpl.class, 
        		IUserMgr.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		userMgrConfig);
        compMgr.registerComponent(userMgrImplCompInfo, true);
        //Prime the component here
        compMgr.getComponent(userMgrImplCompInfo);
        getLog().debug("Initialized the user manager in DAC component.");

        //Initializes the user report manager component
        getLog().debug("Initializing the user report manager in DAC component.");
        HashMapConfiguration userReportMgrConfig = new HashMapConfiguration();
        userReportMgrConfig.setProperty(IUserReportMgr.DATASOURCE_CONFIG_PARAM, activityDataSrc);
        ComponentInfo<UserReportMgrImpl> userReportMgrImplCompInfo = 
        	new ComponentInfo<UserReportMgrImpl>(
        		IUserReportMgr.COMP_NAME, 
        		UserReportMgrImpl.class, 
        		IUserReportMgr.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		userReportMgrConfig);
        compMgr.registerComponent(userReportMgrImplCompInfo, true);
        //Prime the component here
        compMgr.getComponent(userReportMgrImplCompInfo);
        getLog().debug("Initialized the user report manager in DAC component.");

        //Initializes the persistent report manager component
        getLog().debug("Initializing the persistent report manager in DAC component.");
        HashMapConfiguration persistentReportMgrConfig = new HashMapConfiguration();
        persistentReportMgrConfig.setProperty(IPersistentReportMgr.REPORT_DATA_SOURCE_CONFIG_PARAM, activityDataSrc);
        ComponentInfo<PersistentReportMgrImpl> persistentReportMgrImplCompInfo = 
        	new ComponentInfo<PersistentReportMgrImpl>(
        		IPersistentReportMgr.COMP_NAME, 
        		PersistentReportMgrImpl.class, 
        		IPersistentReportMgr.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		persistentReportMgrConfig);
        compMgr.registerComponent(persistentReportMgrImplCompInfo, true);
        //Prime the component here
        compMgr.getComponent(persistentReportMgrImplCompInfo);
        getLog().debug("Initialized the persistent report manager in DAC component.");

        //Initializes the "in memory" report manager
        getLog().debug("Initializing the memory report manager in DAC component.");
        HashMapConfiguration reportMgrConfig = new HashMapConfiguration();
        ComponentInfo<ReportMgrImpl> reportMgrImplCompInfo = 
        	new ComponentInfo<ReportMgrImpl>(
        		IReportMgr.COMP_NAME, 
        		ReportMgrImpl.class, 
        		IReportMgr.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		reportMgrConfig);
        compMgr.registerComponent(reportMgrImplCompInfo, true);
        //Prime the component here
        compMgr.getComponent(reportMgrImplCompInfo);
        getLog().debug("Initialized the memory report manager in DAC component.");

        //Initializes the report execution manager component
        getLog().debug("Initializing the report execution manager in DAC component.");
        HashMapConfiguration reportExecutionMgrConfig = new HashMapConfiguration();
        reportExecutionMgrConfig.setProperty(IReportExecutionMgr.DATA_SOURCE_CONFIG_PARAM, activityDataSrc);
        ComponentInfo<ReportExecutionMgrStatelessImpl> reportExecutionMgrStatelessImplCompInfo = 
        	new ComponentInfo<ReportExecutionMgrStatelessImpl>(
        		IReportExecutionMgr.COMP_NAME, 
        		ReportExecutionMgrStatelessImpl.class,
        		IStatelessReportExecutionMgr.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		reportExecutionMgrConfig);
        compMgr.registerComponent(reportExecutionMgrStatelessImplCompInfo, true);
        //Prime the component here
        compMgr.getComponent(reportExecutionMgrStatelessImplCompInfo);
        getLog().debug("Initialized the report execution manager in DAC component.");
        
        
        HashMapConfiguration reportDataHolderonfig = new HashMapConfiguration();
        reportDataHolderonfig.setProperty(ReportDataHolderManager.DATA_SOURCE_PARAM, activityDataSrc);
        ReportDataHolderManager reportDataHolderManager = compMgr.getComponent(
                ReportDataHolderManager.class, reportDataHolderonfig);
        
        
        //init reporter sync manager
        IDACComponentConfigurationDO dacConfig = (IDACComponentConfigurationDO) confStore
                .retrieveComponentConfiguration(getComponentType().getName());
        
        IActivityJournalSettingConfigurationDO reporterSyncConfigDo = dacConfig.getActivityJournalSettingConfiguration();
        HashMapConfiguration reporterSyncConfig = new HashMapConfiguration();
        reporterSyncConfig.setProperty(IDataSyncManager.DATASOURCE_CONFIG_PARAM, activityDataSrc);
        
        if(reporterSyncConfigDo.getSyncTimeInterval() != null){
            reporterSyncConfig.setProperty(IDataSyncManager.SYNC_TIME_INTERVAL_PARAM,
                    reporterSyncConfigDo.getSyncTimeInterval() * 60 * 1000);
        }
        reporterSyncConfig.setProperty(IDataSyncManager.SYNC_TIME_OF_DAY_PARAM,
                reporterSyncConfigDo.getSyncTimeOfDay());
        reporterSyncConfig.setProperty(IDataSyncManager.SYNC_TIMEOUT_PARAM,
                reporterSyncConfigDo.getSyncTimeoutInMinutes() * 60 * 1000L);
        
        reporterSyncConfig.setProperty(IDataSyncManager.INDEXES_REBUILD_TIME_OF_DAY,
                reporterSyncConfigDo.getIndexRebuildTimeOfDay());
        if (reporterSyncConfigDo.getIndexRebuildDaysOfWeek() != null) {
            reporterSyncConfig.setProperty(IDataSyncManager.INDEXES_REBUILD_DAY_OF_WEEK,
                    reporterSyncConfigDo.getIndexRebuildDaysOfWeek().getDaysOfWeek());
        }
        if (reporterSyncConfigDo.getIndexRebuildDaysOfMonth() != null) {
            reporterSyncConfig.setProperty(IDataSyncManager.INDEXES_REBUILD_DAY_OF_MONTH,
                    reporterSyncConfigDo.getIndexRebuildDaysOfMonth().getDaysOfMonth());
        }
        reporterSyncConfig.setProperty(IDataSyncManager.INDEXES_REBUILD_AUTO_REBUILD_INDEXES,
                reporterSyncConfigDo.getIndexRebuildAutoRebuildIndexes());
        reporterSyncConfig.setProperty(IDataSyncManager.INDEXES_REBUILD_TIMEOUT_PARAM,
                reporterSyncConfigDo.getIndexRebuildTimeoutInMinutes() * 60 * 1000L);

        reporterSyncConfig.setProperty(IDataSyncManager.ARCHIVE_TIME_OF_DAY, reporterSyncConfigDo
                .getArchiveTimeOfDay());
        if (reporterSyncConfigDo.getArchiveDaysOfWeek() != null) {
            reporterSyncConfig.setProperty(IDataSyncManager.ARCHIVE_DAY_OF_WEEK,
                    reporterSyncConfigDo.getArchiveDaysOfWeek().getDaysOfWeek());
        }
        if (reporterSyncConfigDo.getArchiveDaysOfMonth() != null) {
            reporterSyncConfig.setProperty(IDataSyncManager.ARCHIVE_DAY_OF_MONTH,
                    reporterSyncConfigDo.getArchiveDaysOfMonth().getDaysOfMonth());
        }
        reporterSyncConfig.setProperty(IDataSyncManager.ARCHIVE_DAYS_OF_DATA_TO_KEEP,
                reporterSyncConfigDo.getArchiveDaysOfDataToKeep());
        reporterSyncConfig.setProperty(IDataSyncManager.ARCHIVE_AUTO_ARCHIVE, reporterSyncConfigDo
                .getArchiveAutoArchive());
        
        reporterSyncConfig.setProperty(IDataSyncManager.ARCHIVE_TIMEOUT_PARAM,
                reporterSyncConfigDo.getArchiveTimeoutInMinutes() * 60 * 1000L);
        
        reporterSyncConfig.setProperty(IDataSyncManager.REPORT_DATA_HOLDER_MANAGER_PARAM, 
                reportDataHolderManager);
        
        java.util.Properties props = dacConfig.getProperties();
        
        if (props != null && !props.isEmpty())
        {
        	reporterSyncConfig.setProperty(Constants.USER_ATTRIBUTES_BLACKLIST_PROPERTY, 
        			props.getProperty(Constants.USER_ATTRIBUTES_BLACKLIST_PROPERTY));
        	
        	reporterSyncConfig.setProperty(Constants.RESOURCE_ATTRIBUTES_BLACKLIST_PROPERTY, 
        			props.getProperty(Constants.RESOURCE_ATTRIBUTES_BLACKLIST_PROPERTY));
        	
        	reporterSyncConfig.setProperty(Constants.POLICY_ATTRIBUTES_BLACKLIST_PROPERTY, 
        			props.getProperty(Constants.POLICY_ATTRIBUTES_BLACKLIST_PROPERTY));
        	
        	reporterSyncConfig.setProperty(Constants.NUMBER_OF_EXTENDED_ATTRS_PROPERTY, 
        			props.getProperty(Constants.NUMBER_OF_EXTENDED_ATTRS_PROPERTY));
        	
        	getLog().debug("Number of extended attributes: " + props.getProperty(Constants.NUMBER_OF_EXTENDED_ATTRS_PROPERTY));
        	getLog().debug("User Attributes Blacklist: " + props.getProperty(Constants.USER_ATTRIBUTES_BLACKLIST_PROPERTY));
        	getLog().debug("Resource Attributes Blacklist: " + props.getProperty(Constants.RESOURCE_ATTRIBUTES_BLACKLIST_PROPERTY));
        	getLog().debug("Policy Attributes Blacklist: " + props.getProperty(Constants.POLICY_ATTRIBUTES_BLACKLIST_PROPERTY));
        }
        else
        {
        	getLog().debug("Properties bag in DAC Config is NULL or Empty");
        }
        
        compMgr.getComponent(DataSyncManager.class, reporterSyncConfig);
        getLog().debug("Initialized the report sync manager in DAC component.");
    }

}
