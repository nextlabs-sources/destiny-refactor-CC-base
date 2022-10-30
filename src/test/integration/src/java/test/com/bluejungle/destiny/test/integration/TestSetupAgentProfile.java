/*
 * Created on Nov 14, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.test.integration;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

import org.apache.axis.types.UnsignedShort;

import com.bluejungle.destiny.framework.types.TimeIntervalDTO;
import com.bluejungle.destiny.framework.types.TimeUnits;
import com.bluejungle.destiny.services.management.AgentServiceIF;
import com.bluejungle.destiny.services.management.AgentServiceLocator;
import com.bluejungle.destiny.services.management.CommProfileDTOQuery;
import com.bluejungle.destiny.services.management.ProfileServiceIF;
import com.bluejungle.destiny.services.management.ProfileServiceLocator;
import com.bluejungle.destiny.services.management.types.AgentStatistics;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.destiny.services.management.types.CommProfileDTOList;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryField;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryTerm;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryTermSet;
import com.bluejungle.domain.types.AgentTypeDTO;

/**
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/test/integration/src/java/test/com/bluejungle/destiny/test/integration/TestSetupAgentProfile.java#1 $
 */

public class TestSetupAgentProfile extends TestCase {

    private static String buildRoot = System.getProperty("build.root.dir");
    private static String agentRoot = buildRoot + "\\agent_install";
    private static String regInfo = agentRoot + "\\config\\registration.info";
    private static String cert1 = agentRoot + "\\config\\security\\temp_agent-keystore.jks";
    private static String cert2 = agentRoot + "\\config\\security\\agent-keystore.jks";
    private static String cert3 = agentRoot + "\\config\\security\\agent-truststore.jks";
    private static String bundle = agentRoot + "\\bundle.bin";
    private static String commprofile = agentRoot + "\\config\\commprofile.xml";
    private static String castorMapping = agentRoot + "\\config\\mapping.xml";
    private static String agentFetchSize = "0";
    private static int heartBeat = 15;
    private static AgentServiceIF dmsAgentService;
    private static ProfileServiceIF dmsProfileService;
    private static AgentStatistics agentStat;
    
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

    /**
     * Constructor for TestSetupAgentProfile.
     * @param arg0
     */
    public TestSetupAgentProfile(String arg0) {
        super(arg0);
    }

    public void testSetupAgentProfile() throws ServiceException, RemoteException, InterruptedException {
        AgentServiceLocator agentSvcLocator = new AgentServiceLocator();
        agentSvcLocator.setAgentServiceIFPortEndpointAddress("http://localhost:8081/dms/services/AgentServiceIFPort");
        dmsAgentService = agentSvcLocator.getAgentServiceIFPort();
        assertNotNull("DMS Agent Service not created properly", dmsAgentService);

        ProfileServiceLocator profileSvcLocator = new ProfileServiceLocator();
        profileSvcLocator.setProfileServiceIFPortEndpointAddress("http://localhost:8081/dms/services/ProfileServiceIFPort");
        dmsProfileService = profileSvcLocator.getProfileServiceIFPort();
        assertNotNull("DMS Profile Service not created properly", dmsProfileService);

        CommProfileDTOQueryTerm[] queryTerms = { new CommProfileDTOQueryTerm(CommProfileDTOQueryField.name, "Desktop Enforcer Default Profile") };
        CommProfileDTOQueryTermSet singleTermQueryTermSet = new CommProfileDTOQueryTermSet();
        singleTermQueryTermSet.setCommProfileDTOQueryTerm(queryTerms);
        CommProfileDTOQuery singleTermDTOQuery = new CommProfileDTOQuery();
        singleTermDTOQuery.setCommProfileDTOQueryTermSet(singleTermQueryTermSet);
        singleTermDTOQuery.setFetchSize(1);
        CommProfileDTOList profilesRetrieved = dmsProfileService.getCommProfiles(singleTermDTOQuery);
        CommProfileDTO comDTO = profilesRetrieved.getCommProfileDTO(0);
        //URI dabsLocation = new
        // org.apache.axis.types.URI("http://localhost:8081/dabs");
        assertEquals("retrieved incorrect comm profile", AgentTypeDTO.DESKTOP, comDTO.getAgentType());
        //comDTO.setDABSLocation(dabsLocation);

        //CommProfileDTO[] comDTOList = profilesRetrieved.getCommProfileDTO();
        UnsignedShort time = new org.apache.axis.types.UnsignedShort(heartBeat);
        comDTO.setHeartBeatFrequency(new TimeIntervalDTO(time, TimeUnits.fromValue("seconds")));
        dmsProfileService.updateCommProfile(comDTO);
        Thread.sleep(80000);
    }
}
