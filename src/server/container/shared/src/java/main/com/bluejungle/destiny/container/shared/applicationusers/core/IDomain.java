/*
 * Created on Jun 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/framework/src/java/main/com/bluejungle/framework/applicationusers/IDomain.java#1 $
 */

public interface IDomain {

    /**
     * Returns the name of the domain
     * 
     * @return domain name
     */
    public String getName();

    /**
     * Determines if this domain is equal to another domain.
     * 
     * @param o
     * @return equality
     */
    public boolean equals(Object o);
}