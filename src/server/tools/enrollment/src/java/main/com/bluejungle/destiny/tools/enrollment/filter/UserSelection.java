/*
 * Created on Nov 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment.filter;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/filter/UserSelection.java#1 $
 */

public class UserSelection extends AbstractSelection {

    /**
     * Constructor
     *  
     */
    public UserSelection(String expression) {
        super(expression);
    }

    /**
     * @see com.bluejungle.ldap.tools.importcoordinator.filter.AbstractSelection#getDirectReferenceAttribute()
     */
    protected String getDirectReferenceAttribute() {
        return SelectiveFilterConfiguration.SINGLETON.getUserReferenceAttribute();
    }
}