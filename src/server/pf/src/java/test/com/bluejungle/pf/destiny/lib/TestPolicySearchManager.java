package com.bluejungle.pf.destiny.lib;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.pf.destiny.lifecycle.PFTestWithDataSource;

public class TestPolicySearchManager extends PFTestWithDataSource {

    String hostName;
    String userName;
    String policyFrgmnt;
    PolicySearchManager srchManager;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        try {
            // try to find a properties file for data init
            Properties dataProps = new Properties();
            InputStream in = getClass().getClassLoader().getResourceAsStream(
                    "policysearchdata.properties");
            if (in != null) {
                dataProps.load(in);
                userName = dataProps.getProperty("username");
                hostName = dataProps.getProperty("hostname");
                policyFrgmnt = dataProps.getProperty("policyName");
                in.close();
            } else {
                throw new IOException("Could not load properties file");
            }
        } catch (IOException ex) {
            // initialize data without the properties file
            hostName = "ANGEL.bluejungle.com";
            userName  = "abraham.lincoln@test.bluejungle.com";
            policyFrgmnt = "";
        }
        IComponentManager manager = 
            ComponentManagerFactory.getComponentManager();
        srchManager = (PolicySearchManager)manager.getComponent(
                PolicySearchManager.COMP_INFO);
    }
  /*  
    {
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        ComponentInfo locatorInfo = new ComponentInfo(
                IDestinySharedContextLocator.COMP_NAME, 
                MockSharedContextLocator.class.getName(), 
                IDestinySharedContextLocator.class.getName(),
                LifestyleType.SINGLETON_TYPE);
               
        manager.registerComponent(locatorInfo, true); 
        hds = (IHibernateRepository) manager.getComponent(
                DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName() );
        lm = (LifecycleManager) manager.getComponent(LifecycleManager.COMP_INFO);

    }
*/
    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestPolicySearchManager.class);
    }
    
    public void testFindApplicablePoliciesSuccess() {
        try {
            //PolicySearchManager srchManager = new PolicySearchManager();
           List<Long> appPolicies = 
               srchManager.findApplicablePoliciesFor(userName, hostName, policyFrgmnt);
           assert appPolicies != null;
           assert appPolicies.size() == 1;
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }
}
