/*
 * Created on Oct 18, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.test.integration;

import java.rmi.RemoteException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Service;
import org.apache.axis.configuration.FileProvider;

import com.bluejungle.destiny.appframework.appsecurity.axis.SecureSessionVaultGateway;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoginInfo;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoginMgr;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginException;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.remote.RemoteLoginManager;
import com.bluejungle.destiny.appframework.appsecurity.test.MockSecureSessionVault;
import com.bluejungle.destiny.bindings.secure_session.v1.SecureSessionServiceIFBindingStub;
import com.bluejungle.destiny.interfaces.secure_session.v1.SecureSessionServiceIF;
import com.bluejungle.destiny.services.management.UserRoleServiceIF;
import com.bluejungle.destiny.services.management.UserRoleServiceLocator;
import com.bluejungle.destiny.services.management.types.DuplicateLoginNameException;
import com.bluejungle.destiny.services.management.types.UserDTO;
import com.bluejungle.destiny.services.policy.types.DMSUserData;
import com.bluejungle.destiny.test.integration.TestLoggingAndReporting.TestRemoteLoginMgr;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.destiny.services.policy.types.Role;
import com.bluejungle.pf.destiny.services.PolicyEditorClient;
import com.bluejungle.pf.destiny.services.IPolicyEditorClient;

import junit.framework.TestCase;

/**
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/test/integration/src/java/test/com/bluejungle/destiny/test/integration/TestSecurityAndRoles.java#1 $
 */

public class TestSecurityAndRoles extends TestCase {
    
    private static String USER_SERVICE_LOCATION = "http://localhost:8081/dms/services/UserRoleServiceIFPort";
    private static String ADMINISTRATOR_LOCATION = "http://localhost:8081/dms/services/SecureSessionService";
    private static String REPORTER_LOCATION = "http://localhost:8081/dac/services/SecureSessionService";
    private static String POLICYAUTHOR_LOCATION = "http://localhost:8081/dps/services/SecureSessionService";
    private static String ADMINISTRATOR_CLIENT_CONFIG_FILE = "client-config.wsdd";
    private static String REPORTER_CLIENT_CONFIG_FILE = "client-config.wsdd";
    private static UserRoleServiceIF userService;
    private static ILoginMgr administratorLoginManager;
    private static ILoginMgr reporterLoginManager;
    private static ILoginMgr policyauthorLoginManager;
    

    public TestSecurityAndRoles(String testName){
        super(testName);
    }
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        SecureSessionVaultGateway.setSecureSessionVault(new MockSecureSessionVault());
        
	    IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
	    UserRoleServiceLocator locator = new UserRoleServiceLocator();
	    locator.setUserRoleServiceIFPortEndpointAddress(USER_SERVICE_LOCATION);

	    userService = locator.getUserRoleServiceIFPort();

        //Sets the administrator login manager
        HashMapConfiguration adminComponentConfig = new HashMapConfiguration();
        adminComponentConfig.setProperty(TestAdministratorRemoteLoginMgr.SECURE_SESSION_SERVICE_ENDPOINT_PROP_NAME, ADMINISTRATOR_LOCATION);
        ComponentInfo adminComponentInfo = new ComponentInfo("adminComp", TestAdministratorRemoteLoginMgr.class.getName(), ILoginMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, adminComponentConfig);
        administratorLoginManager = (ILoginMgr) componentManager.getComponent(adminComponentInfo);
        //administratorLoginManager.login(new MockLoginInfo("Administrator", "123blue!"));
        
        //Sets the reporter login manager
        HashMapConfiguration reportComponentConfig = new HashMapConfiguration();
        reportComponentConfig.setProperty(TestReporterRemoteLoginMgr.SECURE_SESSION_SERVICE_ENDPOINT_PROP_NAME, REPORTER_LOCATION);
        ComponentInfo reportComponentInfo = new ComponentInfo("reportComp", TestReporterRemoteLoginMgr.class.getName(), ILoginMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, reportComponentConfig);
        reporterLoginManager = (ILoginMgr) componentManager.getComponent(reportComponentInfo);
        //reporterLoginManager.login(new MockLoginInfo("Administrator", "123blue!"));
        
        //Sets the policy author login manager
        //HashMapConfiguration policyComponentConfig = new HashMapConfiguration();
        //policyComponentConfig.setProperty(RemoteLoginManager.SECURE_SESSION_SERVICE_ENDPOINT_PROP_NAME, POLICYAUTHOR_LOCATION);
        //ComponentInfo policyComponentInfo = new ComponentInfo("policyComp", RemoteLoginManager.class.getName(), ILoginMgr.class.getName(), LifestyleType.SINGLETON_TYPE, policyComponentConfig);
        //policyauthorLoginManager = (ILoginMgr) componentManager.getComponent(policyComponentInfo);
        //policyauthorLoginManager.login(new MockLoginInfo("Administrator", "123blue!"));
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
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
	
	
    public static class TestAdministratorRemoteLoginMgr extends RemoteLoginManager {

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
                fail("Bad URL when authenticating user" + e.getMessage());
            }

            return serviceToReturn;
        }
    }
    
    
    public static class TestReporterRemoteLoginMgr extends RemoteLoginManager {

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
                fail("Bad URL when authenticating user" + e.getMessage());
            }

            return serviceToReturn;
        }
    }


    public void testCreateNewUser() throws RemoteException, LoginException {
   
        UserDTO newUser = new UserDTO("Joe", "Schomoe", "123blue!", true, null);
        newUser.setUid("generic");
        newUser.setName("Joe Schmoe");
        newUser.setType(SubjectType.USER.getName());
        newUser.setId(new BigInteger(IHasId.UNKNOWN_ID.toString()));       
        DMSUserData data = new DMSUserData(new Role[]{}, "generic", null);
        userService.createUser(newUser, data);
    
        try {
            administratorLoginManager.login(new MockLoginInfo("generic", "123blue!"));
            fail("user should not be allowed to log into the Administrator (DMS) ");
        } catch (LoginException e){
            
        }
        try {
            reporterLoginManager.login(new MockLoginInfo("generic", "123blue!"));
            fail("user should not be allowed to log into the Reporter (DAC) ");
        } catch (LoginException e){
            
        }
        
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
		HashMapConfiguration pfClientConfig = new HashMapConfiguration();
		pfClientConfig.setProperty(PolicyEditorClient.LOCATION_CONFIG_PARAM, "https://localhost:8443");
		pfClientConfig.setProperty(PolicyEditorClient.USERNAME_CONFIG_PARAM, "generic");
		pfClientConfig.setProperty(PolicyEditorClient.PASSWORD_CONFIG_PARAM, "123blue!");
		
		ComponentInfo compInfo = new ComponentInfo(PolicyEditorClient.COMP_INFO.getName(), 
				PolicyEditorClient.COMP_INFO.getClassName(), PolicyEditorClient.COMP_INFO.getInterfaceName(), 
				LifestyleType.TRANSIENT_TYPE, pfClientConfig);
		IPolicyEditorClient policyEditor = (IPolicyEditorClient) compMgr.getComponent(compInfo);
		
		try {
			policyEditor.login();
            fail("user should not be allowed to log into the Policy Author (DPS)");
        } catch (LoginException e){
            assertNotNull(e);
        }
		
        assertFalse("user should not be allowed to log into the Policy Author (DPS)", policyEditor.isLoggedIn());
    }
    
    /*
    public void testBusinessAnalyst() throws DuplicateLoginNameException, RemoteException, LoginException {
        
        
        UserDTO newUser = new UserDTO("Joe", "Business", "234blue!", true, null);
        newUser.setUid("banalyst");
        newUser.setName("Joe Business");
        newUser.setType(SubjectType.USER.getName());
        newUser.setId(new BigInteger(IHasId.UNKNOWN_ID.toString()));       
        DMSUserData data = new DMSUserData(new Role[]{Role.Business_Analyst}, "banalyst", Role.Business_Analyst);
        userService.createUser(newUser, data);
        
        try {
            administratorLoginManager.login(new MockLoginInfo("banalyst", "234blue!"));
            fail("user should not be allowed to log into the Administrator (DMS) ");
        } catch (LoginException e){
            
        }
        reporterLoginManager.login(new MockLoginInfo("banalyst", "234blue!"));
        IPolicyEditorClient policyEditor = PolicyEditorClient.create("https://localhost:8443", "banalyst", "234blue!");
        assertEquals("user should not be allowed to log into the Policy Author (DPS)", false, policyEditor.isLoggedIn());
     }
     */

    public void testPolicyAnalyst() throws DuplicateLoginNameException, RemoteException, LoginException {
  
        UserDTO newUser = new UserDTO("Joe", "Policy", "345blue!", true, null);
        newUser.setUid("panalyst");
        newUser.setName("Joe Policy");
        newUser.setType(SubjectType.USER.getName());
        newUser.setId(new BigInteger(IHasId.UNKNOWN_ID.toString()));       
        DMSUserData data = new DMSUserData(new Role[]{Role.Policy_Analyst}, "panalyst", Role.Policy_Analyst);
        userService.createUser(newUser, data);
      
        try {
            administratorLoginManager.login(new MockLoginInfo("panalyst", "345blue!"));
            fail("user should not be allowed to log into the Administrator (DMS) ");
        } catch (LoginException e){
            
        }
        reporterLoginManager.login(new MockLoginInfo("panalyst", "345blue!"));
        
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
		HashMapConfiguration pfClientConfig = new HashMapConfiguration();
		pfClientConfig.setProperty(PolicyEditorClient.LOCATION_CONFIG_PARAM, "https://localhost:8443");
		pfClientConfig.setProperty(PolicyEditorClient.USERNAME_CONFIG_PARAM, "panalyst");
		pfClientConfig.setProperty(PolicyEditorClient.PASSWORD_CONFIG_PARAM, "345blue!");
		
		ComponentInfo compInfo = new ComponentInfo(PolicyEditorClient.COMP_INFO.getName(), 
				PolicyEditorClient.COMP_INFO.getClassName(), PolicyEditorClient.COMP_INFO.getInterfaceName(), 
				LifestyleType.TRANSIENT_TYPE, pfClientConfig);
		IPolicyEditorClient policyEditor = (IPolicyEditorClient) compMgr.getComponent(compInfo);
		try {
			policyEditor.login();
			assertTrue("user should be allowed to log into the Policy Author (DPS)", policyEditor.isLoggedIn());
        } catch (LoginException e){
        	fail("user should be allowed to log into the Policy Author (DPS)");
        }
    }



    public void testPolicyAdministrator() throws DuplicateLoginNameException, RemoteException, LoginException {
    
        UserDTO newUser = new UserDTO("Joe", "Admin", "456blue!", true, null);
        newUser.setUid("poladmin");
        newUser.setName("Joe Admin");
        newUser.setType(SubjectType.USER.getName());
        newUser.setId(new BigInteger(IHasId.UNKNOWN_ID.toString()));       
        DMSUserData data = new DMSUserData(new Role[]{Role.Policy_Administrator}, "poladmin", Role.Policy_Administrator);
        userService.createUser(newUser, data);
        
        try {
            administratorLoginManager.login(new MockLoginInfo("poladmin", "456blue!"));
        } catch (LoginException e){
            
        }
        reporterLoginManager.login(new MockLoginInfo("poladmin", "456blue!"));
        
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
		HashMapConfiguration pfClientConfig = new HashMapConfiguration();
		pfClientConfig.setProperty(PolicyEditorClient.LOCATION_CONFIG_PARAM, "https://localhost:8443");
		pfClientConfig.setProperty(PolicyEditorClient.USERNAME_CONFIG_PARAM, "poladmin");
		pfClientConfig.setProperty(PolicyEditorClient.PASSWORD_CONFIG_PARAM, "456blue!");
		
		ComponentInfo compInfo = new ComponentInfo(PolicyEditorClient.COMP_INFO.getName(), 
				PolicyEditorClient.COMP_INFO.getClassName(), PolicyEditorClient.COMP_INFO.getInterfaceName(), 
				LifestyleType.TRANSIENT_TYPE, pfClientConfig);
		IPolicyEditorClient policyEditor = (IPolicyEditorClient) compMgr.getComponent(compInfo);
		try {
			policyEditor.login();
			assertTrue("user should be allowed to log into the Policy Author (DPS)", policyEditor.isLoggedIn());
        } catch (LoginException e){
        	fail("user should be allowed to log into the Policy Author (DPS)");
        }
    }

    /*
    public void testComponentAccess(){
        
    }
    */

    /*
    public void testAccessControlList(){
        
    }
    */
}
