/*
 * Created on Mar 30, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.inquirymgr.report.impl;

import java.util.HashMap;
import java.util.Map;

import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.action.hibernateimpl.ActionEnumUserType;
import com.nextlabs.destiny.container.shared.inquirymgr.report.IReportValueConverter;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/inquirymgr/report/impl/ReportValueConverterShared.java#1 $
 */

public class ReportValueConverterShared implements IReportValueConverter {
    
    protected static final Map<String, String> ACTION_MAP = new HashMap<String, String>();
    
    static{
        final ActionEnumUserType u = new ActionEnumUserType();
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_CHANGE_ATTRIBUTES),       "Change Attribute");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_ABNORMAL_AGENT_SHUTDOWN), "Abnormal Enforcer Shutdown");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_CHANGE_SECURITY),         "Change File Permissions");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_COPY),                    "Copy / Embed File");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_PASTE),                   "Copy Content");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_DELETE),                  "Delete");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_EMBED),                   "Embed");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_MOVE),                    "Move");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_PRINT),                   "Print");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_OPEN),                    "Open");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_EDIT),                    "Create / Edit");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_RENAME),                  "Rename");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_SEND_EMAIL),              "Email");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_SEND_IM),                 "Instant Message");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_START_AGENT),             "Enforcer Startup");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_STOP_AGENT),              "Enforcer Stop");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_AGENT_USER_LOGIN),        "User Login");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_AGENT_USER_LOGOUT),       "User Logout");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_ACCESS_AGENT_CONFIG),     "Enforcer Configuration File Access");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_ACCESS_AGENT_LOGS),       "Enforcer Log File Access");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_ACCESS_AGENT_BINARIES),   "Enforcer Binary File Access");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_INVALID_BUNDLE),          "Policy Bundle Authentication Failed");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_BUNDLE_RECEIVED),         "Policy Bundle Authentication Succeeded");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_ACCESS_AGENT_BUNDLE),     "Policy Bundle File Access");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_EXPORT),                  "Export");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_ATTACH),                  "Attach to Item");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_RUN),                     "Run");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_AVD),                     "Voice Call / Video Call");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_MEETING),                 "Invite");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_PRESENCE),                "Presence");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_SHARE),                   "Share in Meeting");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_RECORD),                  "Record");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_QUESTION),                "Question");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_VOICE),                   "Voice Call");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_VIDEO),                   "Video Call");
        ACTION_MAP.put(u.getCodeByType(ActionEnumType.ACTION_JOIN),                    "Join Meeting");
    }

    @Override
    public String getActionDisplayName(String abbrev) {
        String fullname = ACTION_MAP.get(abbrev);
        return fullname != null ? fullname : "Unknown";
    }
}
