package com.bluejungle.pf.engine.destiny;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/engine/destiny/ClientInformationManagerTests.java#1 $
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bluejungle.pf.domain.destiny.policy.TestUser;

import junit.framework.TestCase;

/**
 * Tests for the client information manager.
 *
 * @author Sergey Kalinichenko
 */
public class ClientInformationManagerTests extends TestCase {

    public ClientInformationManagerTests(String name) {
        super(name);
    }

    private static final String[] CLIENT_IDS = new String[] {
        "XJ123"
    ,   "BF173"
    ,   "5DEX1"
    ,   "x-y-z"
    ,   "12345"
    ,   "AB987"
    ,   "PGX13"
    ,   "22D23"
    ,   "11011"
    ,   "00100"
    ,   "fund1"
    ,   "fund2"
    ,   "fund3"
    ,   "bank1"
    ,   "bank2"
    ,   "common"
    };

    private static final int[][] ID_GROUPS = new int[][] {
        new int[] { 0 }
    ,   new int[] { 1 }
    ,   new int[] { 2 }
    ,   new int[] { 3 } 
    ,   new int[] { 4 }
    ,   new int[] { 5 }
    ,   new int[] { 6 }
    ,   new int[] { 7 }
    ,   new int[] { 8 }
    ,   new int[] { 9 }
    ,   new int[] { 10, 11, 12 }
    ,   new int[] { 10, 11, 12 }
    ,   new int[] { 10, 11, 12 }
    ,   new int[] { 13, 14 }
    ,   new int[] { 13, 14 }
    ,   new int[] { 10, 11, 12, 15 }
    };

    private static final String[] SHORT_NAMES = new String[] {
        "Goldman Sachs"
    ,   "Fidelity"
    ,   "Morgan Stanley"
    ,   "Merrill Lynch"
    ,   "Lehman Brothers"
    ,   "Citigroup"
    ,   "Chase"
    ,   "Bear Stearns"
    ,   "J. P. Morgan"
    ,   "Deutsche Bank"
    ,   "Fund 1"
    ,   "Fund 2"
    ,   "Fund 3"
    ,   "Bank 1"
    ,   "Bank 2"
    ,   "Common"
    };

    private static final String[] LONG_NAMES = new String[] {
        "Goldman Sachs Financial Corporation"
    ,   "Fidelity Asset Management"
    ,   "Morgan Stanley Corporation"
    ,   "Merrill Lynch Financial Services Company"
    ,   "Lehman Brothers, Inc."
    ,   "Citicorp Financial"
    ,   "Chase Manhattan Bank"
    ,   "Bear Stearns Companies Inc."
    ,   "J.P. Morgan Chase Company"
    ,   "Deutsche Bank AG"
    ,   "Fund 1"
    ,   "Fund 2"
    ,   "Fund 3"
    ,   "Bank 1"
    ,   "Bank 2"
    ,   "Common"
    };

    private static final String[][] EMAILS = new String[][] {
        new String[] {"*.gs.com", "gs.com", "gs.co.jp", "*.gs.co.jp", "gs.co.uk", "*.gs.co.uk"}
    ,   new String[] {"fidelity.com"}
    ,   new String[] {"ms.com", "*ms.com"}
    ,   new String[] {"ml.com", "*.ml.com", "ml.co.jp", "ml.co.uk"}
    ,   new String[] {"lehman.com", "lehman.co.uk"}
    ,   new String[] {"citibank.com", "*.citibank.com"}
    ,   new String[] {"chase.com", "*.chase.com", "chemical.com", "*.chemical.com"}
    ,   new String[] {"bs.com", "*.bs.com"}
    ,   new String[] {"jpmorgan.com"}
    ,   new String[] {"db.de", "deutchebank.de", "dbalexbrown.com"}
    ,   new String[] {"*@fund.com"}
    ,   new String[] {"*@fund.com"}
    ,   new String[] {"*@fund.com"}
    ,   new String[] {"bank@gmail.com"}
    ,   new String[] {"bank@gmail.com"}
    ,   new String[] {"common@fund.com"}
    };

    private static final String[][] USERS = new String[][] {
        new String[] {TestUser.AHAN.getSID(), TestUser.AMORGAN.getSID(), TestUser.AYEN.getSID()}
    ,   new String[] {TestUser.BMENG.getSID(), TestUser.HCHAN.getSID(), TestUser.AYEN.getSID(), TestUser.RLIN.getSID()}
    ,   new String[] {TestUser.AHAN.getSID()}
    ,   new String[] {TestUser.HZHOU.getSID(), TestUser.IHANEN.getSID(), TestUser.BMENG.getSID()}
    ,   new String[] {TestUser.HCHAN.getSID(), TestUser.IHANEN.getSID(), TestUser.ISUNDIUS.getSID(), TestUser.HZHOU.getSID()}
    ,   new String[] {TestUser.IHANEN.getSID()}
    ,   new String[] {TestUser.KENG.getSID(), TestUser.RLIN.getSID(), TestUser.ISUNDIUS.getSID()}
    ,   new String[] {TestUser.ISUNDIUS.getSID(), TestUser.KENG.getSID(), TestUser.KENG.getSID(), TestUser.BMENG.getSID(), TestUser.HCHAN.getSID()}
    ,   new String[] {TestUser.AHAN.getSID(), TestUser.SGOLDSTEIN.getSID()}
    ,   new String[] {TestUser.SGOLDSTEIN.getSID(), TestUser.AMORGAN.getSID(), TestUser.SERGEY.getSID()}
    ,   new String[] {TestUser.HZHOU.getSID()}
    ,   new String[] {TestUser.HZHOU.getSID()}
    ,   new String[] {TestUser.HZHOU.getSID()}
    ,   new String[] {TestUser.HCHAN.getSID()}
    ,   new String[] {TestUser.HCHAN.getSID()}
    ,   new String[] {TestUser.AMORGAN.getSID()}
    };

    private static final String[] UIDS;

    static {
        UIDS = new String[TestUser.getAllUsers().size()];
        int i = 0;
        for (TestUser user : TestUser.getAllUsers()) {
            UIDS[i] = user.getSID();
        }
    }

    private static final ClientInformationManager CM = new ClientInformationManager(new Date(), UIDS, CLIENT_IDS, SHORT_NAMES, LONG_NAMES, EMAILS, USERS);

    private static final String[] INSERTS = new String[] {"", "hello", "123", "one.two.com"};

    private static final String[] OUTSIDE = new String[] {"microsoft.com", "xml.com", "10fold.com", "nextlabs.com", "bluejungle.com"};

    private static final Map<String,List<Integer>> USER_TO_CLIENT_INDEX = new HashMap<String,List<Integer>>();

    private static final Set<String> UNKNOWN = new HashSet<String>();

    static {
        for (int i = 0 ; i != USERS.length ; i++) {
            for (String uid : USERS[i]) {
                List<Integer> list = USER_TO_CLIENT_INDEX.get(uid);
                if (list == null) {
                    USER_TO_CLIENT_INDEX.put(uid, list = new ArrayList<Integer>());
                }
                list.add(i);
            }
        }
        for (TestUser user : TestUser.getAllUsers()) {
            String sid = user.getSID();
            if (!USER_TO_CLIENT_INDEX.containsKey(sid)) {
                UNKNOWN.add(sid);
            }
        }
    }

    public void testInitialize() {
        assertNotNull(CM);
    }

    public void testNullBuildTime() {
        try {
            new ClientInformationManager(null, UIDS, new String[0], new String[0], new String[0], new String[0][], new String[0][]);
            fail("NullPointerException expected");
        } catch (NullPointerException ignored) {
        }
    }

    public void testNullClientIds() {
        try {
            new ClientInformationManager(new Date(), UIDS, null, new String[0], new String[0], new String[0][], new String[0][]);
            fail("NullPointerException expected");
        } catch (NullPointerException ignored) {
        }
    }

    public void testNullShortNames() {
        try {
            new ClientInformationManager(new Date(), UIDS, new String[0], null, new String[0], new String[0][], new String[0][]);
            fail("NullPointerException expected");
        } catch (NullPointerException ignored) {
        }
    }

    public void testNullLongNames() {
        try {
            new ClientInformationManager(new Date(), UIDS, new String[0], new String[0], null, new String[0][], new String[0][]);
            fail("NullPointerException expected");
        } catch (NullPointerException ignored) {
        }
    }

    public void testNullEmail() {
        try {
            new ClientInformationManager(new Date(), UIDS, new String[0], new String[0], new String[0], null, new String[0][]);
            fail("NullPointerException expected");
        } catch (NullPointerException ignored) {
        }
    }

    public void testNullUids() {
        try {
            new ClientInformationManager(new Date(), UIDS, new String[0], new String[0], new String[0], new String[0][], null);
            fail("NullPointerException expected");
        } catch (NullPointerException ignored) {
        }
    }

    public void testMismatchedEmailSize() {
        try {
            new ClientInformationManager(
                new Date()
            ,   UIDS
            ,   new String[] {"a"}
            ,   new String[] {"aa"}
            ,   new String[] {"aaa"}
            ,   new String[0][]
            ,   new String[][] {new String[] {"a"}}
            );
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testMismatchedShortNamesSize() {
        try {
            new ClientInformationManager(
                new Date()
            ,   UIDS
            ,   new String[] {"a"}
            ,   new String[0]
            ,   new String[] {"aaa"}
            ,   new String[][] {new String[] {"a"}}
            ,   new String[][] {new String[] {"a"}}
            );
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testMismatchedLongNamesSize() {
        try {
            new ClientInformationManager(
                new Date()
            ,   UIDS
            ,   new String[] {"a"}
            ,   new String[] {"aaa"}
            ,   new String[0]
            ,   new String[][] {new String[] {"a"}}
            ,   new String[][] {new String[] {"a"}}
            );
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testMismatchedUidSize() {
        try {
            new ClientInformationManager(
                new Date()
            ,   UIDS
            ,   new String[] {"a"}
            ,   new String[] {"a"}
            ,   new String[] {"a"}
            ,   new String[][] {new String[] {"a"}}
            ,   new String[0][]
            );
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testNullClientIdMember() {
        try {
            new ClientInformationManager(
                new Date()
            ,   UIDS
            ,   new String[] {null}
            ,   new String[] {"a"}
            ,   new String[] {"a"}
            ,   new String[][] {new String[] {"a"}}
            ,   new String[][] {new String[] {"a"}}
            );
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testNullClientShortName() {
        try {
            new ClientInformationManager(
                new Date()
            ,   UIDS
            ,   new String[] {"a"}
            ,   new String[] {null}
            ,   new String[] {"a"}
            ,   new String[][] {new String[] {"a"}}
            ,   new String[][] {new String[] {"a"}}
            );
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void testNullClientLongName() {
        try {
            new ClientInformationManager(
                new Date()
            ,   UIDS
            ,   new String[] {"a"}
            ,   new String[] {"a"}
            ,   new String[] {null}
            ,   new String[][] {new String[] {"a"}}
            ,   new String[][] {new String[] {"a"}}
            );
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static void compare(int i, String[] ids) {
        assertNotNull(ids);
        Set<String> tmp = new HashSet<String>();
        for (String id : ids) {
            tmp.add(id);
        }
        assertEquals("Element "+i, ID_GROUPS[i].length, tmp.size());
        for (int j : ID_GROUPS[i]) {
            assertTrue("Element "+i+" mismatch", tmp.contains(CLIENT_IDS[j]));
        }
    }

    public void testByExactEmailMatch() {
        for (int i = 0 ; i != EMAILS.length ; i++) {
            for (String email : EMAILS[i]) {
                if (email.indexOf('*') == -1) {
                    compare(i, CM.getClientIdsForEmail(email));
                }
            }
        }
    }

    public void testByWildcardEmailMatch() {
        for (int i = 0 ; i != EMAILS.length ; i++) {
            for (String email : EMAILS[i]) {
                if (email.indexOf('*') != -1) {
                    for (String replacement : INSERTS) {
                        String d = email.replaceAll("[*]", replacement);
                        compare(i, CM.getClientIdsForEmail(d));
                    }
                    for (String outside : OUTSIDE) {
                        assertEquals(0, CM.getClientIdsForEmail(outside).length);
                    }
                }
            }
        }
    }

    public void testByNullEmail() {
        try {
            CM.getClientIdsForEmail(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ignored) {
        }
    }

    public void testGetClientPositive() {
        for (int i = 0 ; i != CLIENT_IDS.length ; i++) {
            ClientInformation client = CM.getClient(CLIENT_IDS[i]);
            assertEquals(CLIENT_IDS[i], client.getIdentifier());
            assertEquals(SHORT_NAMES[i], client.getShortName());
            assertEquals(LONG_NAMES[i], client.getLongName());
        }
    }

    public void testGetClientNegative() {
        for (String id : CLIENT_IDS) {
            for (int i = 1 ; i != id.length() ; i++) {
                assertNull(CM.getClient(id.substring(i)));
                assertNull(CM.getClient(id.substring(0,i)));
            }
        }
    }

    public void testGetClientNull() {
        try {
            CM.getClient(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ignored) {
        }
    }

    public void testGetClientForUserPositive() {
        for (Map.Entry<String,List<Integer>> e : USER_TO_CLIENT_INDEX.entrySet()) {
            ClientInformation[] clients = CM.getClientsForUser(e.getKey());
            assertEquals(clients.length, e.getValue().size());
            for (ClientInformation client : clients) {
                int pos = Arrays.asList(CLIENT_IDS).indexOf(client.getIdentifier());
                assertTrue("Client must be registered", pos != -1);
                assertTrue("Client must be related to the user", e.getValue().indexOf(pos) != -1);
            }
        }
    }

    public void testGetClientForUserNegative() {
        for (String uid : UNKNOWN) {
            ClientInformation[] clients = CM.getClientsForUser(uid);
            assertNotNull("getClientsForUser must not return null", clients);
            assertEquals("getClientsForUser must return an empty array", 0, clients.length);
        }
    }

    public void testGetClientForNullUser() {
        try {
            CM.getClientsForUser(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ignored) {
        }
    }

}
