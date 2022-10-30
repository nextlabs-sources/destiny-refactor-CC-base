/*
 * Created on Feb 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms;

/**
 * This interface lists the names of the different resource locators in the DMS
 * system. Any new resource locators that are added to the system should be
 * given a name here and intialized/access from the Component Manager using this
 * name.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/DMSResourceLocators.java#1 $
 */

public abstract class DMSResourceLocators {

    /*
     * Locator for resources (files) within the web app
     */
    public static final String CONFIGURATION_RESOURCE_LOCATOR = "ConfigurationResourceLocator";
}