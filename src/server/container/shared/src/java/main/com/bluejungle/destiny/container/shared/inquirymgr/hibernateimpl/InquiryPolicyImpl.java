/*
 * Created on Mar 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryPolicy;

/**
 * This is the inquiry policy implementation class. An inquiry policy refers to
 * a name of a policy that has to be taken into account within an inquiry. For
 * example, the user may want to query on allow actions for policy "A", "B", and
 * "C". A, B and C are inquiry policy instances.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/InquiryPolicyImpl.java#1 $
 */

class InquiryPolicyImpl implements IInquiryPolicy {

    private IInquiry inquiry;
    private String name;

    /**
     * Constructor
     */
    public InquiryPolicyImpl() {
        super();
    }

    /**
     * Returns the inquiry associated with this policy
     * 
     * @return the inquiry associated with this policy
     */
    protected IInquiry getInquiry() {
        return this.inquiry;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiryPolicy#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the inquiry object
     * 
     * @param newInquiry
     *            inquiry object to set
     */
    protected void setInquiry(IInquiry newInquiry) {
        this.inquiry = newInquiry;
    }

    /**
     * Sets the policy name
     * 
     * @param newName
     *            the policy name to set
     */
    protected void setName(String newName) {
        this.name = newName;
    }
}