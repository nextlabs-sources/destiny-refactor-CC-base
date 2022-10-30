/*
 * Created on Apr 13, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

/**
 * This is the persistent report manager query interface. It exposes search and
 * search specifications for the persistent reports.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/policymgr/IPolicyMgrQuerySpec.java#1 $
 */

public interface IPersistentReportMgrQuerySpec {

    /**
     * Returns an array collection of search spec terms
     * 
     * @return an array of search spec terms
     */
    public IPersistentReportMgrQueryTerm[] getSearchSpecTerms();

    /**
     * Returns an array of sort spec terms
     * 
     * @return an array of sort spec terms
     */
    public IPersistentReportMgrSortTerm[] getSortSpecTerms();
}