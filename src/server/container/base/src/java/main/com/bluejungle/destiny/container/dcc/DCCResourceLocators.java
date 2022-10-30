/*
 * Created on Feb 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

/**
 * This interface lists the names of the different resource locators in the
 * system. Any new resource locators that are added to the system should be
 * given a name here and intialized/access from the Component Manager using this
 * name.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcc/DCCResourceLocators.java#1 $
 */

public abstract class DCCResourceLocators {

    /*
     * Locator for resources (files) within the web app
     */
    public static final String WEB_APP_RESOURCE_LOCATOR = "WebAppResourceLocator";

    /*
     * Locator for files relative to the server root dir:
     */
    public static final String SERVER_HOME_RESOURCE_LOCATOR = "ServerHomeResourceLocator";

    /*
     * Locator for security files:
     */
    public static final String SECURITY_RESOURCE_LOCATOR = "KeyStoreResourceLocator";
    
    /*
     * Locator for temporary/work files:
     */
    public static final String WEB_APP_TEMP_DIR_RESOURCE_LOCATOR = "WebAppTemporaryDirectoryLocator";
}