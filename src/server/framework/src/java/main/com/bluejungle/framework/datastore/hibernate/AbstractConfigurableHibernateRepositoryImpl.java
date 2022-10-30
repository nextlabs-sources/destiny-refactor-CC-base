/*
 * Created on Aug 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;

import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.configuration.DestinyRepository;

/**
 * This is an abstract class that allows a Hibernate session factory to be
 * configured using 2 input streams - one which is a .properties file and the
 * other which is an XML file containing object mappings. It allows concrete
 * subclasses to add additional configuration, as necessary. The configuration
 * setup by default via this class is NOT generally sufficient to create a
 * session factory from.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/AbstractConfigurableHibernateRepositoryImpl.java#1 $
 */

public abstract class AbstractConfigurableHibernateRepositoryImpl extends BaseHibernateRepositoryImpl {

    private static final long serialVersionUID = 1L;
    
    private static final String ERR_LOAD_COMMON_IS = "Unable to load %s for Hibernate data source '%s'";
    private static final String ERR_UNDEFINE = "'%s' is not definied for Hibernate data source '%s'";
    private static final String ERR_BUILD_SESSION_FACTORY = "Unable to build session factory for Hibernate data source '%s'";
    private static final String ERR_PRE_CUSTOM_CONFIG = "An excpetion occured while doing custom configuration for Hibernate data source '%s'";
    private static final String ERR_POST_CUSTOM_CONFIG = "An excpetion occured while doing post custom configuration for Hibernate data source '%s'";
    private static final String ERR_CONFIGURING = "An excpetion occured while configuring for Hibernate data source '%s'";
    
    /*
     * Required for setting up a HibernateConfiguration instance using an input
     * stream for the XML configuration
     */
    private static final String CONTAINER_CONFIGURATION_XML = "ContainerConfigurationXML";

    /*
     * Parameters accessed via the IConfiguration configuration
     */
    public static final PropertyKey<InputStream> COMMON_CONFIG = 
        new PropertyKey<InputStream>(AbstractConfigurableHibernateRepositoryImpl.class, "CommonConfiguration");
    public static final PropertyKey<InputStream> REPOSITORY_CONFIG = 
        new PropertyKey<InputStream>(AbstractConfigurableHibernateRepositoryImpl.class, "RepositoryConfiguration");

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public final void init() {
        // Obtain the configuration from different sources:
        InputStream commonConfigInputStream = getConfiguration().get(
                AbstractConfigurableHibernateRepositoryImpl.COMMON_CONFIG);
        InputStream repositoryConfigInputStream = getConfiguration().get(
                AbstractConfigurableHibernateRepositoryImpl.REPOSITORY_CONFIG);
        
        DestinyRepository repositoryEnum = getConfiguration().get(REPOSITORY_ENUM_PARAM);
        String repositoryName = repositoryEnum != null ? repositoryEnum.getName() : "<NO NAME>";
        
        if(repositoryConfigInputStream == null) {
            throw new NullPointerException(String.format(ERR_UNDEFINE, "repositoryConfigInputStream", repositoryName));
        }
        
        if(commonConfigInputStream == null) {
            throw new NullPointerException(String.format(ERR_UNDEFINE, "commonConfigInputStream", repositoryName));
        }
        
        setThreadLocalSession(new ThreadLocal());
        
        
        // Initialize the hibernate configuration in 5 steps:
        // 1. Set the common repository properties
        // 2. Set the container-specific properties
        // 3. Do advanced configuration - implementation specific
        
        
        HibernateConfiguration hibernateCfg = new HibernateConfiguration();

        // 1. Do the common configuration:
        Properties hibernateProperties = new Properties();
        try{ 
            hibernateProperties.load(commonConfigInputStream);
        } catch (IOException e) {
            throw new RuntimeException(String.format(ERR_LOAD_COMMON_IS, "commonConfigInputStream", repositoryName, e));
        }
        hibernateCfg.setProperties(hibernateProperties);

        // 2. Do the repository-specific configuration:
        hibernateCfg.setConfigurationInputStream(CONTAINER_CONFIGURATION_XML, repositoryConfigInputStream);
        try {
            hibernateCfg.configure(CONTAINER_CONFIGURATION_XML);
        } catch (HibernateException e) {
            throw new RuntimeException(String.format(ERR_CONFIGURING, repositoryName), e);
        }

        // 3. Do any advanced configuration:
        try {
            doCustomConfiguration(hibernateCfg);
        } catch (Exception e) {
            throw new RuntimeException(String.format(ERR_PRE_CUSTOM_CONFIG, repositoryName), e);
        }
        
        try {
            setSessionFactory(hibernateCfg.buildSessionFactory());
        } catch (HibernateException e) {
            throw new RuntimeException(String.format(ERR_BUILD_SESSION_FACTORY, repositoryName), e); 
        }
        
        // the connection will be closed if after net.sf.hibernate.tool.hbm2ddl.SchemaUpdate
        // I need to reopen it
        // 
        try {
            doPostCustomConfiguration(hibernateCfg);
        } catch (Exception e) {
            throw new RuntimeException(String.format(ERR_POST_CUSTOM_CONFIG, repositoryName), e);
        }
    }
    
    protected void doPostCustomConfiguration(HibernateConfiguration hibernateCfg) throws Exception {
        
    }

    /**
     * This method allows concrete subclases to perform any additional
     * configuration/setup
     * 
     * @param hibernateCfg
     * @throws Exception
     */
    protected abstract void doCustomConfiguration(HibernateConfiguration hibernateCfg) throws Exception;

    /**
     * @see com.bluejungle.framework.datastore.hibernate.BaseHibernateRepositoryImpl#getSession(net.sf.hibernate.Interceptor)
     */
    public Session getSession(Interceptor interceptor) throws HibernateException {
        return this.sessionFactory.openSession(interceptor);
    }

    /**
     * @see com.bluejungle.framework.datastore.hibernate.IHibernateRepository#getSession()
     */
    public Session getSession() throws HibernateException {
        return this.sessionFactory.openSession();
    }

    /**
     * @see net.sf.hibernate.SessionFactory#openSession(net.sf.hibernate.Interceptor)
     */
    public Session openSession(Interceptor interceptor) throws HibernateException {
        return this.sessionFactory.openSession(interceptor);
    }

    /**
     * @see net.sf.hibernate.SessionFactory#openSession()
     */
    public Session openSession() throws HibernateException {
        return this.sessionFactory.openSession();
    }
}