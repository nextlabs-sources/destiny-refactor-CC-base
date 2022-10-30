package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 18, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/pdpapi/PDPResource.java#1 $:
 */

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import com.bluejungle.framework.utils.DynamicAttributes;

public class PDPResource extends PDPNamedAttributes implements IPDPResource
{
    private static final String RESOURCE_NAME_KEY = "ce::id";
    private static final String DESTINY_TYPE_KEY = "ce::destinytype";

    public static final IPDPResource NONE = new IPDPResource() {
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
     * Empty constructor used for NONE object
     */
    private PDPResource() {
        super("from");
    }
    
    /**
     * Creates a resource object
     *
     * @param dimensionName the only ones supported now are "from" and "to"
     * @param resourceName the name of the resource (e.g. c:/foo.txt or  sharepoint://documents/important.doc)
     * @param resourceType the resource type (e.g. fso, portal, server)
     */
    public PDPResource(String dimensionName, String resourceName, String resourceType) {
        super(dimensionName);

        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName was null");
        }

        if (resourceType == null) {
            throw new IllegalArgumentException("resourceType was null");
        }

        setAttribute(RESOURCE_NAME_KEY,  resourceName);
        setAttribute(DESTINY_TYPE_KEY, resourceType);
    }
}
