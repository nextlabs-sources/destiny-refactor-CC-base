/*
 * Created on May 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl;

import java.io.IOException;
import java.security.Security;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.nextlabs.destiny.container.shared.dictionary.enrollment.enroller.ad.ADConnectionHelper;
import com.nextlabs.destiny.container.shared.dictionary.enrollment.enroller.ad.ADConnectionHelper.ConnectionType;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPJSSEStartTLSFactory;
import com.novell.ldap.LDAPSocketFactory;

/**
 * This class is a utility class, mostly for the import tool (specifically at
 * install time) so that the customer can test to see if the Active Direcetory
 * connection information that they provided for import is valid.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/directory/tools/src/java/main/com/bluejungle/ldap/tools/misc/ADConnectionTester.java#1 $
 */

public class ADConnectionTester {
    
    // password decoder
    private static final ReversibleEncryptor CIPHER = new ReversibleEncryptor();
    
    /**
     * Tests whether the given connect info is valid
     * 
     * @param server
     * @param port
     * @param loginDN
     * @param password
     * @param rootDN
     * @return
     */
    public static void testConnection( String server
                                       , int port
                                       , String loginDN
                                       , String password
                                       , String[] subtrees
                                       , String secureTransactionMode
                                       , boolean alwaysTrustAD
                                       ) throws EnrollmentValidationException {
        // Check the connection:
        LDAPConnection connection = null;

        ConnectionType connType = ConnectionType.UNENCRYPTED;

        if (secureTransactionMode != null) {
            if ("SSL".compareToIgnoreCase(secureTransactionMode) == 0) {
                connType = ConnectionType.SSL;
            } else if ("TLS".compareToIgnoreCase(secureTransactionMode) == 0) {
                connType = ConnectionType.TLS;
            }
        }
        
        if (server == null) {
        	throw new EnrollmentValidationException("LDAP server argument can not be null. Update may have failed. Check Configuration.");
        }

        try {
            try {
                connection = ADConnectionHelper.createConnection(connType, alwaysTrustAD);
            } catch (IOException e) {
                throw new EnrollmentValidationException("Unable to create connection to " + server, e);
            }
            
            try {
                connection.connect(server, port);
                if (connType == ConnectionType.TLS) {
                    connection.startTLS();
                }
                connection.bind(LDAPConnection.LDAP_V3, loginDN, CIPHER.decrypt(password).getBytes() );
            } catch (LDAPException e) {
            	String msg;
            	int resultCode = e.getResultCode();
            	switch (resultCode) {
                case LDAPException.INVALID_CREDENTIALS:
                    msg = "Invalid LDAP username or password.";
                    break;
                case LDAPException.CONNECT_ERROR:
                    msg = "Cannot connect to LDAP server: " + e.getMessage() + ".";
                    break;
                default:
                    msg = "LDAP Error: " + e.getMessage() + ".";
                }
                throw new EnrollmentValidationException(msg, e);
            }
            catch (Exception e) {
            	String msg = "LDAP initialization failed with an exception: " + e.getMessage() + ".";
            	throw new EnrollmentValidationException(msg, e);
            }
            
            if (connType == ConnectionType.TLS && !connection.isTLS()) {
            	throw new EnrollmentValidationException("TLS secure transport did not initialize.");
            }
    
            // Now, subtrees has been specified, we should check that:
            if (subtrees != null) {
                for (int i = 0; i < subtrees.length; i++) {
                    try {
                        @SuppressWarnings("unused")
                        LDAPEntry subtreeRoot = connection.read(subtrees[i]);
                    } catch (LDAPException e) {
                        throw new EnrollmentValidationException("Can not access LDAP subtree: "
                                + subtrees[i], e);
                    }
                }
            }
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    // ignore error when failed in disconnect 
                }
            }
        }
    }
}
