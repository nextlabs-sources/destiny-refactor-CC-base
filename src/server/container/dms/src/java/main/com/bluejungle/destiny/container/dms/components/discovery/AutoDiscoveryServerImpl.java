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
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dcc.DCCComponentEnumType;
import com.bluejungle.destiny.container.dms.components.compmgr.IDCCComponentDO;
import com.bluejungle.destiny.container.dms.components.compmgr.IDCCComponentMgr;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * This is the implementation class for the auto discovery server. The auto
 * discovery server relies on the state of the DCC components provided by the
 * DCC component manager to return answer to the client requests. It opens a
 * port and listen to broadcast messages sent by policy authors or by
 * installers.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/discovery/AutoDiscoveryServerImpl.java#1 $
 */
public class AutoDiscoveryServerImpl extends Thread implements IAutoDiscoverServerInternal, ILogEnabled, IConfigurable, IDisposable, IInitializable, IManagerEnabled {

    /**
     * Component data refresh period in milliseconds
     */
    private static final int COMPONENT_DATA_REFRESH_RATE = 1 * 60 * 1000;

    /**
     * Default port number to use
     */
    private static final int DEFAULT_PORT_NB = 19888;

    /**
     * Maximum packet size for caller's packets
     */
    private static final int MAX_CALLER_PACKET_SIZE = 100;

    /**
     * Socket timeout in milliseconds
     */
    private static final int SOCKET_TIMEOUT = 5000;

    private String certificateLocation;
    private IConfiguration config;
    private IDCCComponentDO currentServerInfo;
    private Set<URL> currentDABSInstances;
    private IDCCComponentDO[] currentDPSInstances;
    private IDCCComponentMgr dccCompMgr;
    private IProfileManager profileMgr;
    private IAgentManager agentManager;
    private Log log;
    private IComponentManager manager;
    private int port;
    private long lastRefreshTime = 0;
    
    public AutoDiscoveryServerImpl() {
        super("AutoDiscoveryServer");
    }

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
        this.interrupt();
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.discovery.IAutoDiscoverServerInternal#getActiveDABSInstances()
     */
    public Set<URL> getActiveDABSInstances() {
        refreshComponentInformation();
        return this.currentDABSInstances;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.discovery.IAutoDiscoverServerInternal#getActiveDPSInstances()
     */
    public synchronized IDCCComponentDO[] getActiveDPSInstances() {
        refreshComponentInformation();
        return this.currentDPSInstances;
    }

    /**
     * Returns the certificate location directory
     * 
     * @return the certificate location directory
     */
    public String getCertificateLocation() {
        return this.certificateLocation;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * Returns the DCC component manager object
     * 
     * @return the DCC component manager object
     */
    protected IDCCComponentMgr getDCCComponentMgr() {
        return this.dccCompMgr;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.manager;
    }

    /**
     * Returns the port number to listen on
     * 
     * @return port number to listen on
     */
    protected int getPort() {
        return this.port;
    }

    /**
     * Returns the profile manager
     * 
     * @return the profile manager
     */
    protected IProfileManager getProfileMgr() {
        return this.profileMgr;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.discovery.IAutoDiscoverServerInternal#getServerInfo()
     */
    public synchronized IDCCComponentDO getServerInfo() {
        refreshComponentInformation();
        return this.currentServerInfo;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        IConfiguration compConfig = getConfiguration();
        //Sets the port number
        int newPort = DEFAULT_PORT_NB;
        Integer iPort = (Integer) compConfig.get(IAutoDiscoveryServer.PORT_CONFIG_PARAM);
        if (iPort != null) {
            newPort = iPort.intValue();
        }
        setPort(newPort);

        //Set the DCC component manager
        this.dccCompMgr = (IDCCComponentMgr) compConfig.get(IAutoDiscoveryServer.DCC_COMP_MGR_CONFIG_PARAM);
        if (this.dccCompMgr == null) {
            throw new NullPointerException("DCC component manager cannot be null in auto discover server configuration");
        }

        //Sets the certificate location
        this.certificateLocation = (String) compConfig.get(IAutoDiscoveryServer.CERT_LOCATION_CONFIG_PARAM);
        if (this.certificateLocation == null) {
            throw new NullPointerException("Certificate directory cannot be null in auto discover server configuration");
        }

        //Sets the profile manager
        this.profileMgr = (IProfileManager) compConfig.get(IAutoDiscoveryServer.PROFILE_MGR_CONFIG_PARAM);
        if (this.profileMgr == null) {
            throw new NullPointerException("The profile manager cannot be null");
        }

        //Sets the agent manager
        this.agentManager = (IAgentManager) compConfig.get(IAutoDiscoveryServer.AGENT_MGR_CONFIG_PARAM);
        if (this.agentManager == null) {
            throw new NullPointerException("The agent manager cannot be null");
        }
        
        this.start();
    }

    /**
     * This function is called before processing every request. If the last
     * refresh occured recently, it does not occur again.
     */
    private synchronized void refreshComponentInformation() {
        final long now = System.currentTimeMillis();
        if (now - this.lastRefreshTime > COMPONENT_DATA_REFRESH_RATE) {
            IDCCComponentMgr compMgr = getDCCComponentMgr();
            try {
                List dmsList = compMgr.getComponentByType(DCCComponentEnumType.DMS);
                this.currentServerInfo = null;
                if (dmsList.size() > 0) {
                    this.currentServerInfo = (IDCCComponentDO) dmsList.get(0);
                }
                List dpsList = compMgr.getComponentByType(DCCComponentEnumType.DPS);
                int size = dpsList.size();
                this.currentDPSInstances = new IDCCComponentDO[size];
                for (int i = 0; i < size; i++) {
                    this.currentDPSInstances[i] = (IDCCComponentDO) dpsList.get(i);
                }

                //Find the current DABS instances. The only DABS instances to
                // be broadcasted are the defaults DABS location for the
                //default communication profiles.
                this.currentDABSInstances = new HashSet<URL>();
                List dabsList = compMgr.getComponentByType(DCCComponentEnumType.DABS);
                if (dabsList.size() > 0) {
                    IProfileManager profileManager = getProfileMgr();
                    List<IAgentType> agentTypes = this.agentManager.getAgentTypes();                    
                    Iterator<IAgentType> agentTypesIterator = agentTypes.iterator();
                    while (agentTypesIterator.hasNext()) {
                        IAgentType nextAgentType = agentTypesIterator.next();
                        ICommProfileDO nextCommProfile = profileManager.getDefaultCommProfile(nextAgentType);
                        this.currentDABSInstances.add(nextCommProfile.getDABSLocation().toURL());
                    }
                }
            } catch (DataSourceException e) {
                getLog().error("Error when fetching component information", e);
            } catch (MalformedURLException e) {
                getLog().error("Error when creating URL information", e);
            } finally {
                this.lastRefreshTime = now;
            }
        }
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        DatagramSocket serverSocket = null;
        try {
            InetAddress addressToBind = null;
            Enumeration netIfs = NetworkInterface.getNetworkInterfaces();
            boolean searchMore = true;
            while (netIfs.hasMoreElements() && searchMore) {
                NetworkInterface netface = (NetworkInterface) netIfs.nextElement();
                // get all the IP addresses associated with each interface
                Enumeration addresses = netface.getInetAddresses();
                while (addresses.hasMoreElements() && searchMore) {
                    InetAddress ip = (InetAddress) addresses.nextElement();
                    getLog().trace("Found IP address : " + ip);
                    if (!ip.isLoopbackAddress()) {
                        addressToBind = ip;
                        searchMore = false;
                        getLog().trace("Binding to IP address : " + addressToBind);
                    }
                }
            }
            InetSocketAddress sockAddr = new InetSocketAddress(addressToBind, getPort());
            serverSocket = new DatagramSocket(null);
            serverSocket.setReuseAddress(true);
            serverSocket.setSoTimeout(SOCKET_TIMEOUT);
            serverSocket.bind(sockAddr);
        } catch (IOException e) {
            getLog().error("Error when binding server socket for autodiscovery server", e);
            serverSocket = null;
        }

        //We loop with a timeout to ensure safe shutdown
        while (!this.isInterrupted() && serverSocket != null) {
            byte[] bytes = new byte[MAX_CALLER_PACKET_SIZE];
            DatagramPacket dp = new DatagramPacket(bytes, bytes.length);
            try {
                serverSocket.receive(dp);
                new RequestProcThread(dp, this).start();
            } catch (IOException e) {
                if (!(e instanceof SocketTimeoutException)) {
                    getLog().warn("Error when receiving auto discovery broadcast packets", e);
                }
            }
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration newConfig) {
        this.config = newConfig;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log newLog) {
        this.log = newLog;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager newManager) {
        this.manager = newManager;
    }

    /**
     * Sets the port number on which to listen to broadcast messages
     * 
     * @param newPort
     *            new port to listen on
     */
    protected void setPort(int newPort) {
        this.port = newPort;
    }
}