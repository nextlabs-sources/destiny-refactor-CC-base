/*
 * Created on Feb 5, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager;

import java.util.HashMap;
import java.util.Map;

import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/ComponentEnum.java#2 $
 */

public enum ComponentEnum {
    USER, HOST, APPLICATION, ACTION, RESOURCE, PORTAL;

    private static final Map<String, ComponentEnum> byName = new HashMap<String, ComponentEnum>();

    static {
        for (ComponentEnum ev : values()) {
            // We know there is no naming clash because we named the enum values
            // ourselves:
            assert !byName.containsKey(ev.name().toLowerCase());
            byName.put(ev.name().toLowerCase(), ev);
        }
    }

    public String toString() {
        switch (this) {
        case USER:
            return "User";
        case HOST:
            return "Host";
        case APPLICATION:
            return "Application";
        case ACTION:
            return "Action";
        case RESOURCE:
            return "Resource";
        case PORTAL:
            return "Portal";
        }
        return null;
    }

    public static ComponentEnum forSpecType(SpecType type) {
        if (type == SpecType.ACTION) {
            return ACTION;
        } else if (type == SpecType.APPLICATION) {
            return APPLICATION;
        } else if (type == SpecType.HOST) {
            return HOST;
        } else if (type == SpecType.RESOURCE) {
            return RESOURCE;
        } else if (type == SpecType.PORTAL) {
            return PORTAL;
        } else if (type == SpecType.ACTION) {
            return ACTION;
        } else if (type == SpecType.USER) {
            return USER;
        } else {
            throw new IllegalArgumentException("type");
        }
    }

    /**
     * This method is a case-insensitive version of the
     * ComponentEnum#valueOf(String).
     * 
     * @param name
     *            the name of the enumeration value.
     * @return the enumeration value for the name (case-insensitive).
     */
    public static ComponentEnum forName(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        name = name.toLowerCase();
        if (!byName.containsKey(name)) {
            throw new IllegalArgumentException("name");
        }
        return byName.get(name);
    }
}
