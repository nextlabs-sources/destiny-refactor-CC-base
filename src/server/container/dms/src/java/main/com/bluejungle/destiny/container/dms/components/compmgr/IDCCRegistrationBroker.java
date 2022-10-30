/*
 * Created on Jan 1, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.container.dms.components.compmgr;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;

import com.bluejungle.destiny.container.dcc.DCCComponentEnumType;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.exceptions.RegistrationFailedException;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/compmgr/IDCCComponentAndEventMgrFacade.java#3 $:
 */

public interface IDCCRegistrationBroker {

    public static final String COMP_NAME = "RegistrationBroker";

    /**
     * Unregisters a component
     * 
     * @param compId
     *            component id
     * @throws RemoteException
     *             if unregistering fails
     * @throws RegistrationFailedException
     */
    public void unregisterComponent(IDCCRegistrationInfo unregInfo) throws ComponentRegistrationException;

    /**
     * Registers for an event. The DMS container does not directly register for
     * an event, but dispatches the registration request to other DCSF
     * components scattered around the DCC unit. This method gathers the set of
     * unique DCSF instances, and dispatches the registration request.
     * Additionally, the DMS keeps track of which notifications are already
     * registered, so that new DCC components coming up can become listeners as
     * well.
     * 
     * @param eventName
     *            name of the event
     * @param callback
     *            URL to callback when the event fires
     * @throws RegistrationFailedException
     */
    public void registerEvent(String eventName, URL callback) throws EventRegistrationException;

    /**
     * Unregisters an event from a remote instance. This function is called only
     * when the installation of DCC is distrubuted.
     * 
     * @param eventName
     *            name of the event to unregister
     * @param callback
     *            callback URL of the listener
     * @throws RegistrationFailedException
     * @see com.bluejungle.destiny.services.management.ComponentServiceIF#unregisterEvent(java.lang.String,
     *      org.apache.axis.types.URI)
     */
    public void unregisterEvent(String eventName, URL callback) throws EventRegistrationException;

    /**
     * Registers a DCC component
     * 
     * @param regInfo
     *            registration information
     * @return registration status
     * @throws ComponentRegistrationException
     *             if registration fails
     * @throws ConfigNotFoundException
     *             if no configuration could be found for this component
     */
    public IDCCRegistrationStatus registerComponent(IDCCRegistrationInfo regInfo) throws ComponentRegistrationException, ConfigNotFoundException;

    /**
     * This function updates the component status and is called when a given DCC
     * component sends a heartbeat to DMS. The DMS container uses heartbeat
     * updates to dispatch the event listener information to other DCSF
     * components scattered around the DCC unit.
     * 
     * @param info
     *            component heartbeat information
     * @return update for the DCC component
     */
    public IComponentHeartbeatResponse checkUpdates(IComponentHeartbeatInfo info);

    /**
     * Returns the list of all registered components
     * 
     * @return a list of all registered IDCCComponetDO instancess
     * @throws DataSourceException
     *             if a persistence error ocurrs
     */
    public List<IDCCComponentDO> getRegisteredComponents() throws DataSourceException;

    /**
     * Returns the list of all registered components by type
     * 
     * @param type
     *            type of the components to retrieve
     * @return a list of all registered IDCCComponentDO instances of the
     *         specified type
     * @throws DataSourceException
     *             if a persistence error ocurrs
     */
    public List<IDCCComponentDO> getRegisteredComponentsByType(DCCComponentEnumType type) throws DataSourceException;
}