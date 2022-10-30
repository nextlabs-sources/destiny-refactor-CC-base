/*
 * Created on Oct 20, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;

import com.bluejungle.destiny.container.dcc.IDCCContainer;
import com.bluejungle.destiny.server.shared.internal.IInternalEventManager;
import com.bluejungle.destiny.server.shared.internal.IRegisteredDCSFComponent;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.services.dcsf.DCSFServiceIF;
import com.bluejungle.destiny.services.dcsf.types.DestinyEvent;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/**
 * This is the DCSF Web service implementation. The role of this service is to
 * allow remote DCC components (that are running on other JVM) to register to
 * listen to events fired locally, and to be notified when local events fire.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/DCSFServiceImpl.java#5 $:
 */
public class DCSFServiceImpl implements DCSFServiceIF, ServiceLifecycle {

    protected IInternalEventManager eventManager;
    protected String dcsfContainerName;

    /**
     * This API is called when a remote event has been fired. The DCSF service
     * dispatches this event to the local listeners
     * 
     * @param wsEvent
     *            event object
     * @throws ServiceNotReadyFault
     *             if the service is not ready
     */
    public void notifyEvent(DestinyEvent wsEvent) throws ServiceNotReadyFault {
        checkDCSFComponentReady();

        //Fires event with a "false" flag because the notification is remote
        this.eventManager.fireEvent(WebServiceHelper.convertToDCCServerEvent(wsEvent), false);
    }

    /**
     * This function is called when the web service is initialized. The
     * initialization saves the name of the DCSF container name, and gets a
     * reference to the event manager.
     * 
     * @param initParams
     *            web service initilization parameters
     * @throws ServiceException
     *             if the initialization fails
     * @see javax.xml.rpc.server.ServiceLifecycle#init(java.lang.Object)
     */
    public void init(Object initParams) throws ServiceException {
        //Finds the component manager name
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IConfiguration mainCompConfig = (HashMapConfiguration) compMgr.getComponent(IDCCContainer.MAIN_COMPONENT_CONFIG_COMP_NAME);
        this.dcsfContainerName = (String) mainCompConfig.get(IDCCContainer.COMPONENT_NAME_CONFIG_PARAM);

        //Finds the shared context locator
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) compMgr.getComponent(IDestinySharedContextLocator.COMP_NAME);
        this.eventManager = (IInternalEventManager) locator.getSharedContext().getEventManager();
    }

    /**
     * This function is called when the web service gets destroyed.
     * 
     * @see javax.xml.rpc.server.ServiceLifecycle#destroy()
     */
    public void destroy() {
        this.eventManager = null;
    }

    /**
     * Returns the instance of the DCSF component object. If this web service is
     * invoked too early (for example before the component is actually
     * registered with DMS, the component may be null
     * 
     * @return the instance of the DCSF component
     */
    protected IRegisteredDCSFComponent getDCSFComponent() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        return (IRegisteredDCSFComponent) compMgr.getComponent(this.dcsfContainerName);
    }

    /**
     * Checks whether the DCSF component is ready
     * 
     * @throws ServiceNotReadyFault
     *             if the DCC component is not ready
     */
    protected void checkDCSFComponentReady() throws ServiceNotReadyFault {
        try {
            IRegisteredDCSFComponent dcsf = getDCSFComponent();
        } catch (RuntimeException e) {
            //TODO: get better exception than RuntimeException from Sasha
            throw new ServiceNotReadyFault();
        }
    }
}
