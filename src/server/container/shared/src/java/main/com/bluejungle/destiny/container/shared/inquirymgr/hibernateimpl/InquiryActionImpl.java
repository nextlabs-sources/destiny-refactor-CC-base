/*
 * Created on Feb 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryAction;
import com.bluejungle.domain.action.ActionEnumType;

/**
 * This is the inquiry action implementation class, for in memory inquiries.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/InquiryActionImpl.java#1 $
 */

class InquiryActionImpl implements IInquiryAction {

    private IInquiry inquiry;
    private ActionEnumType actionType;

    /**
     * Constructor
     */
    public InquiryActionImpl() {
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiryAction#getActionType()
     */
    public ActionEnumType getActionType() {
        return this.actionType;
    }

    /**
     * Returns the inquiry associated with the inquiry action
     * 
     * @return the inquiry associated with the inquiry action
     */
    protected IInquiry getInquiry() {
        return this.inquiry;
    }

    /**
     * Sets the action type
     * 
     * @param newActionType
     *            new action type to set
     */
    protected void setActionType(ActionEnumType newActionType) {
        this.actionType = newActionType;
    }

    /**
     * Sets the inquiry object.
     * 
     * @param newInquiry
     *            new inquiry to set
     */
    protected void setInquiry(IInquiry newInquiry) {
        this.inquiry = newInquiry;
    }
}