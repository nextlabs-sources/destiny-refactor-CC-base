/*
 * Created on Jun 24, 2014
 *
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/framework/plugins/rmi/RMIService.java#1 $:
 */

package com.nextlabs.framework.plugins.rmi;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This should be extended by any class that wants to bind as an RMI service
 */
public class RMIService extends UnicastRemoteObject {
    private static final Log log = LogFactory.getLog(RMIService.class);

    public RMIService() throws RemoteException {
        super();
    }

    public RMIService(int port) throws RemoteException {
        super(port);
    }

    public RMIService(int port, RMIClientSocketFactory clientSocketFactory, RMIServerSocketFactory serverSocketFactory) throws RemoteException {
        super(port, clientSocketFactory, serverSocketFactory);
    }

    protected void init(String serviceName, int rmiPortNumber) {
        // Find and connect to RMI server.
        Registry registry = null;
        
        try {
            log.debug("Locating registry at port " + rmiPortNumber);
            registry = LocateRegistry.getRegistry(rmiPortNumber);
            
            // Check if registry is already exist, else create new.
            try {
                // Don't worry about .list() result. Call this method to know
                // registry's state.
                registry.list();
            } catch (RemoteException re) {
                log.info("Creating registry");
                try {
                    registry = LocateRegistry.createRegistry(rmiPortNumber);
                } catch (RemoteException re1) {
                    log.error("RMIService registry creation failed. " + re1);
                    throw new RuntimeException("RMIService - Registry creation failed. ", re1);
                }
            }
            
            log.info("Binding");
            registry.bind(serviceName, this);
        } catch (AlreadyBoundException abe) {
            log.warn("Service " + serviceName + " object already bound", abe);
            try {
                log.info("Re-binding");
                registry.rebind(serviceName, this);
            }  catch (AccessException e) {
                throw new RuntimeException("Access exception while re-binding", e);
            }  catch (RemoteException e) {
                throw new RuntimeException("Remote exception while re-binding", e);
            }
        } catch (RemoteException re) {
            log.error("RMIService - Remote Exception", re);
            throw new RuntimeException(re);
        }
    }
}
