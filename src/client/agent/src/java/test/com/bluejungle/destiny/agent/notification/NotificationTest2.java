/*
 * Created on Apr 19, 2005
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.destiny.agent.notification;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import junit.framework.TestCase;

import com.bluejungle.destiny.agent.commandengine.CommandExecutor;
import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.destiny.agent.communication.ICommunicationManager;
import com.bluejungle.destiny.agent.communication.tests.Global;
import com.bluejungle.destiny.agent.communication.tests.MockCommunicationManager;
import com.bluejungle.destiny.agent.communication.tests.TestControlManager;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.services.agent.types.UserNotification;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;

import com.bluejungle.destiny.agent.activityjournal.ActivityJournal;
import com.bluejungle.destiny.agent.activityjournal.IActivityJournal;
import com.bluejungle.destiny.agent.profile.IProfileManager;
import com.bluejungle.destiny.agent.profile.ProfileManager;


/**
 * Adds a notification to the NotificationManager. Sets the state so that 
 * MockAgentServiceImpl will not throw an exception.
 * This test case expects that NotificationTest1 was run before this.
 * It will fail if it is run before NotificationTest1
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/src/java/test/com/bluejungle/destiny/agent/notification/NotificationTest2.java#1 $:
 */

public class NotificationTest2 extends TestCase {

    INotificationManager notificationManager = null;
    ICommandExecutor commandExecutor = null;
    
    protected void setUp() throws Exception {
        super.setUp();

        IComponentManager cm = ComponentManagerFactory.getComponentManager();
        IControlManager controlManager = cm.getComponent(new ComponentInfo<TestControlManager>(
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
        
        IActivityJournal activityJournal = cm.getComponent(new ComponentInfo<ActivityJournal>(
        		IActivityJournal.NAME, 
        		ActivityJournal.class,
        		IActivityJournal.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		config));
        
        commandExecutor = cm.getComponent(new ComponentInfo<CommandExecutor>(
        		ICommandExecutor.NAME, 
        		CommandExecutor.class, 
        		ICommandExecutor.class, 
        		LifestyleType.SINGLETON_TYPE));

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
    public NotificationTest2(String name) {
        super(name);
    }
    
    public void testNotification2() throws FileNotFoundException, IOException, ClassNotFoundException {
        System.out.println("Start NotificationTest2...");
        // do not throw exception. both notifications should be sent to MockAgentServiceImpl
        Global.throwException = false;
        UserNotification notification = new UserNotification();
        notification.setFrom("a@b.com");
        notification.setTo("y@z.com");
        notification.setSubject("subject2");
        notification.setBody("body2");
        this.notificationManager.addNotification(notification);
        
        String fileName = NotificationManager.LOG_DIR + File.separator + NotificationManager.NOTIFICATION_FILE;
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fileName)));
        ArrayList notifications = (ArrayList) in.readObject();
        assertEquals ("Incorrect size of notifications list.", 0, notifications.size());
        
        System.out.println("End NotificationTest2...");
    }

}
