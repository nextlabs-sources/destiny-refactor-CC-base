/*
 * Created on Mar 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.PolicyDO;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.pf.destiny.lifecycle.DeploymentEntity;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;

/**
 * This is the policy cache implementation class. The policy cache manages
 * cached policies which definitions comes from the policy framework. The policy
 * framework provides API to retrieve the list of policy names and ids. If
 * policies have been stored in the database for too long, then a fresh list is
 * pulled from the policy framework API.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/PolicyCacheImpl.java#1 $
 */

public class PolicyCacheImpl extends BaseResourceCacheImpl {

    /**
     * Size of the batch to flush
     */
    protected static final long FLUSH_THRESHOLD = 100;
    protected static final String SLASH = "/";

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl.BaseResourceCacheImpl#getResourceCacheType()
     */
    protected ResourceCacheType getResourceCacheType() {
        return ResourceCacheType.POLICY;
    }

    /**
     * In this implementation, upon invalidation, the policy cache deletes the
     * records stored in the policy cache table and repopulates the table with
     * the latest data taken from PF.
     * 
     * @see com.bluejungle.destiny.container.shared.inquirymgr.resourceCache.IResourceCache#invalidate()
     */
    public void invalidate() {
        Session s = null;
        Transaction t = null;
        try {
            s = getDataSource().getSession();
            t = s.beginTransaction();
            refreshData(s, new Date());
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            getLog().error("Hibernate exception occured when invalidating policy cache", e);
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.IResourceCache#refresh()
     */
    public void refresh() {
        Session s = null;
        Transaction t = null;
        try {
            Date updateTime = new Date();
            s = getDataSource().getSession();
            t = s.beginTransaction();
            refreshData(s, updateTime);
            t.commit();
            setLastUpdateTime(updateTime);
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
    }

    /**
     * Refreshes the data in the database by pulling fresh data from the policy
     * framework. Since we may be dealing with pretty large quantities of data,
     * the session could be flushed and some objects may be evicted as the
     * processing of data goes on.
     * 
     * @param session
     *            hibernate session to use
     * @param asOf
     *            date that should be used to fetch policies from the policy
     *            framework. Generally, this will be "now".
     * @throws HibernateException
     *             if database level operation fails
     */
    protected void refreshData(Session session, Date asOf) throws HibernateException {
        LifecycleManager lm = (LifecycleManager) getManager().getComponent(LifecycleManager.COMP_INFO);
        try {
            Collection deployedPolicies = lm.getAllDeployedEntities(EntityType.POLICY, asOf, DeploymentType.PRODUCTION);
            Iterator it = deployedPolicies.iterator();
            long recordCount = 0;
            List evictionList = new ArrayList();
            while (it.hasNext()) {
                DeploymentEntity policy = (DeploymentEntity) it.next();
                if (!policy.isHidden()){
                    Long id = policy.getDevelopmentEntity().getId();
                    String name = policy.getName();
                    if (!name.startsWith(SLASH)) {
                        name = SLASH + name;
                    }
                    PolicyDO updatedPolicy = new PolicyDO();
                    updatedPolicy.setId(id);
                    updatedPolicy.setFullName(name);

                    updatedPolicy = (PolicyDO) session.saveOrUpdateCopy(updatedPolicy);
                    recordCount++;
                    evictionList.add(updatedPolicy);
                    if (recordCount % FLUSH_THRESHOLD == 0) {
                        session.flush();
                        HibernateUtils.evictObjects(session, evictionList);
                    }
                }
            }
        } catch (EntityManagementException e) {
            getLog().error("Error while fetching the list of deployed policies", e);
            throw new HibernateException(e);
        }
    }
}