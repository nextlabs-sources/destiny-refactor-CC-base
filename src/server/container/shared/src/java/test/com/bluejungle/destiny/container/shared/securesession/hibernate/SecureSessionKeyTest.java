/*
 * Created on Mar 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.hibernate;

import junit.framework.TestCase;

/**
 * Test for SecureSessionKey
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/securesession/hibernate/SecureSessionKeyTest.java#1 $
 */

public class SecureSessionKeyTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SecureSessionKeyTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for SecureSessionKeyTest.
     * @param arg0
     */
    public SecureSessionKeyTest(String arg0) {
        super(arg0);
    }

    public void testHashCode() {
        SecureSessionKey sessionKeyOne = new SecureSessionKey(new Long(1), new Long(2), new Long(3));
        SecureSessionKey sessionKeyOneMatch = new SecureSessionKey(new Long(1), new Long(2), new Long(3));
        
        assertEquals("testHashCode - Ensure equals keys have same hashcode", sessionKeyOne.hashCode(), sessionKeyOneMatch.hashCode());
    }

    public void testGetId() {
        Long id = new Long(50);
        SecureSessionKey sessionKeyOne = new SecureSessionKey(id, new Long(2), new Long(3));
        assertEquals("testGetId - Ensure id set is that retrieved", id, sessionKeyOne.getId());
    }

    public void testGetExpirationTime() {
        long expirationTime = 50;
        SecureSessionKey sessionKeyOne = new SecureSessionKey(new Long(1), expirationTime, new Long(3));
        assertEquals("testGetId - Ensure expiration time set is that retrieved", expirationTime, sessionKeyOne.getExpirationTime());
    }

    public void testGetEndOfLifeTime() {
        long endOfLife = 50;
        SecureSessionKey sessionKeyOne = new SecureSessionKey(new Long(1), new Long(2), endOfLife);
        assertEquals("testGetId - Ensure end of life time set is that retrieved", endOfLife, sessionKeyOne.getEndOfLifeTime());
    }

    /*
     * Class under test for boolean equals(Object)
     */
    public void testEqualsObject() {
        SecureSessionKey sessionKeyOne = new SecureSessionKey(new Long(1), new Long(2), new Long(3));
        SecureSessionKey sessionKeyOneMatch = new SecureSessionKey(new Long(1), new Long(2), new Long(3));
        SecureSessionKey sessionKeyTwo = new SecureSessionKey(new Long(1), new Long(2), new Long(30));
        
        assertEquals("testHashCode - Ensure equals object are equals", sessionKeyOne, sessionKeyOneMatch);
        assertEquals("testHashCode - Ensure same object is equals to itself", sessionKeyOne, sessionKeyOne);
        assertFalse("testHashCode - Ensure different object are not equal", sessionKeyOne.equals(sessionKeyTwo));
    }

}
