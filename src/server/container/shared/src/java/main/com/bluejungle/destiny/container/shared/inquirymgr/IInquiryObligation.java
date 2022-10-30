/*
 * Created on Feb 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

/**
 * This interface is implemented by the inquiry obligation object. An inquiry
 * obligation represent a particular policy decision to search on when querying
 * the activity database.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IInquiryObligation.java#1 $
 */

public interface IInquiryObligation {

    /**
     * Returns the obligation name
     * 
     * @return the obligation name
     */
    public String getName();
}