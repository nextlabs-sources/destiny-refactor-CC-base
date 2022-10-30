/*
 * Created on May 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.seed;

import java.util.Properties;

import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * This interface represents a seed data tasks. A seed data task is supposed to
 * insert seed data inside the database.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/ISeedDataTask.java#1 $
 */

public interface ISeedDataTask extends IConfigurable {

    /**
     * Config parameter name for the hibernate data source
     */
    PropertyKey<IHibernateRepository> HIBERNATE_DATA_SOURCE_CONFIG_PARAM = new PropertyKey<IHibernateRepository>("hbs");

    /**
     * Config parameter name for the config file properties
     */
    PropertyKey<Properties> CONFIG_PROPS_CONFIG_PARAM = new PropertyKey<Properties>("configFileProps");
    
    // FIXME can't use DialectExtended yet since it is not compiled.
    // PropertyKey<DialectExtended> DIALECT_EXTENDED_CONFIG_PARAM = new PropertyKey<DialectExtended>("dialectX");
    String DIALECT_EXTENDED_CONFIG_PARAM = "dialectX";

    /**
     * Execute the seed data task
     * 
     * @throws SeedDataTaskException
     *             if the seed data execution fails.
     */
    void execute() throws SeedDataTaskException;
}