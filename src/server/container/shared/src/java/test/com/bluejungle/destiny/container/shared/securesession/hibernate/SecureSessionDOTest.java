/*
 * Created on Mar 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.hibernate;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * Unit test for secure session do
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/securesession/hibernate/SecureSessionDOTest.java#1 $
 */

public class SecureSessionDOTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SecureSessionDOTest.class);
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
     * Constructor for SecureSessionDOTest.
     * 
     * @param arg0
     */
    public SecureSessionDOTest(String arg0) {
        super(arg0);
    }

    public void testGetSetId() {
        SecureSessionDO secureSession = new SecureSessionDO();

        assertNull("testGetSetId - Ensure id is initially null", secureSession.getId());

        // Now set it
        Long id = new Long(4455);
        secureSession.setId(id);

        // Now ensure that we can retrieve it
        assertEquals("testGetSetId - Ensure id set is that which is retrieve.", id, secureSession.getId());

        // Check for null
        NullPointerException expectedException = null;
        try {
            secureSession.setId(null);
            fail("Should throw null pointer");
        } catch (NullPointerException excetpion) {
            expectedException = excetpion;
        }
        assertNotNull("testGetSetId - Ensure null pointer was thrown", expectedException);
    }

    public void testGetProperty() {        
        SecureSessionDO secureSession = new SecureSessionDO();
        
        Map<String, String> properties = new HashMap<String, String>();
        String propName = "propName";
        String propValue = "propValue";
        properties.put(propName, propValue);
        secureSession.setPropertiesAsMap(properties);
        
        assertNull("testGetProperty - Non existed property.", secureSession.getProperty("foo"));
        assertEquals("testGetProperty - Existent property.", propValue, secureSession.getProperty(propName));
    }

    public void testGetSetEndOfLife() {
        SecureSessionDO secureSession = new SecureSessionDO();

        assertEquals("testGetSetEndOfLife - Ensure endOfLife is initially null", 0, secureSession.getEndOfLife());

        // Now set it
        long endOfLife = 4455;
        secureSession.setEndOfLife(endOfLife);

        // Now ensure that we can retrieve it
        assertEquals("testGetSetEndOfLife - Ensure endOfLife set is that which is retrieve.", endOfLife, secureSession.getEndOfLife());

        // Check for null
        NullPointerException expectedException = null;
        try {
            secureSession.setEndOfLife(null);
            fail("Should throw null pointer");
        } catch (NullPointerException excetpion) {
            expectedException = excetpion;
        }
        assertNotNull("testGetSetEndOfLife - Ensure null pointer was thrown", expectedException);
    }

    public void testGetSetPropertiesAsMap() {
        SecureSessionDO secureSession = new SecureSessionDO();

        assertNull("testGetSetPropertiesAsMap - Ensure properties is initially null", secureSession.getPropertiesAsMap());

        // Now set it
        Map<String, String> properties = new HashMap<String, String>();
        secureSession.setPropertiesAsMap(properties);

        // Now ensure that we can retrieve it
        assertEquals("testGetSetPropertiesAsMap - Ensure properties set is that which is retrieve.", properties, secureSession.getPropertiesAsMap());

        // Check for null
        NullPointerException expectedException = null;
        try {
            secureSession.setPropertiesAsMap(null);
            fail("Should throw null pointer");
        } catch (NullPointerException excetpion) {
            expectedException = excetpion;
        }
        assertNotNull("testGetSetPropertiesAsMap - Ensure null pointer was thrown", expectedException);
    }
    
    public void testGetProperties() {
        SecureSessionDO secureSession = new SecureSessionDO();

        assertEquals("testGetProperties - Ensure properties is initially empty", 0, secureSession.getProperties().size());

        // Now set it
        Map<String, String> properties = new HashMap<String, String>();
        secureSession.setPropertiesAsMap(properties);

        // Now ensure that we can retrieve it
        assertEquals("testGetSetProperties - Ensure properties set is that which is retrieve.", properties, secureSession.getProperties());
    }
}