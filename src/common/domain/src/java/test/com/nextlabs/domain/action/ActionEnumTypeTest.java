/*
 * Created on Mar 28, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.domain.action;

import junit.framework.TestCase;

import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.action.hibernateimpl.ActionEnumUserType;


/**
 * This class tests the hardcoded list of actions.  WARNING: please make sure that 
 * you change all of the following places (if applicable) when this test case breaks:
 * 
 * //depot/main/Destiny/main/src/common/resources/src/bundle/CommonMessages.properties
 * //depot/main/Destiny/main/src/common/domain/src/java/main/com/bluejungle/domain/action/ActionEnumType.java
 * //depot/main/Destiny/main/src/common/domain/src/java/main/com/bluejungle/domain/action/hibernateimpl/ActionEnumUserType.java
 * //depot/main/Destiny/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/action/DAction.java
 * //depot/main/Destiny/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/action/IDAction.java
 * //depot/main/Destiny/main/src/server/apps/webFramework/src/java/main/com/bluejungle/destiny/webui/framework/faces/UIActionType.java
 * //depot/main/Destiny/main/src/server/container/dac/src/wsdl/com/bluejungle/destiny/types/ActionTypes.v1.xsd
 * //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/AgentManager.java
 * //depot/main/Destiny/main/src/server/tools/reporterData/src/java/main/com/bluejungle/destiny/tools/reporterdata/ReporterData.java
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/test/com/nextlabs/domain/action/ActionEnumTypeTest.java#1 $
 */

public class ActionEnumTypeTest extends TestCase {

    private static ActionEnumUserType actionEnumUserType = new ActionEnumUserType();
    
    /**
     * WARNING: if this test breaks, please see the comments on to the top
     * of this class
     */
    public void testActionEnumType(){
        assertEquals("Action should be CHANGE_ATTRIBUTES", "CHANGE_ATTRIBUTES", ActionEnumType.getActionEnum(0).toString());
        assertEquals("Action should be CHANGE_SECURITY", "CHANGE_SECURITY", ActionEnumType.getActionEnum(1).toString());
        assertEquals("Action should be COPY", "COPY", ActionEnumType.getActionEnum(2).toString());
        assertEquals("Action should be PASTE", "PASTE", ActionEnumType.getActionEnum(3).toString());
        assertEquals("Action should be DELETE", "DELETE", ActionEnumType.getActionEnum(4).toString());
        assertEquals("Action should be EMBED", "EMBED", ActionEnumType.getActionEnum(5).toString());
        assertEquals("Action should be MOVE", "MOVE", ActionEnumType.getActionEnum(6).toString());
        assertEquals("Action should be PRINT", "PRINT", ActionEnumType.getActionEnum(7).toString());
        assertEquals("Action should be OPEN", "OPEN", ActionEnumType.getActionEnum(8).toString());
        assertEquals("Action should be EDIT", "EDIT", ActionEnumType.getActionEnum(9).toString());
        assertEquals("Action should be RENAME", "RENAME", ActionEnumType.getActionEnum(10).toString());
        assertEquals("Action should be EMAIL", "EMAIL", ActionEnumType.getActionEnum(11).toString());
        assertEquals("Action should be IM", "IM", ActionEnumType.getActionEnum(12).toString());
        assertEquals("Action should be START_AGENT", "START_AGENT", ActionEnumType.getActionEnum(13).toString());
        assertEquals("Action should be STOP_AGENT", "STOP_AGENT", ActionEnumType.getActionEnum(14).toString());
        assertEquals("Action should be AGENT_USER_LOGIN", "AGENT_USER_LOGIN", ActionEnumType.getActionEnum(15).toString());
        assertEquals("Action should be AGENT_USER_LOGOUT", "AGENT_USER_LOGOUT", ActionEnumType.getActionEnum(16).toString());
        assertEquals("Action should be ACCESS_AGENT_CONFIG", "ACCESS_AGENT_CONFIG", ActionEnumType.getActionEnum(17).toString());
        assertEquals("Action should be ACCESS_AGENT_LOGS", "ACCESS_AGENT_LOGS", ActionEnumType.getActionEnum(18).toString());
        assertEquals("Action should be ACCESS_AGENT_BINARIES", "ACCESS_AGENT_BINARIES", ActionEnumType.getActionEnum(19).toString());
        assertEquals("Action should be ACCESS_AGENT_BUNDLE", "ACCESS_AGENT_BUNDLE", ActionEnumType.getActionEnum(20).toString());
        assertEquals("Action should be ABNORMAL_AGENT_SHUTDOWN", "ABNORMAL_AGENT_SHUTDOWN", ActionEnumType.getActionEnum(21).toString());
        assertEquals("Action should be INVALID_BUNDLE", "INVALID_BUNDLE", ActionEnumType.getActionEnum(22).toString());
        assertEquals("Action should be BUNDLE_RECEIVED", "BUNDLE_RECEIVED", ActionEnumType.getActionEnum(23).toString());
        assertEquals("Action should be EXPORT", "EXPORT", ActionEnumType.getActionEnum(24).toString());
        assertEquals("Action should be ATTACH", "ATTACH", ActionEnumType.getActionEnum(25).toString());
        assertEquals("Action should be RUN", "RUN", ActionEnumType.getActionEnum(26).toString());
        assertEquals("Action should be AVDCALL", "AVDCALL", ActionEnumType.getActionEnum(27).toString());
        assertEquals("Action should be MEETING", "MEETING", ActionEnumType.getActionEnum(28).toString());
        assertEquals("Action should be PRESENCE", "PRESENCE", ActionEnumType.getActionEnum(29).toString());
        assertEquals("Action should be SHARE", "SHARE", ActionEnumType.getActionEnum(30).toString());
        assertEquals("Action should be RECORD", "RECORD", ActionEnumType.getActionEnum(31).toString());
        assertEquals("Action should be QUESTION", "QUESTION", ActionEnumType.getActionEnum(32).toString());
        assertEquals("Action should be VOICE", "VOICE", ActionEnumType.getActionEnum(33).toString());
        assertEquals("Action should be VIDEO", "VIDEO", ActionEnumType.getActionEnum(34).toString());
        assertEquals("Action should be JOIN", "JOIN", ActionEnumType.getActionEnum(35).toString());
    }    
    
    /**
     * WARNING: if this test breaks, please see the comments on to the top
     * of this class
     */
    public void testActionEnumUserType(){
        assertEquals("The action code should be Ca", "Ca", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(0)));
        assertEquals("The action code should be Cs", "Cs", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(1)));
        assertEquals("The action code should be Co", "Co", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(2)));
        assertEquals("The action code should be CP", "CP", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(3)));
        assertEquals("The action code should be De", "De", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(4)));
        assertEquals("The action code should be Em", "Em", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(5)));
        assertEquals("The action code should be Mo", "Mo", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(6)));
        assertEquals("The action code should be Pr", "Pr", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(7)));
        assertEquals("The action code should be Op", "Op", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(8)));
        assertEquals("The action code should be Ed", "Ed", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(9)));
        assertEquals("The action code should be Rn", "Rn", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(10)));
        assertEquals("The action code should be SE", "SE", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(11)));
        assertEquals("The action code should be SI", "SI", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(12)));
        assertEquals("The action code should be Au", "Au", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(13)));
        assertEquals("The action code should be Ad", "Ad", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(14)));
        assertEquals("The action code should be Uu", "Uu", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(15)));
        assertEquals("The action code should be Ud", "Ud", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(16)));
        assertEquals("The action code should be Ac", "Ac", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(17)));
        assertEquals("The action code should be Al", "Al", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(18)));
        assertEquals("The action code should be Ab", "Ab", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(19)));
        assertEquals("The action code should be Ap", "Ap", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(20)));
        assertEquals("The action code should be As", "As", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(21)));
        assertEquals("The action code should be Ib", "Ib", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(22)));
        assertEquals("The action code should be Br", "Br", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(23)));
        assertEquals("The action code should be Ex", "Ex", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(24)));
        assertEquals("The action code should be At", "At", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(25)));
        assertEquals("The action code should be Ru", "Ru", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(26)));
        assertEquals("The action code should be Av", "Av", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(27)));
        assertEquals("The action code should be Me", "Me", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(28)));
        assertEquals("The action code should be Ps", "Ps", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(29)));
        assertEquals("The action code should be Sh", "Sh", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(30)));
        assertEquals("The action code should be Re", "Re", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(31)));
        assertEquals("The action code should be Qu", "Qu", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(32)));
        assertEquals("The action code should be Vo", "Vo", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(33)));
        assertEquals("The action code should be Vi", "Vi", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(34)));
        assertEquals("The action code should be Jo", "Jo", actionEnumUserType.getCodeByType(ActionEnumType.getActionEnum(35)));
    }
}
