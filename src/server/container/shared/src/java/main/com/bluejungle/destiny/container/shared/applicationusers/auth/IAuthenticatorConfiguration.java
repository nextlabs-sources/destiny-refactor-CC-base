/*
 * Created on Jun 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.auth;

import java.util.Properties;

/**
 * This interface represents the JAAS module configuration
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/auth/IAuthenticatorConfiguration.java#1 $
 */

public interface IAuthenticatorConfiguration {

    /**
     * Returns the class name of the JAAS module
     * 
     * @return JAAS module class name
     */
    public String getAuthenticatorClassName();

    /**
     * Returns the properties that the JAAS module should be configured with
     * 
     * @return JAAS properties
     */
    public Properties getAuthenticatorProperties();
}