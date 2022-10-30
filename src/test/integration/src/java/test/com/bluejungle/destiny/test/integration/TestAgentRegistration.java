/*
 * Created on Oct 18, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.test.integration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Calendar;

import javax.xml.rpc.ServiceException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.MarshalException;

import org.apache.axis.types.UnsignedShort;

import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentSortTermImpl;
import com.bluejungle.destiny.framework.types.RelationalOpDTO;
import com.bluejungle.destiny.framework.types.TimeIntervalDTO;
import com.bluejungle.destiny.framework.types.TimeUnits;
import com.bluejungle.destiny.services.management.AgentServiceIF;
import com.bluejungle.destiny.services.management.AgentServiceLocator;
import com.bluejungle.destiny.services.management.CommProfileDTOQuery;
import com.bluejungle.destiny.services.management.ProfileServiceIF;
import com.bluejungle.destiny.services.management.ProfileServiceLocator;
import com.bluejungle.destiny.services.management.types.AgentCount;
import com.bluejungle.destiny.services.management.types.AgentDTO;
import com.bluejungle.destiny.services.management.types.AgentDTOList;
import com.bluejungle.destiny.services.management.types.AgentDTOQueryField;
import com.bluejungle.destiny.services.management.types.AgentDTOQuerySpec;
import com.bluejungle.destiny.services.management.types.AgentDTOQueryTerm;
import com.bluejungle.destiny.services.management.types.AgentDTOQueryTermList;
import com.bluejungle.destiny.services.management.types.AgentDTOSortTerm;
import com.bluejungle.destiny.services.management.types.AgentDTOSortTermField;
import com.bluejungle.destiny.services.management.types.AgentDTOSortTermList;
import com.bluejungle.destiny.services.management.types.AgentQueryResultsDTO;
import com.bluejungle.destiny.services.management.types.AgentStatistics;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.destiny.services.management.types.CommProfileDTOList;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryField;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryTerm;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryTermSet;
import com.bluejungle.destiny.services.management.types.ConcreteAgentDTOQueryTerm;
import com.bluejungle.destiny.services.policy.types.AgentTypeEnum;
import com.bluejungle.domain.types.AgentTypeDTO;

/**
 * @author rlin
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/test/integration/src/java/test/com/bluejungle/destiny/test/integration/TestAgentRegistration.java#5 $
 */

public class TestAgentRegistration extends TestCase {

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

    /*
     * Constructor
     */
    public TestAgentRegistration(String testName) {
        super(testName);
    }

    /**
     * Creating a Test Suite
     */
    public static Test suite() throws ServiceException, RemoteException, InterruptedException {
        AgentServiceLocator agentSvcLocator = new AgentServiceLocator();
        agentSvcLocator.setAgentServiceIFPortEndpointAddress("http://localhost:8081/dms/services/AgentServiceIFPort");
        dmsAgentService = agentSvcLocator.getAgentServiceIFPort();
        assertNotNull("DMS Agent Service not created properly", dmsAgentService);
        
        TestSuite ts = new TestSuite();
        ts.addTest(new TestAgentRegistration("testAgentRegistration"));
        ts.addTest(new TestAgentRegistration("testAgentSecurityCerts"));
        ts.addTest(new TestAgentRegistration("testAgentBundle"));
        ts.addTest(new TestAgentRegistration("testAgentDefaultProfile"));
        ts.addTest(new TestAgentRegistration("testServerShowAgent"));
        ts.addTest(new TestAgentRegistration("testServerHeartBeat"));
        return (ts);
    }

    public void testAgentRegistration() throws IOException {
        File registration = new File(regInfo);
        assertTrue("registration.info does not exist", registration.exists());
    }

    public void testAgentSecurityCerts() throws IOException {
        File tempKeystore = new File(cert1);
        assertTrue("temp_agent-keystore.jks does not exist", tempKeystore.exists());
        File keystore = new File(cert2);
        assertTrue("agent-keystore.jks does not exist", keystore.exists());
        File truststore = new File(cert3);
        assertTrue("agent-truststore.jks does not exist", truststore.exists());
    }

    public void testAgentBundle() throws IOException {
        File agentBundle = new File(bundle);
        assertTrue("agent bundle does not exist", agentBundle.exists());
    }

    public void testAgentDefaultProfile() throws FileNotFoundException, MarshalException, ValidationException {
        FileReader reader = new FileReader(commprofile);
        assertNotNull("commprofile.xml not found", reader);

        Unmarshaller communicationProfileUnmarshaller = new Unmarshaller(CommProfileDTO.class);
        CommProfileDTO communicationProfile = (CommProfileDTO) communicationProfileUnmarshaller.unmarshal(reader);

        //Extract Heartbeat value
        TimeIntervalDTO timeIntervalDTO = communicationProfile.getHeartBeatFrequency();
        assertNotNull("agent heartbeat value should not be null", timeIntervalDTO);
        assertEquals("agent heartbeat does not match", Integer.toString(heartBeat), timeIntervalDTO.getTime().toString());
    }

    public void testServerShowAgent() throws RemoteException {
        agentStat = dmsAgentService.getAgentStatistics();
        AgentCount[] agentCounts = agentStat.getAgentCount();
        for (int i = 0; i < agentCounts.length; i++) {
            AgentCount nextAgentCount = agentCounts[i];
            if (nextAgentCount.getAgentTypeId().equals(AgentTypeEnum.DESKTOP.getValue())) {                
                assertEquals("wrong number of desktop agents", Long.parseLong("1"), nextAgentCount.getCount());
            } else if (nextAgentCount.getAgentTypeId().equals(AgentTypeEnum.FILE_SERVER.getValue())) {
                assertEquals("wrong number of file server agents", Long.parseLong("0"), nextAgentCount.getCount());
            } else {                
                assertEquals("wrong number of agents", Long.parseLong("0"), nextAgentCount.getCount());
            }
        }
    }

    /**
     * This test checks that the DMS agent service returns the right number of
     * agents and the correct heartbeat information.
     * 
     * @throws RemoteException
     */
    public void testServerHeartBeat() throws RemoteException {
        AgentDTOQuerySpec allDesktopAgentsQuerySpec = new AgentDTOQuerySpec();
        allDesktopAgentsQuerySpec.setLimit(0);
        ConcreteAgentDTOQueryTerm agentTypeQueryTerm = new ConcreteAgentDTOQueryTerm();
        agentTypeQueryTerm.setAgentDTOQueryField(AgentDTOQueryField.TYPE);
        agentTypeQueryTerm.setOperator(RelationalOpDTO.equals);
        agentTypeQueryTerm.setValue(AgentTypeDTO.DESKTOP);
        allDesktopAgentsQuerySpec.setSearchSpec(new AgentDTOQueryTermList(new AgentDTOQueryTerm[] { agentTypeQueryTerm }));
        
        AgentDTOSortTerm sortOnHostTerm = new AgentDTOSortTerm();
        sortOnHostTerm.setField(AgentDTOSortTermField.HOST);
        sortOnHostTerm.setAscending(true);
        allDesktopAgentsQuerySpec.setSortSpec(new AgentDTOSortTermList(new AgentDTOSortTerm[] { sortOnHostTerm }));
        
        AgentQueryResultsDTO queryResults = dmsAgentService.getAgents(allDesktopAgentsQuerySpec);
        assertNotNull("dms agent service should return search results", queryResults);
        AgentDTOList agentList = queryResults.getAgentList();
        assertNotNull("agent search results should return a list of agents", agentList);
        AgentDTO agent = agentList.getAgents(0);
        assertNotNull("An agent should be returned", agent);
        Calendar heartbeat = agent.getLastHeartbeat();
        assertNotNull("The agent should have heartbeat information", heartbeat);
    }
}
