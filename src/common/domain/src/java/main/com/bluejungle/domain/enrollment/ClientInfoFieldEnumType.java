/*
 * Created on Mar 25, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.enrollment;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/bluejungle/domain/enrollment/ClientInfoFieldEnumType.java#1 $
 */

public class ClientInfoFieldEnumType extends EnumBase {
	private static final long serialVersionUID = 1L;

	public static final String IDENTIFIER_LABEL = "Identifier";
	public static final ClientInfoFieldEnumType IDENTIFIER = new ClientInfoFieldEnumType("identifier") {
        private static final long serialVersionUID = 1L;
    };
    
    public static final String SHORT_NAME_LABEL = "Short name";
	public static final ClientInfoFieldEnumType SHORT_NAME = new ClientInfoFieldEnumType("shortName") {
        private static final long serialVersionUID = 1L;
    };
    
    public static final String LONG_NAME_LABEL = "Long name";
	public static final ClientInfoFieldEnumType LONG_NAME = new ClientInfoFieldEnumType("longName") {
        private static final long serialVersionUID = 1L;
    };
    
    public static final String EMAIL_TEMPLATES_LABEL = "Email Templates";
	public static final ClientInfoFieldEnumType EMAIL_TEMPLATES = new ClientInfoFieldEnumType("emailTemplates") {
        private static final long serialVersionUID = 1L;
    };
    
    public static final String USER_NAMES_LABEL = "User names";
	public static final ClientInfoFieldEnumType USER_NAMES = new ClientInfoFieldEnumType("userNames") {
        private static final long serialVersionUID = 1L;
    };

    public ClientInfoFieldEnumType(String name) {
        super(name);
    }
}
