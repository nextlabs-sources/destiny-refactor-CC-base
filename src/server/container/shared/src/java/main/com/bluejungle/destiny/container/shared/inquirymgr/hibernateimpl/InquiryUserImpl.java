/*
 * Created on Mar 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryUser;

/**
 * This is the "on the fly" implementation of the inquiry user object. There is
 * one instance of this class for each user (or user class?) associated with an
 * inquiry.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/InquiryUserImpl.java#1 $
 */

class InquiryUserImpl implements IInquiryUser {

    private IInquiry inquiry;
    private String displayName;

    /**
     * Constructor
     */
    public InquiryUserImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiryUser#getDisplayName()
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Returns the inquiry associated with this user
     * 
     * @return the inquiry associated with this user
     */
    protected IInquiry getInquiry() {
        return this.inquiry;
    }

    /**
     * Sets the name of the user associated with this inquiry
     * 
     * @param newName
     *            name to set
     */
    protected void setDisplayName(String newName) {
        this.displayName = newName;
    }

    /**
     * Sets the inquiry associated with this user object
     * 
     * @param newInquiry
     *            new inquiry to set
     */
    protected void setInquiry(IInquiry newInquiry) {
        this.inquiry = newInquiry;
    }
}