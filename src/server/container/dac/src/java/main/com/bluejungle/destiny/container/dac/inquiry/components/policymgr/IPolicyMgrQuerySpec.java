/*
 * Created on Apr 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.policymgr;


/**
 * This is the policy manager query interface. It exposes search and search
 * specifications for the policy manager.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/policymgr/IPolicyMgrQuerySpec.java#1 $
 */

public interface IPolicyMgrQuerySpec {

    /**
     * Returns array collection of search spec terms
     * 
     * @return an array of search spec terms
     */
    public IPolicyMgrQueryTerm[] getSearchSpecTerms();

    /**
     * Returns an array of sort spec terms
     * 
     * @return an array of sort spec terms
     */
    public IPolicyMgrSortTerm[] getSortSpecTerms();
}