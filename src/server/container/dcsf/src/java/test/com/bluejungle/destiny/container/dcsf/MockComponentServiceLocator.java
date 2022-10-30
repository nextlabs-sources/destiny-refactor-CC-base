/*
 * Created on Dec 9, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import javax.xml.rpc.ServiceException;

import com.bluejungle.destiny.services.management.ComponentServiceIF;
import com.bluejungle.destiny.services.management.ComponentServiceLocator;

/**
 * This class is a dummy component service locator. It returns a dummy web
 * service implementation to see whether the web service is properly invoked.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/dcsf/src/java/test/com/bluejungle/destiny/container/dcsf/MockComponentServiceLocator.java#1 $:
 */

public class MockComponentServiceLocator extends ComponentServiceLocator {

    private static final MockComponentServiceStub SERVICE_STUB = new MockComponentServiceStub();

    /**
     * @return the dummy component service
     * @throws ServiceException
     */
    public ComponentServiceIF getComponentServiceIFPort() throws ServiceException {
        return SERVICE_STUB;
    }

    /**
     * Returns the service stub.
     * 
     * @return the service stub.
     */
    public static MockComponentServiceStub getServiceStub() {
        return SERVICE_STUB;
    }
}
