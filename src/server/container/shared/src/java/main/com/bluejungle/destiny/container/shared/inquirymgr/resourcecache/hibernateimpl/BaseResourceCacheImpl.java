/*
 * Created on Mar 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl;

import java.util.Calendar;
import java.util.Date;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Expression;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.IResourceCache;
import com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.IResourceCacheState;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * This is the base class for all component classes. It contains the basic
 * member variables for the component manager.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/BaseResourceCacheImpl.java#1 $
 */

public abstract class BaseResourceCacheImpl implements IDisposable, ILogEnabled, IManagerEnabled, IResourceCache, IInitializable, IConfigurable {

    private IConfiguration configuration;
    private IHibernateRepository dataSource;
    private Log log;
    private IComponentManager manager;

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Returns the data source object used by this cache
     * 
     * @return the data source object used by this cache
     */
    protected IHibernateRepository getDataSource() {
        return this.dataSource;
    }

    /**
     * Returns the last update time for for the current cache
     * 
     * @return the last update time for for the current cache
     * @throws HibernateException
     *             if the date cannot be retrieved
     */
    protected Date getLastUpdateTime() throws HibernateException {
        return getLastUpdateTime(getResourceCacheType());
    }

    /**
     * Returns the last update time for a particular resource cache type
     * 
     * @param type
     *            type of the resource cache
     * @return the last update time for this cache type
     * @throws HibernateException
     *             if the date cannot be retrieved
     */
    private Date getLastUpdateTime(ResourceCacheType type) throws HibernateException {
        Session s = null;
        try {
            s = getDataSource().getSession();
            Criteria crit = s.createCriteria(ResourceCacheStateDO.class);
            crit.add(Expression.eq("type", type));
            IResourceCacheState cacheState = (IResourceCacheState) crit.uniqueResult();
            return cacheState.getLastUpdated().getTime();
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
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
     * Returns the type of resource cache items handled by the resource cache.
     * 
     * @return the type of resource cache items handled by the resource cache.
     */
    protected abstract ResourceCacheType getResourceCacheType();

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        this.dataSource = (IHibernateRepository) getConfiguration().get(ACTIVITY_DATASOURCE_CONFIG_ATTR);
        if (this.dataSource == null) {
            throw new NullPointerException("Data source must be specified for the resource cache");
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration newConfig) {
        this.configuration = newConfig;
    }

    /**
     * Sets the last updated time.
     * 
     * @param newUpdateTime
     *            new updated time to set
     * @throws HibernateException
     *             if storing the updated time fails
     */
    protected void setLastUpdateTime(Date newUpdateTime) throws HibernateException {
        setLastUpdateTime(newUpdateTime, getResourceCacheType());
    }

    /**
     * Sets the last updated time for a particular cache type
     * 
     * @param newUpdateTime
     *            new updated time to set
     * @param type
     *            type of resource cache
     * @throws HibernateException
     *             if storing the updated time fails.
     */
    private void setLastUpdateTime(Date newUpdateTime, ResourceCacheType type) throws HibernateException {
        Session s = null;
        Transaction t = null;
        try {
            s = getDataSource().getSession();
            Criteria crit = s.createCriteria(ResourceCacheStateDO.class);
            crit.add(Expression.eq("type", type));
            ResourceCacheStateDO cacheState = (ResourceCacheStateDO) crit.uniqueResult();
            final Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(newUpdateTime.getTime());
            cacheState.setLastUpdated(cal);
            t = s.beginTransaction();
            s.update(cacheState);
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            throw e;
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log newLog) {
        this.log = newLog;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager newManager) {
        this.manager = newManager;
    }
}