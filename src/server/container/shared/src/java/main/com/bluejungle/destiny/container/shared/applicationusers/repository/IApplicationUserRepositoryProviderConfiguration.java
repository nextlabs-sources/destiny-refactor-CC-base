/*
 * Created on Jun 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository;

import java.util.Properties;

/**
 * This interface represents the configuration for the local user repository -
 * i.e OpenLDAP repository for local/imported Application Users.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/framework/src/java/main/com/bluejungle/framework/applicationusers/IApplicationUserRepositoryConfiguration.java#1 $
 */

public interface IApplicationUserRepositoryProviderConfiguration {

    public String getUserRepositoryProviderClassName();

    public Properties getProperties();
}