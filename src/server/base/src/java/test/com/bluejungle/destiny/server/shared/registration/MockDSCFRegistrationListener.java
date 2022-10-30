/*
 * Created on Dec 8, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;

import com.bluejungle.destiny.server.shared.context.IDCSFRegistrationListener;
import com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent;

/**
 * This is a mock class listening to DCSF registration / unregistration event
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/test/com/bluejungle/destiny/server/shared/registration/MockDSCFRegistrationListener.java#1 $:
 */

public class MockDSCFRegistrationListener implements IDCSFRegistrationListener {

    IRegisteredDCSFComponent registeredDCSF;
    
    /**
     * Constructor
     * 
     */
    public MockDSCFRegistrationListener() {
        super();
    }

    /**
     * @param dcsfContainer the DCSF container that registers
     * @see com.bluejungle.destiny.server.shared.context.IDCSFRegistrationListener#onDCSFRegistered(com.bluejungle.destiny.server.shared.registration.IRegisteredDCSFComponent)
     */
    public void onDCSFRegistered(IRegisteredDCSFComponent dcsfContainer) {
        this.registeredDCSF = dcsfContainer;
    }

    /**
     * @param dcsfContainer the DCSF container that unregisters
     * @see com.bluejungle.destiny.server.shared.context.IDCSFRegistrationListener#onDCSFUnRegistered(com.bluejungle.destiny.server.shared.registration.IRegisteredDCSFComponent)
     */
    public void onDCSFUnRegistered(IRegisteredDCSFComponent dcsfContainer) {
        this.registeredDCSF = dcsfContainer;
    }
    
    /**
     * Resets the mock object
     */
    public void reset () {
        this.registeredDCSF = null;
    }
        
    /**
     * Returns the DCSF container  
     * @return the DCSF container
     */
    public IRegisteredDCSFComponent getRegisteredDCSF() {
        return this.registeredDCSF;
    }
}
