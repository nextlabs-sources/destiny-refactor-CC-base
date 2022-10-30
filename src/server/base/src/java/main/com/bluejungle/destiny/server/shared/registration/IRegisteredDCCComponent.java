/*
 * Created on Oct 25, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.destiny.server.shared.registration;


/**
 * This interface is implemented by all DCC components that want to register
 * with the DCSF shared context.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/registration/IRegisteredDCCComponent.java#1 $
 */
public interface IRegisteredDCCComponent {

    /**
     * Name of the registration status config parameter
     */
    public static final String DMS_REGISTRATION_STATUS_CONFIG_PARAM = "DMSRegistrationStatus";

    /**
     * Returns the DCC component type
     * 
     * @return the DCC component type
     */
    public ServerComponentType getComponentType();

    /**
     * Returns the DCC component name
     * 
     * @return the DCC component name
     */
    public String getComponentName();
}