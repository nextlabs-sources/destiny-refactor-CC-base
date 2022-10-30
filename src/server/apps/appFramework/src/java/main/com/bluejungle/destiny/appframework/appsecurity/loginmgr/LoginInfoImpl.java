/*
 * Created on Feb 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.appsecurity.loginmgr;

/**
 * This is the login bean implementation. It stores the credential information
 * provided by the user.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/framework/com/bluejungle/destiny/server/dac/framework/components/loginmgr/LoginInfoImpl.java#1 $
 */

public class LoginInfoImpl implements ILoginInfo {

    private String userName;
    private String password;

    /**
     * @return the username provided by the user
     * @see com.bluejungle.destiny.webui.framework.loginmgr.ILoginInfo#getUserName()
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * @return the password provided by the user
     * @see com.bluejungle.destiny.webui.framework.loginmgr.ILoginInfo#getPassword()
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets the password provided by the user
     * 
     * @param password
     *            provided by the user
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Set the username provided by the user
     * 
     * @param userName
     *            username provided by the user
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
}