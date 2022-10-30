/*
 * Created on Apr 18, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.notification;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.agent.communication.ICommunicationManager;
import com.bluejungle.destiny.services.agent.types.UserNotification;
import com.bluejungle.destiny.services.agent.types.UserNotificationBag;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.PropertyKey;

/**
 * This class manages notifications being sent to the server.
 * 
 * This class is not threadsafe by design. It is only called from the Command
 * Executor thread. Synchronization code will need to be added if we ever need
 * to call this from multiple threads, (e.g., if we decide to add more threads
 * to the command executor),
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class NotificationManager implements ILogEnabled, IInitializable, INotificationManager, IConfigurable {
    public static final PropertyKey<String> BASE_DIR_PROPERTY_NAME = new PropertyKey<String>("baseDirProperty");
    private static final String DEFAULT_BASE_DIR = ".";
    private static String baseDir = DEFAULT_BASE_DIR;

    static final String LOG_DIR = "logs";
    static final String NOTIFICATION_FILE = "notification.dat";
    private IConfiguration config;
    protected ICommunicationManager communicationManager = null;

    protected ArrayList<UserNotification> notifications = new ArrayList<UserNotification>();

    protected Log log = null;

    /**
     * Add notification to the list and sends notifications to the server.
     * 
     * @param notification
     *            UserNotification instance to add.
     */
    public synchronized void addNotification(UserNotification notification) {
        this.notifications.add(notification);
        if (!sendNotifications()){
            this.saveNotificationsToDisk();
        }
    }

    /**
     * Loads notifications from disk.
     */
    private void loadNotifications() {
        String fileName = baseDir + File.separator + LOG_DIR + File.separator + NOTIFICATION_FILE;
        try {
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fileName)));
            this.notifications = (ArrayList) in.readObject();
        } catch (FileNotFoundException e) {
            //Not an error
        } catch (IOException e) {
            this.log.error("Unable to read notifications file.", e);
        } catch (ClassNotFoundException e) {
            this.log.error("Invalid notifications file.", e);
        }
    }

    /**
     * Saves notifications to disk.
     */
    private void saveNotificationsToDisk() {
        String fileName = baseDir + File.separator + LOG_DIR + File.separator + NOTIFICATION_FILE;
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
            out.writeObject(this.notifications);
            out.close();
        } catch (FileNotFoundException e1) {
            this.log.error("Unable to write notifications file", e1);
        } catch (IOException e1) {
            this.log.error("Unable to write notifications file", e1);
        }
    }

    /**
     * Tries to send notifications to the server. If send is successful, remove
     * notifications from list.
     * 
     * @return true if notifications were sent to the server
     */
    public synchronized boolean sendNotifications() {
        if (this.notifications.size() <= 0){
            return true;
        }
        
        boolean notificationsSent = false;
        UserNotificationBag notificationBag = new UserNotificationBag();
        notificationBag.setNotifications((UserNotification[]) this.notifications.toArray(new UserNotification[this.notifications.size()]));

        try {
            this.communicationManager.getAgentServiceIF().sendUserNotifications(notificationBag);
            notificationsSent = true;
        } catch (ServiceNotReadyFault e) {
            this.log.error("Notification send failed. Service not ready", e);
        } catch (UnauthorizedCallerFault e) {
            this.log.error("Notification send failed. Access denied by server", e);
        } catch (RemoteException e) {
            this.log.error("Notification send failed. Service threw exception", e);
        } catch (ServiceException e) {
            this.log.error("Notification send failed.", e);
        }

        if (notificationsSent) {
            this.notifications.clear();
            this.saveNotificationsToDisk();                
        }
        
        return notificationsSent;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return config;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        IConfiguration configuration = getConfiguration();

        if (configuration != null) {
            baseDir = configuration.get(BASE_DIR_PROPERTY_NAME, DEFAULT_BASE_DIR);
        }

        IComponentManager manager = ComponentManagerFactory.getComponentManager();

        if (this.communicationManager == null) {
            this.communicationManager = (ICommunicationManager) manager.getComponent(ICommunicationManager.NAME);
            if (this.communicationManager == null) {
                this.log.error("Notification Manager init failed. Communication Manager not found.");
                return;
            }
        }
        
        this.loadNotifications();

    }
}
