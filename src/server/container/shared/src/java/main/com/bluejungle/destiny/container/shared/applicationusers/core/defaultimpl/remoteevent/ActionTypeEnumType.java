/*
 * Created on Sep 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.core.defaultimpl.remoteevent;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This enum type lists the different actions that the user management change
 * notifications within DCC Components can involve
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/core/defaultimpl/remoteevent/ActionTypeEnumType.java#1 $
 */

public class ActionTypeEnumType extends EnumBase {

    /*
     * This action corresponds to the deletion of a group entry
     */
    public static final ActionTypeEnumType GROUP_DELETE = new ActionTypeEnumType("GroupDelete");

    /*
     * This action corresponds to the deletion of a user entry
     */
    public static final ActionTypeEnumType USER_DELETE = new ActionTypeEnumType("UserDelete");

    /**
     * Constructor
     * 
     * @param name
     */
    public ActionTypeEnumType(String name) {
        super(name);
    }

    /**
     * 
     * @param name
     * @return
     */
    public static ActionTypeEnumType getByName(String name) {
        return EnumBase.getElement(name, ActionTypeEnumType.class);
    }
}