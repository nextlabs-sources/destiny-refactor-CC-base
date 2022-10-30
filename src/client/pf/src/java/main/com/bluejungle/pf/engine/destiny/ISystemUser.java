/*
 * Created on Aug 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.engine.destiny;

/**
 * Represents a user of the system on which the agent is installed
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/engine/destiny/ISystemUser.java#1 $
 */

public interface ISystemUser {

    /**
     * Retrieve this user's type (e.g. Windows, Linux, Custom Application, etc)
     * 
     * @return this user's type
     */
    public String getUserSubjectTypeId();

    /**
     * Retrieve the system dependent identified of this user
     * 
     * @return the system dependent identified of this user
     */
    public String getSystemId();
}
