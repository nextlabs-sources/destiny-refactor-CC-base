/*
 * Created on May 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.seedtasks;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;

import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.BaseHibernateRepositoryImpl;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * This is a special implementation of the hibernate data source for the seed
 * data tool. This special instance can be passed to the various data components
 * and seed data tasks that insert or manipulate seed data. The initialization
 * for this hibernate data source is different, since the configuration comes
 * from the tool itself.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/seedtasks/SeedHibernateRepositoryImplImpl.java#1 $
 */

public class SeedHibernateDataSourceImpl extends BaseHibernateRepositoryImpl implements IHibernateRepository {

    public static final PropertyKey<SessionFactory> SESSION_FACTORY_CONFIG_PARAM = new PropertyKey<SessionFactory>("sessionFactory");

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        super.setThreadLocalSession(new ThreadLocal());
        //Retrieves the session factory from the configuration
        SessionFactory sf = getConfiguration().get(SESSION_FACTORY_CONFIG_PARAM);
        if (sf == null) {
            throw new NullPointerException("Session factory need to be set in the configuration object");
        }
        setSessionFactory(sf);
    }

    /**
     * @see com.bluejungle.framework.datastore.hibernate.IHibernateRepository#getSession()
     */
    public Session getSession() throws HibernateException {
        return this.sessionFactory.openSession();
    }

    /**
     * @see net.sf.hibernate.SessionFactory#openSession()
     */
    public Session openSession() throws HibernateException {
        return this.sessionFactory.openSession();
    }

    /**
     * @see net.sf.hibernate.SessionFactory#openSession(net.sf.hibernate.Interceptor)
     */
    public Session openSession(Interceptor interceptor) throws HibernateException {
        return this.sessionFactory.openSession(interceptor);
    }

    /**
     * @see com.bluejungle.framework.datastore.hibernate.BaseHibernateRepositoryImpl#getSession(net.sf.hibernate.Interceptor)
     */
    public Session getSession(Interceptor interceptor) throws HibernateException {
        return this.sessionFactory.openSession(interceptor);
    }
}