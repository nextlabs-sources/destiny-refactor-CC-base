/*
 * Created on Jul 13, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl;

import junit.framework.TestCase;

/**
 * Test case for InternalApplicationUserDO
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/repository/hibernateimpl/TestInternalApplicationUserDO.java#1 $
 */

public class TestInternalApplicationUserDO extends TestCase {

    private InternalApplicationUserDO userToTest = new InternalApplicationUserDO();

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.InternalApplicationUserDO#isManuallyCreated()}.
     */
    public void testIsManuallyCreated() {
        assertTrue("testIsManuallyCreated - Ensure manually created is true.", this.userToTest.isManuallyCreated());
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.InternalApplicationUserDO#getPassword()}
     * and
     * {@link com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl.InternalApplicationUserDO#setPassword(byte[])}.
     */
    public void testGetSetPassword() {
        assertNull("testGetSetPassword - Ensure password initially null.", this.userToTest.getPassword());
        byte[] passwordToSet = new byte[1];
        this.userToTest.setPassword(passwordToSet);
        assertEquals("testGetSetPassword - Ensure password set as expected.", passwordToSet, this.userToTest.getPassword());

        // Test NPE
        try {
            this.userToTest.setPassword((byte[])null);
            fail("testGetSetPassword - Should throw NPE when specifying null password to setPassword(byte[])");
        } catch (NullPointerException exception) {
        }
        
        try {
            this.userToTest.setPassword((String)null);
            fail("testGetSetPassword - Should throw NPE when specifying null password to setPassword(String)");
        } catch (NullPointerException exception) {
        }
    }
}
