/*
 * Created on Dec 8, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;


/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/src/java/test/com/bluejungle/destiny/server/shared/registration/MockDCCComponent.java#2 $:
 */

public class MockDCCComponent implements IRegisteredDCCComponent {

    ServerComponentType compType;

    /**
     * Constructor
     * 
     * @param type
     *            type of the DCC component
     *  
     */
    public MockDCCComponent(ServerComponentType type) {
        super();
        this.compType = type;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent#getComponentType()
     */
    public ServerComponentType getComponentType() {
        return this.compType;
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent#getComponentName()
     */
    public String getComponentName() {
        return "";
    }
}
