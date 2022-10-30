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
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/test/integration/src/java/test/com/bluejungle/destiny/test/integration/TestPolicyEnforcement.java#1 $
 */

public class TestPolicyEnforcement extends TestCase {

    private static String username = System.getProperty("user.name") + "@bluejungle.com";
    private static String policy2;
    private static String policy3; 
    private static String policy4; 							
    private static String policy5;
    private static String policy6;
    private static String policy7;
    private static String policy8;
    private static String policy9;
    private static String policy10;
    private static String policy11;
    private static String policy12; 
    private static int heartbeat = 15;
    public static final Map DESTINY_DATA_SOURCES = new HashMap();
    private static String buildRoot = System.getProperty("build.root.dir");
    private static String bundle = buildRoot + "\\bundle.bin";
    private static String testRoot = buildRoot + "\\integration-test";
    private static String policy1FilePath = testRoot + "\\edit-deny-user\\test.txt";
    private static String policy2FilePath = testRoot + "\\edit-allow-user\\test.txt";
    private static String policy3FilePath = testRoot + "\\deny-read.txt";
    private static String policy4FilePath = testRoot + "\\allow-group-read.txt";
    private static String policy5FilePath = testRoot + "\\delete-deny-user\\test.txt";
    private static String policy6FilePath = testRoot + "\\delete-allow-user\\test.txt";
    private static String policy7FilePath = testRoot + "\\move-deny-user\\test.txt";
    private static String policy8FilePath = testRoot + "\\move-allow-user\\test.txt";
    private static String policy9FilePath = testRoot + "\\rename-deny-user\\test.txt";
    private static String policy10FilePath = testRoot + "\\rename-allow-user\\test.txt";
    private static String policy11FilePath = testRoot + "\\attribute-deny-user\\test.txt";
    private static String policy12FilePath = testRoot + "\\attribute-allow-user\\test.txt";
    private static String moveToFilePath = testRoot + "\\moved.txt";
    private static String denyRenameFilePath = testRoot + "\\rename-deny-user\\test-renamed.txt";
    private static String allowRenameFilePath = testRoot + "\\rename-allow-user\\test-renamed.txt";
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
        policy2 = "POLICY policy2 FOR Resource.name=\"" + buildRoot + "\\\\integration-test\\\\" +
				  "edit-allow-user\\\\**\" " +
				  "ON EDIT BY User.name=\"" + username + "\" DO ALLOW BY DEFAULT DO DENY ON ALLOW DO LOG";
        policy3 = "POLICY policy3 FOR Resource.name=\"**\\\\deny-read.txt\" " +
        		  "ON OPEN BY * DO DENY";
        policy4 = "POLICY policy4 FOR Resource.name=\"**\\\\allow-group-read.txt\" " +
				  "ON OPEN BY USER.LDAPGROUP = \"BLUEJUNGLE.COM:Groups:destiny-eng@bluejungle.com\" " + 
				  "DO ALLOW BY DEFAULT DO DENY ON ALLOW DO LOG";   
        policy5 = "POLICY policy5 FOR Resource.name=\"" + buildRoot + "\\\\integration-test\\\\" +
        		  "delete-deny-user\\\\**\" " +
        		  "ON DELETE BY * DO DENY";
        policy6 = "POLICY policy6 FOR Resource.name=\"" + buildRoot + "\\\\integration-test\\\\" +
				  "delete-allow-user\\\\**\" " +
				  "ON DELETE BY USER.LDAPGROUP = \"BLUEJUNGLE.COM:Groups:destiny-eng@bluejungle.com\" DO ALLOW BY DEFAULT DO DENY";
        policy7 = "POLICY policy7 FOR Resource.name=\"" + buildRoot + "\\\\integration-test\\\\" +
			      "move-deny-user\\\\**\" " +
			      "ON MOVE BY * DO DENY";
        policy8 = "POLICY policy8 FOR Resource.name=\"" + buildRoot + "\\\\integration-test\\\\" +
				  "move-allow-user\\\\**\" " +
				  "ON MOVE BY * DO ALLOW BY DEFAULT DO DENY";
        policy9 = "POLICY policy9 FOR Resource.name=\"" + buildRoot + "\\\\integration-test\\\\" +
				  "rename-deny-user\\\\**\" " +
				  "ON RENAME BY * DO DENY";
        policy10 = "POLICY policy10 FOR Resource.name=\"" + buildRoot + "\\\\integration-test\\\\" +
		 		   "rename-allow-user\\\\**\" " +
		 		   "ON RENAME BY * DO ALLOW BY DEFAULT DO DENY";
        policy11 = "POLICY policy11 FOR Resource.name=\"" + buildRoot + "\\\\integration-test\\\\" +
		 		   "attribute-deny-user\\\\**\" " +
		 		   "ON CHANGE_ATTRIBUTES BY * DO DENY";
        policy12 = "POLICY policy12 FOR Resource.name=\"" + buildRoot + "\\\\integration-test\\\\" +
		 		   "attribute-allow-user\\\\**\" " +
		 		   "ON CHANGE_ATTRIBUTES BY * DO ALLOW BY DEFAULT DO DENY";
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    

    public void testDeployAllPolicies() throws PQLException, EntityManagementException, InterruptedException {
        Calendar cal = Calendar.getInstance();
        Date deployTime = cal.getTime();

        String policy2Name = pImporter.processCreate(policy2);        
        String policy3Name = pImporter.processCreate(policy3);
        String policy4Name = pImporter.processCreate(policy4);
        String policy5Name = pImporter.processCreate(policy5);
        String policy6Name = pImporter.processCreate(policy6);
        String policy7Name = pImporter.processCreate(policy7);
        String policy8Name = pImporter.processCreate(policy8);
        String policy9Name = pImporter.processCreate(policy9);
        String policy10Name = pImporter.processCreate(policy10);
        String policy11Name = pImporter.processCreate(policy11);
        String policy12Name = pImporter.processCreate(policy12);
       
        pImporter.processDeploy("policy1", deployTime);
        pImporter.processDeploy(policy2Name, deployTime);
        pImporter.processDeploy(policy3Name, deployTime);
        pImporter.processDeploy(policy4Name, deployTime);
        pImporter.processDeploy(policy5Name, deployTime);
        pImporter.processDeploy(policy6Name, deployTime);
        pImporter.processDeploy(policy7Name, deployTime);
        pImporter.processDeploy(policy8Name, deployTime);
        pImporter.processDeploy(policy9Name, deployTime);
        pImporter.processDeploy(policy10Name, deployTime);
        pImporter.processDeploy(policy11Name, deployTime);
        pImporter.processDeploy(policy12Name, deployTime);
        Thread.sleep(240000);
    }
 
    public void testEnforceDenyEDIT() throws FileNotFoundException, IOException {
        File testFile = new File(policy1FilePath);
        FileReader fileRead = new FileReader(testFile);
        fileRead.read();
        try {
            FileWriter fileWrite = new FileWriter(testFile);
            fileWrite.write("I wrote to this file in TestPolicyEnforcement.testEnforceDenyEDIT");
            //TODO: need to fail this test case if it comes here
            fail("should not be able to edit the file " + policy1FilePath);
        } catch (FileNotFoundException e){
            
        }
    }
    
    public void testEnforceAllowEDITWithLOG() throws FileNotFoundException, IOException {
        File testFile = new File(policy2FilePath);
        FileReader fileRead = new FileReader(testFile);
        FileWriter fileWrite = new FileWriter(testFile);
        BufferedWriter out = new BufferedWriter(fileWrite);
        out.write("I wrote to this file in TestPolicyEnforcement.testEnforceAllowEDITWithLOG");
        out.close();
    }

    public void testEnforceDenyOPEN() throws FileNotFoundException, IOException {
        try {
            File testFile = new File(policy3FilePath);
            FileReader fileRead = new FileReader(testFile);
            fileRead.read();
            //TO-DO: need to fail this test case if it comes here
            fail("should not be able to open to the file " + policy3FilePath);
        } catch (FileNotFoundException e){
            
        }
    }
 
    public void testEnforceAllowGroupOPENWithLOG() throws IOException {
        File testFile = new File(policy4FilePath);
        assertEquals("policy is not enforced, read the test file", true, testFile.canRead());
        FileReader fileRead = new FileReader(testFile);
        fileRead.read();
    }
    
    public void testEnforceDenyDELETE(){
        File testFile = new File(policy5FilePath);
        assertEquals("policy is not enforced, can delete the file", false, testFile.delete());
        assertTrue("the test file " + policy5FilePath + " no longer exits", testFile.exists());
    }

    public void testEnforceAllowGroupDELETE(){
        File testFile = new File(policy6FilePath);
        assertEquals("policy is not enforced, cannot delete the file", true, testFile.delete());
        assertTrue("the test file " + policy6FilePath + " should have been deleted", !testFile.exists());
    }

    public void testEnforceDenyMOVE(){
        File testFile = new File(policy7FilePath);
        assertEquals("policy is not enforced, can move the file", false, testFile.renameTo(new File(moveToFilePath)));
        assertTrue("the test file " + policy7FilePath + " no longer exits", testFile.exists());
    }
    
    public void testEnforceAllowMOVE(){
        File testFile = new File(policy8FilePath);
        assertEquals("policy is not enforced, cannot move the file", true, testFile.renameTo(new File(moveToFilePath)));
        assertTrue("the test file " + policy8FilePath + " should have been moved", !testFile.exists());
        assertTrue("the test file should have been moved to " + moveToFilePath, (new File(moveToFilePath)).exists());
    }

    public void testEnforceDenyRENAME(){
        File testFile = new File(policy9FilePath);
        assertEquals("policy is not enforced, can rename the file", false, testFile.renameTo(new File(denyRenameFilePath)));
        assertTrue("the test file " + policy9FilePath + " no longer exits", testFile.exists());
    }
    
    public void testEnforceAllowRENAME(){
        File testFile = new File(policy10FilePath);
        assertEquals("policy is not enforced, cannot rename the file", true, testFile.renameTo(new File(allowRenameFilePath)));
        assertTrue("the test file " + policy10FilePath + " should have been renamed", !testFile.exists());
        assertTrue("the test file should have been renamed to " + allowRenameFilePath, (new File(allowRenameFilePath)).exists());
    }
    
    public void testEnforceDenyCHANGE_ATTRIBUTE(){
        File testFile = new File(policy11FilePath);
        assertFalse("should not be able to set file attribute", testFile.setReadOnly());
    }
    
    public void testEnforceAllowCHANGE_ATTRIBUTE(){
        File testFile = new File(policy12FilePath);
        assertTrue("should be able to set file attribute", testFile.setReadOnly());
    }
}
