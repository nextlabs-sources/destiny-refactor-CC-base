/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/DictionaryTestWithDataSource.java#1 $
 */

package com.bluejungle.dictionary;

import java.util.HashSet;
import java.util.Set;

import com.bluejungle.destiny.container.dcc.BaseDCCComponentTestCase;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.dictionary.tools.MockSharedContextLocator;

/**
 * This is the base class for Server-Side Dictionary tests.
 * Extend this class if you need the data source of the dictionary
 * to be available to your tests.
 */
public abstract class DictionaryTestWithDataSource extends BaseDCCComponentTestCase {

    protected IComponentManager cm;
    private static final ComponentInfo<MockSharedContextLocator> LOCATOR_COMPONENT_INFO = 
    			new ComponentInfo<MockSharedContextLocator>(
				IDestinySharedContextLocator.COMP_NAME, 
				MockSharedContextLocator.class, 
				IDestinySharedContextLocator.class, 
				LifestyleType.SINGLETON_TYPE);

    public static final String COMPONENT_NAME = "dabs";
    public static final Set<DestinyRepository> REQUIRED_DATA_REPOSITORIES =
        new HashSet<DestinyRepository>();

    static {
        REQUIRED_DATA_REPOSITORIES.add(
            DestinyRepository.DICTIONARY_REPOSITORY
        );
    }

    protected void setUp() throws Exception {
        super.setUp();
        cm = ComponentManagerFactory.getComponentManager();
        cm.registerComponent(LOCATOR_COMPONENT_INFO, true);

        ComponentInfo<MockSharedContextLocator> locatorInfo =
				new ComponentInfo<MockSharedContextLocator>(
        		IDestinySharedContextLocator.COMP_NAME,
        		MockSharedContextLocator.class,
        		IDestinySharedContextLocator.class,
        		LifestyleType.SINGLETON_TYPE);
        cm.registerComponent(locatorInfo, true);
        cm.getComponent(locatorInfo);
    }

    /**
     * The default constructor.
     */
    public DictionaryTestWithDataSource() {
        super(false);
    }

    /**
     * Constructor for DictionaryTestWithDataSource.
     * @param name the name of the test.
     */
    public DictionaryTestWithDataSource(String name) {
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
    protected Set<DestinyRepository> getDataRepositories() {
        return REQUIRED_DATA_REPOSITORIES;
    }

}
