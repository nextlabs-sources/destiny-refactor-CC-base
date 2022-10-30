/*
 * Created on Feb 22, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr;

/**
 * An activity journaling audit level
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/agentmgr/IActivityJournalingAuditLevel.java#1 $
 */

public interface IActivityJournalingAuditLevel {

    /**
     * Retrieve the id of this audit level
     * 
     * @return the id of this audit level
     */
    public String getId();

    /**
     * Retrieve the title of this audit level
     * 
     * @return the title of this audit level
     */
    public String getTitle();

    /**
     * Retrieve the ordinal of this audit level. All actions contained within
     * auditing level are also present in all other auditing levels with higher
     * ordinal
     * 
     * @return the ordinal of this audit level
     */
    public int getOrdinal();
}
