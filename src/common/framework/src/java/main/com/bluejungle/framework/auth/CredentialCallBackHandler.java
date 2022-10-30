/*
 * Created on Feb 8, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.framework.auth;
import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * This class handle the call back for the credential. It gives the user
 * credential to the Kerberos client whenever this becomes necesary. In this
 * implementation, the class does not need to interact with the end user. The
 * credentials are given when the class is instantiated.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/auth/CredentialCallBackHandler.java#1 $
 */
public class CredentialCallBackHandler implements CallbackHandler {

    private String userName;
    private String password;

    /**
     * Constructor
     * 
     * @param login
     *            userName
     * @param passwd
     *            password
     * @throws NullPointerException
     *             if userName or password is null
     * @throws IllegalArgumentException
     *             if userName is empty
     */
    public CredentialCallBackHandler(String loginName, String passwd) {
        super();
        if (loginName == null) {
            throw new NullPointerException("loginName cannot be null");
        }
        if (loginName.length() == 0) {
            throw new IllegalArgumentException("loginName cannot be empty");
        }
        if (passwd == null) {
            throw new NullPointerException("password cannot be null");
        }
        this.userName = loginName;
        this.password = passwd;
    }

    /**
     * Handles the callback. The username and password are transferred to the
     * security context.
     * 
     * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
     */
    public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        //Loop through the callbacks and enter user information
        for (int cbIdx = 0; cbIdx < callbacks.length; cbIdx++) {
            final Callback cb = callbacks[cbIdx];
            if (cb instanceof NameCallback) {
                final NameCallback callback = (NameCallback) callbacks[cbIdx];
                callback.setName(this.userName);
            } else if (cb instanceof PasswordCallback) {
                final PasswordCallback callback = (PasswordCallback) callbacks[cbIdx];
                callback.setPassword(password.toCharArray());
            } else {
                throw new UnsupportedCallbackException(cb);
            }
        }
    }
}