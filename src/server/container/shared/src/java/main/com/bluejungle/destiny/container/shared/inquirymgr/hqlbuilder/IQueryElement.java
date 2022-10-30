/*
 * Created on Apr 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder;

import java.util.Map;
import java.util.Set;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hqlbuilder/IQueryElement.java#1 $
 */

public interface IQueryElement {

    /**
     * Returns a set of HQL condition
     * 
     * @return a set of IConditionElement
     */
    public Set getConditions();

    /**
     * Returns a set of grouping elements
     * 
     * @return a set of IGroupingElement
     */
    public Set getGroupings();

    /**
     * Returns the list of parameters (and their associated value) set by this
     * condition on the HQL expression.
     * 
     * @return the list of parameters (and their associated value)
     */
    public Map getNamedParameters();

    /**
     * Returns the set of order by
     * 
     * @return a set of IOrderByElement
     */
    public Set getOrderBys();

    /**
     * Returns the set of items that should be selected
     * 
     * @return a set of ISelectElement
     */
    public Set getSelects();
}