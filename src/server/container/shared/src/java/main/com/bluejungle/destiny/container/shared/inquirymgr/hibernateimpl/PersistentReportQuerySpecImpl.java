/*
 * Created on Apr 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQuerySpec;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQueryTerm;
import com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrSortTerm;

/**
 * Implementation class of the persistent report query specification.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/PersistentReportQuerySpecImpl.java#1 $
 */

public class PersistentReportQuerySpecImpl implements IPersistentReportMgrQuerySpec {

    private IPersistentReportMgrQueryTerm[] searchSpecTerms;
    private IPersistentReportMgrSortTerm[] sortSpecTerms;

    /**
     * Constructor
     * 
     * @param newSearchTerms
     *            search terms to use
     * @param newSortTerms
     *            sort terms to use
     */
    public PersistentReportQuerySpecImpl(IPersistentReportMgrQueryTerm[] newSearchTerms, IPersistentReportMgrSortTerm[] newSortTerms) {
        this.searchSpecTerms = newSearchTerms;
        this.sortSpecTerms = newSortTerms;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQuerySpec#getSearchSpecTerms()
     */
    public IPersistentReportMgrQueryTerm[] getSearchSpecTerms() {
        return this.searchSpecTerms;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IPersistentReportMgrQuerySpec#getSortSpecTerms()
     */
    public IPersistentReportMgrSortTerm[] getSortSpecTerms() {
        return this.sortSpecTerms;
    }

}