/*
 * Created on Oct 18, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.test.integration;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.types.UnsignedShort;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;

import com.bluejungle.destiny.framework.types.TimeIntervalDTO;
import com.bluejungle.destiny.framework.types.TimeUnits;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.services.management.CommProfileDTOQuery;
import com.bluejungle.destiny.services.management.ProfileServiceIF;
import com.bluejungle.destiny.services.management.ProfileServiceLocator;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.destiny.services.management.types.CommProfileDTOList;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryField;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryTerm;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryTermSet;
import com.bluejungle.domain.types.AgentTypeDTO;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/test/integration/src/java/test/com/bluejungle/destiny/test/integration/TestUpdateAgent.java#1 $
 */

public class TestUpdateAgent extends TestCase {
    private static ProfileServiceIF dmsProfileService;
    private int heartBeat = 10;
    private int log = 5;
    private int replaceHeartBeat = 8;
    private int replaceLog = 3;
    private static String buildRoot = System.getProperty("build.root.dir");
    private static String agentRoot = buildRoot + "\\agent_install";
    private static String commprofile = agentRoot + "\\config\\commprofile.xml";
    
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
    
    /*
     * Constructor
     */
    public TestUpdateAgent(String testName) {
        super(testName);
    }
    
	/**
	 * Creating a Test Suite
	 */
	public static Test suite() throws ServiceException, RemoteException, InterruptedException {
        ProfileServiceLocator profileSvcLocator = new ProfileServiceLocator();
        profileSvcLocator.setProfileServiceIFPortEndpointAddress("http://localhost:8081/dms/services/ProfileServiceIFPort");
        dmsProfileService = profileSvcLocator.getProfileServiceIFPort();
        assertNotNull("DMS Profile Service not created properly", dmsProfileService);
	    
        TestSuite ts = new TestSuite();        
        ts.addTest(new TestUpdateAgent("testChangeProfile"));
        return (ts);
	}

    public void testChangeProfile() throws ServiceNotReadyFault, RemoteException, Exception {
        
        //First update the profile on the server
        CommProfileDTOQueryTerm[] queryTerms = { 
                new CommProfileDTOQueryTerm(CommProfileDTOQueryField.name, "Desktop Enforcer Default Profile")
        };
        CommProfileDTOQueryTermSet singleTermQueryTermSet = new CommProfileDTOQueryTermSet();
        singleTermQueryTermSet.setCommProfileDTOQueryTerm(queryTerms);
        CommProfileDTOQuery singleTermDTOQuery = new CommProfileDTOQuery();
        singleTermDTOQuery.setCommProfileDTOQueryTermSet(singleTermQueryTermSet);
        singleTermDTOQuery.setFetchSize(1);
        CommProfileDTOList profilesRetrieved = dmsProfileService.getCommProfiles(singleTermDTOQuery);
        CommProfileDTO comDTO = profilesRetrieved.getCommProfileDTO(0);
        assertEquals("retrieved incorrect comm profile", AgentTypeDTO.DESKTOP, comDTO.getAgentType());
        UnsignedShort heartbeatTime = new org.apache.axis.types.UnsignedShort(heartBeat);
        UnsignedShort logTime = new org.apache.axis.types.UnsignedShort(log);
        comDTO.setHeartBeatFrequency(new TimeIntervalDTO(heartbeatTime, TimeUnits.fromValue("seconds"))); 
        comDTO.setLogFrequency(new TimeIntervalDTO(logTime, TimeUnits.fromValue("seconds")));
        dmsProfileService.updateCommProfile(comDTO);
        Thread.sleep(20000);//need to change this to 15000 later
        
        //Then read the profile on the agent
        FileReader reader = new FileReader(commprofile);
        assertNotNull("commprofile.xml not found", reader);
        Unmarshaller communicationProfileUnmarshaller = new Unmarshaller(CommProfileDTO.class);
        CommProfileDTO communicationProfile = (CommProfileDTO) communicationProfileUnmarshaller.unmarshal(reader);
        TimeIntervalDTO heartbeatFreq = communicationProfile.getHeartBeatFrequency();
        TimeIntervalDTO logFreq = communicationProfile.getLogFrequency();
        assertNotNull("agent heartbeat frequency value should not be null", heartbeatFreq);
        assertNotNull("agent log frequency value should not be null", logFreq);
        assertEquals("agent heartbeat frequency does not match", Integer.toString(heartBeat), heartbeatFreq.getTime().toString());
        assertEquals("agent log frequency does not match", Integer.toString(log), logFreq.getTime().toString());
    }
    
    /*
    public void testCreateNewProfile() throws ServiceNotReadyFault, RemoteException, Exception {
        //First update the profile on the server
        CommProfileDTOQueryTerm[] queryTerms = { 
                new CommProfileDTOQueryTerm(CommProfileDTOQueryField.name, "Desktop_Replacement_Profile")
        };
        CommProfileDTOQueryTermSet singleTermQueryTermSet = new CommProfileDTOQueryTermSet();
        singleTermQueryTermSet.setCommProfileDTOQueryTerm(queryTerms);
        CommProfileDTOQuery singleTermDTOQuery = new CommProfileDTOQuery();
        singleTermDTOQuery.setCommProfileDTOQueryTermSet(singleTermQueryTermSet);
        singleTermDTOQuery.setFetchSize(1);
        CommProfileDTOList profilesRetrieved = dmsProfileService.getCommProfiles(singleTermDTOQuery);
        CommProfileDTO comDTO = profilesRetrieved.getCommProfileDTO(0);
        assertEquals("retrieved incorrect comm profile", AgentTypeDTO.DESKTOP, comDTO.getAgentType());
        UnsignedShort heartbeatTime = new org.apache.axis.types.UnsignedShort(heartBeat);
        UnsignedShort logTime = new org.apache.axis.types.UnsignedShort(log);
        comDTO.setHeartBeatFrequency(new TimeIntervalDTO(heartbeatTime, TimeUnits.fromValue("seconds"))); 
        comDTO.setLogFrequency(new TimeIntervalDTO(logTime, TimeUnits.fromValue("seconds")));
        dmsProfileService.updateCommProfile(comDTO);
        Thread.sleep(15000);//need to change this to 15000 later
        
        //Then read the profile on the agent
        FileReader reader = new FileReader(commprofile);
        assertNotNull("commprofile.xml not found", reader);
        Unmarshaller communicationProfileUnmarshaller = new Unmarshaller(CommProfileDTO.class);
        CommProfileDTO communicationProfile = (CommProfileDTO) communicationProfileUnmarshaller.unmarshal(reader);
        TimeIntervalDTO heartbeatFreq = communicationProfile.getHeartBeatFrequency();
        TimeIntervalDTO logFreq = communicationProfile.getLogFrequency();
        assertNotNull("agent heartbeat frequency value should not be null", heartbeatFreq);
        assertNotNull("agent log frequency value should not be null", logFreq);
        assertEquals("agent heartbeat frequency does not match", Integer.toString(heartBeat), heartbeatFreq.getTime().toString());
        assertEquals("agent log frequency does not match", Integer.toString(log), logFreq.getTime().toString());
    }
    */
}
