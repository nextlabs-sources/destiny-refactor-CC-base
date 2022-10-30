/*
 * Created on Aug 10, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.HostDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.UserDO;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.seed.SeedDataTaskException;
import com.bluejungle.framework.datastore.hibernate.seed.seedtasks.SeedDataTaskBase;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import java.util.Calendar;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/ResourceHistorySeedDataTask.java#1 $
 */

public class ResourceHistorySeedDataTask extends SeedDataTaskBase {

    private static TimeRelation ALWAYS_ACTIVE_TIME_RELATION = new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME);
    
    /**
     * @see com.bluejungle.destiny.tools.dbinit.seedtasks.SeedDataTaskBase#execute()
     */
    public void execute() throws SeedDataTaskException {
        try {
            insertSystemHosts();
            insertSystemUsers();
            initializeResourceCacheState();
        } catch (HibernateException exception) {
            throw new SeedDataTaskException("Failed to initialize resource history", exception);
        }
    }

    /**
     * @throws HibernateException 
     * 
     */
    private void insertSystemHosts() throws HibernateException {
        HostDO unknowHost = new HostDO();
        unknowHost.setId(IHasId.UNKNOWN_ID);
        unknowHost.setOriginalId(IHasId.UNKNOWN_ID);
        unknowHost.setName("?");
        unknowHost.setTimeRelation(ALWAYS_ACTIVE_TIME_RELATION);
        
        insertPersistentObject(unknowHost);
    }

    /**
     * @throws HibernateException 
     * 
     */
    private void insertSystemUsers() throws HibernateException {
        UserDO unknowUser = new UserDO();        
        unknowUser.setOriginalId(IHasId.UNKNOWN_ID);
        unknowUser.setFirstName("User");
        unknowUser.setLastName("Unknown");
        unknowUser.setDisplayName(IHasId.UNKNOWN_NAME);
        unknowUser.setSID("000-0000-2");
        unknowUser.setTimeRelation(ALWAYS_ACTIVE_TIME_RELATION);
        insertPersistentObject(unknowUser);
        
        UserDO systemUser = new UserDO();
        systemUser.setOriginalId(IHasId.SYSTEM_USER_ID);
        systemUser.setFirstName("User");
        systemUser.setLastName("System");
        systemUser.setDisplayName(IHasId.SYSTEM_USER_NAME);
        systemUser.setSID("000-0000-1");
        systemUser.setTimeRelation(ALWAYS_ACTIVE_TIME_RELATION);
        insertPersistentObject(systemUser);        
    }

    /**
     * @throws HibernateException
     * 
     */
    private void initializeResourceCacheState() throws HibernateException {
        initializeResourceCacheState(ResourceCacheType.APPLICATION);
        initializeResourceCacheState(ResourceCacheType.HOST);
        initializeResourceCacheState(ResourceCacheType.POLICY);
        initializeResourceCacheState(ResourceCacheType.USER);
    }

    /**
     * @throws HibernateException
     * 
     */
    private void initializeResourceCacheState(ResourceCacheType type) throws HibernateException {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        ResourceCacheStateDO resourceCacheStateDO = new ResourceCacheStateDO();
        resourceCacheStateDO.setLastUpdated(cal);
        resourceCacheStateDO.setType(type);

        insertPersistentObject(resourceCacheStateDO);
    }

    /**
     * @param objectToPersist
     * @throws HibernateException
     */
    private void insertPersistentObject(Object objectToPersist) throws HibernateException {
        Session s = null;
        Transaction t = null;
        try {
            s = getHibernateDataSource().getSession();
            t = s.beginTransaction();
            s.save(objectToPersist);
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, getLog());
            throw e;
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
    }
}
