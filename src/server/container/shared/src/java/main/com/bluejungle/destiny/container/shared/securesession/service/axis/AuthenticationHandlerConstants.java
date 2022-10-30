/*
 * Created on Mar 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.service.axis;

/**
 * Constants utilized by the Authentication Handlers
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/securesession/service/axis/AuthenticationHandlerConstants.java#1 $
 */

public interface AuthenticationHandlerConstants {
    public static final String SECURE_SESSION_PROPERTY_NAME = "secureSessionPropertyName";
    
    public static final String SECURE_SESSION_TYPE_NAMESPACE = "http://com.bluejungle/destiny/FIXME";
    public static final String SECURE_SESSION_HEADER_ELEMENT_NAME = "secureSession";    
    public static final String SECURE_SESSION_KEY_ELEMENT_NAME = "key";
        
    public static final String WS_SECURITY_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    public static final String WS_SECURITY_HEADER_ELEMENT_NAME = "Security";
    public static final String WS_SECURITY_USERNAME_TOKEN_ELEMENT_NAME = "UsernameToken";    
    public static final String WS_SECURITY_USERNAME_ELEMENT_NAME = "Username";
    public static final String WS_SECURITY_PASSWORD_ELEMENT_NAME = "Password";
}
