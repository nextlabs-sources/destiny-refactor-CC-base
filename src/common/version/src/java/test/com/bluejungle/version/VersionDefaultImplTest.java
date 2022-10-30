/*
 * Created on Aug 8, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2006 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.version;

import junit.framework.TestCase;

/**
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/version/src/java/test/com/bluejungle/version/VersionDefaultImplTest.java#1 $
 */

public class VersionDefaultImplTest extends TestCase {

    private final int MAJOR_VERSION = 1;
    private final int MINOR_VERSION = 2;
    private final int MAINTENANCE_VERSION = 3;
    private final int PATCH_VERSION = 4;
    private final int BUILD_VERSION = 5;
    
    /**
     * This tests the VersionDefaultImpl class
     *
     */
    public void testVersionDefaultImpl(){
        IVersion version = new VersionDefaultImpl(MAJOR_VERSION, MINOR_VERSION, MAINTENANCE_VERSION, PATCH_VERSION, BUILD_VERSION);
        assertEquals("Major version should match", MAJOR_VERSION, version.getMajor());
        assertEquals("Minor version should match", MINOR_VERSION, version.getMinor());
        assertEquals("Maintenance version should match", MAINTENANCE_VERSION, version.getMaintenance());
        assertEquals("Patch version should match", PATCH_VERSION, version.getPatch());
        assertEquals("Build version should match", BUILD_VERSION, version.getBuild());
    }
    
    /**
     * This tests the VersionDefaultImpl class with negative version numbers
     *
     */
    public void testNegativeVersionDefaultImpl(){
        IVersion version = new VersionDefaultImpl(-1, -2, -3, -4, -5);
        assertEquals("Major version should be 0", 0, version.getMajor());
        assertEquals("Minor version should be 0", 0, version.getMinor());
        assertEquals("Maintenance version should be 0", 0, version.getMaintenance());
        assertEquals("Patch version should be 0", 0, version.getPatch());
        assertEquals("Build version should be 0", 0, version.getBuild());
    }
}
