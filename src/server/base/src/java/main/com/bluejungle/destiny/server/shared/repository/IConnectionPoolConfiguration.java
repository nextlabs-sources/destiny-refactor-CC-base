/*
 * Created on Aug 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.repository;

import java.util.Properties;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/repository/IConnectionPoolConfiguration.java#1 $
 */

public interface IConnectionPoolConfiguration {

    public String getName();

    public String getUserName();

    public String getPassword();

    public String getJDBCConnectString();

    public String getDriverClassName();
    
    public int getMaxPoolSize();

    public Properties getProperties();
}