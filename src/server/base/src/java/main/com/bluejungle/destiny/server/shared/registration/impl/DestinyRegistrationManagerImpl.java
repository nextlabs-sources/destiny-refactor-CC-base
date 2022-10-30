/*
 * Created on Oct 24, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.destiny.server.shared.registration.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bluejungle.destiny.server.shared.context.IDCSFRegistrationListener;
import com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener;
import com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager;
import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;

/**
 * This is the DCC component registration manager class. This class queues the
 * DCC component registration request with DMS until the DCSF component is
 * registered. Once the DCSF application is up and running, this class simply
 * forwards the registration request directly to the DCSF application.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/registration/DestinyRegistrationManagerImpl.java#7 $:
 */
public class DestinyRegistrationManagerImpl implements IDestinyRegistrationManager {

    /**
     * Empty heartbeat response
     */
    private static final IComponentHeartbeatResponse EMPTY_RESPONSE = new ComponentHeartbeatResponseImpl();

    IRegisteredDCSFComponent dcsfComponent;
    private List<ComponentRegistrationRequest> registrationQueue;
    private Set<IRegisteredDCCComponent> components; //set of registered components
    private Set<IDCSFRegistrationListener> dcsfRegistrationListeners;

    /**
     * Constructor
     */
    public DestinyRegistrationManagerImpl() {
        super();
        components = new HashSet<IRegisteredDCCComponent>();
        dcsfRegistrationListeners = new HashSet<IDCSFRegistrationListener>();
        registrationQueue = new ArrayList<ComponentRegistrationRequest>();
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#sendHeartbeat(com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo)
     */
    public IComponentHeartbeatResponse sendHeartbeat(IComponentHeartbeatInfo heartbeatInfo) {
        IRegisteredDCSFComponent dcsfApp;
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        synchronized (this) {
            if (dcsfComponent == null) {
                return EMPTY_RESPONSE;
            } else {
                dcsfApp = dcsfComponent;
            }
        }
        Thread.currentThread().setContextClassLoader(dcsfApp.getClass().getClassLoader());
        final IComponentHeartbeatResponse response = dcsfApp.sendHeartbeat(heartbeatInfo);
        Thread.currentThread().setContextClassLoader(contextClassLoader);
        return response;
    }

    /**
     * Add a listener to be notified when the DCSF web application is registered
     * 
     * @param listener
     *            callback object
     */
    public void addDCSFRegistrationListener(IDCSFRegistrationListener listener) {
        synchronized (this) {
            dcsfRegistrationListeners.add(listener);
        }
    }

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IDestinyRegistrationManager#getDefaultRegistrationInfo()
     */
    public IDCCRegistrationInfo getDefaultRegistrationInfo() {
        return new DCCRegistrationInfoImpl();
    }

    /**
     * Registers a given container. If the container is a DCSF container, then
     * the registration manager is ready to perform registration with DMS.
     * 
     * @param component
     *            instance of the DCC component that gets registered
     */
    public void registerComponent(IRegisteredDCCComponent component) {
        synchronized (this) {
            //Notify the listeners only if the DCSF component was not already
            // added
            if (!components.contains(component)) {
                components.add(component);
                if (ServerComponentType.DCSF.equals(component.getComponentType())) {
                    //We have found the DCSF container! Now that the DCSF web
                    // application is registered, process all the queued DMS
                    // registration requests that we received so far. Also,
                    // notify
                    // all listeners that the DCSF web application is
                    // registered.
                    dcsfComponent = (IRegisteredDCSFComponent) component;

                    for (ComponentRegistrationRequest req : registrationQueue) {
                        processDMSRegistration(req.getRegistrationInfo(), req.getCallback());
                    }
                    registrationQueue.clear();

                    // Now, notify all listeners that the DCSF web application
                    // is registered.

                    for (IDCSFRegistrationListener listener : dcsfRegistrationListeners) {
                        listener.onDCSFRegistered(dcsfComponent);
                    }
                }
            }
        }
    }

    /**
     * Unregisters a given container. To unregister a container, the container
     * had to be registered in the past.
     * 
     * @param component
     *            instance of the DCC component that unregisters.
     */
    public void unregisterComponent(IRegisteredDCCComponent component) {
        synchronized (this) {
            //Fire notifications only if DCSF component was registered
            if (components.contains(component)) {
                components.remove(component);

                //Asks the DCSF web app to notify DMS about unregistration, if
                // DCSF is still here
                if (dcsfComponent != null) {
                    IDCCRegistrationInfo regInfo = getDefaultRegistrationInfo();
                    regInfo.setComponentName(component.getComponentName());
                    regInfo.setComponentType(component.getComponentType());
                    dcsfComponent.unregisterComponentWithDMS(regInfo);
                }

                //Fire listeners if necessary
                if (ServerComponentType.DCSF.equals(component.getComponentType())) {
                    //Notify all listeners that the DCSF web application is
                    //unregistered.

                    for (IDCSFRegistrationListener listener : dcsfRegistrationListeners) {
                        listener.onDCSFUnRegistered(dcsfComponent);
                    }
                    dcsfComponent = null;
                }
            }
        }
    }

    /**
     * Registers one DCC container with the DMS. If DCSF is not ready yet, then
     * the registration request is placed in a queue and will be processed once
     * DCSF is ready. The caller does not know what the DCSF URL is, so it is
     * the registration manager responsibility to place the correct DCSF service
     * URL in the registration info.
     * 
     * @param regInfo
     *            registration information
     * @param callback
     *            callback interface once the registration is complete
     */
    public void registerWithDMS(IDCCRegistrationInfo regInfo, IDMSRegistrationListener callback) {
        synchronized (this) {
            if (dcsfComponent == null) {
                registrationQueue.add(new ComponentRegistrationRequest(regInfo, callback));
                return;
            }
        }
        processDMSRegistration(regInfo, callback);
    }

    /**
     * Sends a DMS registration request to the DCSF component. This function
     * should always be called once the DCSF component has been registered.
     * 
     * @param regInfo
     *            DCC component registration information
     * @param callback
     *            callback interface that will be called back once the DMS
     *            registration is completed.
     */
    protected void processDMSRegistration(IDCCRegistrationInfo regInfo, IDMSRegistrationListener callback) {
        dcsfComponent.registerComponentWithDMS(regInfo, callback);
    }

    /**
     * The nested ComponentRegistrationRequest class is used only when
     * registration requests come up before the DCSF component is registered.
     * The registration with the DMS cannot occur unless DCSF is ready, so
     * instances of this class are created to queue up the registration
     * requests.
     */
    protected final class ComponentRegistrationRequest {

        protected IDCCRegistrationInfo registrationInfo;
        protected IDMSRegistrationListener callback;

        /**
         * Constructor
         * 
         * @param info
         *            registration information
         * @param cb
         *            DCSF URL to call back
         */
        ComponentRegistrationRequest(IDCCRegistrationInfo info, IDMSRegistrationListener cb) {
            this.registrationInfo = info;
            this.callback = cb;
        }

        /**
         * @return Returns the callback URL.
         */
        public IDMSRegistrationListener getCallback() {
            return callback;
        }

        /**
         * @return Returns the registration information
         */
        public IDCCRegistrationInfo getRegistrationInfo() {
            return registrationInfo;
        }
    }
}
