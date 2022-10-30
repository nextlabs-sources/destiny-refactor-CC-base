/*
 * Created on Aug 8, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2006 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.versionfactory;

import java.io.IOException;

import junit.framework.TestCase;

import com.bluejungle.version.IVersion;
import com.bluejungle.versionexception.InvalidVersionException;

/**
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/version/src/java/test/com/bluejungle/versionfactory/VersionFactoryTest.java#1 $
 */

public class VersionFactoryTest extends TestCase {
    
    // these are hardcoded version numbers for testing purposes, the numbers come from
    // version-test.jar, which is packaged inside the ant build file under src/common/version
    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 1;
    private static final int MAINTENANCE_VERSION = 0;
    private static final int PATCH_VERSION = 0;
    private static final int BUILD_VERSION = 143;
    
    /**
     * This tests the VersionFactory by loading a hardcoded version.properties file
     * @throws InvalidVersionException
     * @throws IOException
     */
    public void testVersionFactory() throws InvalidVersionException, IOException {
        VersionFactory versionFactory = new VersionFactory();
        IVersion version = versionFactory.getVersion();
        assertEquals("Major version should match", MAJOR_VERSION, version.getMajor());
        assertEquals("Minor version should match", MINOR_VERSION, version.getMinor());
        assertEquals("Maintenance version should match", MAINTENANCE_VERSION, version.getMaintenance());
        assertEquals("Patch version should match", PATCH_VERSION, version.getPatch());
        assertEquals("Build version should match", BUILD_VERSION, version.getBuild());
    }
}
