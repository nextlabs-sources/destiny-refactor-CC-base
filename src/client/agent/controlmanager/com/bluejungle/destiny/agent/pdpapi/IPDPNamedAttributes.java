package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 18, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2010 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/pdpapi/IPDPNamedAttributes.java#1 $:
 */

import java.util.Map;
import java.util.Set;

import com.bluejungle.framework.utils.DynamicAttributes;

public interface IPDPNamedAttributes
{
    String getName();

    void setAttribute(String key, String value);

    String getValue(String key);

    String[] getValues(String key);

    Set<String> keySet();
    
    void addSelfToMap(Map<String, DynamicAttributes> map);

    void remove(String key);
}
