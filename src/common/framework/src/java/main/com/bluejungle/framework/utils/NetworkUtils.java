/*
 * Created on May 30, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.framework.utils;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class NetworkUtils {

    static {
        System.loadLibrary("NetworkUtils");
    }

    /**
     * Constructor
     *  
     */
    public NetworkUtils() {
        super();
    }

    /**
     * @return string array containing list of servers on the network.
     */
    public static native String[] getServerList();

    /**
     * @param server
     *            name of server
     * @return list of folders shared on the specified server
     */
    public static native String[] getSharedFolderList(String server);

}
