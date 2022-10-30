/*
 * Created on Sep 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder;

/**
 * This is a special query implementation that does not take into account all
 * aspects of the query definition. All group by and sort are ignored in this
 * query when generating the HQL.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hqlbuilder/SimpleQueryImpl.java#1 $
 */

public class SimpleQueryImpl extends QueryImpl {

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.QueryImpl#generateGroupByStatement(java.lang.StringBuffer)
     */
    protected void generateGroupByStatement(final StringBuffer currentBuffer) {
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.QueryImpl#generateOrderByStatement(java.lang.StringBuffer)
     */
    protected void generateOrderByStatement(final StringBuffer currentBuffer) {
    }
}