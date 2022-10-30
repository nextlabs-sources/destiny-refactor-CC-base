/*
 * Created on Feb 21, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;


/**
 * This is the result data interface. This interface acts as a base for result
 * rows. All the report result types extend this base interface.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IResultData.java#1 $
 */

public interface IResultData {

    /**
     * Returns the record id
     * 
     * @return the record id
     */
    public Long getId();
}