/*
 * Created on Mar 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession;

import java.util.Properties;

/**
 * A Secure Session, created upon proper authentication
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/securesession/ISecureSession.java#1 $
 */
public interface ISecureSession {

    /**
     * Secure session id
     */
    public static final String ID_PROP_NAME = "Id";

    /**
     * Secure session principal name
     */
    public static final String PRINCIPALNAME_PROP_NAME = "PrincipalName";

    /**
     * Secure session username
     */
    public static final String USERNAME_PROP_NAME = "Username";

    /**
     * Boolean property declaring if the password on the user can be changed
     */
    public static final String CAN_CHANGE_PASSWORD = "CanChangePassword";

    /**
     * Retrieve the id of this secure session
     * 
     * @return the id of this secure session
     */
    public Long getId();

    /**
     * Generate a key which the client cen use to refer to this secure session
     * 
     * @return a key which the client cen use to refer to this secure session
     */
    public String generateKey();

    /**
     * Retrieve a property associated with this secure session
     * 
     * @param name
     *            the name of a property associated with this secure session
     * @return a property associated with this secure session
     */
    public String getProperty(String name);

    /**
     * Retrieve all properties associated with this secure session
     */
    public Properties getProperties();
}