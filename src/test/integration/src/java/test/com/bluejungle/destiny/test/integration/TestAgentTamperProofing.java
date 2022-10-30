/*
 * Created on Oct 18, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.test.integration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.bluejungle.destiny.agent.controlmanager.CMRequestHandler;
import com.bluejungle.destiny.agent.ipc.IIPCProxy;
import com.bluejungle.destiny.agent.ipc.IPCProxy;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;

//import com.bluejungle.destiny.agent.ipc.IPCProxy;

import junit.framework.TestCase;

/**
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/test/integration/src/java/test/com/bluejungle/destiny/test/integration/TestAgentTamperProofing.java#1 $
 */

public class TestAgentTamperProofing extends TestCase {

    private static String buildRoot = System.getProperty("build.root.dir");
    private static String agentRoot = buildRoot + "\\agent_install";
    private static String bundle = agentRoot + "\\bundle.bin";
    private static String srcRoot = System.getProperty("src.root.dir");
    private static String cmd2Path = srcRoot + "\\etc\\cmd2";
    
    
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

    public void testStopAgentService() throws IOException, InterruptedException { 
        Runtime rt = Runtime.getRuntime();        
        Process p = rt.exec("cmd /c NET STOP \"Compliance Agent Service\"");
        p.waitFor();
        assertEquals("exit value of NET STOP should be 2 (error value) ", 2, p.exitValue());
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IIPCProxy.REQUEST_HANDLER_CLASS_NAME, CMRequestHandler.class.getName());
        ComponentInfo info = new ComponentInfo(IPCProxy.class.getName(), IPCProxy.class.getName(), IIPCProxy.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        IComponentManager cm = ComponentManagerFactory.getComponentManager();
        IPCProxy ipcProxy = (IPCProxy) cm.getComponent(info);
        boolean isAgentRunning = ipcProxy.invoke("getAgentInfo", new ArrayList(), new ArrayList());
        assertTrue("agent should be running", isAgentRunning);
    }
    
    public void testDeleteAgentFiles() throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();        
        String cmd = cmd2Path + " /c Del " + bundle;
        Process p = rt.exec(cmd);
        p.waitFor();
        
        File agentBundle = new File(bundle);
        assertTrue("agent bundle should not have been deleted", agentBundle.exists());
    }
}
