/*
 * Created on May 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.utils.defaultimpl;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.dcc.IDCCContainer;
import com.bluejungle.destiny.container.shared.utils.IDCCComponentURLLocator;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.destiny.services.management.ComponentServiceIF;
import com.bluejungle.destiny.services.management.ComponentServiceLocator;
import com.bluejungle.destiny.services.management.types.Component;
import com.bluejungle.destiny.services.management.types.ComponentList;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/utils/defaultimpl/DCCComponentURLLocatorImpl.java#1 $
 */

public class DCCComponentURLLocatorImpl implements IDCCComponentURLLocator {

    /*
     * Constants:
     */
    private static final String COMPONENT_SERVICE_LOCATION_SERVLET_PATH = "/services/ComponentServiceIFPort";

    /*
     * Private variables:
     */
    private Log logger;
    private IComponentManager manager;
    private ComponentServiceIF componentService;

    /**
     * @see com.bluejungle.destiny.container.shared.utils.IDCCComponentURLLocator#getComponentURLs(ServerComponentType)
     */
    public String[] getComponentURLs(ServerComponentType componentType) throws ServiceNotReadyFault, UnauthorizedCallerFault, RemoteException, ServiceException {
        String[] urls = null;
        ComponentServiceIF componentService = getComponentServiceInterface();
        ComponentList dccComponentList = componentService.getComponentsByType(componentType.getName());
        if (dccComponentList != null) {
            Component[] dccComponentsArr = dccComponentList.getComp();
            if (dccComponentsArr != null) {
                urls = new String[dccComponentsArr.length];
                for (int i = 0; i < urls.length; i++) {
                    urls[i] = dccComponentsArr[i].getLoadBalancerURL();
                }
            }
        }
        return urls;
    }

    /**
     * Retrieve the Component Service interface.
     * 
     * @return the Component Service interface
     * @throws ServiceException
     * @throws ServiceException
     *             if the component service interface could not be located
     */
    private ComponentServiceIF getComponentServiceInterface() throws ServiceException {
        if (this.componentService == null) {
            IComponentManager compMgr = this.getManager();
            IConfiguration mainConfig = (IConfiguration) compMgr.getComponent(IDCCContainer.MAIN_COMPONENT_CONFIG_COMP_NAME);
            String location = (String) mainConfig.get(IDCCContainer.DMS_LOCATION_CONFIG_PARAM);
            if (location == null) {
                // We may be in DMS - This is a bit of a hack, but I'm not sure
                // what else to do
                location = (String) mainConfig.get(IDCCContainer.COMPONENT_LOCATION_CONFIG_PARAM);
            }
            location += COMPONENT_SERVICE_LOCATION_SERVLET_PATH;
            ComponentServiceLocator locator = new ComponentServiceLocator();
            locator.setComponentServiceIFPortEndpointAddress(location);

            this.componentService = locator.getComponentServiceIFPort();
        }

        return this.componentService;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.logger;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.logger = log;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.manager;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }
}