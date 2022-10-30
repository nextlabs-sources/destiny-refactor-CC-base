/*
 * Created on Apr 18, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.notification;

import com.bluejungle.destiny.services.agent.types.UserNotification;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public interface INotificationManager {

    public static final String NAME = INotificationManager.class.getName();

    /**
     * Add notification to the list.
     * 
     * @param notification
     *            UserNotification instance to add
     */
    public abstract void addNotification(UserNotification notification);

    /**
     * Tries to send notifications to the server. If send is successful, remove
     * notifications from list.
     * 
     * @return true if notifications were sent to the server
     */
    public abstract boolean sendNotifications();
}