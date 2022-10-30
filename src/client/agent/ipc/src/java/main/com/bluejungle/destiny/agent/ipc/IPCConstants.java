// All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
//Redwood City CA, Ownership remains with Blue Jungle Inc,
//All rights reserved worldwide.

package com.bluejungle.destiny.agent.ipc;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */
public class IPCConstants {

    public static final int CHANNEL_SIZE = 8192;

    public static final int TIMEOUT = 10000;

    public static final int WAIT_TIMEOUT = 0x102; //from win32.h

    public static final int WAIT_STATUS_ABANDONED_0 = 0x80; //from .h files

    public static final int WAIT_OBJECT_0 = 0; //from .h files
    
    public static final int MAXIMUM_WAIT_OBJECTS = 64; //from .h files    

    public static final int LINUX_MAXIMUM_WAIT_OBJECTS = 1; //Linux does NOT support waitForMultipleObjects        
    
    public static final String CONNECT = "CONNECT";

    public static final String DISCONNECT = "DISCONNECT";

    public static final String SHARED_MEMORY_SUFFIX = "SHAREDMEM";

    public static final String SEND_EVENT_SUFFIX = "SEND_EVENT";

    public static final String RECEIVE_EVENT_SUFFIX = "RECEIVE_EVENT";

    public static final String MUTEX_SUFFIX = "HANDSHAKE_MUTEX";

    public static final String GLOBAL_PREFIX = "Global\\";
}
