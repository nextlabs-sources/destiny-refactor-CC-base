/**
 * DABSPingServiceImpl.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis 1.2 May 03, 2005
 * (02:20:24 EDT) WSDL2Java emitter.
 */

package com.bluejungle.destiny.container.dabs;

/**
 * Ping service used to valid dabs locations
 * 
 * @author sgoldstein
 */
public class DABSPingServiceImpl implements com.bluejungle.destiny.services.ping.PingServiceIF {

    /**
     * Send a ping to DABS. The provided ping data will be returned unchanged
     * 
     * @param pingData
     * @return
     * @throws java.rmi.RemoteException
     */
    public String ping(String pingData) throws java.rmi.RemoteException {
        return pingData;
    }
}
