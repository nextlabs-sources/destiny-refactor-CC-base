/*
 * Created on May 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.seed.seedtasks;

import java.sql.Connection;
import java.sql.SQLException;

import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.seed.ISeedDataTask;
import com.bluejungle.framework.datastore.hibernate.seed.SeedDataTaskException;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;

/**
 * This is the base implementation class for the seed data. Seed data tasks will
 * typically extend this class.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/seed/seedtasks/SeedDataTaskBase.java#1 $
 */

public abstract class SeedDataTaskBase implements ISeedDataTask, IConfigurable, ILogEnabled, IInitializable, IManagerEnabled, IDisposable {

    private IConfiguration configuration;
    private IHibernateRepository hibernateDataSource;
    private IComponentManager manager;
    private Log log;

    public abstract class MicroTask {
        public void run() throws SeedDataTaskException{
            Session session = null;
            try {
                session = getHibernateDataSource().getSession();
                Connection connection = session.connection();
                
                run(connection);
                
                //have to commit, otherwise the changes doesn't go to database
                connection.commit();
            } catch (SQLException e) {
                throw new SeedDataTaskException(e);
            } catch (HibernateException e) {
                throw new SeedDataTaskException(e);
            } finally {
                HibernateUtils.closeSession(session, getLog());
            }
        }
        
        public abstract void run(Connection connection) throws SeedDataTaskException, SQLException,
                HibernateException;
    }
    
    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
    }

    /**
     * @see com.bluejungle.destiny.tools.dbinit.ISeedDataTask#execute()
     */
    public abstract void execute() throws SeedDataTaskException;

    /**
     * Returns the hibernate data source that can be used by the components
     * 
     * @return the hibernate data source
     */
    protected IHibernateRepository getHibernateDataSource() {
        return this.hibernateDataSource;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    public Log getLog() {
        return this.log;
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
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        this.hibernateDataSource = getConfiguration().get(HIBERNATE_DATA_SOURCE_CONFIG_PARAM);
        if (this.hibernateDataSource == null) {
            throw new NullPointerException("Hibernate data source needs to be set in the configuration");
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration newConfig) {
        this.configuration = newConfig;
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
    public void setManager(IComponentManager newMgr) {
        this.manager = newMgr;
    }
}