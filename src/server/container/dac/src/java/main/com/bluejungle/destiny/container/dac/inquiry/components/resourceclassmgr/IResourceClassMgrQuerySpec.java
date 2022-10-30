/*
 * Created on Apr 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.resourceclassmgr;


/**
 * This is the resource class manager query interface. It exposes search and search
 * specifications for the resource class manager.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/bluejungle/destiny/container/dac/inquiry/components/resourceclassmgr/IResourceClassMgrQuerySpec.java#1 $
 */

public interface IResourceClassMgrQuerySpec {

    /**
     * Returns array collection of search spec terms
     * 
     * @return an array of search spec terms
     */
    public IResourceClassMgrQueryTerm[] getSearchSpecTerms();

    /**
     * Returns an array of sort spec terms
     * 
     * @return an array of sort spec terms
     */
    public IResourceClassMgrSortTerm[] getSortSpecTerms();
}