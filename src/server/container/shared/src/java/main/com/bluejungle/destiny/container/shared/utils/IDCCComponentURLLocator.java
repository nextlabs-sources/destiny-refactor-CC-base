/*
 * Created on May 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.utils;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;

/**
 * Interface to locate urls of DCC components
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/utils/IDCCComponentURLLocator.java#1 $
 */

public interface IDCCComponentURLLocator extends IManagerEnabled, ILogEnabled {

    /*
     * Component name:
     */
    public static final String COMP_NAME = "DCCComponentURLLocator";

    /**
     * Returns an array of URLs for all DCC components of a given type
     * 
     * @param componentType
     * @return array of urls
     */
    public String[] getComponentURLs(ServerComponentType componentType) throws ServiceNotReadyFault, UnauthorizedCallerFault, RemoteException, ServiceException;
}