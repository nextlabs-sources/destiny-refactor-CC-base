/*
 * Created on May 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ldap.tools.misc;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;

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

    /**
     * Installer status results
     */
    private static final String FAIL = "FAIL";
    private static final String OK = "OK";

    /**
     * Main function. This function is used only by the installer, and should
     * not be used in command line directly.
     * 
     * @param args -
     *            server - server port - login name (e.g. ihanen@bluejungle.com) -
     *            password - rootDN
     */
    public static void main(String[] args) {
        final String server = args[0];
        final int port = (Integer.valueOf(args[1])).intValue();
        final String login = args[2];
        final String password = args[3];
        final String rootDN = args[4];

        boolean result = ADConnectionTester.testConnection(server, port, login, password, rootDN);
        if (result == true) {
            System.out.println(OK);
        } else {
            System.out.println(FAIL);
        }
    }

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
    public static boolean testConnection(String server, int port, String loginDN, String password, String rootDN) {
        boolean connectionValid = false;

        // Check the connection:
        try {
            LDAPConnection connection;
            byte[] passwd = null;
            if (password != null) {
                passwd = password.getBytes();
            }
            connection = new LDAPConnection();
            connection.connect(server, port);
            connection.bind(LDAPConnection.LDAP_V3, loginDN, passwd);

            // Now make sure that the root dn exists:
            LDAPEntry rootDNEntry = connection.read(rootDN);
            if (rootDNEntry == null) {
                connectionValid = false;
            } else {
                connectionValid = true;
            }
        } catch (LDAPException e) {
            connectionValid = false;
        }

        return connectionValid;
    }
}