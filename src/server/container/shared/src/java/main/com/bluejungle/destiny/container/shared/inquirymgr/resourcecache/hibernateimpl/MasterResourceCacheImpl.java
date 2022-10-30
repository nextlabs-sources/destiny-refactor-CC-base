/*
 * Created on Mar 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.IMasterResourceCache;
import com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.IResourceCache;
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
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * This is the master resource cache implementation class. The master resource
 * cache orchestrates the update of all data within the resource data cache. It
 * pulls the state of every cached resource object, and decides whether an
 * update needs to be done in the database tables. All report execution requests
 * go through this class, and this class will lock the report execution if a
 * resource data cache refresh needs to occur.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/MasterResourceCacheImpl.java#1 $
 */

public class MasterResourceCacheImpl implements IMasterResourceCache, IInitializable, IDisposable, IConfigurable, IManagerEnabled, ILogEnabled {

    private IConfiguration configuration;
    private IHibernateRepository activityDataSource;
    private Log log;
    private IComponentManager manager;
    private Collection<IResourceCache> resourceCaches;

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        if (resourceCaches != null) {
            resourceCaches.clear();
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }


    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.manager;
    }

    /**
     * Creates the various resource data caches and initializes them
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {

        this.activityDataSource = getConfiguration().get(ACTIVITY_DATASOURCE_CONFIG_ATTR);
        if (this.activityDataSource == null) {
            throw new NullPointerException("Activity data source cannot be NULL");
        }
        
        resourceCaches = new LinkedList<IResourceCache>();

        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(ACTIVITY_DATASOURCE_CONFIG_ATTR, this.activityDataSource);

        //Creates the policy resource cache
        IComponentManager compMgr = getManager();
        ComponentInfo<IResourceCache> appInfo = new ComponentInfo<IResourceCache>(
                ApplicationCacheImpl.class, 
                LifestyleType.SINGLETON_TYPE, 
                config);
        resourceCaches.add(compMgr.getComponent(appInfo));

        ComponentInfo<IResourceCache> hostInfo = new ComponentInfo<IResourceCache>(
                HostAndGroupCacheImpl.class, 
                LifestyleType.SINGLETON_TYPE, 
                config);
        resourceCaches.add(compMgr.getComponent(hostInfo));

        ComponentInfo<IResourceCache> policyInfo = new ComponentInfo<IResourceCache>(
                PolicyCacheImpl.class, 
                LifestyleType.SINGLETON_TYPE, 
                config);
        resourceCaches.add(compMgr.getComponent(policyInfo));

        ComponentInfo<IResourceCache> userInfo = new ComponentInfo<IResourceCache>(
                UserAndGroupsCacheImpl.class, 
                LifestyleType.SINGLETON_TYPE, 
                config);
        resourceCaches.add(compMgr.getComponent(userInfo));
    }

    /**
     * This method is called whenever an access to a cached resource is about to
     * occur. The master cache figures out if any of the caches needs to be
     * refreshed, based on the current time.
     * 
     * @see com.bluejungle.destiny.container.shared.inquirymgr.resourceCache.IMasterResourceCache#refreshCaches()
     */
    public void refreshCaches() {
        getLog().debug("Loading master resource cache");
        for(IResourceCache resourceCache : resourceCaches){
            getLog().debug("Refreshing " + resourceCache.toString());
            resourceCache.refresh();
        }
        getLog().debug("Master resource cache loading complete");
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
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
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }
}
