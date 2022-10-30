package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 18, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/embeddedapi/com/bluejungle/destiny/agent/pdpapi/PDPNamedAttributes.java#1 $:
 */

import java.util.Map;
import java.util.Set;

/**
 * <code>PDPNamedAttributes</code> describes attributes (key/value pairs) with an associated name or identifier.
 */
public class PDPNamedAttributes implements IPDPNamedAttributes
{
    private final String name;
    private final Object /* DynamicAttributes */ attrs;

    /**
     * Create a named attribue with the given name
     * @param name the name to be associated with the attributes. Must not be null
     */
    public PDPNamedAttributes(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }

        this.name = name;
        this.attrs = PDPSDK.dynamicAttributesNewInstance();
    }

    /**
     * Returns the name of this object
     * @return the name given in the constructor
     */
    public String getName() {
        return name;
    }

    /**
     * Associates the given key and value. It is not an error to assign multiple different values to a key through
     * multiple calls to this method (a "multi-value").
     * @param key the key (must not be null)
     * @param value the value (must not be null)
     */
    public void setAttribute(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }
        PDPSDK.dynamicAttributesAddMethod(attrs, key.toLowerCase(), value);
    }

    /**
     * Gets the value associated with this key. Note: if multiple values have been
     * associated with this key, the first one is returned
     * @param key (must not be null)
     * @return the value. If the key does not exist, returns null
     */
    public String getValue(String key) {
        return PDPSDK.dynamicAttributesGetStringMethod(attrs, key);
    }

    
    public String[] getValues(String key) {
        return PDPSDK.dynamicAttributesGetStringsMethod(attrs, key);
    }

    public Set<String> keySet() {
        return PDPSDK.dynamicAttributesKeySetMethod(attrs);
    }
    
    public void remove(String key) {
        PDPSDK.dynamicAttributesRemoveMethod(attrs, key);
    }

    /**
     * Exports the contents of the attributes into a map. For internal use only, do not call this method.
     */
    public void addSelfToMap(Map map) {
        if (map == null) {
            throw new IllegalArgumentException("map is null");
        }

        map.put(name, attrs);
    }
}
