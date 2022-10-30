/*
 * Created on Apr 18, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.commandengine;

import java.util.ArrayList;

import com.bluejungle.destiny.agent.notification.INotificationManager;
import com.bluejungle.destiny.services.agent.types.UserNotification;
import com.bluejungle.framework.comp.ComponentManagerFactory;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class NotifyCommand extends AgentCommandBase {

    UserNotification notification = null;

    /**
     * If there is a notification, send it to the notification manager.
     * Otherwise, ask the notification manager to send notifications to the
     * server
     * 
     * @see com.bluejungle.destiny.agent.commandengine.IAgentCommand#execute()
     * 
     *  
     */
    public int execute() {
        INotificationManager notificationManager = (INotificationManager) ComponentManagerFactory.getComponentManager().getComponent(INotificationManager.NAME);
        if (this.notification != null) {
            notificationManager.addNotification(this.notification);
        } else {
            notificationManager.sendNotifications();
        }

        return ErrorCode.SUCCESS;
    }

    /**
     * @return UserNotification object
     */
    public UserNotification getNotification() {
        return this.notification;
    }

    /**
     * @param notification
     *            UserNotification object
     */
    public void setNotification(UserNotification notification) {
        this.notification = notification;
    }

    public void init(ArrayList paramArray) {
        super.init(paramArray);
        this.notification = (UserNotification) paramArray.get(0);
    }
}