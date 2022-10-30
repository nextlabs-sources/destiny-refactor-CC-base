/*
 * Created on Mar 18, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.policyutil.deploy;

/**
 * @author Robert Lin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/policyutil/src/java/test/com/bluejungle/destiny/policyutil/deploy/TestPQLImporter.java#1 $
 */
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.tools.MockSharedContextLocator;

public class TestPQLImporter extends TestCase {

    private static ComponentInfo info; 
    private static PQLImporter pImporter;
    private static ComponentInfo locatorInfo;
    private static IDestinySharedContextLocator locator; 
    private static String policy1 = "POLICY policy1 FOR * " +
									"ON * BY * DO DENY";
    private static String policyName = "policy1";
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testPQLImporter(){
        info = new ComponentInfo("PQLImporter", 
                                 PQLImporter.class.getName(), 
                                 null, 
                                 LifestyleType.SINGLETON_TYPE);
        pImporter = (PQLImporter) ComponentManagerFactory.getComponentManager().getComponent(info);
        locatorInfo = new ComponentInfo(IDestinySharedContextLocator.COMP_NAME, 
                						MockSharedContextLocator.class.getName(), 
                						IDestinySharedContextLocator.class.getName(), 
                						LifestyleType.SINGLETON_TYPE);
        locator = (IDestinySharedContextLocator) ComponentManagerFactory.getComponentManager().getComponent(locatorInfo);
    }
    
    public void testCreate() throws PQLException, EntityManagementException {
        String createdPolicyName = pImporter.processCreate(policy1);
        assertEquals("policy created does not have the correct name", policyName, createdPolicyName);
    }
    
    public void testDeploy() throws PQLException, EntityManagementException {
        Calendar cal = Calendar.getInstance();
        Date deployTime = cal.getTime();
        deployTime.setSeconds(deployTime.getSeconds()+15);
        pImporter.processDeploy(policyName, deployTime);
    }
    
    public void testUndeploy() throws PQLException, EntityManagementException {
        Calendar cal = Calendar.getInstance();
        Date undeployTime = cal.getTime();
        undeployTime.setSeconds(undeployTime.getSeconds()+60);
        pImporter.processUndeploy(policyName, undeployTime);
    }

}