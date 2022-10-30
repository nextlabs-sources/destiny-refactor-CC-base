/*
 * Created on Dec 9, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus;
import com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener;

/**
 * This is a dummy DMS registration listener
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/src/java/test/com/bluejungle/destiny/container/dcsf/MockDMSRegistrationListener.java#1 $:
 */

public class MockDMSRegistrationListener implements IDMSRegistrationListener {

    private IDCCRegistrationStatus registrationStatus;

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener#onDMSRegistration(com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus)
     */
    public void onDMSRegistration(IDCCRegistrationStatus status) {
        this.registrationStatus = status;
    }

    /**
     * Returns the registration Status.
     * 
     * @return the registration Status.
     */
    public IDCCRegistrationStatus getRegistrationStatus() {
        return this.registrationStatus;
    }
}