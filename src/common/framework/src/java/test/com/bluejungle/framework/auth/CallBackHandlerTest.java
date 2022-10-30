/*
 * Created on Feb 9, 2005
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

import com.bluejungle.framework.auth.CredentialCallBackHandler;
import com.bluejungle.framework.test.BaseDestinyTestCase;

/**
 * This is the test class for the callback handler
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/framework/com/bluejungle/framework/auth/test/CallBackHandlerTest.java#1 $
 */

public class CallBackHandlerTest extends BaseDestinyTestCase {

    /**
     * Constructor
     */
    public CallBackHandlerTest() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            the name of the test
     */
    public CallBackHandlerTest(String testName) {
        super(testName);
    }

    /**
     * This test verifies that the class has the correct characteristics
     */
    public void testClass() {
        CredentialCallBackHandler cb = new CredentialCallBackHandler("a", "a");
        assertTrue("Callback handler implements the correct interface", cb instanceof CallbackHandler);
    }

    /**
     * This test verifies that only correct arguments can be given to the
     * callback handler.
     */
    public void testArgumentsValidation() {
        //Try NULL login
        boolean exThrown = false;
        try {
            CredentialCallBackHandler cb = new CredentialCallBackHandler(null, "a");
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("NULL userName is not allowed", exThrown);

        //Try NULL password
        exThrown = false;
        try {
            CredentialCallBackHandler cb = new CredentialCallBackHandler("a", null);
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("NULL password is not allowed", exThrown);

        //Try empty userName
        exThrown = false;
        try {
            CredentialCallBackHandler cb = new CredentialCallBackHandler("", "a");
        } catch (IllegalArgumentException e) {
            exThrown = true;
        }
        assertTrue("Empty userName is not allowed", exThrown);

        //Try userName with empty password (should be fine)
        exThrown = false;
        try {
            CredentialCallBackHandler cb = new CredentialCallBackHandler("a", "");
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertFalse("Empty password is allowed", exThrown);
    }

    /**
     * This test verifies that the callback arguments are set properly
     */
    public void testCallbackSetup() {
        final String userName = "myUserName";
        final String password = "myPassword";
        CredentialCallBackHandler cb = new CredentialCallBackHandler(userName, password);
        NameCallback nameCB = new NameCallback("testName");
        PasswordCallback passCB = new PasswordCallback("testPass", true);
        Callback[] cbArray = { nameCB, passCB };

        boolean exThrown = false;
        try {
            cb.handle(cbArray);
        } catch (IOException e) {
            exThrown = true;
        } catch (UnsupportedCallbackException e) {
            exThrown = true;
        }
        assertFalse("Callback processing should succeed", exThrown);
        assertEquals("Callback handler should get the right userName", userName, nameCB.getName());
        assertEquals("Callback handler should get the right password", password, new String(passCB.getPassword()));
    }
}