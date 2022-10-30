/*
 * Created on Oct 25, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.compmgr;

import java.util.Calendar;
import java.util.List;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Expression;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dcc.DCCComponentEnumType;
import com.bluejungle.destiny.container.dms.data.ComponentDO;
import com.bluejungle.destiny.container.dms.data.DCCComponentEnumUserType;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * CRUD library implementation for the Component Manager
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/DCCComponentMgrImpl.java#9 $
 */
public class DCCComponentMgrImpl implements IDCCComponentMgr {

    /**
     * Query strings
     */
    private static final String QUERY_BY_NAME = "from ComponentDO c where c.name=:name";
    private static final String QUERY_ALL_BY_TYPE = "from ComponentDO c where c.type=:type";
    private static final String QUERY_DCSF_BY_CALLBACK_URL = "from ComponentDO c where c.type=:type and c.callbackURL=:callbackURL";
    private static final String QUERY_ALL = "from ComponentDO";

    private IHibernateRepository dataSource;
    private Log log;

    /**
     * Init
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        this.dataSource = (IHibernateRepository) compMgr.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());

        if (this.dataSource == null) {
            throw new RuntimeException("Required datasource '" + DestinyRepository.MANAGEMENT_REPOSITORY + "' is not initialized.");
        }
    }

    /**
     * Dispose
     * 
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        this.dataSource = null;
        this.log = null;
    }

    /**
     * Set the log
     * 
     * @param Log
     *            the logger object
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * Get the log
     * 
     * @return The logger object
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * Register a DCC component. Also makes sure that there is no *other* DCSF
     * component with the same callback URL (Note: The callback URL is not a
     * unique key since different co-located DCC components will have the same
     * callback URL - so for the DCSF case the uniqueness needs to be
     * programmatically ensured).
     * 
     * @param regInfo
     *            registration information
     * @throws DataSourceException
     */
    public void enableComponent(ComponentDO newComponent) throws DataSourceException {
        Session session = null;
        try {
            session = this.dataSource.getCountedSession();

            try {
                // We need to make sure there is no other DCSF component with the
                // same callback URL as this one:
                if (newComponent.getType().getName().equals(ServerComponentType.DCSF.getName())) {
                    ComponentDO existingDCSF = this.getDCSFByCallbackURL(newComponent.getCallbackURL());
                    if (existingDCSF != null) {
                        // If such a dcsf already exists, and it is not the same
                        // dcsf component as this one, we throw an exception:
                        if (!existingDCSF.getName().equals(newComponent.getName())) {
                            throw new DataSourceException();
                        }
                    }
                }
            
                // Get rid of old versions of this object
                session.evict(newComponent);

                // If all goes well, we commit:
                session.saveOrUpdate(newComponent);
            } finally {
                this.dataSource.closeCurrentSession();
            }
        } catch (HibernateException e) {
            throw new DataSourceException(e);
        }
    }

    /**
     * Unregister a DCC component
     * 
     * @param regInfo
     *            registration information
     */
    public void disableComponent(ComponentDO component) throws DataSourceException {
        Session session = null;

        try {
            session = this.dataSource.getCountedSession();

            try {
                // If component exists and is persisted:
                if ((component != null) && (component.getId() != null)) {
                    component.setHeartbeatRate(0);
                    session.saveOrUpdate(component);
                } else {
                    this.log.warn("Component cannot be disabled because it either doesn't exist or is not a persisted object.");
                }
            } finally {
                this.dataSource.closeCurrentSession();
            }
        } catch (HibernateException e) {
            throw new DataSourceException(e);
        }
    }

    /**
     * Register a heartbeat from a DCC component.
     * 
     * @param info -
     *            heartbeat info
     * @throws DataSourceException
     * @see com.bluejungle.destiny.container.dms.IDCCComponentMgr#confirmActive(com.bluejungle.destiny.services.management.types.ComponentHeartbeatInfo)
     */
    public void confirmActive(IComponentHeartbeatInfo info) throws DataSourceException {
        this.log.info("DMS received heartbeat from " + info.getComponentName());
        Session session = null;
        ComponentDO component = null;

        try {
            session = this.dataSource.getCountedSession();
            try {
                component = this.getComponentByName(info.getComponentName());
                
                // Set the last heartbeat time:
                if (component != null) {
                    component.setLastHeartbeat(Calendar.getInstance());
                    session.saveOrUpdate(component);
                } else {
                    this.log.warn("Component '" + info.getComponentName() + "' sent a heartbeat but is not a registered DCC component.");
                }
            } finally {
                this.dataSource.closeCurrentSession();
            }
        } catch (HibernateException e) {
            throw new DataSourceException(e);
        }
    }

    /**
     * Returns a list of DCSF components. This API allows to locate all the DCSF
     * modules within the DCC unit
     * 
     * @return a list of DCSF components.
     * @throws DataSourceException
     */
    public List<IDCCComponentDO> getDCSFs() throws DataSourceException {
        Session session = null;
        List<IDCCComponentDO> result = null;
        try {
            session = this.dataSource.getCountedSession();
            try {
                final Query q = session.createQuery(QUERY_ALL_BY_TYPE);
                q.setParameter("type", DCCComponentEnumType.DCSF, Hibernate.custom(DCCComponentEnumUserType.class));
                result = q.list();
            } finally {
                this.dataSource.closeCurrentSession();
            }
        } catch (HibernateException e) {
            throw new DataSourceException(e);
        }

        return result;
    }

    /**
     * Returns the component with the given name.
     * 
     * @param name
     * @return DCC Component
     * @throws DataSourceException
     * @see com.bluejungle.destiny.container.dms.IDCCComponentMgr#getComponentByName(java.lang.String)
     */
    public ComponentDO getComponentByName(String name) throws DataSourceException {
        Session session = null;
        ComponentDO component = null;

        try {
            session = this.dataSource.getCountedSession();
            try {
                component = (ComponentDO) session.createQuery(QUERY_BY_NAME).setString("name", name).uniqueResult();
            } finally {
                this.dataSource.closeCurrentSession();
            }
        } catch (HibernateException e) {
            throw new DataSourceException(e);
        }

        return component;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.compmgr.IDCCComponentMgr#getComponents(java.lang.String)
     */
    public List<IDCCComponentDO> getComponents() throws DataSourceException {
        Session session = null;
        List<IDCCComponentDO> results = null;
        try {
            session = this.dataSource.getCountedSession();
            try {
                results = session.createQuery(QUERY_ALL).list();
            } finally {
                this.dataSource.closeCurrentSession();
            }
        } catch (HibernateException e) {
            throw new DataSourceException(e);
        }
          
        return results;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.compmgr.IDCCComponentMgr#getComponentByType(com.bluejungle.destiny.container.dcc.DCCComponentEnumType)
     */
    public List<IDCCComponentDO> getComponentByType(DCCComponentEnumType type) throws DataSourceException {
        Session session = null;
        List<IDCCComponentDO> results = null;
        try {
            session = this.dataSource.getCountedSession();
            try {
                Criteria crit = session.createCriteria(ComponentDO.class);
                crit.add(Expression.eq("type", type));
                results = crit.list();
            } finally {
                this.dataSource.closeCurrentSession();
            }
        } catch (HibernateException e) {
            throw new DataSourceException(e);
        }
        return results;
    }

    /**
     * Returns the DCSF component that has the given callback url.
     * 
     * @param url
     * @return DCSF Component
     * @throws DataSourceException
     * @see com.bluejungle.destiny.container.dms.IDCCComponentMgr#getDCSFByCallbackURL(java.lang.String)
     */
    public ComponentDO getDCSFByCallbackURL(String url) throws DataSourceException {
        Session session = null;
        ComponentDO component = null;

        try {
            session = this.dataSource.getCountedSession();
            try {
                final Query q = session.createQuery(QUERY_DCSF_BY_CALLBACK_URL);
                q.setParameter("type", DCCComponentEnumType.DCSF, Hibernate.custom(DCCComponentEnumUserType.class));
                q.setString("callbackURL", url);
                component = (ComponentDO) q.uniqueResult();
            } finally {
                this.dataSource.closeCurrentSession();
            }
        } catch (HibernateException e) {
            throw new DataSourceException(e);
        }
        return component;
    }

    /**
     * Cleans the database tables associated with component registration.
     * Currently the only use for this is with JUnit testing. TODO: This doesn't
     * work!
     * 
     * @throws DataSourceException
     */
    public void clearAll() throws DataSourceException {
        Session session = null;
        ComponentDO lastComponent = null;
        try {
            session = this.dataSource.getCountedSession();
            try {
                List<ComponentDO> components = session.createQuery(QUERY_ALL).list();
                for (ComponentDO component : components) {
                    lastComponent = component;
                    session.delete(component);
                }
            } finally {
                this.dataSource.closeCurrentSession();
            }
        } catch (HibernateException e) {
            Object[] values = null;
            if (lastComponent != null) {
                values = new Object[] { lastComponent.getClass(), lastComponent.getId() };
            }
            throw new DataSourceException(e);
        }
    }
}
