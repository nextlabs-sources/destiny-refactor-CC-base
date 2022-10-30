/*
 * Created on Apr 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This is the resource cache test suite
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/ResourceCacheTestSuite.java#1 $
 */

public class ResourceCacheTestSuite {

    /**
     * Returns the test suite
     * 
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Resource caches");
        suite.addTest(new TestSuite(MasterResourceCacheTest.class, "Master resource cache"));
        suite.addTest(new TestSuite(PolicyCacheTest.class, "Policy resource cache"));
        suite.addTest(new TestSuite(HostAndGroupCacheTest.class, "Host and host group resource cache"));
        suite.addTest(new TestSuite(UserAndGroupCacheTest.class, "User and user group resource cache"));
        return suite;
    }
}