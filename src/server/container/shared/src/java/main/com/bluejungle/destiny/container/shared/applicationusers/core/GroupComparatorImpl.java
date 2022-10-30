/*
 * Created on Jul 19, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core;

import java.util.Comparator;


/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/repository/openldapimpl/OpenLDAPApplicationUserComparatorImpl.java#1 $
 */

public class GroupComparatorImpl implements Comparator<IGroup> {

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compare(IGroup lhs, IGroup rhs) {
        int compareResult = lhs.getTitle().toUpperCase().compareTo(rhs.getTitle().toUpperCase());

        // If 2 groups have the same title, we then compare using the domain
        // name. If same still, they are really the same group.
        if (compareResult == 0) {
            compareResult = lhs.getDomainName().toUpperCase().compareTo(rhs.getDomainName().toUpperCase());
        }
        return compareResult;
    }
}