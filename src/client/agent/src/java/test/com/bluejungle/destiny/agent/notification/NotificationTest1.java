/*
 * Created on Apr 19, 2005
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.destiny.agent.notification;

import junit.framework.TestCase;

import com.bluejungle.destiny.agent.activityjournal.ActivityJournal;
import com.bluejungle.destiny.agent.activityjournal.IActivityJournal;
import com.bluejungle.destiny.agent.commandengine.CommandExecutor;
import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.destiny.agent.communication.ICommunicationManager;
import com.bluejungle.destiny.agent.communication.tests.Global;
import com.bluejungle.destiny.agent.communication.tests.MockCommunicationManager;
import com.bluejungle.destiny.agent.communication.tests.TestControlManager;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.agent.profile.IProfileManager;
import com.bluejungle.destiny.agent.profile.ProfileManager;
import com.bluejungle.destiny.services.agent.types.UserNotification;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * Adds a notification to the NotificationManager. Sets the state so that 
 * MockAgentServiceImpl will throw an exception.
 * This should cause the notification to be saved to disk.
 * This test should be followed by NotificationTest2.
 * 
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/src/java/test/com/bluejungle/destiny/agent/notification/NotificationTest1.java#1 $:
 */

public class NotificationTest1 extends TestCase {

    INotificationManager notificationManager = null;
    ICommandExecutor commandExecutor = null;
    
    protected void setUp() throws Exception {
        super.setUp();

        IComponentManager cm = ComponentManagerFactory.getComponentManager();
        IControlManager controlManager = cm.getComponent(
        		new ComponentInfo<TestControlManager>(
        		IControlManager.NAME, 
        		TestControlManager.class, 
        		IControlManager.class, 
        		LifestyleType.SINGLETON_TYPE));
        
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IControlManager.NAME, controlManager);
        ICommunicationManager communicationManager = cm.getComponent(MockCommunicationManager.class, config);
        
        IProfileManager profileManager = cm.getComponent(new ComponentInfo<ProfileManager>(
        		IProfileManager.class.getName(), 
        		ProfileManager.class, 
        		IProfileManager.class, 
        		LifestyleType.SINGLETON_TYPE));
        profileManager.setCommunicationProfile(controlManager.getCommunicationProfile());
        
        ComponentInfo<CommandExecutor> commandExecutorInfo = new ComponentInfo<CommandExecutor>(
        		ICommandExecutor.NAME, 
        		CommandExecutor.class, 
        		ICommandExecutor.class, 
        		LifestyleType.SINGLETON_TYPE);
        ComponentManagerFactory.getComponentManager().registerComponent(commandExecutorInfo, true);        
        commandExecutor = cm.getComponent(commandExecutorInfo);
        
        IActivityJournal activityJournal = cm.getComponent(new ComponentInfo<ActivityJournal>(
        		IActivityJournal.NAME, 
        		ActivityJournal.class, 
        		IActivityJournal.class, 
        		LifestyleType.SINGLETON_TYPE, config));

        this.notificationManager = cm.getComponent(new ComponentInfo<NotificationManager>(
        		INotificationManager.class.getName(), 
        		NotificationManager.class, 
        		INotificationManager.class, 
        		LifestyleType.SINGLETON_TYPE));
        
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for NotificationTest1.
     * @param name
     */
    public NotificationTest1(String name) {
        super(name);
    }
    
    public void testNotification1(){
        System.out.println("Start NotificationTest1...");
        //throw exception in MockAgentServiceImpl. Notification should be saved to disk
        Global.throwException = true;
        UserNotification notification = new UserNotification();
        notification.setFrom("a@b.com");
        notification.setTo("c@d.com");
        notification.setSubject("subject");
        notification.setBody("body");
        this.notificationManager.addNotification(notification);
        System.out.println("End NotificationTest1...");
    }

}
