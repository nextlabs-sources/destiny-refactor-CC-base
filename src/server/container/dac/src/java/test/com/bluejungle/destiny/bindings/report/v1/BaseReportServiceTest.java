/*
 * Created on Apr 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.bindings.report.v1;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Service;

import com.bluejungle.destiny.appframework.appsecurity.axis.AuthenticationContext;
import com.bluejungle.destiny.appframework.appsecurity.axis.SecureSessionVaultGateway;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoginInfo;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoginMgr;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.remote.RemoteLoginManager;
import com.bluejungle.destiny.appframework.appsecurity.test.MockSecureSessionVault;
import com.bluejungle.destiny.bindings.secure_session.v1.SecureSessionServiceIFBindingStub;
import com.bluejungle.destiny.container.dac.BaseDACComponentTestCase;
import com.bluejungle.destiny.interfaces.report.v1.ComponentLookupIF;
import com.bluejungle.destiny.interfaces.report.v1.ReportExecutionIF;
import com.bluejungle.destiny.interfaces.report.v1.ReportLibraryIF;
import com.bluejungle.destiny.interfaces.secure_session.v1.SecureSessionServiceIF;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * This is the base test class for the report service. This base class sets up
 * the security model for the web service clients and provides APIs to get the
 * various web service interfaces.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/bindings/report/v1/BaseReportServiceTest.java#2 $
 */

class BaseReportServiceTest extends BaseDACComponentTestCase {

    private static final String DESTINY_INSTALL_PATH_PROPERTY_NAME = "build.root.dir";
    private static final String DEFAULT_DESTINY_INSTALL_PATH = "c:\\builds\\destiny";
    private static final String INSTALL_DIR = System.getProperty(DESTINY_INSTALL_PATH_PROPERTY_NAME, DEFAULT_DESTINY_INSTALL_PATH);
    private static final String CLIENT_CONFIG_FILE = INSTALL_DIR + "\\server\\apps\\inquiryCenter\\WEB-INF\\client-config.wsdd";
    private static final String SERVICE_LOCATION = "http://localhost:8081/dac/services/SecureSessionService";

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public BaseReportServiceTest(String testName) {
        super(testName);
    }

    /**
     * Returns a new instance of a component lookup client.
     * 
     * @return a component lookup client
     */
    protected ComponentLookupIF getComponentLookup() {
        URL location = null;
        try {
            location = new URL("http://localhost:8081/dac/services/ComponentLookup");
        } catch (MalformedURLException e) {
            fail("Invalid URL for report component lookup location");
        }
        ComponentLookupIF lookup = null;
        try {
            lookup = new ComponentLookupIFBindingStub(location, getNewService());
        } catch (AxisFault e1) {
            fail("No axis fault should be thrown when creating report component lookup service : " + e1.getLocalizedMessage());
        }
        return lookup;
    }

    /**
     * Returns a new service client object
     * 
     * @return a new service client object
     */
    private Service getNewService() {
        return new org.apache.axis.client.Service();
    }

    /**
     * Returns a new instance of the report execution client
     * 
     * @return a report execution service client
     */
    protected ReportExecutionIF getReportExecution() {
        URL location = null;
        try {
            location = new URL("http://localhost:8081/dac/services/ReportExecution");
        } catch (MalformedURLException e) {
            fail("Invalid URL for report execution service location");
        }
        ReportExecutionIFBindingStub execution = null;
        try {
            execution = new ReportExecutionIFBindingStub(location, getNewService());
        } catch (AxisFault e1) {
            fail("No axis fault should be thrown when creating report execution service : " + e1.getLocalizedMessage());
        }
        return execution;
    }

    /**
     * Returns a new instance of the report library client
     * 
     * @return a report library service client
     */
    protected ReportLibraryIF getReportLibrary() {
        URL location = null;
        try {
            location = new URL("http://localhost:8081/dac/services/ReportLibrary");
        } catch (MalformedURLException e) {
            fail("Invalid URL for report execution library location");
        }
        ReportLibraryIF library = null;
        try {
            library = new ReportLibraryIFBindingStub(location, getNewService());
        } catch (AxisFault e1) {
            fail("No axis fault should be thrown when creating report library service : " + e1.getLocalizedMessage());
        }
        return library;
    }

    
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        SecureSessionVaultGateway.setSecureSessionVault(new MockSecureSessionVault());

        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();

        //Sets the remote login manager to test
        HashMapConfiguration componentConfig = new HashMapConfiguration();
        componentConfig.setProperty(TestRemoteLoginMgr.SECURE_SESSION_SERVICE_ENDPOINT_PROP_NAME, SERVICE_LOCATION);
        ComponentInfo componentInfo = new ComponentInfo(ILoginMgr.COMP_NAME, TestRemoteLoginMgr.class.getName(), ILoginMgr.class.getName(), LifestyleType.SINGLETON_TYPE, componentConfig);
        ILoginMgr loginMgr = (ILoginMgr) compMgr.getComponent(componentInfo);
        //Simulates a user login
        loginMgr.login(new MockLoginInfo("Administrator", "123blue!"));
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        AuthenticationContext.clearCurrentContext();
        super.tearDown();
    }

    protected class MockLoginInfo implements ILoginInfo {

        private String userName;
        private String password;

        public MockLoginInfo(String name, String pass) {
            this.userName = name;
            this.password = pass;
        }

        /**
         * @see com.bluejungle.destiny.webui.framework.loginmgr.ILoginInfo#getApplicationName()
         */
        public String getApplicationName() {
            return null;
        }

        /**
         * @see com.bluejungle.destiny.webui.framework.loginmgr.ILoginInfo#getUserName()
         */
        public String getUserName() {
            return this.userName;
        }

        /**
         * @see com.bluejungle.destiny.webui.framework.loginmgr.ILoginInfo#getPassword()
         */
        public String getPassword() {
            return this.password;
        }
    }

    public static class TestRemoteLoginMgr extends RemoteLoginManager {

        /**
         * @see com.bluejungle.destiny.appframework.appsecurity.loginmgr.remote.RemoteLoginManager#getSecureSessionService(java.lang.String)
         */
        protected SecureSessionServiceIF getSecureSessionService(String serviceLocation) throws ServiceException, RemoteException {
            SecureSessionServiceIF serviceToReturn = null;

            try {
                URL location = new URL(serviceLocation);
                Service clientService = new org.apache.axis.client.Service();
                serviceToReturn = new SecureSessionServiceIFBindingStub(location, clientService);
            } catch (MalformedURLException e) {
                // Should never happen
                BaseReportServiceTest.fail("Bad URL when authenticating user" + e.getMessage());
            }

            return serviceToReturn;
        }
    }
}
