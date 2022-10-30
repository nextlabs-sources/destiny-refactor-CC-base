/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.registration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The server component type class represents the various types of server
 * components available in the control center. Since this class is used in the
 * shared context, it does not follow the generic "smart enum" pattern.
 * 
 * The name is case insensitive.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/registration/ServerComponentType.java#1 $
 */

public class ServerComponentType {

    private static final Map<String, ServerComponentType> STRING2TYPE = new ConcurrentHashMap<String, ServerComponentType>();
    
    /**
     * the value must be same as com.bluejungle.destiny.container.dcc.DCCComponentEnumType
     */
	public static final ServerComponentType DABS = new ServerComponentType("DABS");
    public static final ServerComponentType DAC = new ServerComponentType("DAC");
    public static final ServerComponentType DCSF = new ServerComponentType("DCSF");
    public static final ServerComponentType DEM = new ServerComponentType("DEM");
    public static final ServerComponentType DMS = new ServerComponentType("DMS");
    public static final ServerComponentType DPS = new ServerComponentType("DPS");
    public static final ServerComponentType MGMT_CONSOLE = new ServerComponentType("MGMT_CONSOLE");
    public static final ServerComponentType REPORTER = new ServerComponentType("REPORTER");

    private String name;

    /**
     * Constructor
     * 
     * @param name
     *            Server component type name
     */
    private ServerComponentType(String name) {
        this.name = formatName(name);
        STRING2TYPE.put(name, this);
    }
    
    private static final String formatName(String name) {
        if(name != null){
            name = name.toUpperCase();
        }
        return name;
    }

    /**
     * Returns the ServerComponentType corresponding to the string
     * 
     * @param name
     * @return
     * @throws IllegalArgumentException
     *             if the name does not match any existing server component type
     */
    public static ServerComponentType fromString(String name) {
        name = formatName(name);
        ServerComponentType result = (ServerComponentType) STRING2TYPE.get(name);
        if (result == null) {
            result = new ServerComponentType(name);
        }
        return result;
    }

    /**
     * Returns the server component type name
     * 
     * @return the server component type name
     */
    public String getName() {
        return this.name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServerComponentType other = (ServerComponentType) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
