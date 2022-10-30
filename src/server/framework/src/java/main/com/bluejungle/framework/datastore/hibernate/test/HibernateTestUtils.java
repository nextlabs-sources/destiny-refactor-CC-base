/*
 * Created on Feb 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.test;

import java.io.InputStream;
import java.util.List;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.AbstractConfigurableHibernateRepositoryImpl;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * A utility for performing common hibernate related functions for unit tests
 * 
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/test/HibernateTestUtils.java#1 $
 */
public class HibernateTestUtils {

    /**
     * Creates a data source component
     * 
     * @param commonConfigStream
     *            common configuration file input stream
     * @param repositoryConfigStream
     *            repository configuration file input stream
     * @param repository
     *            data source configuration
     * @param dynamicMappings
     *            list of class objects to be added to the mapping list
     * @return
     */
    public static IHibernateRepository createDataSource(
    		DestinyRepository repositoryEnum,
			InputStream commonConfigStream, 
			InputStream repositoryConfigStream,
			IConfiguration partialRepositoryConfiguration, 
			List<Class<?>> dynamicMappings) {
        HashMapConfiguration completeRepositoryConfiguration = new HashMapConfiguration();
        completeRepositoryConfiguration.override(partialRepositoryConfiguration);
        completeRepositoryConfiguration.setProperty(
        		AbstractConfigurableHibernateRepositoryImpl.COMMON_CONFIG, commonConfigStream);
        completeRepositoryConfiguration.setProperty(
        		TestOrientedHibernateRepositoryImpl.DYNAMIC_MAPPINGS, dynamicMappings);
        completeRepositoryConfiguration.setProperty(
        		AbstractConfigurableHibernateRepositoryImpl.REPOSITORY_CONFIG, repositoryConfigStream);

        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ComponentInfo<TestOrientedHibernateRepositoryImpl> repositoryInfo = 
        	new ComponentInfo<TestOrientedHibernateRepositoryImpl>(repositoryEnum.getName(), 
        		TestOrientedHibernateRepositoryImpl.class, 
        		IHibernateRepository.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		completeRepositoryConfiguration);
        return compMgr.getComponent(repositoryInfo);
    }
}