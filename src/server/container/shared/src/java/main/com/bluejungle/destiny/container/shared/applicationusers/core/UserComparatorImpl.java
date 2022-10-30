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
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/core/UserComparatorImpl.java#1 $
 */

public class UserComparatorImpl implements Comparator<IUser> {

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compare(IUser lhs, IUser rhs) {
        int compareResult = lhs.getLastName().toUpperCase().compareTo(rhs.getLastName().toUpperCase());

        // If 2 users have the same last name, we then compare using the first
        // name. If same still, we compare using unique name. At that point, the
        // entries cannot be equal unless they are really the same user.
        if (compareResult == 0) {
            compareResult = lhs.getFirstName().toUpperCase().compareTo(rhs.getFirstName().toUpperCase());
            if (compareResult == 0) {
                compareResult = lhs.getUniqueName().toUpperCase().compareTo(rhs.getUniqueName().toUpperCase());
            }
        }
        return compareResult;
    }
}