/*
 * Created on Feb 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

import com.bluejungle.domain.action.ActionEnumType;

/**
 * This interface is implemented by the inquiry action object. An inquiry action
 * represent a particular user action to search on when querying the activity
 * database.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IInquiryAction.java#1 $
 */

public interface IInquiryAction {

    /**
     * Returns the action type
     * 
     * @return the action type
     */
    public ActionEnumType getActionType();
}