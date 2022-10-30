/*
 * Created on Feb 25, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.test;

import com.bluejungle.framework.comp.ComponentManagerFactory;

import junit.framework.TestCase;

/**
 * This is the base Destiny test case class. All Destiny tests cases should
 * extend this basic test case.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/framework/com/bluejungle/framework/test/BaseDestinyTestCase.java#1 $
 */

public abstract class BaseDestinyTestCase extends TestCase {
	protected static final String SRC_ROOT_DIR = System.getProperty("src.root.dir");

    /**
     * Constructor
     */
    public BaseDestinyTestCase() {
        super();
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public BaseDestinyTestCase(String testName) {
        super(testName);
    }

    /**
     * Sets up the JUnit test case.
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        ComponentManagerFactory.getComponentManager().shutdown();
    }

    /**
     * Cleans up the component manager state
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        ComponentManagerFactory.getComponentManager().shutdown();
    }
}