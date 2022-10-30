/*
 * Created on Mar 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

/**
 * This interface represents a policy object. It exposes the policy attributes
 * that the inquiry manager needs to know about.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/IPolicy.java#2 $
 */

public interface IPolicy {

    /**
     * Returns the policy folder name
     * 
     * @return the policy folder name
     */
    public String getFolderName();

    /**
     * Returns the policy full name (folder + name)
     * 
     * @return the policy full name (folder + name)
     */
    public String getFullName();

    /**
     * Returns the policy id
     * 
     * @return the policy id
     */
    public Long getId();

    /**
     * Returns the policy name
     * 
     * @return the policy name
     */
    public String getName();
}