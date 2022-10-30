/*
 * Created on Mar 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.action.hibernateimpl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.bluejungle.destiny.server.shared.configuration.IActionConfigDO;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.framework.datastore.hibernate.usertypes.EnumUserType;

/**
 * This is the user hibernate class for the action type. It allows the various
 * action types to be stored in the database.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/alpha_demo/Destiny/main/src/common/domain/src/java/main/com/bluejungle/domain/action/hibernateimpl/ActionEnumUserType.java#1 $
 */

public class ActionEnumUserType extends EnumUserType<ActionEnumType> {

    /**
     * Actions are stored as char(2)
     */
    private static int[] SQL_TYPES = { Types.CHAR };

    /**
     * Create array of action enumerations
     */
    private static final List<ActionEnumType> ACTION_ENUM_LIST = new ArrayList<ActionEnumType>(36);
    private static final List<String> ACTION_ABBREV_LIST = new ArrayList<String>(36);
    private static ActionEnumType[] actionEnumArray;
    private static String[] actionAbbrevArray;
    private static boolean loadedActions = false;

    static {
        add(ActionEnumType.ACTION_CHANGE_ATTRIBUTES,       "Ca");
        add(ActionEnumType.ACTION_ABNORMAL_AGENT_SHUTDOWN, "As");
        add(ActionEnumType.ACTION_CHANGE_SECURITY,         "Cs");
        add(ActionEnumType.ACTION_COPY,                    "Co");
        add(ActionEnumType.ACTION_PASTE,                   "CP");
        add(ActionEnumType.ACTION_DELETE,                  "De");
        add(ActionEnumType.ACTION_EMBED,                   "Em");
        add(ActionEnumType.ACTION_MOVE,                    "Mo");
        add(ActionEnumType.ACTION_PRINT,                   "Pr");
        add(ActionEnumType.ACTION_OPEN,                    "Op");
        add(ActionEnumType.ACTION_EDIT,                    "Ed");
        add(ActionEnumType.ACTION_RENAME,                  "Rn");
        add(ActionEnumType.ACTION_SEND_EMAIL,              "SE");
        add(ActionEnumType.ACTION_SEND_IM,                 "SI");
        add(ActionEnumType.ACTION_START_AGENT,             "Au");
        add(ActionEnumType.ACTION_STOP_AGENT,              "Ad");
        add(ActionEnumType.ACTION_AGENT_USER_LOGIN,        "Uu");
        add(ActionEnumType.ACTION_AGENT_USER_LOGOUT,       "Ud");
        add(ActionEnumType.ACTION_ACCESS_AGENT_CONFIG,     "Ac");
        add(ActionEnumType.ACTION_ACCESS_AGENT_LOGS,       "Al");
        add(ActionEnumType.ACTION_ACCESS_AGENT_BINARIES,   "Ab");
        add(ActionEnumType.ACTION_INVALID_BUNDLE,          "Ib");
        add(ActionEnumType.ACTION_BUNDLE_RECEIVED,         "Br");
        add(ActionEnumType.ACTION_ACCESS_AGENT_BUNDLE,     "Ap");
        add(ActionEnumType.ACTION_EXPORT,                  "Ex");
        add(ActionEnumType.ACTION_ATTACH,                  "At");
        add(ActionEnumType.ACTION_RUN,                     "Ru");
        add(ActionEnumType.ACTION_AVD,                     "Av");
        add(ActionEnumType.ACTION_MEETING,                 "Me");
        add(ActionEnumType.ACTION_PRESENCE,                "Ps");
        add(ActionEnumType.ACTION_SHARE,                   "Sh");
        add(ActionEnumType.ACTION_RECORD,                  "Re");
        add(ActionEnumType.ACTION_QUESTION,                "Qu");
        add(ActionEnumType.ACTION_VOICE,                   "Vo");
        add(ActionEnumType.ACTION_VIDEO,                   "Vi");
        add(ActionEnumType.ACTION_JOIN,                    "Jo");

        actionEnumArray = ACTION_ENUM_LIST.toArray(new ActionEnumType[ACTION_ENUM_LIST.size()]);
        actionAbbrevArray = ACTION_ABBREV_LIST.toArray(new String[ACTION_ABBREV_LIST.size()]);
    }
    
    private static void add(ActionEnumType action, String abbrev){
        ACTION_ENUM_LIST.add(action);
        ACTION_ABBREV_LIST.add(abbrev);
    }

    /**
     * @see net.sf.hibernate.UserType#sqlTypes()
     */
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    /**
     * Constructor
     */
    public ActionEnumUserType() {
        super(actionEnumArray, actionAbbrevArray, ActionEnumType.class);

        if (!loadedActions) {
            // Add actions in configuration.xml file
            // Technically this is not thread safe, but in practice it isn't a problem
            IComponentManager manager = ComponentManagerFactory.getComponentManager();
            IDestinyConfigurationStore confStore = (IDestinyConfigurationStore) manager.getComponent(DestinyConfigurationStoreImpl.COMP_INFO);

            /*
             * I'd like to read these actions earlier (like in the
             * static initializer), but this class is created by
             * HibernateRepository too early to use the conf store.
             * Luckily, hibernate doesn't actually need any of the
             * values at that point, so we can safely wait until the
             * config store exists.
             */
            if (confStore.retrieveActionListConfig() != null) {
                for (IActionConfigDO configAction : confStore.retrieveActionListConfig().getActions()) {
                    checkForDuplicateName(configAction.getName(), configAction.getShortName());

                    add(ActionEnumType.getActionEnum(configAction.getName()), configAction.getShortName());
                    addEnum(ActionEnumType.getActionEnum(configAction.getName()), configAction.getShortName(), ActionEnumType.class);
                }
                
                // Recreate the actionEnumArray for next caller
                actionEnumArray = ACTION_ENUM_LIST.toArray(new ActionEnumType[ACTION_ENUM_LIST.size()]);
                actionAbbrevArray = ACTION_ABBREV_LIST.toArray(new String[ACTION_ABBREV_LIST.size()]);
                
                loadedActions = true;
            }
        }
    }

    private void checkForDuplicateName(String longName, String shortName) {
        ActionEnumType existing = getTypeByCode(shortName);
        if (existing != null) {
            // We have a dup
            throw new IllegalArgumentException("Duplicate short name " + shortName 
                    + " (for " + longName + ") already used by " + existing + " action");
        }
    }

    /**
     * Extend the existing action/abbreviation list
     */
    synchronized public void addAction(String name, String abbrev) {
        addEnum(ActionEnumType.getActionEnum(name), abbrev, ActionEnumType.class);
    } 

    /**
     * Get the abbreviation code from the name of the action
     */
    public String getCodeByName(String name) {
        return getCodeByType(ActionEnumType.getActionEnum(name));
    }
}
