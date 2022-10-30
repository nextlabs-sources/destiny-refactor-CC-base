/*
 * All sources, binaries, and HTML pages (c) copyright 2007 by Next Labs Inc.,
 * San Mateo CA.  Ownership remains with Next Labsl Inc.  All rights reserved worldwide
 */

package com.bluejungle.destiny.agent.ipc;

import com.bluejungle.framework.patterns.EnumBase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OSType extends EnumBase {
    public static final OSType OS_LINUX = new OSType("Linux");
    public static final OSType OS_WINDOWS = new OSType("Windows");
    public static final OSType OS_MACINTOSH = new OSType("Mac OS X");
    public static final OSType OS_SUN_OS = new OSType("SunOS");
    public static final OSType OS_UNKNOWN = new OSType("Unkown Operating System");

    /**
     * The constructor is private to prevent unwanted instantiations from the
     * outside.
     * 
     * @param name
     *            is passed through to the constructor of the superclass.
     */
    private OSType(String name) {
        super(name);
    }

    private static final OSType thisSystem = getOSTypeFromString(System.getProperty("os.name"));

    private static OSType getOSTypeFromString(String name) {
        if (name.startsWith("Windows")) {
            // All Windows systems are the same
            name = "Windows";
        }
        
        if (existsElement(name, OSType.class)) {
            return getElement(name, OSType.class);
        } else {
            return OS_UNKNOWN;
        }
    }

    public static OSType getSystemOS() {
        return thisSystem;
    }
}
