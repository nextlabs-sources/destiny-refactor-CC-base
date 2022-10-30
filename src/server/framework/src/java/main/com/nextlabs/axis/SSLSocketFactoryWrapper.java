/*
 * Created on Apr 9, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.axis;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The purpose of this class is to workaround the hang in org.apache.axis.components.net.JSEESocketFactory
 * During tomcat is shutting down. The sslSocket.startHandshake() is hanged and never return.
 * Since it is making a blocking I/O call. Thread.interrupt() doesn't do anything.
 * But we can do Socket.close(). It will interrupt the socket and throws a SockectException.
 * 
 * The workaround is set the timeout before handshake.
 * 
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/nextlabs/axis/SSLSocketFactoryWrapper.java#1 $
 */

public class SSLSocketFactoryWrapper extends SSLSocketFactory {
    private static final String GRACE_PERIOD_PROPERTY = SSLSocketFactoryWrapper.class.getName() + ".graceperiod";
    private static final int GRACE_PERIOD_DEFAULT = 3000;
    
    private static final String TIMEOUT_PROPERTY = SSLSocketFactoryWrapper.class.getName() + ".timeout";
    private static final int TIMEOUT_DEFAULT = 20000;
    
    private static final Log LOG = LogFactory.getLog(SSLSocketFactoryWrapper.class);
    
    private static final Set<SSLSocketWrapper> openSockets = new HashSet<SSLSocketWrapper>();
    private static volatile boolean isClosed = false;
    
    
    private final SSLSocketFactory original;
    
    public SSLSocketFactoryWrapper(SSLSocketFactory original) {
        this.original = original;
    }
    
    
    public static void shutdown() {
        int gracePeriod = getIntProperty(GRACE_PERIOD_PROPERTY, GRACE_PERIOD_DEFAULT);
        int timeout = getIntProperty(TIMEOUT_PROPERTY, TIMEOUT_DEFAULT);
        shutdown(gracePeriod, timeout);
    }
    
    private static int getIntProperty(String key, int def) {
        String value = System.getProperty(key);
        if (value == null) {
            return def;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOG.warn("Invalid integer value for property '" + key + "'. Default value, " + def
                    + ",will be used", e);
            return def;
        }
    }
    
    private static class ShutdownThread extends Thread {
        final int gracePeriod;
        final int timeout;
        
        ShutdownThread(int gracePeriod, int timeout) {
            super();
            this.gracePeriod = gracePeriod;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            LOG.trace("Socket Factory will be closed in " + timeout + " ms.");
            
            try {
                Thread.sleep(gracePeriod);
            } catch (InterruptedException e) {
                // I wake up early.
            }
            
            synchronized (openSockets) {
                isClosed = true;
                // no more sockets can open from now
                
                if(openSockets.isEmpty()){
                    LOG.debug("No open sockets need to be closed.");
                    return;
                }
                
                // I still have some open sockets. let's wait
                int delta = timeout - gracePeriod;
                if (delta > 0) {
                    try {
                        // I am going to wait a while, the last socket should notify me.
                        openSockets.wait(delta);
                    } catch (InterruptedException e) {
                        // I wake up early again.
                    }
                }
            }
            
            
            // ready to close all reminding open sockets
            // If the lock was timeout, there should be some open sockets.
            // If the lock was notifited, the queue is empty.
            // In any case, I am going to check.
            
            int total;
            synchronized (openSockets) {
                total = openSockets.size();
                
                LOG.debug("Closing " + total + " open sockets.");
                SSLSocketWrapper[] willClosedSockets = openSockets.toArray(new SSLSocketWrapper[total]);
                for(SSLSocketWrapper s : willClosedSockets){
                    try {
                        s.socket.close();
                    } catch (IOException e) {
                        String message = e.getMessage();
                        if (message == null || ! message.equals("socket closed")) {
                            // unexpected message.
                            LOG.warn("Error during closing a socket.", e);
                        }
                    }
                }
                openSockets.clear();
            }
            LOG.debug("Closed all " + total + " open sockets.");
        }
    }
    
    
    /**
     * A timeout of zero is interpreted as an infinite timeout.
     * @param timeout in milliseconds
     */
    public static void shutdown(final int gracePeriod, final int timeout) throws IllegalArgumentException{
        if(gracePeriod > timeout){
            throw new IllegalArgumentException("The grace period can't be greater than the timeout.");
        }
        if (isClosed) {
            //already closed
            return;
        }
        new ShutdownThread(gracePeriod, timeout).start();
    }
    
    protected Socket wrap(Socket socket) throws SocketException {
        SSLSocketWrapper s = new SSLSocketWrapper((SSLSocket)socket);
        synchronized (openSockets) {
            if(isClosed){
                throw new SocketException("The socket factory is closed");
            }
            
            openSockets.add(s);
        }
        return s;
    }
    
    // this must be extending SSLSocket but not Socket
    // the JSSESocketFactory will cast this to SSLSocket.
    private class SSLSocketWrapper extends SSLSocket {
        final SSLSocket socket;

        SSLSocketWrapper(SSLSocket socket) {
            super();
            this.socket = socket;
        }

        public void close() throws IOException {
            synchronized (openSockets) {
                openSockets.remove(this);
                if (isClosed && openSockets.isEmpty()) {
                    // the factory is closing, and I am the last one. I will close the door.
                    openSockets.notifyAll();
                }
            }
            socket.close();
        }

        public void addHandshakeCompletedListener(
                HandshakeCompletedListener handshakecompletedlistener) {
            socket.addHandshakeCompletedListener(handshakecompletedlistener);
        }
        public void bind(SocketAddress bindpoint) throws IOException {
            socket.bind(bindpoint);
        }
        public void connect(SocketAddress endpoint, int timeout) throws IOException {
            socket.connect(endpoint, timeout);
        }
        public void connect(SocketAddress endpoint) throws IOException {
            socket.connect(endpoint);
        }
        public boolean equals(Object obj) {
            return socket.equals(obj);
        }
        public SocketChannel getChannel() {
            return socket.getChannel();
        }
        public String[] getEnabledCipherSuites() {
            return socket.getEnabledCipherSuites();
        }
        public String[] getEnabledProtocols() {
            return socket.getEnabledProtocols();
        }
        public boolean getEnableSessionCreation() {
            return socket.getEnableSessionCreation();
        }
        public InetAddress getInetAddress() {
            return socket.getInetAddress();
        }
        public InputStream getInputStream() throws IOException {
            return socket.getInputStream();
        }
        public boolean getKeepAlive() throws SocketException {
            return socket.getKeepAlive();
        }
        public InetAddress getLocalAddress() {
            return socket.getLocalAddress();
        }
        public int getLocalPort() {
            return socket.getLocalPort();
        }
        public SocketAddress getLocalSocketAddress() {
            return socket.getLocalSocketAddress();
        }
        public boolean getNeedClientAuth() {
            return socket.getNeedClientAuth();
        }
        public boolean getOOBInline() throws SocketException {
            return socket.getOOBInline();
        }
        public OutputStream getOutputStream() throws IOException {
            return socket.getOutputStream();
        }
        public int getPort() {
            return socket.getPort();
        }
        public int getReceiveBufferSize() throws SocketException {
            return socket.getReceiveBufferSize();
        }
        public SocketAddress getRemoteSocketAddress() {
            return socket.getRemoteSocketAddress();
        }
        public boolean getReuseAddress() throws SocketException {
            return socket.getReuseAddress();
        }
        public int getSendBufferSize() throws SocketException {
            return socket.getSendBufferSize();
        }
        public SSLSession getSession() {
            return socket.getSession();
        }
        public int getSoLinger() throws SocketException {
            return socket.getSoLinger();
        }
        public int getSoTimeout() throws SocketException {
            return socket.getSoTimeout();
        }
        public SSLParameters getSSLParameters() {
            return socket.getSSLParameters();
        }
        public String[] getSupportedCipherSuites() {
            return socket.getSupportedCipherSuites();
        }
        public String[] getSupportedProtocols() {
            return socket.getSupportedProtocols();
        }
        public boolean getTcpNoDelay() throws SocketException {
            return socket.getTcpNoDelay();
        }
        public int getTrafficClass() throws SocketException {
            return socket.getTrafficClass();
        }
        public boolean getUseClientMode() {
            return socket.getUseClientMode();
        }
        public boolean getWantClientAuth() {
            return socket.getWantClientAuth();
        }
        public int hashCode() {
            return socket.hashCode();
        }
        public boolean isBound() {
            return socket.isBound();
        }
        public boolean isClosed() {
            return socket.isClosed();
        }
        public boolean isConnected() {
            return socket.isConnected();
        }
        public boolean isInputShutdown() {
            return socket.isInputShutdown();
        }
        public boolean isOutputShutdown() {
            return socket.isOutputShutdown();
        }
        public void removeHandshakeCompletedListener(
                HandshakeCompletedListener handshakecompletedlistener) {
            socket.removeHandshakeCompletedListener(handshakecompletedlistener);
        }
        public void sendUrgentData(int data) throws IOException {
            socket.sendUrgentData(data);
        }
        public void setEnabledCipherSuites(String[] as) {
            socket.setEnabledCipherSuites(as);
        }
        public void setEnabledProtocols(String[] as) {
            socket.setEnabledProtocols(as);
        }
        public void setEnableSessionCreation(boolean flag) {
            socket.setEnableSessionCreation(flag);
        }
        public void setKeepAlive(boolean on) throws SocketException {
            socket.setKeepAlive(on);
        }
        public void setNeedClientAuth(boolean flag) {
            socket.setNeedClientAuth(flag);
        }
        public void setOOBInline(boolean on) throws SocketException {
            socket.setOOBInline(on);
        }
        public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
            socket.setPerformancePreferences(connectionTime, latency, bandwidth);
        }
        public void setReceiveBufferSize(int size) throws SocketException {
            socket.setReceiveBufferSize(size);
        }
        public void setReuseAddress(boolean on) throws SocketException {
            socket.setReuseAddress(on);
        }
        public void setSendBufferSize(int size) throws SocketException {
            socket.setSendBufferSize(size);
        }
        public void setSoLinger(boolean on, int linger) throws SocketException {
            socket.setSoLinger(on, linger);
        }
        public void setSoTimeout(int timeout) throws SocketException {
            socket.setSoTimeout(timeout);
        }
        public void setSSLParameters(SSLParameters sslparameters) {
            socket.setSSLParameters(sslparameters);
        }
        public void setTcpNoDelay(boolean on) throws SocketException {
            socket.setTcpNoDelay(on);
        }
        public void setTrafficClass(int tc) throws SocketException {
            socket.setTrafficClass(tc);
        }
        public void setUseClientMode(boolean flag) {
            socket.setUseClientMode(flag);
        }
        public void setWantClientAuth(boolean flag) {
            socket.setWantClientAuth(flag);
        }
        public void shutdownInput() throws IOException {
            socket.shutdownInput();
        }
        public void shutdownOutput() throws IOException {
            socket.shutdownOutput();
        }
        public void startHandshake() throws IOException {
            socket.startHandshake();
        }
        public String toString() {
            return socket.toString();
        }
    }
    
    @Override
    public String[] getDefaultCipherSuites() {
        return original.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return original.getSupportedCipherSuites();
    }
    
    @Override
    public Socket createSocket(Socket socket, String s, int i, boolean flag) throws IOException {
        socket = original.createSocket(socket, s, i, flag);
        return wrap(socket);
    }

    @Override
    public Socket createSocket(String s, int i) throws IOException, UnknownHostException {
        Socket socket = original.createSocket(s, i);
        return wrap(socket);
    }

    @Override
    public Socket createSocket(InetAddress inetaddress, int i) throws IOException {
        Socket socket = original.createSocket(inetaddress, i);
        return wrap(socket);
    }

    @Override
    public Socket createSocket(String s, int i, InetAddress inetaddress, int j) throws IOException,
            UnknownHostException {
        Socket socket = original.createSocket(s, i, inetaddress, j);
        return wrap(socket);
    }

    @Override
    public Socket createSocket(InetAddress inetaddress, int i, InetAddress inetaddress1, int j)
            throws IOException {
        Socket socket = original.createSocket(inetaddress, i, inetaddress1, j);
        return wrap(socket);
    }
}
