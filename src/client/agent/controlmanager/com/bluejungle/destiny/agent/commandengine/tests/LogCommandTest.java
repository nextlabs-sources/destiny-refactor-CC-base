/*
 * Created on Dec 13, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004-2007 by NextLabs, Inc., San Mateo CA, Ownership remains with NextLabs,
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.commandengine.tests;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.activityjournal.ActivityJournal;
import com.bluejungle.destiny.agent.activityjournal.IActivityJournal;
import com.bluejungle.destiny.agent.commandengine.CommandExecutor;
import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.destiny.agent.commandengine.LogCommand;
import com.bluejungle.destiny.agent.communication.ICommunicationManager;
import com.bluejungle.destiny.agent.communication.tests.MockCommunicationManager;
import com.bluejungle.destiny.agent.communication.tests.TestControlManager;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.agent.profile.IProfileManager;
import com.bluejungle.destiny.agent.profile.ProfileManager;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.PolicyActivityInfo;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.utils.DynamicAttributes;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/commandengine/tests/LogCommandTest.java#6 $:
 */

public class LogCommandTest extends TestCase {

    private IActivityJournal activityJournal;
    private IComponentManager cm;

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
        ICommunicationManager communicationManager = cm.getComponent(MockCommunicationManager.class, config);

        info = new ComponentInfo<ProfileManager>(
        		IProfileManager.class.getName(), 
        		ProfileManager.class, 
        		IProfileManager.class, 
        		LifestyleType.SINGLETON_TYPE);
        this.cm.registerComponent(info, true);

        info = new ComponentInfo<CommandExecutor>(
        		ICommandExecutor.NAME, 
        		CommandExecutor.class, 
        		ICommandExecutor.class, 
        		LifestyleType.SINGLETON_TYPE);
        this.cm.registerComponent(info, true);

        ComponentInfo<ActivityJournal> activityJournalInfo = new ComponentInfo<ActivityJournal>(
        		IActivityJournal.NAME, 
        		ActivityJournal.class, 
        		IActivityJournal.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		config);
        this.cm.registerComponent(info, true);
        this.activityJournal = this.cm.getComponent(activityJournalInfo);

    }

    /**
     * Constructor for LogCommandTest.
     * 
     * @param name
     */
    public LogCommandTest(String name) {
        super(name);
    }

    public void testLogCommand() {
        long now = System.currentTimeMillis();
        FromResourceInformation resourceInfo = new FromResourceInformation();
        resourceInfo.setCreatedDate(now);
        resourceInfo.setModifiedDate(now);
        resourceInfo.setName("c:\\files\\my files\\your files\\our files\\bogus.txt");
        resourceInfo.setOwnerId("SOME-SID-LIKE-THING");
        resourceInfo.setSize((234));
        String key = "key1";
        String value = "value1";
        DynamicAttributes fromResAttrs = new DynamicAttributes();
        fromResAttrs.put(key, value);
        
        PolicyActivityInfo info =
            new PolicyActivityInfo(resourceInfo,
                    null,
                    "bob",
                    1,
                    "host",
                    "1.2.3.4",
                    1,
                    "notepad.exe",
                    89,
                    ActionEnumType.ACTION_MOVE,
                    PolicyDecisionEnumType.POLICY_DECISION_ALLOW,
                    456,
                    now, 
                    0, 
                    fromResAttrs);
                    
        PolicyActivityLogEntry logEntry = new PolicyActivityLogEntry(info, 345);
        Log log = LogFactory.getLog(LogCommand.class.getName());
        LogCommand command = new LogCommand(logEntry, log, this.activityJournal, this.cm);
        command.execute();
    }
}
