package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 19, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/embeddedapi/com/bluejungle/destiny/agent/pdpapi/PDPHost.java#1 $:
 */

public class PDPHost extends PDPNamedAttributes implements IPDPHost
{
    private static final String DIMENSION_NAME = "host";
    private static final String IP_ADDR_KEY = "inet_address";
    private static final String HOST_NAME_KEY = "name";

    private PDPHost() {
        super(DIMENSION_NAME);
    }

    /**
     * Create a host object from an ip address
     *
     * @param ipAddress the ip address expressed as a 32bit integer using the standard encoding
     *
     * Note: the loopback address (127.0.0.1, 1677734 as an integer) is acceptable
     */
    public PDPHost(int ipAddress) {
        this();
        setAttribute(IP_ADDR_KEY, Integer.toString(ipAddress));
    }

    /**
     * Create a host object from an IPV6 address (unsupported)
     *
     * @param ipv6Address the ipv6 address expressed as a byte array
     */
    public PDPHost(byte[] ipv6Address) throws PDPException { 
        this();
        throw new PDPException("IPV6 not supported");
    }

    /**
     * Create a host object by name
     *
     * @param hostname fully qualified host name
     */
    public PDPHost(String hostname) {
        this();
        if (hostname == null) {
            throw new IllegalArgumentException("hostname is null");
        }
        setAttribute(HOST_NAME_KEY, hostname);
    }
}
