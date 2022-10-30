/*
 * Created on Mar 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryResource;

/**
 * This is the "on the fly" representation of the inquiry resource data object.
 * Each instance of this class represents a resource associated with an inquiry.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/InquiryResourceImpl.java#1 $
 */

class InquiryResourceImpl implements IInquiryResource {

    private IInquiry inquiry;
    private String name;

    /**
     * Constructor
     */
    public InquiryResourceImpl() {
        super();
    }

    /**
     * Returns the inquiry associated with this resource
     * 
     * @return the inquiry associated with this resource
     */
    protected IInquiry getInquiry() {
        return this.inquiry;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiryResource#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the inquiry object associated with this resource
     * 
     * @param newInquiry
     *            inquiry object to set.
     */
    protected void setInquiry(IInquiry newInquiry) {
        this.inquiry = newInquiry;
    }

    /**
     * Sets the resource name
     * 
     * @param newName
     *            name to set
     */
    protected void setName(String newName) {
        this.name = newName;
    }
}