/*
 * Created on Oct 19, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.destiny.container.dms;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.dcc.DCCComponentEnumType;
import com.bluejungle.destiny.container.dms.components.compmgr.ComponentRegistrationException;
import com.bluejungle.destiny.container.dms.components.compmgr.ConfigNotFoundException;
import com.bluejungle.destiny.container.dms.components.compmgr.EventRegistrationException;
import com.bluejungle.destiny.container.dms.components.compmgr.IDCCComponentDO;
import com.bluejungle.destiny.container.dms.components.compmgr.IDCCRegistrationBroker;
import com.bluejungle.destiny.container.shared.sharedfolder.ISharedFolderInformationSource;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderData;
import com.bluejungle.destiny.framework.types.CommitFault;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.services.management.ComponentServiceIF;
import com.bluejungle.destiny.services.management.types.Component;
import com.bluejungle.destiny.services.management.types.ComponentHeartbeatInfo;
import com.bluejungle.destiny.services.management.types.ComponentHeartbeatUpdate;
import com.bluejungle.destiny.services.management.types.ComponentList;
import com.bluejungle.destiny.services.management.types.Cookie;
import com.bluejungle.destiny.services.management.types.DCCRegistrationInformation;
import com.bluejungle.destiny.services.management.types.DCCRegistrationStatus;
import com.bluejungle.destiny.services.management.types.RegistrationFailedException;
import com.bluejungle.destiny.types.shared_folder.SharedFolderData;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.utils.CollectionUtils;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/DMSComponentServiceImpl.java#12 $
 */
public class DMSComponentServiceImpl implements ComponentServiceIF {

    private Log log = LogFactory.getLog(DMSComponentServiceImpl.class.getName());
    private IDCCRegistrationBroker regBrokerFacade;
    private ISharedFolderInformationSource sharedFolderInfoSource;

    /**
     * Returns a list of registered components
     * 
     * @return a list of registered components
     * @throws RemoteException
     *             if retrieving the list fails
     */
    public ComponentList getComponents() throws CommitFault, RemoteException {
        IDCCRegistrationBroker compFacade = this.getCompFacade();
        ComponentList listToReturn;
        try {
            List<IDCCComponentDO> registeredComponents = compFacade.getRegisteredComponents();
            listToReturn = buildComponentList(registeredComponents);
        } catch (DataSourceException exception) {
            throw new CommitFault(); // FIX ME - We really need a general db
            // fault, not just a commit fault. Name
            // needs to be changed
        }
        return listToReturn;
    }

    /**
     * Returns a list of registered components of the specified type
     * 
     * @return a list of registered components of the specified type
     * @throws RemoteException
     *             if retrieving the list fails
     */
    public ComponentList getComponentsByType(String componentType) throws CommitFault, RemoteException {
        IDCCRegistrationBroker compFacade = this.getCompFacade();
        ComponentList listToReturn;
        try {
            DCCComponentEnumType type = DCCComponentEnumType.getServerComponentTypeEnum(componentType);
            List<IDCCComponentDO> registeredComponents = compFacade.getRegisteredComponentsByType(type);
            listToReturn = buildComponentList(registeredComponents);
        } catch (DataSourceException exception) {
            throw new CommitFault(); // FIX ME - We really need a general db
            // fault, not just a commit fault. Name
            // needs to be changed
        }
        return listToReturn;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    protected Log getLog() {
        return this.log;
    }

    /**
     * Unregisters a component
     * 
     * @param compId
     *            component id
     * @throws RemoteException
     *             if unregistering fails
     */
    public void unregisterComponent(DCCRegistrationInformation unregInfo)
            throws RegistrationFailedException, ServiceNotReadyFault, RemoteException {
        IDCCRegistrationBroker compFacade = this.getCompFacade();
        try {
            IDCCRegistrationInfo regInfo = WebServiceHelper.convertDCCRegistration(unregInfo);
            compFacade.unregisterComponent(regInfo);
        } catch (ComponentRegistrationException e) {
            String[] errorMessages = CollectionUtils.toStringArray(e.getErrorMessages());
            throw new RegistrationFailedException(errorMessages);
        }
    }

    /**
     * Registers for an event.
     * 
     * @param eventName
     *            name of the event
     * @param wsCallback
     *            URL to callback when the event fires
     * @throws RemoteException
     *             if the event registration fails
     */
    public void registerEvent(String eventName, URI wsCallback) 
            throws RegistrationFailedException, ServiceNotReadyFault, RemoteException {
        IDCCRegistrationBroker compFacade = this.getCompFacade();
        try {
            URL callback = new URL(wsCallback.toString());
            compFacade.registerEvent(eventName, callback);
        } catch (EventRegistrationException e) {
            throw new RegistrationFailedException();
        } catch (MalformedURLException e) {
            throw new RegistrationFailedException();
        }
    }

    /**
     * Unregisters an event from a remote instance. This function is called only
     * when the installation of DCC is distrubuted.
     * 
     * @param eventName
     *            name of the event to unregister
     * @param callback
     *            callback URL of the listener
     * @throws RemoteException
     *             if the event unregistration fails
     * @see com.bluejungle.destiny.services.management.ComponentServiceIF#unregisterEvent(java.lang.String,
     *      org.apache.axis.types.URI)
     */
    public void unregisterEvent(String eventName, URI wsCallback) 
            throws RegistrationFailedException, ServiceNotReadyFault, RemoteException {
        IDCCRegistrationBroker compFacade = this.getCompFacade();
        try {
            URL callback = new URL(wsCallback.toString());
            compFacade.unregisterEvent(eventName, callback);
        } catch (EventRegistrationException e) {
            throw new RegistrationFailedException();
        } catch (MalformedURLException e) {
            throw new RegistrationFailedException();
        }
    }

    /**
     * Registers a DCC component
     * 
     * @param regInfo
     *            registration information
     * @return registration status
     * @throws RegistrationFailedException
     *             if the registration fails
     */
    public DCCRegistrationStatus registerComponent(
            DCCRegistrationInformation regInfoDTO)
            throws RegistrationFailedException, ServiceNotReadyFault, RemoteException {
        IDCCRegistrationBroker compFacade = this.getCompFacade();
        DCCRegistrationStatus regStatusDTO;
        try {
            IDCCRegistrationInfo dccRegInfo = WebServiceHelper.convertDCCRegistration(regInfoDTO);
            IDCCRegistrationStatus dccRegStatus = compFacade.registerComponent(dccRegInfo);
            regStatusDTO = WebServiceHelper.convertDCCRegistrationStatusToServiceType(dccRegStatus);
        } catch (ComponentRegistrationException e) {
            String[] errorMessages = CollectionUtils.toStringArray(e.getErrorMessages());
            throw new RegistrationFailedException(errorMessages);
        } catch (ConfigNotFoundException e) {
            throw new RegistrationFailedException();
        }
        return regStatusDTO;
    }

    /**
     * This function updates the component status and is called when a given DCC
     * component sends a heartbeat to DMS.
     * 
     * @param info
     *            component heartbeat information
     * @return update for the DCC component
     * @throws RemoteException
     *             if the heartbeat processing fails
     * @throws ServiceNotReadyFault
     *             if the service is not yet ready to receive requests
     */
    public ComponentHeartbeatUpdate checkUpdates(ComponentHeartbeatInfo info) 
            throws ServiceNotReadyFault, RemoteException {
        IDCCRegistrationBroker compFacade = this.getCompFacade();
        IComponentHeartbeatResponse heartbeatUpdate = compFacade.checkUpdates(WebServiceHelper.convertComponentHeartbeatInfo(info));

        ComponentHeartbeatUpdate wsCompHeartbeatUpdate = new ComponentHeartbeatUpdate();
        wsCompHeartbeatUpdate.setConfigurationUpdate(null);
        if (heartbeatUpdate.getCookie() != null) {
            wsCompHeartbeatUpdate.setCookie(new Cookie(heartbeatUpdate.getCookie().getUpdateTimestamp()));
        }
        wsCompHeartbeatUpdate.setEventRegistrations(WebServiceHelper.convertEventRegistrationsInfo(heartbeatUpdate.getEventRegistrationInfo()));

        // Check if there are any shared-folder related updates and send them
        // over. Need to optimize since we only send this info back to the DABS
        // components:
        if (DCCComponentEnumType.DABS.getName().equals(info.getCompType())) {
            ISharedFolderInformationSource sharedFolderInfoSource = getSharedFolderInformationSource();
            ISharedFolderCookie sharedFolderInfoCookie = WebServiceHelper.convertSharedFolderCookieData(info.getSharedFolderDataCookie());
            ISharedFolderData sharedFolderInfo = sharedFolderInfoSource.getSharedFolderInformationUpdateSince(sharedFolderInfoCookie);
            SharedFolderData sharedFolderDataUpdate = WebServiceHelper.convertFromSharedFolderData(sharedFolderInfo);
            wsCompHeartbeatUpdate.setSharedFolderData(sharedFolderDataUpdate);
        }

        return wsCompHeartbeatUpdate;
    }

    /**
     * Returns an instance of the IDCCRegistrationBroker implementation class
     * after obtaining it from the component manager.
     * 
     * @return IDCCRegistrationBroker
     */
    protected IDCCRegistrationBroker getCompFacade() throws ServiceNotReadyFault {
        if (this.regBrokerFacade == null) {
            IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
            if (!compMgr.isComponentRegistered(IDCCRegistrationBroker.COMP_NAME)) {
                throw new ServiceNotReadyFault();
            }
            this.regBrokerFacade = (IDCCRegistrationBroker) compMgr.getComponent(IDCCRegistrationBroker.COMP_NAME);
        }
        return this.regBrokerFacade;
    }

    /**
     * Returns an instance of the ISharedFolderInformationSource implementation
     * class after obtaining it from the component manager.
     * 
     * @return ISharedFolderInformationSource
     */
    protected ISharedFolderInformationSource getSharedFolderInformationSource() throws ServiceNotReadyFault {
        if (this.sharedFolderInfoSource == null) {
            IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
            if (!compMgr.isComponentRegistered(ISharedFolderInformationSource.COMP_NAME)) {
                throw new ServiceNotReadyFault();
            }
            this.sharedFolderInfoSource = (ISharedFolderInformationSource) compMgr.getComponent(
                    ISharedFolderInformationSource.COMP_NAME);
        }
        return this.sharedFolderInfoSource;
    }

    /**
     * Build a ComponentList instance from the specified list of IDCCComponentDO
     * objects
     * 
     * @param registeredComponents
     *            the list of components which will comprise the ComponentList
     * @return a ComponentList instance built from the specified list of
     *         IDCCComponentDO objects
     */
    private ComponentList buildComponentList(List<IDCCComponentDO> registeredComponents) {
        ComponentList componentsToReturn = new ComponentList();

        Component[] resultsToReturn = new Component[registeredComponents.size()];

        /**
         * Unfortunately, at this point, we either need to extend
         * services.types.ComponentList or iterate through the list in order to
         * return the correct type I've chosen to iterate through the list
         */
        for (int i = 0; i < registeredComponents.size(); i++) {
            resultsToReturn[i] = buildComponent(registeredComponents.get(i));
        }

        componentsToReturn.setComp(resultsToReturn);
        return componentsToReturn;
    }

    /**
     * Converts a component object to a web service component object
     * 
     * @param component
     *            component object
     * @return the corresponding web service component object
     */
    private Component buildComponent(IDCCComponentDO component) {
        Component wsComponent = new Component(
                component.getId().longValue(), //long id,
                component.getName(), //java.lang.String name,
                component.getType().getName(), //java.lang.String type,
                component.getTypeDisplayName(), //java.lang.String displayName,
                component.getCallbackURL(), //java.lang.String callbackURL,
                component.getComponentURL(), //java.lang.String componentURL,
                component.getLoadBalancerURL(), //java.lang.String loadBalancerURL,
                component.getLastHeartbeat().getTimeInMillis(), //long lastHeartbeat,
                component.getHeartbeatRate(), //int heartbeatRate,
                component.isActive() //boolean active
        );
        return wsComponent;
    }

}
