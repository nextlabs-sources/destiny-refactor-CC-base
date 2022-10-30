/*
 * Created on Oct 18, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.test.integration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.bluejungle.destiny.policyutil.deploy.PQLImporter;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.tools.MockSharedContextLocator;

/**
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/test/integration/src/java/test/com/bluejungle/destiny/test/integration/TestPolicyDeployment.java#1 $
 */

public class TestPolicyDeployment extends TestCase {
    private static int heartbeat = 15;
    public static final Map DESTINY_DATA_SOURCES = new HashMap();
    private static String buildRoot = System.getProperty("build.root.dir");
    private static String agentRoot = buildRoot + "\\agent_install";
    private static String bundle = agentRoot + "\\bundle.bin";
    private static String testFilePath = buildRoot + "\\integration-test\\edit-deny-user\\test.txt";
    private static String policy1; 
    private static String policyName = "policy1";
    private static ComponentInfo info = new ComponentInfo("PQLImporter", 
                                                          PQLImporter.class.getName(), 
                                                          null, 
                                                          LifestyleType.SINGLETON_TYPE);
    private static PQLImporter pImporter = (PQLImporter) ComponentManagerFactory.getComponentManager().getComponent(info);
    private static ComponentInfo locatorInfo = new ComponentInfo(IDestinySharedContextLocator.COMP_NAME, 
            								 					 MockSharedContextLocator.class.getName(), 
            													 IDestinySharedContextLocator.class.getName(), 
            													 LifestyleType.SINGLETON_TYPE);
    private static IDestinySharedContextLocator locator = 
        			(IDestinySharedContextLocator) ComponentManagerFactory.getComponentManager().getComponent(locatorInfo);

    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        int idx = buildRoot.lastIndexOf("\\");
        if ( idx != -1 ) {
            StringBuffer ret = new StringBuffer(buildRoot);
            ret.replace(idx, idx+"\\".length(), "\\\\");
            while( (idx=buildRoot.lastIndexOf("\\", idx-1)) != -1 ) {
                ret.replace(idx, idx+"\\".length(), "\\\\");
            }
            buildRoot = ret.toString();
        }
        policy1 = "POLICY policy1 FOR Resource.name=\"" + buildRoot + "\\\\integration-test\\\\edit-deny-user\\\\**\" " +
		    	  "ON EDIT BY * DO DENY";   
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testDeployPolicy() throws PQLException, EntityManagementException, InterruptedException, FileNotFoundException, IOException {
        
        File bundleFile = new File(bundle);
        
        policyName = pImporter.processCreate(policy1);
        
        Calendar cal = Calendar.getInstance();
        Date deployTime = cal.getTime();
        deployTime.setSeconds(deployTime.getSeconds()+15);
        pImporter.processDeploy(policyName, deployTime);
        Thread.sleep(90000);

        File testFile = new File(testFilePath);
        FileReader fileRead = new FileReader(testFile);
        fileRead.read();
        try {
            FileWriter fileWrite = new FileWriter(testFile);
            fail("should not be able to write to the file " + testFilePath);
        } catch (FileNotFoundException e){
            
        }
    }
    
    public void testDeactivatePolicy() throws PQLException, EntityManagementException, InterruptedException, FileNotFoundException, IOException {
       
        File bundleFile = new File(bundle);
        
        Calendar cal = Calendar.getInstance();
        Date undeployTime = cal.getTime();
        undeployTime.setSeconds(undeployTime.getSeconds());
        pImporter.processUndeploy(policyName, undeployTime);
        Thread.sleep(120000);

        File testFile = new File(testFilePath);
        FileReader fileRead = new FileReader(testFile);
        FileWriter fileWrite = new FileWriter(testFile);
        BufferedWriter out = new BufferedWriter(fileWrite);
        out.write("I wrote to this file in TestPolicyDeployment");
        out.close();
    }
    
    /*
    public void testPushMechanism(){
        
    }
    */
    
    /*
    public void testTargetedDeployment(){
        
    }
    */
}
