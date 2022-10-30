/*
 * Created on Feb 7, 2005
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.destiny.agent.profile;

import org.exolab.castor.mapping.GeneralizedFieldHandler;

import com.bluejungle.domain.types.ActionTypeDTO;

/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/profile/ActionTypeCastorHandler.java#1 $:
 */

public class ActionTypeCastorHandler extends GeneralizedFieldHandler {

    /**
     * @see org.exolab.castor.mapping.GeneralizedFieldHandler#convertUponGet(java.lang.Object)
     */
    public Object convertUponGet(Object value) {
        if (value == null){
            return (null);
        }
        ActionTypeDTO[] actionTypes = (ActionTypeDTO[]) value;
        String actions = "";
        for (int i = 0; i < actionTypes.length; i++) {
            if (i != 0) {
                actions += ",";                
            }
            actions += actionTypes[i].getValue();
        }
        return actions;
    }

    /**
     * @see org.exolab.castor.mapping.GeneralizedFieldHandler#convertUponSet(java.lang.Object)
     */
    public Object convertUponSet(Object value) {
        String[] actionNameArray = ((String) value).split(",");
        ActionTypeDTO[] actionTypeArray = new ActionTypeDTO [actionNameArray.length];
        for (int i = 0; i < actionNameArray.length; i++) {
            actionTypeArray[i] = ActionTypeDTO.fromString(actionNameArray[i]);
        }
        return actionTypeArray;
    }

    /**
     * @see org.exolab.castor.mapping.GeneralizedFieldHandler#getFieldType()
     */
    public Class getFieldType() {
        return ActionTypeDTO[].class;
    }

}
