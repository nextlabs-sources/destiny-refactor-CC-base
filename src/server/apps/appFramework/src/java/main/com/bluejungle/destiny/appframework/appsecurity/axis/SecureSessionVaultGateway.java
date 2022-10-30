/*
 * Created on Feb 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.appsecurity.axis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.types.secure_session.v1.SecureSession;

/**
 * A gateway to the secure session vault used to store secure sessions for a
 * secure web service client. The secure session vault utilized is set in one of
 * two ways. First, it can be explicitly set by invoking the
 * {@see #setSecureSessionValue(ISecureSessionVault)}method. Secondly, it can
 * be declared within a jar file at the path,
 * {@see #SECURE_SESSION_VAULT_DECLARATION_RESOURCE_NAME}.
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/apps/inquiryCenter/src/java/main/com/bluejungle/destiny/inquirycenter/security/axis/SecureSessionContext.java#1 $
 */

public class SecureSessionVaultGateway {

    private static final Log LOG = LogFactory.getLog(SecureSessionVaultGateway.class.getName());

    public static final String SECURE_SESSION_VAULT_DECLARATION_RESOURCE_NAME = "META-INF/services/com.bluejungle.destiny.appsecurity.axis.SecureSessionVault";

    private static ISecureSessionVault secureSessionVault;
    static {
        secureSessionVault = createSecureSessionVault();
    }

    private SecureSessionVaultGateway() {
    }

    /**
     * Set the secure session for this context
     * 
     * @param secureSession
     *            the secure session for this context
     */
    public static void setSecureSession(SecureSession secureSession) {
        getSecureSessionVault().storeSecureSession(secureSession);
    }

    /**
     * Retrieve the secure session for this context
     * 
     * @return the secure session for this context
     */
    public static SecureSession getSecureSession() {
        return getSecureSessionVault().getSecureSession();
    }

    /**
     * Clear the current secure session context
     */
    public static void clearSecureSession() {
        getSecureSessionVault().clearSecureSession();
    }

    /**
     * Set the secure session vault
     * 
     * @param secureSessionVaultToSet
     *            the secure session vault to set
     */
    public static void setSecureSessionVault(ISecureSessionVault secureSessionVaultToSet) {
        if (secureSessionVaultToSet == null) {
            throw new NullPointerException("secureSessionVault cannot be null.");
        }

        secureSessionVault = secureSessionVaultToSet;
    }

    /**
     * Retrieve the secure session vault
     * 
     * @return the secure session vault
     */
    private static ISecureSessionVault getSecureSessionVault() {
        return secureSessionVault;
    }

    /**
     * Create the ISecureSessionVault instance based on the first declaration
     * file found in a jar in the classpath
     * 
     * @return the created ISecureSessionVault instance of null if the
     *         configuration file cannot be found or references an invalid class
     */
    private static ISecureSessionVault createSecureSessionVault() {
        ISecureSessionVault secureSessionVaultToReturn = null;

        ClassLoader classLoader = ISecureSessionVault.class.getClassLoader();
        InputStream secureSessionVaultConfigFileStream = classLoader.getResourceAsStream(SECURE_SESSION_VAULT_DECLARATION_RESOURCE_NAME);
        if (secureSessionVaultConfigFileStream != null) {
            try {
                BufferedReader secureSessionVaultConfigFileReader = new BufferedReader(new InputStreamReader(secureSessionVaultConfigFileStream));
                try {
                    String secureSessionVaultClassName = secureSessionVaultConfigFileReader.readLine();
                    if (secureSessionVaultClassName == null) {
                        getLog().debug("ISecureSessionVault class declaration file not found.");
                    } else {
                        try {
                            Class secureSessionVaultClass = classLoader.loadClass(secureSessionVaultClassName);
                            secureSessionVaultToReturn = (ISecureSessionVault) secureSessionVaultClass.newInstance();
                        } catch (ClassNotFoundException exception) {
                            getLog().warn("Declared ISecureSessionVault class, " + secureSessionVaultClassName + ", not found", exception);
                        } catch (IllegalAccessException exception) {
                            getLog().warn("Failed to instantiate declared ISecureSessionVault class, " + secureSessionVaultClassName + ".  Class or default constructor is not accessible.", exception);
                        } catch (InstantiationException exception) {
                            getLog().warn("Failed to instantiate declared ISecureSessionVault class, " + secureSessionVaultClassName + ".  Default constructor threw an Exception.", exception);
                        }
                    }
                } catch (IOException exception) {
                    getLog().warn("Failed to read secure session vault class declaration file", exception);
                } finally {
                    try {
                        secureSessionVaultConfigFileReader.close();
                    } catch (IOException e) {
                        getLog().warn("Fail to close secureSessionVaultConfigFileReader", e);
                    }
                }
            } finally {
                try {
                    secureSessionVaultConfigFileStream.close();
                } catch (IOException e) {
                    getLog().warn("Fail to close secureSessionVaultConfigFileStream", e);
                }
            }
        }

        return secureSessionVaultToReturn;
    }

    /**
     * Retrieve a Log instance
     * 
     * @return a Log instance
     */
    private static Log getLog() {
        return LOG;
    }
}