package com.bluejungle.pf.destiny.services;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $ Id: $
 */

import com.bluejungle.destiny.appframework.appsecurity.axis.ISecureSessionVault;
import com.bluejungle.destiny.types.secure_session.v1.SecureSession;

/**
 * A trivial implementation of ISecureSessionVault.
 */
public class SecureSessionVault implements ISecureSessionVault {

    private SecureSession secureSession;

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
        return secureSession;
    }

    /**
     * @see com.bluejungle.destiny.appframework.appsecurity.axis.ISecureSessionVault#clearSecureSession()
     */
    public void clearSecureSession() {
        secureSession = null;
    }

}
