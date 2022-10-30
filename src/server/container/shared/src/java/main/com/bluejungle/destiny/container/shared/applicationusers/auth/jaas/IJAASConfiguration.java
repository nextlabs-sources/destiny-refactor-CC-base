/*
 * Created on Sep 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.auth.jaas;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/auth/jaas/IJAASConfiguration.java#1 $
 */

public interface IJAASConfiguration {

    /**
     * Returns the KDC
     * 
     * @return kdc
     */
    public String getKDC();

    /**
     * Returns the realm
     * 
     * @return realm
     */
    public String getRealm();
}