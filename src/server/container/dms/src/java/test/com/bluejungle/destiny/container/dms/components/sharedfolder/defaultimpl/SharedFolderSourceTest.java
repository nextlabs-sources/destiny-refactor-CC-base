/*
 * Created on September 11, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.sharedfolder.defaultimpl;

import java.util.Date;

import junit.framework.TestCase;

import com.bluejungle.destiny.container.dcc.DCCResourceLocators;
import com.bluejungle.destiny.container.dcc.FileSystemResourceLocatorImpl;
import com.bluejungle.destiny.container.dcc.INamedResourceLocator;
import com.bluejungle.destiny.container.dms.components.sharedfolder.defaultimpl.DMSSharedFolderInformationSourceImpl;
import com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationRelay;
import com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationSource;
import com.bluejungle.destiny.container.shared.sharedfolder.defaultimpl.SharedFolderInformationCookieImpl;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderData;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.environment.IResourceLocator;

public class SharedFolderSourceTest extends TestCase {

    protected IComponentManager compMgr;

    /**
     * Constructor
     *
     */
    public SharedFolderSourceTest() {
        super();
    }

    /**
     * Constructor
     *
     * @param name
     *            test name
     */
    public SharedFolderSourceTest(String name) {
        super(name);
    }

    /**
     * Sets up the test
     * 
     * @throws Exception
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception {
        super.setUp();

        compMgr = ComponentManagerFactory.getComponentManager();
    }

    public void testSharedFolderImpl() {
        ComponentInfo sharedFolderInfoSourceCompInfo = new ComponentInfo(ISharedFolderInformationSource.COMP_NAME,
                                                                         DMSSharedFolderInformationSourceImpl.class.getName(),
                                                                         ISharedFolderInformationRelay.class.getName(),
                                                                         LifestyleType.SINGLETON_TYPE);

        // Is this really the best I can do???
        String runDirectory = System.getProperty("build.root.dir") + "/run/server";

        HashMapConfiguration serverLocatorConfig = new HashMapConfiguration();
        serverLocatorConfig.setProperty(FileSystemResourceLocatorImpl.ROOT_PATH_PARAM, runDirectory);
        ComponentInfo serverLocatorInfo = new ComponentInfo(DCCResourceLocators.SERVER_HOME_RESOURCE_LOCATOR,
                                                            FileSystemResourceLocatorImpl.class.getName(),
                                                            INamedResourceLocator.class.getName(),
                                                            LifestyleType.SINGLETON_TYPE,
                                                            serverLocatorConfig);
        INamedResourceLocator serverResourceLocator = (INamedResourceLocator) compMgr.getComponent(serverLocatorInfo);

        ISharedFolderInformationSource sharedFolderInfoSource = (ISharedFolderInformationSource) compMgr.getComponent(sharedFolderInfoSourceCompInfo);
        ISharedFolderCookie cookie = new SharedFolderInformationCookieImpl(new Date());
        ISharedFolderData sharedFolderData = sharedFolderInfoSource.getSharedFolderInformationUpdateSince(cookie);

        // Ideally we would test this by looking at the contents of
        // the sharedFolderData, but we haven't set the aliases.txt
        // file to anything in particular, so we don't know what it
        // is.  All we can do (for now) is check the consistency call
        // to call.

        if (sharedFolderData != null) {
            ISharedFolderData nextFolderData = sharedFolderInfoSource.getSharedFolderInformationUpdateSince(sharedFolderData.getCookie());

            assertNotNull("Shared folder contents changed during test", nextFolderData);
            assertEquals("Timestamp changed", sharedFolderData.getCookie().getTimestamp(), nextFolderData.getCookie().getTimestamp());
            assertEquals("Alias list should be empty", 0, nextFolderData.getAliases().length);
        } else {
            assertNull("Shared folder contents changed during test", sharedFolderInfoSource.getSharedFolderInformationUpdateSince(cookie));
        }
    }
}
