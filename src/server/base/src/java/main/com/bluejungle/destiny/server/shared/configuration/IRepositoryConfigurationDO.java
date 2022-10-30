/*
 * Created on Feb 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration;

import java.util.Properties;

/**
 * This interface represents a data source configuration domain object.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/configmgr/IDataSourceConfigurationDO.java#3 $
 */

public interface IRepositoryConfigurationDO /* extends IDomainObject */{

    /**
     * Returns the name of this data source
     * 
     * @return name
     */
    public String getName();

    /**
     * Returns the connection pool configuration referenced by this repository
     * 
     * @return connection pool configuration
     */
    public IConnectionPoolConfigurationDO getConnectionPoolConfiguration();

    /**
     * Returns the list of properties that are being overridden for this
     * datasource
     * 
     * @return list of property overrides
     */
    public Properties getProperties();
}