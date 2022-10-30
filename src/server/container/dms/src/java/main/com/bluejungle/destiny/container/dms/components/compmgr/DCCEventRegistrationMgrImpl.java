/*
 * Created on Oct 26, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.destiny.container.dms.components.compmgr;

import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Criterion;
import net.sf.hibernate.expression.Expression;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dms.data.ComponentDO;
import com.bluejungle.destiny.container.dms.data.EventDO;
import com.bluejungle.destiny.container.dms.data.EventRegistration;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * CRUD library interface for the logical Event Registration Manager
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/DCCEventRegistrationMgrImpl.java#7 $
 */
public class DCCEventRegistrationMgrImpl implements IDCCEventRegistrationMgr {

    private static final String QUERY_BY_NAME = "from EventDO e where e.name=:name";
    private static final String QUERY_ALL_EVENTS = "from EventDO";
    private static final String QUERY_ALL_REGISTRATIONS_BY_CONSUMER = "from EventRegistration r where r.consumer=:consumer";

    private IHibernateRepository dataSource;
    private Log log;

    /**
     * Constructor
     */
    public DCCEventRegistrationMgrImpl() {
        super();
    }

    /**
     * Init
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        this.dataSource = (IHibernateRepository) compMgr.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());

        if (this.dataSource == null) {
            throw new RuntimeException("Required datasource not initialized for AgentManager.");
        }
    }

    /**
     * Dispose the object
     * 
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        this.dataSource = null;
        this.log = null;
    }

    /**
     * Set the log object
     * 
     * @param log
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * Get the log object
     * 
     * @return log
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * Registers a new event into the system. The DMS container does not
     * directly register for an event, but dispatches the registration request
     * to other DCSF components scattered around the DCC unit. This method
     * gathers the set of unique DCSF instances, and dispatches the registration
     * request. Additionally, the DMS keeps track of which notifications are
     * already registered, so that new DCC components coming up can become
     * listeners as well.
     * 
     * @param eventName
     *            name of the event
     * @param dcsf
     *            dcsf that will consume this event
     * @throws DataSourceException
     */
    public void registerConsumerForEvent(String eventName, ComponentDO dcsf) throws DataSourceException {
        Session session = null;

        try {
            session = this.dataSource.getCurrentSession();

            // Create event if it doesn't exist:
            EventDO event = this.getEventByName(eventName);
            if (event == null) {
                event = new EventDO();
                event.setName(eventName);
            }

            // Save the information:
            session.save(event);

            // Create the registration:
            event.addRegistration(dcsf);
        } catch (HibernateException e) {
            throw new DataSourceException(e);
        }
    }

    /**
     * Unregisters an event from an external listener.
     * 
     * @param eventName
     *            name of the event
     * @param dcsf
     *            dcsf that will consume this event
     * @throws DataSourceException
     */
    public void unregisterConsumerForEvent(String eventName, ComponentDO dcsf) throws DataSourceException {
        Session session = null;

        try {
            session = this.dataSource.getCurrentSession();

            // Create event if it doesn't exist:
            EventDO event = this.getEventByName(eventName);
            if (event != null) {
                // Create the registration:
                event.disableRegistration(dcsf);

                // Save the information:
                session.save(event);
            }
        } catch (HibernateException e) {
            throw new DataSourceException(e);
        }
    }

    /**
     * Unregisters the given consumer from all events in the system.
     * Implementation is trivial - the registrations are obtained from the
     * component and are inactivated.
     * 
     * @param dcsf
     * @throws DataSourceException
     */
    public void unregisterConsumerForAllEvents(ComponentDO dcsf) throws DataSourceException {
        Session session = null;
        try {
            session = this.dataSource.getCurrentSession();
            List<EventRegistration> registrations = session.createQuery(QUERY_ALL_REGISTRATIONS_BY_CONSUMER).setParameter("consumer", dcsf, Hibernate.entity(ComponentDO.class)).list();
            for (EventRegistration reg : registrations) {
                reg.setIsActive(false);
                reg.touch();
                session.save(reg);
            }
        } catch (HibernateException e) {
            throw new DataSourceException(e);
        }
    }

    /**
     * Returns an array of event registrations that occured since 'date'.
     * 
     * @param date
     * @return Array of registrations
     * @throws DataSourceException
     */
    public List<EventRegistration> getRegistrationsSince(long timestamp, ComponentDO forComponent) throws DataSourceException {
        List<EventRegistration> registrations;
        Session session = null;

        try {
            // We create a search spec:
            session = this.dataSource.getCurrentSession();
            Criteria registrationCriteria = session.createCriteria(EventRegistration.class);
            Criterion sinceCriterion = Expression.ge("lastModified", new Long(timestamp));
            registrationCriteria.add(sinceCriterion);

            Criterion exceptForListener = Expression.not(Expression.eq("consumer", forComponent));
            registrationCriteria.add(exceptForListener);
            registrations = registrationCriteria.list();
        } catch (HibernateException e) {
            throw new DataSourceException(e);
        }

        return registrations;
    }

    /**
     * Returns an event object with the given name if it exists
     * 
     * @param name
     * @return event with given name
     * @throws DataSourceException
     */
    protected EventDO getEventByName(String name) throws DataSourceException {
        Session session = null;
        EventDO event;
        try {
            session = this.dataSource.getCurrentSession();
            event = (EventDO) session.createQuery(QUERY_BY_NAME).setString("name", name).uniqueResult();
        } catch (HibernateException e) {
            throw new DataSourceException(e);
        }

        return event;
    }

    /**
     * Cleans the database tables associated with component registration.
     * Currently the only use for this is with JUnit testing.
     * 
     * @throws DataSourceException
     */
    public void clearAll() throws DataSourceException {
        Session session = null;
        try {
            session = this.dataSource.getCurrentSession();
            List<EventDO> allEvents = session.createQuery(QUERY_ALL_EVENTS).list();
            for (EventDO event : allEvents) {
                //event.deleteAllRegistrations();
                //session.save(event);
                session.delete(event);
            }
        } catch (HibernateException e) {
            throw new DataSourceException(e);
        }
    }
}
