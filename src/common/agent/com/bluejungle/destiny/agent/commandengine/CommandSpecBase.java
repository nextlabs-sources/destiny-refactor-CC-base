/*
 * Created on Dec 10, 2004
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.destiny.agent.commandengine;


/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/agent/com/bluejungle/destiny/agent/commandengine/CommandSpecBase.java#1 $:
 */

public class CommandSpecBase {

    String name;
    
    /**
     * Returns the name.
     * @return the name.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Sets the name
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
}
