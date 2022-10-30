/*
 * Created on Aug 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.dms.components.compmgr.IDCCComponentDO;
import com.bluejungle.framework.utils.SerialUtil;

/**
 * This class is the request processing thread for auto discovery requests. The
 * main server socket dispatches the request to this thread for processing. The
 * thread extracts the request information, and performs the required operation.
 * Then, it opens a socket back to the caller and sends the data back.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/discovery/RequestProcThread.java#2 $
 */

class RequestProcThread extends Thread {

    private static final Log LOG = LogFactory.getLog(RequestProcThread.class.getName());

    /**
     * Empty results to be returned in case of errors
     */
    private static final String[] EMPTY_RESULT = new String[] {};

    /**
     * Operation requesting to list all servers
     */
    private static final String FIND_SERVERS_OP = "S";

    /**
     * Operation requesting to list all policy servers
     */
    private static final String FIND_POLICY_SERVERS_OP = "P";

    /**
     * Operation requesting to list the DABS servers
     */
    private static final String FIND_DABS_SERVERS_OP = "D";

    /**
     * Properties related to the server information
     */
    private static final String SERVER_HOST_PROP = "H";
    private static final String SERVER_PORT_PROP = "P";
    private static final String SERVER_CERT_PROP = "C";

    /**
     * Properties related to the policy server information
     */
    private static final String POLICY_SERVER_HOST_PROP = "H";
    private static final String POLICY_SERVER_PORT_PROP = "P";

    /**
     * Packet sent by the sender
     */
    private DatagramPacket senderPacket;
    private IAutoDiscoverServerInternal discoveryServer;

    /**
     * Constructor
     * 
     * @param packetToProcess
     *            packet received from the caller
     */
    public RequestProcThread(final DatagramPacket packetToProcess, final IAutoDiscoverServerInternal server) {
        super("AutoDiscoveryThread");
        this.senderPacket = packetToProcess;
        this.discoveryServer = server;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    protected Log getLog() {
        return LOG;
    }

    /**
     * Returns the list of DABS servers. Each DABS server is represented with an
     * array of 4 elements and consists of a hostname and a port number.
     * 
     * @return a string array containing the DABS server information
     */
    protected String[] listDABSServers() {
        Set<URL> urls = this.discoveryServer.getActiveDABSInstances();
        final String[] result = new String[urls.size() * 4];
        int index = 0;
        for (URL currentDABSLocation : urls) {
            final int currentIndex = index * 4;
            result[currentIndex] = SERVER_HOST_PROP;
            result[currentIndex + 1] = currentDABSLocation.getHost();
            result[currentIndex + 2] = SERVER_PORT_PROP;
            result[currentIndex + 3] = Integer.toString(currentDABSLocation.getPort());
            
            index++;
        }

        return result;
    }

    /**
     * Returns the list of policy servers and their properties. In the current
     * implementation, each DPS instance takes 4 elements in the string array.
     * If there is more than one DPS in the list, 4 elements will be added for
     * each instance.
     * 
     * @return a list of policy server locations
     */
    protected String[] listPolicyServers() {
        final IDCCComponentDO[] dpsServers = this.discoveryServer.getActiveDPSInstances();
        int size = dpsServers.length;
        if (size > 0) {
            final String[] result = new String[dpsServers.length * 4];
            for (int i = 0; i < size; i++) {
                IDCCComponentDO currentDPS = dpsServers[i];
                try {
                    final URL url = new URL(currentDPS.getComponentURL());
                    final int currentIndex = i * 4;
                    result[currentIndex] = POLICY_SERVER_HOST_PROP;
                    result[currentIndex + 1] = url.getHost();
                    result[currentIndex + 2] = POLICY_SERVER_PORT_PROP;
                    result[currentIndex + 3] = Integer.toString(url.getPort());
                } catch (MalformedURLException e) {
                    //Should never happen
                    return EMPTY_RESULT;
                }
            }
            return result;
        } else {
            return EMPTY_RESULT;
        }
    }

    /**
     * Returns the server related information. The server information includes
     * the server hostname, server web service port and certificate location.
     * 
     * @return the server related information
     */
    protected String[] listServer() {
        try {
            final IDCCComponentDO server = this.discoveryServer.getServerInfo();
            final URL url = new URL(server.getComponentURL());
            final String[] serverDesc = new String[6];
            serverDesc[0] = SERVER_HOST_PROP;
            serverDesc[1] = url.getHost();
            serverDesc[2] = SERVER_PORT_PROP;
            serverDesc[3] = Integer.toString(url.getPort());
            serverDesc[4] = SERVER_CERT_PROP;
            serverDesc[5] = this.discoveryServer.getCertificateLocation();
            return serverDesc;
        } catch (MalformedURLException e) {
            //URL is supposed to be valid, this should never happen
            return EMPTY_RESULT;
        }
    }

    /**
     * Processes the data sent by the caller. Based on the operation requested,
     * various results can be returned. As of now, two operations are supported:
     * 
     * 1. List servers : this operation requests all servers to identify
     * themselves. The function is supposed to returned the server address, the
     * server internal port and the location of the certificates on the local
     * hard drive.
     * 
     * 2. List policy servers : this operation requests the list of all DPS
     * instances running. This function queries the DCC component manager and
     * returns the location and port of the policy servers.
     * 
     * @param requestData
     *            data sent by the caller
     * @return an array of string containing the results to the request
     */
    protected String[] processRequestData(final String[] requestData) {
        String[] result;
        if (requestData != null && requestData.length > 0) {
            final String version = requestData[0];
            final String operation = requestData[1];
            if (FIND_POLICY_SERVERS_OP.equals(operation)) {
                result = listPolicyServers();
            } else if (FIND_SERVERS_OP.equals(operation)) {
                result = listServer();
            } else if (FIND_DABS_SERVERS_OP.equals(operation)) {
                result = listDABSServers();
            } else {
                result = EMPTY_RESULT;
            }
        } else {
            result = EMPTY_RESULT;
        }

        return result;
    }

    /**
     * Main processing function. The data is extracted from the caller's paket,
     * processed, and a response is sent back to the caller.
     */
    public void run() {
        final String receivedData = new String(this.senderPacket.getData(), 0, this.senderPacket.getLength());
        final InetAddress senderAddress = this.senderPacket.getAddress();
        final int senderPort = this.senderPacket.getPort();
        final String[] requestData = SerialUtil.StringToArray(receivedData);

        final String[] response = processRequestData(requestData);
        //No need to reply if the reponse is empty
        if (!Arrays.equals(EMPTY_RESULT, response)) {
            final byte[] responseData = SerialUtil.ArrayToString(response);

            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, senderAddress, senderPort);
            DatagramSocket responseSocket;
            try {
                responseSocket = new DatagramSocket();
                responseSocket.send(responsePacket);
                responseSocket.close();
            } catch (IOException e) {
                getLog().error("Error while replying to discovery broadcast. ", e);
            }
        }
    }
}