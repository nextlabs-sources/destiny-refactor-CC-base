/*
 * Created on Feb 15, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.test;

import java.util.HashSet;
import java.util.Set;

import com.bluejungle.destiny.container.dcc.BaseDCCComponentTestCase;
import com.bluejungle.framework.configuration.DestinyRepository;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/com/bluejungle/destiny/container/shared/test/BaseContainerSharedTestCase.java#1 $
 */

public abstract class BaseContainerSharedTestCase extends BaseDCCComponentTestCase {

    public static final Set<DestinyRepository> REQUIRED_DATA_REPOSITORIES = new HashSet<DestinyRepository>();
    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY);
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.MANAGEMENT_REPOSITORY);
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.DICTIONARY_REPOSITORY);
    }

    /**
     * Use the dms component as the testing component
     */
    public static final String COMPONENT_NAME = "dms";

    /**
     * Constructor
     */
    public BaseContainerSharedTestCase() {
        super();
    }

    /**
     * Constructor for BaseContainerSharedTestCase.
     * 
     * @param testCase
     *            name of the test
     */
    public BaseContainerSharedTestCase(String testCase) {
        super(testCase);
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
    protected Set<DestinyRepository> getDataRepositories() {
        return REQUIRED_DATA_REPOSITORIES;
    }
}
