/*
 * Created on Jan 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr;

/**
 * Enumeration of the CommProfileDO Query Term fields.
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/profilemgr/CommProfileQueryField.java#2 $
 */
public class CommProfileQueryField extends BaseProfileQueryFields {

    public static final CommProfileQueryField ID = new CommProfileQueryField("id");
    public static final CommProfileQueryField NAME = new CommProfileQueryField("name");
    public static final CommProfileQueryField DABSLocation = new CommProfileQueryField("DABSLocation");
    public static final CommProfileQueryField AGENT_TYPE = new CommProfileQueryField("agentType");

    /**
     * Create a CommProfileQueryField.
     * 
     * @param fieldName
     *            the name of the query field
     */
    public CommProfileQueryField(String fieldName) {
        super(fieldName);
    }
}