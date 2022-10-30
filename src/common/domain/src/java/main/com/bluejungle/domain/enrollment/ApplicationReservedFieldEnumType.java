package com.bluejungle.domain.enrollment;

/*
 * Created on May 12, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *
 * @author safdar, atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/bluejungle/domain/enrollment/ApplicationReservedFieldEnumType.java#1 $
 */

//import com.bluejungle.dictionary.ElementFieldType;
import com.bluejungle.framework.patterns.EnumBase;

/**
 * This enumeration defines application reserved fields.
 */
public class ApplicationReservedFieldEnumType extends EnumBase {
    private static final long serialVersionUID = 1L;

    public static final ApplicationReservedFieldEnumType UNIQUE_NAME = new ApplicationReservedFieldEnumType("uniqueName") {
        private static final long serialVersionUID = 1L;
    };
    public static final String UNIQUE_NAME_LABEL = "Unique Name";

    public static final ApplicationReservedFieldEnumType DISPLAY_NAME = new ApplicationReservedFieldEnumType("displayName") {
        private static final long serialVersionUID = 1L;
    };
    public static final String DISPLAY_NAME_LABEL = "Display Name";

	public static final ApplicationReservedFieldEnumType APP_FINGER_PRINT = new ApplicationReservedFieldEnumType("appFingerPrint") {
        private static final long serialVersionUID = 1L;
    };
    public static final String APP_FINGER_PRINT_LABEL = "Application Finger Print";

    public static final ApplicationReservedFieldEnumType SYSTEM_REFERENCE = new ApplicationReservedFieldEnumType("systemReference") {
        private static final long serialVersionUID = 1L;
    };
    public static final String SYSTEM_REFERENCE_LABEL = "System Name";

    /**
     * Constructor
     * 
     * @param name
     */
    public ApplicationReservedFieldEnumType(String name) {
        super(name);
    }

}
