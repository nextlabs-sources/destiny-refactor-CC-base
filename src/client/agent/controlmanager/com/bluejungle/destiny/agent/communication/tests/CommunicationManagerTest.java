/*
 * Created on Dec 14, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.communication.tests;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import junit.framework.TestCase;

import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.destiny.agent.commandengine.tests.TestCommandExecutor;
import com.bluejungle.destiny.agent.communication.ICommunicationManager;
import com.bluejungle.destiny.agent.communication.PushListener;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.services.agent.AgentServiceIF;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.oil.OSType;

import com.nextlabs.destiny.interfaces.log.v2.LogServiceIF;
/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class CommunicationManagerTest extends TestCase {

    private static final int ITERATIONS = 5;
    private TestControlManager controlManager = null;
    private OSType ot = null;
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        //create control manager instance
        ComponentInfo<TestControlManager> info = new ComponentInfo<TestControlManager>(
        		IControlManager.NAME, 
        		TestControlManager.class, 
        		IControlManager.class, 
        		LifestyleType.SINGLETON_TYPE);
        this.controlManager = ComponentManagerFactory.getComponentManager().getComponent(info);
    }

    /**
     * Constructor for PushListenerTest.
     * 
     * @param name
     */
    public CommunicationManagerTest(String name) {
        super(name);
        ot = new OSType();
    }

//    public void testCommunicationManager() throws RemoteException, ServiceException, MalformedURLException, IOException {
//        System.out.println("Communication Manager Test Start...");
//        TestCommandExecutor commandExecutor = (TestCommandExecutor) ComponentManagerFactory.getComponentManager().getComponent(ICommandExecutor.NAME);
//        HashMapConfiguration config = new HashMapConfiguration();
//        config.setProperty(IControlManager.NAME, this.controlManager);
//        ICommunicationManager communicationManager = ComponentManagerFactory.getComponentManager().getComponent(MockCommunicationManager.class, config);
//        int portNumber = communicationManager.getPort();
//        boolean isPortValid = (portNumber == TestControlManager.DEFAULT_PORT || (portNumber <= TestControlManager.DEFAULT_PORT + PushListener.PORT_RANGE_DEVIANCE && portNumber >= TestControlManager.DEFAULT_PORT - PushListener.PORT_RANGE_DEVIANCE));
//        assertTrue("Invalid port number.", isPortValid);

//        sendPushRequests(portNumber);
//        assertEquals("Heartbeat count incorrect", ITERATIONS, commandExecutor.getHeartbeatCount());

//        AgentServiceIF agentServiceIF = communicationManager.getAgentServiceIF();
//        agentServiceIF.checkUpdates(null, null);
//        agentServiceIF.registerAgent(null);
 //       agentServiceIF.startupAgent(null, null);
 //       agentServiceIF.shutdownAgent(null, null);
 //       LogServiceIF logServiceIF = communicationManager.getLogServiceIF();
  //      logServiceIF.logPolicyActivity(null);

 //       assertEquals("Agent service. Incorrect number of calls.", 4, ((MockAgentServiceImpl) agentServiceIF).getNumberOfCalls());
 //       assertEquals("Log service. Incorrect number of calls.", 1, ((MockLogServiceImpl) logServiceIF).getNumberOfCalls());
//        ((MockLogServiceImpl) logServiceIF).reset();

 //       this.controlManager.changeConfig();

 //       communicationManager.reinit();

 //       portNumber = communicationManager.getPort();
 //       int expectedDefaultPortNumber = TestControlManager.DEFAULT_PORT + TestControlManager.CHANGE_INCREMENT;
 //       int maxAllowedPortNumber = TestControlManager.DEFAULT_PORT + TestControlManager.CHANGE_INCREMENT + PushListener.PORT_RANGE_DEVIANCE;
 //       int minAllowedPortNumber = TestControlManager.DEFAULT_PORT + TestControlManager.CHANGE_INCREMENT - PushListener.PORT_RANGE_DEVIANCE;
 //       isPortValid = (portNumber == expectedDefaultPortNumber || (portNumber <= maxAllowedPortNumber && portNumber >= minAllowedPortNumber));
 //       assertTrue("Invalid port number after reinit.", isPortValid);

 //       sendPushRequests(portNumber);

  //      assertEquals("Heartbeat count incorrect", ITERATIONS * 2, commandExecutor.getHeartbeatCount());

  //      agentServiceIF = communicationManager.getAgentServiceIF();
   //     agentServiceIF.checkUpdates(null, null);
  //      agentServiceIF.registerAgent(null);
   //     logServiceIF = communicationManager.getLogServiceIF();
   //     logServiceIF.logPolicyActivity(null);

  //      assertEquals("Agent service. Incorrect number of calls.", 2, ((MockAgentServiceImpl) agentServiceIF).getNumberOfCalls());
  //      assertEquals("Log service. Incorrect number of calls.", 1, ((MockLogServiceImpl) logServiceIF).getNumberOfCalls());
  //      ((MockLogServiceImpl) logServiceIF).reset();

  //      // Disable push and test that port is freed.
  //      this.controlManager.removePushConfig();
  //      communicationManager.reinit();

  //      // With push disabled, the agent is not listening for connections.
  //      // Therefore, the socket creation should fail
        
 //       //sleep 1 sec on Linux to wait connection close completely
 //       //otherwise, the port is still in CLOSE_WAIT state which will not give exception when connecting
 //       if(ot.getOSType()==OSType.OS_TYPE_LINUX)
 //       {
//	        try{
//	        Thread.sleep(1000);
//	        }
//	        catch (InterruptedException err)
//	        {
//	            System.out.println(err.getMessage());	        	
//	        }
 //       }
        
 //       IOException expectedException = null;
 //       Socket s = null;
 //       try {
  //          s = new Socket("localhost", portNumber);
  //      } catch (IOException exception) {
  //          expectedException = exception;
   //     }

    //    assertNotNull("Ensure IOException was thrown during attempted connection.", expectedException);
   //     assertTrue("Port was not released when push is disabled.", s == null || !s.isBound());
   //     if (s != null) {
   //         s.close();
   //     }

   //     agentServiceIF = communicationManager.getAgentServiceIF();
   //     agentServiceIF.startupAgent(null, null);
   //     agentServiceIF.shutdownAgent(null, null);
  //      logServiceIF = communicationManager.getLogServiceIF();
  //      logServiceIF.logPolicyActivity(null);

   //     assertEquals("Agent service. Incorrect number of calls.", 2, ((MockAgentServiceImpl) agentServiceIF).getNumberOfCalls());
   //     assertEquals("Log service. Incorrect number of calls.", 1, ((MockLogServiceImpl) logServiceIF).getNumberOfCalls());
   //     ((MockLogServiceImpl) logServiceIF).reset();

  //      //add push again and test
  //      this.controlManager.changeConfig();
  //      communicationManager.reinit();

  //      portNumber = communicationManager.getPort();
  //      isPortValid = (portNumber == expectedDefaultPortNumber || (portNumber <= maxAllowedPortNumber && portNumber >= minAllowedPortNumber));
  //      assertTrue("Invalid port number after reinit.", isPortValid);
  //      sendPushRequests(portNumber);
   //     assertEquals("Heartbeat count incorrect", ITERATIONS * 3, commandExecutor.getHeartbeatCount());

 //   }

    /**
     * Sends ITERATIONS push requests to the specified port number
     * 
     * @param portNumber
     */
    private void sendPushRequests(int portNumber) throws IOException {
        Socket socket = null;
        for (int i = 0; i < ITERATIONS; i++) {
            try {
                socket = new Socket("localhost", portNumber);

                assertNotNull(socket);
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                out.print('x');
                out.flush();
                BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                in.read();
                socket.close();
            } catch (UnknownHostException exception) {
                // Should happen
                fail("Failed to open socked on localhost: " + exception.getMessage());
            }
        }
    }
}
