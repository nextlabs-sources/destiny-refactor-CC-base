/*
 * Created on Apr 9, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryApplication;

/**
 * This is the "in memory" implementation of the inquiry application object.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/InquiryApplicationImpl.java#1 $
 */

class InquiryApplicationImpl implements IInquiryApplication {

    private IInquiry inquiry;
    private String name;

    /**
     * Constructor
     */
    public InquiryApplicationImpl() {
    }

    /**
     * Returns the inquiry object associated with this application
     * 
     * @return the inquiry object associated with this application
     */
    protected IInquiry getInquiry() {
        return this.inquiry;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiryApplication#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the inquiry object
     * 
     * @param newInquiry
     *            the inquiry to set
     */
    protected void setInquiry(IInquiry newInquiry) {
        this.inquiry = newInquiry;
    }

    /**
     * Sets the application name (or expression)
     * 
     * @param newName
     *            new expression or name to set
     */
    protected void setName(String newName) {
        this.name = newName;
    }
}