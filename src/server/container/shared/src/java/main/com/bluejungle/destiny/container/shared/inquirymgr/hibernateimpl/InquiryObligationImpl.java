/*
 * Created on Mar 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryObligation;

/**
 * This is the "on the fly" implementation of the inquiry obligation class. An
 * inquiry obligation represents a particular obligation used in an inquiry.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/InquiryObligationImpl.java#1 $
 */

class InquiryObligationImpl implements IInquiryObligation {

    private IInquiry inquiry;
    private String name;

    /**
     * Constructor
     */
    public InquiryObligationImpl() {
        super();
    }

    /**
     * Returns the inquiry associated with this obligation
     * 
     * @return the inquiry associated with this obligation
     */
    protected IInquiry getInquiry() {
        return this.inquiry;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiryObligation#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the inquiry associated with this obligation
     * 
     * @param newInquiry
     *            inquiry to be set
     */
    protected void setInquiry(IInquiry newInquiry) {
        this.inquiry = newInquiry;
    }

    /**
     * Sets the obligation name
     * 
     * @param obligationName
     *            name of the obligation to be set
     */
    protected void setName(String obligationName) {
        this.name = obligationName;
    }
}