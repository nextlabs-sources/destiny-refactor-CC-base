/*
 * Created on Mar 13, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.enrollment;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/bluejungle/domain/enrollment/SiteReservedFieldEnumType.java#1 $
 */

public class SiteReservedFieldEnumType extends EnumBase {
    
    private static final long serialVersionUID = 1L;

    public static final SiteReservedFieldEnumType NAME = new SiteReservedFieldEnumType("Name") {
        private static final long serialVersionUID = 1L;
    };
    
    public static final String NAME_LABEL = "Name";

    public static final SiteReservedFieldEnumType IP_ADDRESS = new SiteReservedFieldEnumType("IP") {
        private static final long serialVersionUID = 1L;
    };
    public static final String IP_LABEL = "IP Address";

    /**
     * Constructor
     * 
     * @param name
     */
    public SiteReservedFieldEnumType(String name) {
        super(name);
    }

}
