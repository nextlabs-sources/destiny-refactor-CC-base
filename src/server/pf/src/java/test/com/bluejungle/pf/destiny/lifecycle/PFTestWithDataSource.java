package com.bluejungle.pf.destiny.lifecycle;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/test/com/bluejungle/pf/destiny/lifecycle/PFTestWithDataSource.java#1 $
 */

import java.util.HashSet;
import java.util.Set;

import com.bluejungle.destiny.container.dcc.BaseDCCComponentTestCase;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.pf.tools.MockSharedContextLocator;

/**
 * This is the base class for Policy Framework tests.
 * Extend this class if you need the data source
 * of the Policy Framework to be available.
 */
public class PFTestWithDataSource extends BaseDCCComponentTestCase {
    
    protected IComponentManager cm;

    public static final String COMPONENT_NAME = "dabs";
    public static final Set REQUIRED_DATA_REPOSITORIES = new HashSet();

    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY);
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.MANAGEMENT_REPOSITORY);
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.DICTIONARY_REPOSITORY);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        cm = ComponentManagerFactory.getComponentManager();
        
        ComponentInfo locatorInfo = new ComponentInfo(IDestinySharedContextLocator.COMP_NAME, MockSharedContextLocator.class.getName(), IDestinySharedContextLocator.class.getName(), LifestyleType.SINGLETON_TYPE);
        cm.registerComponent(locatorInfo, true);
        

    }
    

    /**
     * The default constructor.
     */
    public PFTestWithDataSource() {
        super(false);
    }

    /**
     * Constructor for PFTestWithDataSource.
     * @param name the name of the test.
     */
    public PFTestWithDataSource(String name) {
        super(name, false);
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
