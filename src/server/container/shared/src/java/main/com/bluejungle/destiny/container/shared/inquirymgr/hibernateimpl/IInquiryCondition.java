/*
 * Created on Mar 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Map;

/**
 * This is the inquiry condition interface. An inquiry condition represents a
 * condition for a given query, and is used to generate Hibernate queries
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/IInquiryCondition.java#1 $
 */

interface IInquiryCondition {

    /**
     * Returns the HQL expression for the condition
     * 
     * @return the HQL expression representing this condition
     */
    public String getHQLExpression();

    /**
     * Returns the list of parameters (and their associated value) set by this
     * condition on the HQL expression.
     * 
     * @return the list of parameters (and their associated value)
     */
    public Map getNamedParameters();
}