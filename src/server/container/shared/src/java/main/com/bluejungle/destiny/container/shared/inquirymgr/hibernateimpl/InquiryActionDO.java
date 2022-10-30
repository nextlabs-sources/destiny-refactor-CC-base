/*
 * Created on Feb 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryAction;

/**
 * This is the persistent inquiry action class implementation.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/InquiryActionDO.java#1 $
 */

public class InquiryActionDO extends InquiryActionImpl implements IInquiryAction {

    private Long id;

    /**
     * Constructor
     */
    public InquiryActionDO() {
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentInquiryAction#getId()
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