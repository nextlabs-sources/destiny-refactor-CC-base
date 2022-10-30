package com.bluejungle.pf.destiny.lifecycle;

import com.bluejungle.pf.domain.epicenter.action.IAction;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lifecycle/PolicyActionsDescriptor.java#1 $
 */

public class PolicyActionsDescriptor {
    /** Action object */
    private final IAction action;
    
    /** Displayable name of action */
    private final String displayName;

    /** Name of group to which action belongs (e.g. Communication, Data Access, etc) */
    private final String category;

    /**
     * Constructs a <code>PolicyActionsDescriptor</code> with the specified
     * <code>action</code>, <code>displayName</code>, and <code>category</code>
     * @param action the action 
     * @param displayName the display name of the action
     * @param category name of group to which action belongs
     */
    public PolicyActionsDescriptor(IAction action, String displayName, String category) {
        this.action = action;
        this.displayName = displayName;
        this.category = category;
    }

    /**
     * Accesses the internal action name
     * @return the action name
     */
    public IAction getAction() {
        return action;
    }

    /**
     * Accesses the display name of the action
     * @return the display name of the action
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Accesses the category of the action
     * @return the category of the action
     */
    public String getCategory() {
        return category;
    }
    
}
