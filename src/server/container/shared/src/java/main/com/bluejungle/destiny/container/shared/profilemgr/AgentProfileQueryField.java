/*
 * Created on Jan 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr;

/**
 * Enumeration of the AgentProfileDO Query Term fields.
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/profilemgr/AgentProfileQueryField.java#1 $
 */
public class AgentProfileQueryField extends BaseProfileQueryFields {

    public static final AgentProfileQueryField ID = new AgentProfileQueryField("id");
    public static final AgentProfileQueryField NAME = new AgentProfileQueryField("name");
    public static final AgentProfileQueryField LOG_VIEW_ENABLED = new AgentProfileQueryField("logViewingEnabled");
    public static final AgentProfileQueryField TRAY_ICON_ENABLED = new AgentProfileQueryField("trayIconEnabled");

    /**
     * Create an AgentProfileQueryField instance
     * 
     * @param fieldName
     *            the name of the query field
     */
    public AgentProfileQueryField(String fieldName) {
        super(fieldName);
    }
}