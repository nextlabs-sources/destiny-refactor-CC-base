/*
 * Created on Jan 1, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.container.dms.data;

import java.util.Calendar;

/**
 * This class represents an association record between an event and a DCSF
 * component. It maintains a counter of the number of reg/unreg requests made.
 * Only a positive count is considered to indicate registration. Negative or 0
 * means no registration.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 *  
 */

public class EventRegistration {

    private Long id;
    private ComponentDO consumer;
    private EventDO event;
    private boolean isActive;
    private long lastModified;
    private String callbackURL;

    /**
     * Zero-argument Constructor
     *  
     */
    public EventRegistration() {
        super();
    }

    /**
     * Returns the ID of this registration
     * 
     * @return
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the ID for this registration
     * 
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the event associated with this registration
     * 
     * @return
     */
    public EventDO getEvent() {
        return this.event;
    }

    /**
     * Sets the event associated with this registration
     * 
     * @param event
     */
    public void setEvent(EventDO event) {
        this.event = event;
    }

    /**
     * Returns the consumer associated with this registration
     * 
     * @return Consuming DCSF
     */
    public ComponentDO getConsumer() {
        return this.consumer;
    }

    /**
     * Sets the consumer of this registration
     * 
     * @param listeningDCSF
     */
    public void setConsumer(ComponentDO consumer) {
        this.consumer = consumer;
        this.callbackURL = consumer.getCallbackURL();
    }

    /**
     * Returns whether the association is active - i.e. if the DCSF is listening
     * to the given event. This will be a calculated field based on the
     * registrationCount.
     * 
     * @return boolean
     */
    public boolean getIsActive() {
        return this.isActive;
    }

    /**
     * Sets whether the given association is active or not.
     * 
     * @param isActive
     */
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * This method "dirties" the event registration so that it can be picked up
     * and returned the next time a DCSF heartbeat is received.
     */
    public void touch() {
        this.setLastModified(Calendar.getInstance().getTimeInMillis());
    }

    /**
     * Sets the last modified date for this registration
     * 
     * @return date of last modification
     */
    public long getLastModified() {
        return this.lastModified;
    }

    /**
     * Gets the last modified date for this registration
     * 
     * @param lastModified
     */
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Returns the callback URL association with this registration
     * 
     * @return callback URL
     */
    public String getCallbackURL() {
        return this.callbackURL;
    }

    /**
     * Sets the callback URL associated with this registration
     * 
     * @param callbackURL
     */
    public void setCallbackURL(String callbackURL) {
        this.callbackURL = callbackURL;
    }
}