/*
 * Created on Aug 5, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.repository;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/repository/ConnectionPoolConfigurationException.java#1 $
 */

public class ConnectionPoolConfigurationException extends Exception {

    /**
     * Constructor
     *  
     */
    public ConnectionPoolConfigurationException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param arg0
     */
    public ConnectionPoolConfigurationException(String arg0) {
        super(arg0);
    }

    /**
     * Constructor
     * 
     * @param arg0
     * @param arg1
     */
    public ConnectionPoolConfigurationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * Constructor
     * 
     * @param arg0
     */
    public ConnectionPoolConfigurationException(Throwable arg0) {
        super(arg0);
    }
}