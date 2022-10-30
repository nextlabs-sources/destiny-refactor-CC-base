/*
 * Created on Apr 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.usermgr;


/**
 * This is the policy manager query interface. It exposes search and search
 * specifications for the policy manager.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/usermgr/IUserMgrQuerySpec.java#1 $
 */

public interface IUserMgrQuerySpec {

    /**
     * Returns the maximum number of rows to return (0 means no limit)
     * 
     * @return the maximum number of rows to return (0 means no limit)
     */
    public int getLimit();

    /**
     * Returns array collection of search spec terms
     * 
     * @return an array of search spec terms
     */
    public IUserMgrQueryTerm[] getSearchSpecTerms();

    /**
     * Returns an array of sort spec terms
     * 
     * @return an array of sort spec terms
     */
    public IUserMgrSortTerm[] getSortSpecTerms();
}
