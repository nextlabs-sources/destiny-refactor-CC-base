package com.nextlabs.framework.test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.bluejungle.framework.comp.ComponentManagerFactory;

public abstract class BaseDestinyTestCase {

    private static final String SRC_ROOT_PROPERTY_KEY = "src.root.dir";
    
    protected static final String SRC_ROOT_DIR = System.getProperty(SRC_ROOT_PROPERTY_KEY);

    @BeforeClass
    public static void checkSrcParamater() {
        assertNotNull("Please define system property \"" + SRC_ROOT_PROPERTY_KEY + "\"", SRC_ROOT_DIR);
    }
    
    @AfterClass
    public static void afterClass() {
        ComponentManagerFactory.getComponentManager().shutdown();
    }

}
