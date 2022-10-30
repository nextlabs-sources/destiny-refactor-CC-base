/*
 * Created on Dec 9, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import javax.xml.rpc.ServiceException;

import com.bluejungle.destiny.services.dcsf.DCSFServiceIF;
import com.bluejungle.destiny.services.dcsf.DCSFServiceLocator;

/**
 * This class is a dummy DCSF service locator. It returns a dummy web service
 * implementation to see whether the web service is properly invoked.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/tests/MockDCSFServiceLocator.java#1 $:
 */

public class MockDCSFServiceLocator extends DCSFServiceLocator {

    private static final MockDCSFServiceStub SERVICE_STUB = new MockDCSFServiceStub();

    private static String address;

    /**
     * @return the dummy DCSF service
     * @throws ServiceException
     */
    public DCSFServiceIF getDCSFServiceIFPort() throws ServiceException {
        return SERVICE_STUB;
    }

    /**
     * Returns the service stub.
     * 
     * @return the service stub.
     */
    public static MockDCSFServiceStub getServiceStub() {
        return SERVICE_STUB;
    }

    /**
     * Sets the DCSF service port
     * 
     * @param addr
     *            address of the web service
     */
    public void setDCSFServiceIFPortEndpointAddress(String addr) {
        super.setDCSFServiceIFPortEndpointAddress(address);
        address = addr;
    }

    /**
     * Returns the address set for the locator
     * 
     * @return the address set for the locator
     */
    public static String getAddress() {
        return address;
    }
}