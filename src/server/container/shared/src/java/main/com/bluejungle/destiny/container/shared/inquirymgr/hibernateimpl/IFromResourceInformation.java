/*
 * Created on Jan 25, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;

/**
 * This is the from resource information interface. It exposes various
 * information about "from resource" gathered from the agents.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/IFromResourceInformation.java#1 $
 */

public interface IFromResourceInformation extends IResourceInformation {

    /**
     * Returns the resource creation date
     * 
     * @return the resource creation date
     */
    public Calendar getCreatedDate();

    /**
     * Returns the resource last modification date
     * 
     * @return the resource last modification date
     */
    public Calendar getModifiedDate();

    /**
     * Returns the resource size
     * 
     * @return the resource size
     */
    public Long getSize();

    /**
     * Returns the resource owner id
     * 
     * @return the resource owner id
     */
    public String getOwnerId();

}
