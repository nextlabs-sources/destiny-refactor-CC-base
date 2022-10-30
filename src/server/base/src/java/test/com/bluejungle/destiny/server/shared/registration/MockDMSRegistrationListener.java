/*
 * Created on Dec 8, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;

/**
 * This is a dummy DMS registration listener
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/src/java/test/com/bluejungle/destiny/server/shared/registration/MockDMSRegistrationListener.java#2 $:
 */

public class MockDMSRegistrationListener implements IDMSRegistrationListener {

    IDCCRegistrationStatus registrationStatus;

    /**
     * Returns the registration status
     * 
     * @return the registration status
     */
    public IDCCRegistrationStatus getRegistrationStatus() {
        return this.registrationStatus;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener#onDMSRegistration(com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus)
     */
    public void onDMSRegistration(IDCCRegistrationStatus status) {
        this.registrationStatus = status;
    }

    /**
     * Resets the mock listener
     */
    public void reset() {
        this.registrationStatus = null;
    }

}