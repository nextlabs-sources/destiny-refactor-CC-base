/*
 * Created on May 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.appsecurity.test;

import com.bluejungle.destiny.appframework.appsecurity.axis.ISecureSessionVault;
import com.bluejungle.destiny.types.secure_session.v1.SecureSession;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/pcv/Nimbus_8.1_bugfixes/main/src/server/apps/appFramework/src/java/test/com/bluejungle/destiny/appframework/appsecurity/test/MockSecureSessionVault.java#1 $
 */

public class MockSecureSessionVault implements ISecureSessionVault {
    private SecureSession secureSession;
    
    /**
     * Create an instance of MockSecureSessionVault
     * 
     */
    public MockSecureSessionVault() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.appframework.appsecurity.axis.ISecureSessionVault#storeSecureSession(com.bluejungle.destiny.types.secure_session.v1.SecureSession)
     */
    public void storeSecureSession(SecureSession secureSession) {
        this.secureSession = secureSession;
    }

    /**
     * @see com.bluejungle.destiny.appframework.appsecurity.axis.ISecureSessionVault#getSecureSession()
     */
    public SecureSession getSecureSession() {
        return this.secureSession;
    }

    /**
     * @see com.bluejungle.destiny.appframework.appsecurity.axis.ISecureSessionVault#clearSecureSession()
     */
    public void clearSecureSession() {
        this.secureSession = null;
    }

}
