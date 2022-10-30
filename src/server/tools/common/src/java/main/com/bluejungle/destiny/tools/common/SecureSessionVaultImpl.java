/*
 * Created on Mar 1, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.common;

import com.bluejungle.destiny.appframework.appsecurity.axis.ISecureSessionVault;
import com.bluejungle.destiny.types.secure_session.v1.SecureSession;

/**
 * A simple implementation of the secure session vault (all values are kept in
 * memory)
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/common/src/java/main/com/bluejungle/destiny/tools/common/SecureSessionVaultImpl.java#1 $
 */

public class SecureSessionVaultImpl implements ISecureSessionVault {

    private SecureSession session;

    /**
     * Constructor
     */
    public SecureSessionVaultImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.appframework.appsecurity.axis.ISecureSessionVault#storeSecureSession(com.bluejungle.destiny.types.secure_session.v1.SecureSession)
     */
    public void storeSecureSession(SecureSession sessionToStore) {
        this.session = sessionToStore;
    }

    /**
     * @see com.bluejungle.destiny.appframework.appsecurity.axis.ISecureSessionVault#getSecureSession()
     */
    public SecureSession getSecureSession() {
        return this.session;
    }

    /**
     * @see com.bluejungle.destiny.appframework.appsecurity.axis.ISecureSessionVault#clearSecureSession()
     */
    public void clearSecureSession() {
        storeSecureSession(null);
    }

}
