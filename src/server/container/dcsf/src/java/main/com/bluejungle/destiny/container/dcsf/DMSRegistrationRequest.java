/*
 * Created on Dec 2, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener;
import com.bluejungle.framework.threading.ITask;

/**
 * This class is used to pass a DMS registration request or a DMS unregistration
 * request to the DMS registration worker object
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/DMSRegistrationRequest.java#3 $:
 */

public class DMSRegistrationRequest implements ITask {

    private IDCCRegistrationInfo registrationInfo;
    private IDMSRegistrationListener listener;
    private boolean registration;

    /**
     * Constructor
     * 
     * @param listener
     *            callback listener
     * @param registrationInformation
     *            information for registration
     */
    public DMSRegistrationRequest(IDCCRegistrationInfo registrationInformation, IDMSRegistrationListener listener) {
        super();
        this.listener = listener;
        this.registrationInfo = registrationInformation;
        this.registration = true;
    }

    /**
     * This constructor is used for unregistration purposes.
     * 
     * @param unregInfo
     *            partial registration information to unregister the component
     */
    public DMSRegistrationRequest(IDCCRegistrationInfo unregInfo) {
        this.listener = null;
        this.registrationInfo = unregInfo;
        this.registration = false;
    }

    /**
     * Returns the DMS registration listener
     * 
     * @return the DMS registration listener
     */
    public IDMSRegistrationListener getListener() {
        return this.listener;
    }

    /**
     * Returns the registration information
     * 
     * @return the registration information
     */
    public IDCCRegistrationInfo getRegistrationInfo() {
        return this.registrationInfo;
    }

    /**
     * Returns true if this operation is to register registration, false if it
     * is to unregister
     * 
     * @return the registration flag.
     */
    public boolean isRegistration() {
        return this.registration;
    }
}