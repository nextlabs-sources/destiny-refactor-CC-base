/*
 * Created on Aug 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sf.hibernate.MappingException;
import net.sf.hibernate.cfg.Environment;

import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.datastore.hibernate.AbstractConfigurableHibernateRepositoryImpl;
import com.bluejungle.framework.datastore.hibernate.HibernateConfiguration;

/**
 * This repository implementation is oriented toward JUnit tests. It allows for
 * command-line property overrides as well as dynamic mappings
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/test/TestOrientedHibernateRepositoryImpl.java#1 $
 */

public class TestOrientedHibernateRepositoryImpl extends AbstractConfigurableHibernateRepositoryImpl {

    public static final String CONNECT_STRING_PARAM = "ConnectString";
    public static final String USER_NAME_PARAM = "UserName";
    public static final String PASSWORD_PARAM = "Password";
    public static final String DYNAMIC_MAPPINGS = "DynamicMappings";

    /**
     * @see com.bluejungle.framework.datastore.hibernate.AbstractConfigurableHibernateRepositoryImpl#doCustomConfiguration(com.bluejungle.framework.datastore.hibernate.HibernateConfiguration)
     */
    protected void doCustomConfiguration(HibernateConfiguration hibernateCfg) throws MappingException {
        // Set dynamic mappings, if any:
        List<Class<?>> dynamicMappings = (List<Class<?>>) getConfiguration().get(
						TestOrientedHibernateRepositoryImpl.DYNAMIC_MAPPINGS);
		if (dynamicMappings != null) {
			for (Class clazz : dynamicMappings) {
				hibernateCfg.addClass(clazz);
			}
		}

        // Set command-line property overrides:
        Properties props = new Properties();
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            String propName = (String) entry.getKey();
            if (propName.startsWith("hibernate")) {
                props.put(propName, entry.getValue());
                if (getLog().isInfoEnabled()) {
                    getLog().info("overriding " + propName + " with system property value: " + entry.getValue());
                }
            }
        }
        hibernateCfg.addProperties(props);

        IConfiguration conf = getConfiguration();
        for (String propName : conf.propertySet()) {
            if ( propName != null && propName.startsWith("hibernate.") ) {
                Object val = conf.get( propName );
                if ( val instanceof String ) {
                    hibernateCfg.setProperty( propName, (String) val );
                }
            }
        }

        // Set the username, password, and connectstring
        String connectString = (String) conf.get(CONNECT_STRING_PARAM);
        hibernateCfg.setProperty(Environment.URL, connectString);
        String username = (String) conf.get(USER_NAME_PARAM);
        hibernateCfg.setProperty(Environment.USER, username);
        String password = (String) conf.get(PASSWORD_PARAM);
        hibernateCfg.setProperty(Environment.PASS, password);
    }
}