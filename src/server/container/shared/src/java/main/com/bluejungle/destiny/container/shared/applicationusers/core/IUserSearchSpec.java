/*
 * Created on Jul 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/core/IUserSearchSpec.java#1 $
 */

public interface IUserSearchSpec {

    /**
     * Returns the search string for last-name-starts-with
     * 
     * @return search string
     */
    public String getLastNameStartsWith();
}