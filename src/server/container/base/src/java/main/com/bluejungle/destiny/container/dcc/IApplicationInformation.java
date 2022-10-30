/*
 * Created on Nov 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

/**
 * This interface returns information about the application that could be
 * considered equivalent (currently on an as-needed basis) to the web
 * application context.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/base/src/java/main/com/bluejungle/destiny/container/dcc/IApplicationInformation.java#1 $
 */

public interface IApplicationInformation {

    public static final String COMP_NAME = "ApplicationInformation";
    
    /**
     * Returns the name of the web application
     * 
     * @return web application name
     */
    public String getApplicationName();
}