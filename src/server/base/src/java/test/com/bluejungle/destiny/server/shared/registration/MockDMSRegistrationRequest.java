/*
 * Created on Dec 8, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;


/**
 * This is a dummy class storing DMS registration requests
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/src/java/test/com/bluejungle/destiny/server/shared/registration/MockDMSRegistrationRequest.java#2 $:
 */

public class MockDMSRegistrationRequest {

    private IDCCRegistrationInfo registrationInfo;
    private IDMSRegistrationListener dmsRegistrationlistener;

    /**
     * Constructor
     * 
     * @param info
     *            registration info
     * @param listener
     *            callback object
     */
    public MockDMSRegistrationRequest(IDCCRegistrationInfo info, IDMSRegistrationListener listener) {
        super();
        this.registrationInfo = info;
        this.dmsRegistrationlistener = listener;
    }

    /**
     * Returns the DMS registration listener
     * 
     * @return the DMS registration listener
     */
    public IDMSRegistrationListener getDmsRegistrationlistener() {
        return this.dmsRegistrationlistener;
    }

    /**
     * Returns the registration info
     * 
     * @return the registration info
     */
    public IDCCRegistrationInfo getRegistrationInfo() {
        return this.registrationInfo;
    }
}