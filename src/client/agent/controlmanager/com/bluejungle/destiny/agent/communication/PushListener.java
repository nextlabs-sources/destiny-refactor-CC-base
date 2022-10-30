/*
 * Created on Dec 13, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.communication;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IStartable;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class PushListener implements ILogEnabled, IConfigurable, IInitializable, IStartable, Runnable, IPushListener {
    public static final int PORT_RANGE_DEVIANCE = 10;
    
    private Log log = null;
    private ServerSocket serverSocket;
    private Thread listenerThread = null;
    IConfiguration config = null;    

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        CommProfileDTO communicationProfile = (CommProfileDTO) getConfiguration().get(CommunicationManager.COMM_PROFILE_CONFIG_KEY);
        if (communicationProfile == null) {
            throw new NullPointerException("The communiation profile was not provided");
        }

        if (!communicationProfile.isPushEnabled()) {
            return;
        }
        
        int portNumber = communicationProfile.getDefaultPushPort().intValue();

        try {
            this.serverSocket = new ServerSocket(portNumber);
        } catch (IOException e2) {
            if (getLog().isInfoEnabled()) {
                getLog().info("Could not bind to port: " + Integer.toString(portNumber));
            }

            int minPortRange = portNumber - PORT_RANGE_DEVIANCE;
            int maxPortRange = portNumber + PORT_RANGE_DEVIANCE;

            for (int i = minPortRange; i <= maxPortRange; i++) {
                try {
                    this.serverSocket = new ServerSocket(i);
                } catch (IOException e) {
                    if (getLog().isInfoEnabled()) {
                        getLog().info("Could not bind to port: " + Integer.toString(i));
                    }
                }
                if (this.serverSocket != null && this.serverSocket.isBound()) {
                    break;
                }
            }
        }

        if (this.serverSocket != null) { 
            String port = Integer.toString(this.serverSocket.getLocalPort());
            if (getLog().isInfoEnabled()) {
                getLog().info("Push Listener bound to port: " + port);
            }
        } else {
            getLog().info("Push Listener could not be enabled. Ports not available.");        
        }
        
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Socket s = this.serverSocket.accept();
                // TODO: check if buffered stream is necessary
                InputStream is = new BufferedInputStream(s.getInputStream());

                // Read one byte and send heartbeat if byte is read.
                if (is.read() != -1) {
                    if (getLog().isInfoEnabled()) {
                        getLog().info("Push Request Received from host: " + s.getInetAddress());
                    }
                    ICommandExecutor commandExecutor = (ICommandExecutor) ComponentManagerFactory.getComponentManager().getComponent(ICommandExecutor.NAME);
                    commandExecutor.sendHeartBeat();
                    PrintWriter out = new PrintWriter(s.getOutputStream());
                    out.print('x');
                    out.flush();
                }

            } catch (SocketException socketException) {
                // Should be caused if socket was closed.
                continue;
            } catch (IOException e) {
                getLog().error("Error while listening for push requests.");
            }
        }
    }

    /**
     * @return port number on which the PushListener is listening. -1 if no port
     *         is set up
     */
    public int getPort() {
        if (this.serverSocket != null) {
            return this.serverSocket.getLocalPort();
        }
        return -1;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IStartable#start()
     */
    public void start() {
        if (this.serverSocket != null) {
            if (getLog().isInfoEnabled()) {
                getLog().info("Push Listener started.");
            }

            this.listenerThread = new Thread(this, "PushListener");
            this.listenerThread.start();
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IStartable#stop()
     */
    public void stop() {
        if (this.serverSocket != null) {
            if (getLog().isInfoEnabled()) {
                getLog().info("Push Listener stopped.");
            }

            try {
                this.listenerThread.interrupt();
                this.serverSocket.close();
            } catch (IOException e) {
                getLog().error("Error while closing ServerSocket.");
            }
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.config;
    }

}
