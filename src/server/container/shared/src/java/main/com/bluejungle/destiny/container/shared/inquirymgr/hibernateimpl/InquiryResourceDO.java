/*
 * Created on Mar 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryResource;

/**
 * This is the persistent implementation of the inquiry resource data object.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/InquiryResourceDO.java#1 $
 */

public class InquiryResourceDO extends InquiryResourceImpl implements IInquiryResource {

    private Long id;

    /**
     * Constructor
     */
    public InquiryResourceDO() {
        super();
    }

    /**
     * Returns the resource id.
     * 
     * @return the resource id.
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the resource id
     * 
     * @param id
     *            the resource id to set.
     */
    protected void setId(Long id) {
        this.id = id;
    }
}