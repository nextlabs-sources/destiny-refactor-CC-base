/*
 * Created on Aug 16, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

/**
 * This is the persistent implementation of the inquiry policy decision data
 * object. Each instance of this class represents a policy decision record
 * related to an inquiry object.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/InquiryPolicyDecisionDO.java#1 $
 */

public class InquiryPolicyDecisionDO extends InquiryPolicyDecisionImpl {

    private Long id;

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