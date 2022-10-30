/*
 * Created on Oct 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/core/AuthenticationModeEnumType.java#1 $
 */

public class AuthenticationModeEnumType extends EnumBase {

    public static final AuthenticationModeEnumType LOCAL = new AuthenticationModeEnumType("Local");
    public static final AuthenticationModeEnumType REMOTE = new AuthenticationModeEnumType("Remote");
    public static final AuthenticationModeEnumType HYBRID = new AuthenticationModeEnumType("Hybrid");

    /**
     * Constructor
     * 
     * @param modeValue
     */
    public AuthenticationModeEnumType(String modeValue) {
        super(modeValue);
    }

    /**
     * @see com.bluejungle.framework.patterns.IEnum#getName()
     */
    public String getName() {
        return super.getName();
    }

    /**
     * Retrieves an enum by reverse-lookup of the mode
     * 
     * @param mode
     * @return
     */
    public static AuthenticationModeEnumType getByName(String mode) {
        return EnumBase.getElement(mode, AuthenticationModeEnumType.class);
    }
}