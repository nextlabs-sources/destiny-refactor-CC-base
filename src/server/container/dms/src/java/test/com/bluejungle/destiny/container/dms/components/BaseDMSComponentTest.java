/*
 * Created on Jan 12, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.container.dms.components;

import java.util.HashSet;
import java.util.Set;

import com.bluejungle.destiny.container.dcc.BaseDCCComponentTestCase;
import com.bluejungle.framework.configuration.DestinyRepository;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public abstract class BaseDMSComponentTest extends BaseDCCComponentTestCase {

    public static final String COMPONENT_NAME = "dms";
    public static final Set REQUIRED_DATA_REPOSITORIES = new HashSet();
    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.MANAGEMENT_REPOSITORY);
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY);
    }

    /**
     * Constructor.
     * 
     * @param testName
     *            the name of the test
     */
    public BaseDMSComponentTest(String testName) {
        super(testName);
    }

    /**
     * Sets up the Mock DMS Container for testing
     * 
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception {
        super.setUp();
    }
    
    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    /**
     * @see com.bluejungle.destiny.container.dcc.test.BaseDCCComponentTestCase#getComponentName()
     */
    protected String getComponentName() {
        return COMPONENT_NAME;
    }
    
    
    /**
     * @see com.bluejungle.destiny.container.dcc.test.BaseDCCComponentTestCase#getDataRepositories()
     */
    protected Set getDataRepositories() {
        return REQUIRED_DATA_REPOSITORIES;
    }
}
