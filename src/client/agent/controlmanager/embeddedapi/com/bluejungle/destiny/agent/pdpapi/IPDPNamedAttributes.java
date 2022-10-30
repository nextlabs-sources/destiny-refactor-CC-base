package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 18, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2010 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/embeddedapi/com/bluejungle/destiny/agent/pdpapi/IPDPNamedAttributes.java#1 $:
 */

import java.util.Map;
import java.util.Set;

/**
 * Named attributes consist of a set of key/value pairs along with an
 * associated name describing the nature of the attributes
 * (e.g. "host" or "user") A key can have multiple associated values.
 */
public interface IPDPNamedAttributes
{
    /**
     * Get the name associated with these attributes
     * @return the name
     */
    String getName();

    /**
     * Assign the value to the specified key. If the key was previously added with a different value
     * the new value will be added rather than replacing the existing one
     * @param key the key
     * @param value the value
     */
    void setAttribute(String key, String value);

    /**
     * Return a Set consisting of all the keys in this named attribute
     * @return the set of keys
     */
    Set<String> keySet();

    /**
     * Returns the value associated with this key (or, if multiple values are associated with this key, returns
     * the first)
     * @param key the key
     * @return the associated value
     */
    String getValue(String key);

    /**
     * Return an array of all the values associated with this key
     * @param key the key
     * @return the value(s). A zero-length array will be returned if there are no values or if the key does not exist
     */
    String[] getValues(String key);

    void addSelfToMap(Map map);

    /**
     * Removes the key and associate value from the named attributes. It is not an error to call this on a key that
     * does not exist
     * @param key the key
     */
    void remove(String key);
}
