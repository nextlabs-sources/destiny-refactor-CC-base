/*
 * Created on Dec 13, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004-2007 by NextLabs, Inc., San Mateo CA, Ownership remains with NextLabs,
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.commandengine.tests;

import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.xml.rpc.ServiceException;

import com.bluejungle.destiny.agent.activityjournal.ActivityJournal;
import com.bluejungle.destiny.agent.activityjournal.IActivityJournal;
import com.bluejungle.destiny.agent.commandengine.CommandExecutor;
import com.bluejungle.destiny.agent.commandengine.CommandSpecBase;
import com.bluejungle.destiny.agent.commandengine.IAgentCommand;
import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.destiny.agent.commandengine.IAgentCommand.CommunicationType;
import com.bluejungle.destiny.agent.communication.tests.MockCommunicationManager;
import com.bluejungle.destiny.agent.communication.tests.MockLogServiceImpl;
import com.bluejungle.destiny.agent.communication.tests.TestControlManager;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.agent.profile.IProfileManager;
import com.bluejungle.destiny.agent.profile.ProfileManager;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.PolicyActivityInfoV2;
import com.bluejungle.domain.log.PolicyActivityLogEntryV2;
import com.bluejungle.domain.log.ToResourceInformation;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.test.BaseDestinyTestCase;
import com.bluejungle.framework.utils.DynamicAttributes;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class CommandExecutorTest extends BaseDestinyTestCase {

    protected IComponentManager cm;
    protected MockCommunicationManager commMgr;

    /**
     * Returns the communication manager object
     * 
     * @return the communication manager object
     */
    protected MockCommunicationManager getCommMgr() {
        return this.commMgr;
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.cm = ComponentManagerFactory.getComponentManager();

        //create control manager instance
        ComponentInfo<?> info = new ComponentInfo<TestControlManager>(
        		IControlManager.NAME, 
        		TestControlManager.class, 
        		IControlManager.class, 
        		LifestyleType.SINGLETON_TYPE);
        this.cm.registerComponent(info, true);
        IControlManager controlManager = (IControlManager) this.cm.getComponent(IControlManager.NAME);

        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IControlManager.NAME, controlManager);
        this.commMgr = (MockCommunicationManager) ComponentManagerFactory.getComponentManager().getComponent(MockCommunicationManager.class, config);

        info = new ComponentInfo<ProfileManager>(
                IProfileManager.class.getName(), 
                ProfileManager.class, 
                IProfileManager.class, 
                LifestyleType.SINGLETON_TYPE);
        this.cm.registerComponent(info, true);
        
        info = new ComponentInfo<ActivityJournal>(
        		IActivityJournal.NAME, 
        		ActivityJournal.class, 
        		IActivityJournal.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		config);
        this.cm.registerComponent(info, true);

        info = new ComponentInfo<CommandExecutor>(
        		ICommandExecutor.NAME, 
        		CommandExecutor.class, 
        		ICommandExecutor.class, 
        		LifestyleType.SINGLETON_TYPE);
        this.cm.registerComponent(info, true);
    }

    /**
     * Constructor for CommandExecutorTest.
     * 
     * @param name
     *            name of the test to perform
     */
    public CommandExecutorTest(String name) {
        super(name);
    }

    /**
     * This test verifies that the command executor logs the policy activity
     * properly
     * 
     * @throws InterruptedException
     */
 //   public void testCommandExecutor() throws InterruptedException, ServiceException {
 //       ICommandExecutor commandExecutor = (ICommandExecutor) this.cm.getComponent(ICommandExecutor.NAME);
 //       ((CommandExecutor) commandExecutor).start();
 //       FromResourceInformation fromResourceInfo = new FromResourceInformation();
 //       fromResourceInfo.setCreatedDate(System.currentTimeMillis());
 //       fromResourceInfo.setModifiedDate(System.currentTimeMillis());
 //       fromResourceInfo.setName("file:///c:/docs/fromResource.doc");
 //       fromResourceInfo.setOwnerId("0");
 //       fromResourceInfo.setSize(1000);

//        ToResourceInformation toResourceInfo = new ToResourceInformation();
//        toResourceInfo.setName("file:///c:/docs/fromResource-renamed.doc");
//        String key = "key1";
//        String value = "value1";
//        DynamicAttributes fromResAttrs = new DynamicAttributes();
//        fromResAttrs.put(key, value);

//        // Fire the log command
 //       final int nbLogs = 10;
 //       MockLogServiceImpl service = (MockLogServiceImpl) getCommMgr().getLogServiceIF();
 //       service.reset();
 //       for (int i = 0; i < nbLogs; i++) {
 //           FromResourceInformation ri = new FromResourceInformation(MockLogServiceImpl.FROM_FILE_NAME, 234, 78624243, 212131, "SOME-SID-LIKE-THING");
 //           PolicyActivityInfoV2 req = new PolicyActivityInfoV2(ri, null, "FargeyMcGunkle@bluejungle.com", 1, "destiny.bluejungle.com", "1.2.3.4", 54, "notepad.exe", 89, ActionEnumType.ACTION_MOVE.getName(), PolicyDecisionEnumType.POLICY_DECISION_ALLOW, 456, 24423, 0, fromResAttrs);
 //           PolicyActivityLogEntryV2 logEntry = new PolicyActivityLogEntryV2(req, 345);
 //           commandExecutor.logActivity(logEntry);
 //       }

 //       Thread.sleep(200);
 //       ((CommandExecutor) commandExecutor).stop();

 //       //Check that the logs have been sent
 //       IActivityJournal activityJournal = (IActivityJournal) this.cm.getComponent(IActivityJournal.NAME);
 //       activityJournal.uploadActivityLogs();
 //       assertEquals(10, service.getNumberOfLogs());
 //       service.reset();
 //   }
    
    private static final int ONE_MB = 1024*1024;
    
    private class ForeverCommand implements IAgentCommand {
        byte[] b;
        private final CommunicationType type;
        ForeverCommand(CommunicationType type){
            this.type = type;
            b = new byte[ONE_MB];
        }
        
        
        @Override
        public int execute() {
            //hang forever if it is NETWORK
            if(type == CommunicationType.NETWORK){
                boolean inTerrupted = false;
                while (!inTerrupted) {
                    try {
                        Thread.sleep(1000);
                        b[0] = b[1];
                    } catch (InterruptedException e) {
                        inTerrupted = true;
                    }
                }
            }
            return 0;
        }

        @Override
        public CommunicationType getCommunicationType() {
            return type;
        }

        @Override
        public void init(ArrayList paramArray) {
        }

        @Override
        public void init(CommandSpecBase commandSpec) {
        }
        
    }
    
    private long usedMemory(){
        System.gc();
        Thread.yield();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        }
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
    
    public void testStress() throws Throwable {
        if (true) {
            // don't run this thread pool test with regular test suite.
            return;
        }
        long low = (long)(ONE_MB * 0.95);
        long high = (long)(ONE_MB * 1.2);
        
        Method addQueueMethod = CommandExecutor.class.getDeclaredMethod("addCommandToQueue", IAgentCommand.class);
        addQueueMethod.setAccessible(true);
        CommandExecutor commandExecutor = (CommandExecutor) this.cm.getComponent(ICommandExecutor.NAME);
        commandExecutor.start();
        
        for (int i = 0; i < 50; i++) {
            long t0 = usedMemory(); 
            addQueueMethod.invoke(commandExecutor, new ForeverCommand(CommunicationType.NETWORK));
            long delta = usedMemory() - t0;
            if(low < delta && delta < high){
                //in range
                System.out.println("delta = " + delta);
            }else{
                //out range
                System.err.println(" DELTA = " + delta);
            }
        }
        
        long t1 = usedMemory(); 
        for (int i = 0; i < 100; i++) {
            addQueueMethod.invoke(commandExecutor, new ForeverCommand(CommunicationType.LOCAL));
        }
        
        long delta = usedMemory() - t1;
        System.out.println("if doing LOCAL, the delta should be small. delta = " + delta);
        
        Thread.sleep(000);
    }
}
