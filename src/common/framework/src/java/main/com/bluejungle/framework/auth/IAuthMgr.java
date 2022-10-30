/*
 * Created on Feb 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.auth;

/**
 * This is the authentication manager interface.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/framework/com/bluejungle/framework/auth/IAuthMgr.java#2 $
 */

public interface IAuthMgr {

    public static final String COMP_NAME = "AuthMgr";
    public static final String KDC_CONFIG_PARAM = "java.security.krb5.kdc";
    public static final String REALM_CONFIG_PARAM = "java.security.krb5.realm";

    /**
     * Returns whether the the set of credentials are valid or not
     * 
     * @param userName
     *            name of the user
     * @param password
     *            password of the user
     * @return information about the authenticated user
     */
    public IAuthenticatedUser authenticate(String userName, String password) throws AuthenticationException;
}