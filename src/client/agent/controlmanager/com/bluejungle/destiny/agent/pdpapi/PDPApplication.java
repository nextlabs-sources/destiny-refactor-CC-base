package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 19, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/pdpapi/PDPApplication.java#1 $:
 */

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.bluejungle.framework.utils.DynamicAttributes;

public class PDPApplication extends PDPNamedAttributes implements IPDPApplication
{
    private static final String DIMENSION_NAME = "application";

    public static final IPDPApplication NONE = new IPDPApplication() {
        @Override
        public String getName() {
            throw new UnsupportedOperationException("getName");
        }
        @Override
        public void setAttribute(String key, String value) {
            throw new UnsupportedOperationException("setAttribute");
        }
        @Override
        public String getValue(String key) {
            throw new UnsupportedOperationException("getValue");
        }
        @Override
        public String[] getValues(String key) {
            throw new UnsupportedOperationException("getValues");
        }
        @Override
        public Set<String> keySet() {
            return Collections.<String>emptySet();
        }
        @Override
        public void addSelfToMap(Map<String, DynamicAttributes> map) {
        }
        @Override
        public void remove(String key) {
        }
    };

    /**
     * Create an application object
     *
     * @param name the full name of the application (usually this will be a path)
     * @param pid the process id
     */
    public PDPApplication(String name, long pid) {
        this(name);

        setAttribute("pid", Long.toString(pid));
    }

    /**
     * Create an application object without a pid
     *
     * @param name the full name of the application (usually this will be a path)
     */
    public PDPApplication(String name) {
        super(DIMENSION_NAME);

        if (name == null) {
            throw new IllegalArgumentException("Application name was null");
        }

        setAttribute("name", name);
    }
}
