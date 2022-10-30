/*
 * Created on Feb 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs.components.test;

import java.util.HashSet;
import java.util.Set;

import com.bluejungle.destiny.container.dcc.BaseDCCComponentTestCase;
import com.bluejungle.framework.configuration.DestinyRepository;


/**
 * Base Dabs Component Test Case. Currently it only sets of the data source
 * required by DABS
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/test/com/bluejungle/destiny/container/dabs/components/test/BaseDabsComponentTest.java#1 $
 */
public abstract class BaseDabsComponentTest extends BaseDCCComponentTestCase {

    public static final String COMPONENT_NAME = "dabs";
    public static final Set REQUIRED_DATA_REPOSITORIES = new HashSet();
    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.MANAGEMENT_REPOSITORY);
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.ACTIVITY_REPOSITORY);
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
     * Constructor for BaseDabsComponentTest.
     * 
     * @param arg0
     */
    public BaseDabsComponentTest(String arg0) {
        super(arg0);
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