/*
 * Created on Apr 14, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dem;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.IMasterResourceCache;
import com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl.MasterResourceCacheImpl;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * This thread is started upon component startup, and takes care of periodic
 * refreshes of the reporter data. The reporter data is refreshed
 * asynchronously.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dem/src/java/main/com/bluejungle/destiny/container/dem/ReporterCacheRefreshThread.java#1 $
 */

public class ReporterCacheRefreshThread extends Thread implements IInitializable, IConfigurable,
		IDisposable, IManagerEnabled, ILogEnabled {

	private static final String REPORTER_CACHE_THREAD_COMP_NAME = "ReporterCacheRefreshThread";
	
    /**
     * Reporter cache thread component configuration
     */
    public static final PropertyKey<Integer> REPORTER_CACHE_THREAD_REFRESH_PROP_NAME = 
            new PropertyKey<Integer>("refreshInterval");
    private static final Integer REPORTER_CACHE_THREAD_DEFAULT_REFRESH_RATE = new Integer(60 * 1000);
    
    public static final ComponentInfo<ReporterCacheRefreshThread> COMP_INFO = 
    	new ComponentInfo<ReporterCacheRefreshThread>(
    			REPORTER_CACHE_THREAD_COMP_NAME, 
    			ReporterCacheRefreshThread.class, 
    			ReporterCacheRefreshThread.class, 
    			LifestyleType.SINGLETON_TYPE);
    
    private static final String MASTER_CACHE_COMPONENT_NAME = "MasterResourceCache";

    private IConfiguration config;
    private IComponentManager manager;
    private IMasterResourceCache masterCache;
    private Log log;
    private int refreshInterval;

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        interrupt();
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        //Mark the thread as daemon thread
        setDaemon(true);
        
        // Sets the refresh interval
        IConfiguration config = getConfiguration();
        setRefreshInterval(config.get(REPORTER_CACHE_THREAD_REFRESH_PROP_NAME, 
        		REPORTER_CACHE_THREAD_DEFAULT_REFRESH_RATE));
        
        IComponentManager compMgr = getManager();
        getLog().debug("Initializing the report components in DEM.");
        IHibernateRepository activityDataSrc = (IHibernateRepository) compMgr.getComponent(
        		DestinyRepository.ACTIVITY_REPOSITORY.getName());

        //Initializes the master resource cache
        getLog().debug("Initializing the master resource cache in DEM component.");
        HashMapConfiguration resourceCacheConfig = new HashMapConfiguration();
        resourceCacheConfig.setProperty(IMasterResourceCache.ACTIVITY_DATASOURCE_CONFIG_ATTR, activityDataSrc);
        ComponentInfo<MasterResourceCacheImpl> resourceCacheCompInfo = 
        	new ComponentInfo<MasterResourceCacheImpl>(
        		MASTER_CACHE_COMPONENT_NAME, 
        		MasterResourceCacheImpl.class, 
        		IMasterResourceCache.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		resourceCacheConfig);
        compMgr.registerComponent(resourceCacheCompInfo, true);
        this.masterCache = compMgr.getComponent(resourceCacheCompInfo);
        getLog().debug("Initialized the master resource cache in DEM component.");

        //Starts the thread
        start();
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.manager;
    }

    /**
     * Returns the master cache object
     * 
     * @return the master cache object
     */
    protected IMasterResourceCache getMasterCache() {
        return this.masterCache;
    }

    /**
     * Returns the refresh interval
     * 
     * @return the refresh interval
     */
    protected int getRefreshInterval() {
        return this.refreshInterval;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        //Loads the resource cache data
        try {
            while (!interrupted()) {
                final long start = System.currentTimeMillis();
                getMasterCache().refreshCaches();
                final long end = System.currentTimeMillis();
                getLog().debug("Completed refresh of reporter cache. Refresh took " + (end - start) + " ms.");
                synchronized (this) {
                    this.wait(getRefreshInterval());
                }
            }
        } catch (InterruptedException e) {
            //Exiting the thread
            getLog().debug("Report cache refresh thread interrupted");
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * Sets the refresh interval
     * 
     * @param newRefreshInterval
     *            refresh interval to set
     */
    protected void setRefreshInterval(int newRefreshInterval) {
        this.refreshInterval = newRefreshInterval;
    }
}
