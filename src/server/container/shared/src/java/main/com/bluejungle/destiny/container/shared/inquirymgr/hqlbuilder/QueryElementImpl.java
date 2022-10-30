/*
 * Created on Apr 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is the query element class implementation. A query element is typically
 * returned as a fragment of a larger HQL query. All elements are eventually
 * assembled to produce a final HQL query.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hqlbuilder/QueryElementImpl.java#1 $
 */

public class QueryElementImpl implements IQueryElement {

    private Set conditions = new LinkedHashSet();
    private Set grouping = new LinkedHashSet();
    private Set orderBys = new LinkedHashSet();
    private Map namedParameters = new HashMap();
    private Set selects = new LinkedHashSet();

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQueryElement#getConditions()
     */
    public Set getConditions() {
        return this.conditions;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQueryElement#getGroupings()
     */
    public Set getGroupings() {
        return this.grouping;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQueryElement#getNamedParameters()
     */
    public Map getNamedParameters() {
        return this.namedParameters;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQueryElement#getOrderBys()
     */
    public Set getOrderBys() {
        return this.orderBys;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.IQueryElement#getSelects()
     */
    public Set getSelects() {
        return this.selects;
    }
}