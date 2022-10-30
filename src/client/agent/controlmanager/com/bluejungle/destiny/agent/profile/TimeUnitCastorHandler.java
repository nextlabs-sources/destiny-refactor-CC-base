/*
 * Created on Feb 7, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.profile;

import org.exolab.castor.mapping.GeneralizedFieldHandler;

import com.bluejungle.destiny.framework.types.TimeUnits;

/**
 * @author fuad
 * @version $Id:
 *          //depot/personal/ihanen/main/Destiny/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/profile/TimeUnitCastorHandler.java#1 $:
 */

public class TimeUnitCastorHandler extends GeneralizedFieldHandler {

    /**
     * @see org.exolab.castor.mapping.GeneralizedFieldHandler#convertUponGet(java.lang.Object)
     */
    public Object convertUponGet(Object value) {
        if (value == null) {
            return (null);
        }
        return value.toString();
    }

    /**
     * @see org.exolab.castor.mapping.GeneralizedFieldHandler#convertUponSet(java.lang.Object)
     */
    public Object convertUponSet(Object value) {
        return TimeUnits.fromString((String) value);
    }

    /**
     * @see org.exolab.castor.mapping.GeneralizedFieldHandler#getFieldType()
     */
    public Class getFieldType() {
        return TimeUnits.class;
    }

}
