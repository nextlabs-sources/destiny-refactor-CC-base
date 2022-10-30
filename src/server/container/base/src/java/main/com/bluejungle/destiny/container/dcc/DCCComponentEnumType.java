/*
 * Created on Feb 17, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import java.util.Set;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/base/src/java/main/com/bluejungle/destiny/container/dcc/DCCComponentEnumType.java#1 $
 */

public class DCCComponentEnumType extends EnumBase {

    /**
     * Supported DCC server component types
     * the value must be same as com.bluejungle.destiny.server.shared.registration.ServerComponentType
     */
    public static final DCCComponentEnumType DABS = new DCCComponentEnumType("DABS");
    public static final DCCComponentEnumType DAC = new DCCComponentEnumType("DAC");
    public static final DCCComponentEnumType DCSF = new DCCComponentEnumType("DCSF");
    public static final DCCComponentEnumType DEM = new DCCComponentEnumType("DEM");
    public static final DCCComponentEnumType DMS = new DCCComponentEnumType("DMS");
    public static final DCCComponentEnumType DPS = new DCCComponentEnumType("DPS");
    public static final DCCComponentEnumType MGMT_CONSOLE = new DCCComponentEnumType("MGMT_CONSOLE");
    public static final DCCComponentEnumType REPORTER = new DCCComponentEnumType("REPORTER");

    /**
     * Constructor
     * 
     * @param name
     *            server component type name
     */
    protected DCCComponentEnumType(String name) {
        super(name);
    }

    /**
     * Retrieve an DCCComponentEnumType instance by name
     * 
     * @param name
     *            the name of the DCCComponentEnumType
     * @return the DCCComponentEnumType associated with the provided name
     * @throws IllegalArgumentException
     *             if no DCCComponentEnumType exists with the specified name
     */
    public static DCCComponentEnumType getServerComponentTypeEnum(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null.");
        }
        
        if(!existsElement(name, DCCComponentEnumType.class)){
            new DCCComponentEnumType(name);
        }
        
        return getElement(name, DCCComponentEnumType.class);
    }

    /**
     * Returns all the DCCComponentEnumType enums
     * 
     * @return set of enums
     */
    public static Set<DCCComponentEnumType> elements() {
        return EnumBase.elements(DCCComponentEnumType.class);
    }

    /**
     * Returns the number of elements in this enumeration
     * 
     * @return the number of elements in this enumeration
     */
    public static int numElements() {
        return numElements(DCCComponentEnumType.class);
    }
}
