/*
 * Created on Mar 25, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.enrollment;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/bluejungle/domain/enrollment/ClientInfoReservedFieldEnumType.java#1 $
 */

public class ClientInfoReservedFieldEnumType extends AbstractFieldEnumType {
	private static final long serialVersionUID = 1L;

	public static final ClientInfoReservedFieldEnumType IDENTIFIER =
            new ClientInfoReservedFieldEnumType("identifier", "Identifier") {
                private static final long serialVersionUID = 1L;
            };

    public static final ClientInfoReservedFieldEnumType SHORT_NAME =
            new ClientInfoReservedFieldEnumType("shortName", "Short name") {
                private static final long serialVersionUID = 1L;
            };

    public static final ClientInfoReservedFieldEnumType LONG_NAME =
            new ClientInfoReservedFieldEnumType("longName", "Long name") {
                private static final long serialVersionUID = 1L;
            };

    public static final ClientInfoReservedFieldEnumType EMAIL_TEMPLATES =
            new ClientInfoReservedFieldEnumType("emailTemplates", "Email Templates") {
                private static final long serialVersionUID = 1L;
            };

    public static final ClientInfoReservedFieldEnumType USER_NAMES =
            new ClientInfoReservedFieldEnumType("userNames", "User names") {
                private static final long serialVersionUID = 1L;
            };
    
    /**
     * @param name
     * @param label
     */
    ClientInfoReservedFieldEnumType(String name, String label) {
        super(name, label);
    }
}
