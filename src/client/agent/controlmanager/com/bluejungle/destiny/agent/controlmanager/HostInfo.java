/*
 * Created on Nov 13, 2005
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.destiny.agent.controlmanager;

import java.io.Serializable;


/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/HostInfo.java#1 $:
 */

public class HostInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String hostName;
    private String hostIP;
    
    public HostInfo (String hostName, String hostIP) {
        this.hostName = hostName;
        this.hostIP = hostIP;
    }

    
    /**
     * Returns the hostIP.
     * @return the hostIP.
     */
    public final String getHostIP() {
        return this.hostIP;
    }

    
    /**
     * Returns the hostName.
     * @return the hostName.
     */
    public final String getHostName() {
        return this.hostName;
    }

    /**
     * Converts the integer into a string representing the IP address
     * @return the ip address as a string
     */
    public static final String toIP(long ip)
    {
        int hostAddress[] = new int[4];
        int i = 24;
        for(int j = 0; i >= 0; j++)
        {
            hostAddress[j] = (int)((ip >> i) & 0xff);
            i -= 8;
        }

        return ("" + hostAddress[0] 
                + "." + hostAddress[1]
                + "." + hostAddress[2]
                + "." + hostAddress[3]);
    }
}
