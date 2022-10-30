/*
 * Created on Feb 25, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs.components.deployment;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * This dummy agent listener listens on some port. It is used to simulate an
 * agent that could push information.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/test/com/bluejungle/destiny/container/dabs/components/deployment/MockAgentListener.java#1 $
 */

class MockAgentListener implements Runnable {

    private int port;
    private ServerSocket socket;

    /**
     * Constructor
     * 
     * @param port
     *            port to listen on
     */
    public MockAgentListener(int port) {
        super();
        this.port = port;
    }

    /**
     * Creates a socket on the given port and listens on this port.
     */
    public void run() {
        try {
            socket = new ServerSocket(port);
        } catch (IOException e1) {
            return;
        }
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket s = socket.accept();
                InputStream is = new BufferedInputStream(s.getInputStream());

                // Read one byte - Note that interrupted status does
                // not interrupt blocked IO
                if (is.read() != -1) {
                    PrintWriter out = new PrintWriter(s.getOutputStream());
                    out.print('x');
                    out.flush();
                }
            } catch (SocketException socketException) {
                continue;
            } catch (IOException e) {
                return;
            }
        }
    }
}
