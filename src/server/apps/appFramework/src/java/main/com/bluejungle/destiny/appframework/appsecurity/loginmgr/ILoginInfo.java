/*
 * Created on Feb 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.appsecurity.loginmgr;

/**
 * This is the login info interface. Beans that contain login information can
 * implement that interface.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/pcv/Nimbus_8.1_bugfixes/main/src/server/apps/appFramework/src/java/main/com/bluejungle/destiny/appframework/appsecurity/loginmgr/ILoginInfo.java#1 $
 */

public interface ILoginInfo {

    /**
     * Returns the user name provided by the user
     * 
     * @return the user name provided by the user
     */
    public String getUserName();

    /**
     * Returns the password provided by the user
     * 
     * @return the password provided by the user
     */
    public String getPassword();
}