/*
 * Created on Sep 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.client.common.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.utils.SerialUtil;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/common/src/java/main/com/bluejungle/destiny/client/common/discovery/DiscoverClient.java#1 $
 */

public class DiscoverClient implements IDiscoverClient, ILogEnabled {

    /**
     * Mapping of discovery types to the message that should be broadcasted
     */
    private static final Map DISCOVERYTYPE_2_MESSAGE = new HashMap();
    static {
        DISCOVERYTYPE_2_MESSAGE.put(DiscoveryType.POLICY_SERVERS, "P");
        DISCOVERYTYPE_2_MESSAGE.put(DiscoveryType.SERVER_INSTANCES, "S");
    }

    /**
     * Port number on which the broadcast takes place
     */
    private static final String BROADCAST_CHANNEL = "255.255.255.255";
    private static final int BROADCAST_PORT = 19888;

    /**
     * Component manager information
     */
    public static final ComponentInfo COMP_INFO = new ComponentInfo(DiscoverClient.class.getName(), DiscoverClient.class.getName(), IDiscoverClient.class.getName(), LifestyleType.SINGLETON_TYPE);

    private Log log;

    /**
     * @see com.bluejungle.destiny.client.common.discovery.IDiscoverClient#discover(com.bluejungle.destiny.client.common.discovery.DiscoveryType,
     *      long)
     */
    public Set discover(DiscoveryType type, long maxSearchDuration) {
        final Set result = new HashSet();
        final byte[] bytesToSend = prepareBroadcastMessage(type);
        final long beginTime = System.currentTimeMillis();

        boolean sendSucceeded = false;
        DatagramSocket socket = null;

        //Need at least one second to discover
        if (maxSearchDuration < 1000) {
            maxSearchDuration = 1000;
        }

        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(50);
            DatagramPacket dp = new DatagramPacket(bytesToSend, bytesToSend.length, InetAddress.getByName(BROADCAST_CHANNEL), BROADCAST_PORT);
            socket.send(dp);
            sendSucceeded = true;
        } catch (IOException e) {
            getLog().error("Error when broadcasting discovery packets", e);
        }

        if (sendSucceeded) {
            //Check the current time. Given the low timeout value, we don't
            // bother substracting it to match exactly the maximum broadcast
            // time
            List answers = new ArrayList();
            while (System.currentTimeMillis() - beginTime < maxSearchDuration) {
                try {
                    DatagramPacket oResponsePacket = null;
                    byte[] recv_buf = new byte[1000];
                    oResponsePacket = new DatagramPacket(recv_buf, recv_buf.length);
                    socket.receive(oResponsePacket);
                    answers.add(new String(oResponsePacket.getData(), 0, oResponsePacket.getLength()));
                } catch (IOException e) {
                    if (!(e instanceof SocketTimeoutException)) {
                        getLog().error("Failed to receive discovery packets response", e);
                    }
                }
            }
            processAnswers(answers, result, type);
        }
        if (socket != null) {
            socket.close();
        }
        return result;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * For testing only
     * 
     * @param args
     */
    public static void main(String[] args) {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IDiscoverClient client = (IDiscoverClient) compMgr.getComponent(DiscoverClient.COMP_INFO);
        Set discoveredServers = client.discover(DiscoveryType.SERVER_INSTANCES, 5000);
        Iterator it = discoveredServers.iterator();
        while (it.hasNext()) {
            IDiscoveredServer server = (IDiscoveredServer) it.next();
            String certLocation = server.getCertificatesLocation();
            String host = server.getHost();
            int port = server.getPort();
        }

        Set discoveredDPS = client.discover(DiscoveryType.POLICY_SERVERS, 5000);
        it = discoveredDPS.iterator();
        while (it.hasNext()) {
            IDiscoveredItem dps = (IDiscoveredItem) it.next();
            String host = dps.getHost();
            int port = dps.getPort();
        }
    }

    /**
     * Creates a request message that will be broadcasted on the network
     * 
     * @param type
     *            type of discovery to perform
     * @return message that needs to be broadcasted
     */
    protected byte[] prepareBroadcastMessage(DiscoveryType type) {
        String[] request = new String[2];
        request[0] = "1.0";
        request[1] = (String) DISCOVERYTYPE_2_MESSAGE.get(type);
        return (SerialUtil.ArrayToString(request));
    }

    /**
     * This function takes the raw answers received from the network and
     * transforms them into readable objects.
     * 
     * @param answers
     *            list of raw answers receveived
     * @param resultToReturn
     *            set of items to be returned to the caller
     * @param type
     *            type of discovery performed
     */
    protected void processAnswers(final List answers, final Set resultToReturn, final DiscoveryType type) {
        resultToReturn.clear();
        Iterator it = answers.iterator();
        while (it.hasNext()) {
            final String currentAnswer = (String) it.next();
            final String[] array = SerialUtil.StringToArray(currentAnswer);
            if (DiscoveryType.POLICY_SERVERS.equals(type)) {
                int size = array.length;
                for (int i = 0; i < size; i += 4) {
                    resultToReturn.add(new DiscoveredItem(array[i + 1], Integer.parseInt(array[i + 3])));
                }
            } else if (DiscoveryType.SERVER_INSTANCES.equals(type)) {
                resultToReturn.add(new DiscoveredServerItem(array[1], Integer.parseInt(array[3]), array[5]));
            }
        }
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log newLog) {
        this.log = newLog;
    }

    /**
     * This is the implementation of the discovered item class
     * 
     * @author ihanen
     */
    protected class DiscoveredItem implements IDiscoveredItem {

        private String host;
        private int port;

        /**
         * Constructor
         * 
         * @param newHost
         *            host where the item resides
         * @param newPort
         *            port on which the item is available
         */
        public DiscoveredItem(String newHost, int newPort) {
            super();
            this.host = newHost;
            this.port = newPort;
        }

        /**
         * @see com.bluejungle.destiny.client.common.discovery.IDiscoveredItem#getHost()
         */
        public String getHost() {
            return this.host;
        }

        /**
         * @see com.bluejungle.destiny.client.common.discovery.IDiscoveredItem#getPort()
         */
        public int getPort() {
            return this.port;
        }
    }

    /**
     * This class represents an instance of the server that has been discovered.
     * 
     * @author ihanen
     */
    protected class DiscoveredServerItem extends DiscoveredItem implements IDiscoveredServer {

        private String certificatesLocation;

        /**
         * Constructor
         * 
         * @param newHost
         *            host where the server resides
         * @param newPort
         *            internal port number for the server
         * @param newCertLoc
         *            physical location of the server certificates
         */
        public DiscoveredServerItem(String newHost, int newPort, String newCertLoc) {
            super(newHost, newPort);
            this.certificatesLocation = newCertLoc;
        }

        /**
         * @see com.bluejungle.destiny.client.common.discovery.IDiscoveredServer#getCertificatesLocation()
         */
        public String getCertificatesLocation() {
            return this.certificatesLocation;
        }
    }
}