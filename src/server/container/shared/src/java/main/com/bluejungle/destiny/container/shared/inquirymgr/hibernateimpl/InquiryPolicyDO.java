/*
 * Created on Mar 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryPolicy;

/**
 * This is the persistent version of the inquiry policy data object
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/InquiryPolicyDO.java#1 $
 */

public class InquiryPolicyDO extends InquiryPolicyImpl implements IInquiryPolicy {

    private Long id;

    /**
     * Constructor
     */
    public InquiryPolicyDO() {
        super();
    }

    /**
     * Returns the id.
     * 
     * @return the id.
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the id
     * 
     * @param id
     *            The id to set.
     */
    protected void setId(Long id) {
        this.id = id;
    }

}