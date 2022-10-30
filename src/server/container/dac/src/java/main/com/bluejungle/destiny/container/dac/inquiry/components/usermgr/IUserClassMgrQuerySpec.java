/*
 * Created on Apr 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.usermgr;

/**
 * This is the user class manager query specification interface. It exposes
 * search and sort specifications for the user manager.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/usermgr/IUserClassMgrQuerySpec.java#1 $
 */

public interface IUserClassMgrQuerySpec {

    /**
     * Returns array collection of search spec terms
     * 
     * @return an array of search spec terms
     */
    public IUserClassMgrQueryTerm[] getSearchSpecTerms();

    /**
     * Returns an array of sort spec terms
     * 
     * @return an array of sort spec terms
     */
    public IUserClassMgrSortTerm[] getSortSpecTerms();
}