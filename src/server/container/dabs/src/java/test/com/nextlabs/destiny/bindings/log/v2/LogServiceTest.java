/*
 * Created on Feb 2, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.bindings.log.v2;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.HibernateException;

import com.bluejungle.destiny.container.dabs.components.log.hibernateimpl.LogWriterTestHelper;
import com.bluejungle.destiny.container.dabs.components.test.BaseDabsComponentTest;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.log.PolicyActivityLogEntryTestData;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.domain.log.PolicyAssistantLogEntryTestData;
import com.bluejungle.domain.log.TrackingLogEntry;
import com.bluejungle.domain.log.TrackingLogEntryTestData;

import com.nextlabs.destiny.interfaces.log.v2.LogServiceIF;
import com.nextlabs.destiny.services.log.v2.LogServiceV2Locator;
import com.nextlabs.destiny.types.log.v2.LogStatus;


/**
 * This is the dabs log service v2 test cases
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/test/com/nextlabs/destiny/bindings/log/v2/LogServiceTest.java#1 $
 */

public class LogServiceTest extends BaseDabsComponentTest {

    private LogServiceIF logService;
    private LogWriterTestHelper helper;
    private BaseLogEntrySorter baseLogEntrySorter = 
    							new BaseLogEntrySorter();
    
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
        helper.deleteAllLogs();
    }




    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public LogServiceTest(String testName) {
        super(testName);
        try {
            LogServiceV2Locator locator = new LogServiceV2Locator();
            locator.setLogServiceIFPortV2EndpointAddress("http://localhost:8081/dabs/services/LogServiceIFPort.v2");
            this.logService = locator.getLogServiceIFPortV2();
        } catch (Exception e) {
            fail(e.getMessage());
        }
        helper = LogWriterTestHelper.getInstance();
        assertNotNull("Log Service properly created", this.logService);        
    }

    /**
     * @throws RemoteException
     * @throws UnauthorizedCallerFault
     * @throws ServiceNotReadyFault
     * @throws HibernateException
     * @throws InterruptedException 
     * 
     */
    public void testPolicyActivityLogV2DataType() throws ServiceNotReadyFault, UnauthorizedCallerFault,
                                                         RemoteException, IOException, 
                                                         HibernateException, SQLException, InterruptedException {
        
        PolicyActivityLogEntry[] entries = PolicyActivityLogEntryTestData.generateRandom(100);
        
        Arrays.sort(entries, baseLogEntrySorter);
        
        String data = DABSLogServiceWSConverter.encodeLogEntries(Arrays.asList(entries));
        LogStatus status = logService.logPolicyActivity(data);
        assertNotNull("log status should not be null", status);
        assertEquals("log status should be success", LogStatus.Success, status);
        Thread.sleep(15000);
        
        List saveEntries = helper.retrievePolicyActivityLogs();
        int i = 0;
        for (Iterator iter = saveEntries.iterator(); iter.hasNext(); i++) {
            PolicyActivityLogEntry entry = (PolicyActivityLogEntry) iter.next();
            assertEquals("Retrieved policy activity log entry should equal the original one.", entries[i], entry);
        }
        assertEquals("Number of saved entries should be the same as the original.", entries.length, i);
        
        // attempt to send the same thing, should not fail, but handle
        // gracefully
        status = logService.logPolicyActivity(data);
        assertNotNull("log status for repeated data should not be null", status);
        assertEquals("log status for repeated data should be success", LogStatus.Success, status);
        
        saveEntries = helper.retrievePolicyActivityLogs();
        i = 0;
        for (Iterator iter = saveEntries.iterator(); iter.hasNext(); i++) {
            PolicyActivityLogEntry entry = (PolicyActivityLogEntry) iter.next();
            assertEquals("Retrieved policy activity log entry should equal the original one.", entries[i], entry);
        }
        assertEquals("Number of saved entries should be the same as the original.", entries.length, i);

    }

    /**
     * @throws RemoteException
     * @throws UnauthorizedCallerFault
     * @throws ServiceNotReadyFault
     * @throws HibernateException
     * @throws InterruptedException 
     * 
     */
    public void testPolicyAssistantLogV2DataType() 
    	throws ServiceNotReadyFault, UnauthorizedCallerFault,
           	   RemoteException, IOException, 
           	   HibernateException, SQLException, InterruptedException {
    	PolicyActivityLogEntry[] policyLogEntries = 
    		PolicyActivityLogEntryTestData.generateRandom(5);
    	
    	String data = DABSLogServiceWSConverter.encodeLogEntries(
    			Arrays.asList(policyLogEntries));
        LogStatus status = logService.logPolicyActivity(data);
        Thread.sleep(15000);
        
    	for (PolicyActivityLogEntry thisEntry : policyLogEntries) {
    		PolicyAssistantLogEntry[] entries = 
    			PolicyAssistantLogEntryTestData.generateRandom(
    					String.valueOf(thisEntry.getUid()), 5);
    		
    		Arrays.sort(entries, baseLogEntrySorter);
    		
    		String policyLogId = String.valueOf(thisEntry.getUid());
    		data = DABSLogServiceWSConverter.encodeLogEntries(
    											Arrays.asList(entries));
    		status = logService.logPolicyAssistant(data);
            assertNotNull("log status should not be null", status);
            assertEquals("log status should be success", LogStatus.Success, status);
            Thread.sleep(10000);
            
            List<PolicyAssistantLogEntry> saveEntries = 
            	helper.retrievePolicyAssistantLogs(policyLogId);
            int i = 0;
            for (Iterator<PolicyAssistantLogEntry> iter = saveEntries.iterator();
            	 iter.hasNext(); i++) {
                PolicyAssistantLogEntry entry = (PolicyAssistantLogEntry) iter.next();
                assertEquals("Retrieved policy assistant log entry should equal the original one.", entries[i], entry);
            }
            assertEquals("Number of saved entries should be the same as the original.", entries.length, i);
            
            // attempt to send the same thing, should not fail, but handle
            // gracefully
            status = logService.logPolicyAssistant(data);
            assertNotNull("log status for repeated data should not be null", status);
            assertEquals("log status for repeated data should be success", LogStatus.Success, status);
            
            saveEntries = helper.retrievePolicyAssistantLogs(policyLogId);
            i = 0;
            for (Iterator<PolicyAssistantLogEntry> iter = saveEntries.iterator(); 
            	 iter.hasNext(); i++) {
                PolicyAssistantLogEntry entry = (PolicyAssistantLogEntry) iter.next();
                assertEquals("Retrieved policy assistant log entry should equal the original one.", entries[i], entry);
            }
            assertEquals("Number of saved entries should be the same as the original.", entries.length, i);
    	}
    }

    /**
     * @throws RemoteException
     * @throws UnauthorizedCallerFault
     * @throws ServiceNotReadyFault
     * @throws HibernateException
     * @throws InterruptedException 
     * 
     */
    public void testTrackingActivityLogV2DataType() throws ServiceNotReadyFault, UnauthorizedCallerFault, 
                                                           RemoteException, IOException, 
                                                           HibernateException, SQLException, InterruptedException {
        TrackingLogEntry[] entries = TrackingLogEntryTestData.generateRandom(100);
       
        Arrays.sort(entries, baseLogEntrySorter);
       
        String data = DABSLogServiceWSConverter.encodeLogEntries(Arrays.asList(entries));
        LogStatus status = logService.logTracking(data);
        assertNotNull("log status should not be null", status);
        assertEquals("log status should be success", LogStatus.Success, status);
        Thread.sleep(15000);
        
        List saveEntries = helper.retrieveTrackingActivityLogs();
        int i = 0;
        for (Iterator iter = saveEntries.iterator(); iter.hasNext(); i++) {
            TrackingLogEntry entry = (TrackingLogEntry) iter.next();
            assertEquals("Retrieved tracking log entry should equal the original one.", entries[i], entry);
        }
        assertEquals("Number of saved entries should be the same as the original.", entries.length, i);            
        
        // attempt to send the same thing, should not fail, but handle
        // gracefully
        status = logService.logTracking(data);
        assertNotNull("log status for repeated data should not be null", status);
        assertEquals("log status for repeated data should be success", LogStatus.Success, status);
        
        saveEntries = helper.retrieveTrackingActivityLogs();
        i = 0;
        for (Iterator iter = saveEntries.iterator(); iter.hasNext(); i++) {
            TrackingLogEntry entry = (TrackingLogEntry) iter.next();
            assertEquals("Retrieved tracking log entry should equal the original one.", entries[i], entry);
        }
        assertEquals("Number of saved entries should be the same as the original.", entries.length, i);            
    }
    
    public static class BaseLogEntrySorter implements Comparator<BaseLogEntry> {
    	
    	public int compare(BaseLogEntry first, BaseLogEntry second) {
    		int retVal = 1;
    		if(first.getUid() < second.getUid()) {
    			retVal = -1;
    		} else if (first.getUid() == second.getUid()) {
    			retVal = 0;
    		}
    		return retVal;
    	}
    	
    }
}
