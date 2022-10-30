package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 18, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/pdpapi/PDPNamedAttributes.java#1 $:
 */

import java.util.Map;
import java.util.Set;

import com.bluejungle.framework.utils.DynamicAttributes;

public class PDPNamedAttributes implements IPDPNamedAttributes
{
    private final String name;
    private final DynamicAttributes attrs;

    public PDPNamedAttributes(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }

        this.name = name;
        this.attrs = new DynamicAttributes();
    }

    public String getName() {
        return name;
    }

    public void setAttribute(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }
        attrs.add(key.toLowerCase(), value);
    }

    public Set<String> keySet() {
        return attrs.keySet();
    }
    
    public String getValue(String key) {
        return attrs.getString(key);
    }
    
    public String[] getValues(String key) {
        return attrs.getStrings(key);
    }

    public void remove(String key) {
        attrs.remove(key);
    }
    
    public void addSelfToMap(Map<String, DynamicAttributes> map) {
        if (map == null) {
            throw new IllegalArgumentException("map is null");
        }

        map.put(name, attrs);
    }
}
