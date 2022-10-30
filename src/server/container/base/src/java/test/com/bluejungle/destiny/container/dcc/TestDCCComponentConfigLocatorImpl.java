/*
 * Created on Feb 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import java.io.InputStream;

import com.bluejungle.destiny.container.dcc.DCCResourceLocators;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.environment.IResourceLocator;

/**
 * Test implementation of a Config File Resource Locator. Though, this
 * implementation is generic, so it should be moved into the product and renamed
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcc/test/TestDCCComponentConfigLocatorImpl.java#2 $
 */
public class TestDCCComponentConfigLocatorImpl implements IDCCComponentConfigResourceLocator, IConfigurable {

    public static final String CONFIG_FOLDER_RELATIVE_PATH_PROPERTY_NAME = "configFolderRelativePath";

    private IConfiguration configuration;

    /**
     * Constructor
     *  
     */
    public TestDCCComponentConfigLocatorImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.dcc.test.IDCCComponentConfigResourceLocator#getConfigResourceAsStream()
     */
    public InputStream getConfigResourceAsStream(String resourceName) {
        if (resourceName == null) {
            throw new NullPointerException("resourceName cannot be null");
        }

        String relativeConfigFolderPath = (String) configuration.get(CONFIG_FOLDER_RELATIVE_PATH_PROPERTY_NAME);

        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IResourceLocator resourceLocator = (IResourceLocator) compMgr.getComponent(DCCResourceLocators.WEB_APP_RESOURCE_LOCATOR);

        return resourceLocator.getResourceAsStream(relativeConfigFolderPath + resourceName);
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

}