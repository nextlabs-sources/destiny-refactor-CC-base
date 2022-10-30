/*
 * Created on Mar 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the inquiry condition implementation class. This class stores the HQL
 * expression for a condition as well as the corresponding named parameters (if
 * any) applicable to this HQL expression.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/InquiryConditionImpl.java#1 $
 */

class InquiryConditionImpl implements IInquiryCondition {

    private String hqlExpression;
    private Map paramArray = new HashMap();

    /**
     * Constructor
     */
    public InquiryConditionImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IInquiryCondition#getHQLExpression()
     */
    public String getHQLExpression() {
        return this.hqlExpression;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IInquiryCondition#getNamedParameters()
     */
    public Map getNamedParameters() {
        return this.paramArray;
    }

    /**
     * Sets the HQL expression
     * 
     * @param newExpression
     *            the new expression to set
     */
    public void setHQLExpression(String newExpression) {
        this.hqlExpression = newExpression;
    }
}