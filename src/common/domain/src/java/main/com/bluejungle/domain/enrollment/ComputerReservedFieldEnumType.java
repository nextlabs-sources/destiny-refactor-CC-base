package com.bluejungle.domain.enrollment;

/*
 * Created on May 12, 2006
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *
 * @author safdar, atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/bluejungle/domain/enrollment/ComputerReservedFieldEnumType.java#1 $
 */

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This enumeration defines host reserved fields.
 */
public class ComputerReservedFieldEnumType extends EnumBase {
    private static final long serialVersionUID = 1L;

    public static final ComputerReservedFieldEnumType DNS_NAME = new ComputerReservedFieldEnumType("dnsName") {
        private static final long serialVersionUID = 1L;
    };
    public static final String DNS_NAME_LABEL = "DNS Host Name";

    public static final ComputerReservedFieldEnumType WINDOWS_SID = new ComputerReservedFieldEnumType("windowsSid") {
        private static final long serialVersionUID = 1L;
    };
    public static final String WINDOWS_SID_LABEL = "Host System ID";

    public static final ComputerReservedFieldEnumType UNIX_ID = new ComputerReservedFieldEnumType("unixId") {
        private static final long serialVersionUID = 1L;
    };
    public static final String UNIX_ID_LABEL = "UNIX Computer ID";

    /**
     * Constructor
     *
     * @param arg0
     */
    public ComputerReservedFieldEnumType(String name) {
        super(name);
    }

}
