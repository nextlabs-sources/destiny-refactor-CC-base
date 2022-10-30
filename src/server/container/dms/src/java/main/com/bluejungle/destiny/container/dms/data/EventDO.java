/*
 * Created on Jan 1, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.container.dms.data;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the persisted event object.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 * 
 * TODO: Need to refer to listener components using their own type, not
 * ComponentDO - preferably DCSFDO.
 */

public class EventDO {

    private long id;
    private String name;
    private Map registrations;

    /**
     * Constructor
     *  
     */
    public EventDO() {
        super();
    }

    /**
     * Gets the ID
     * 
     * @return id
     */
    public long getId() {
        return this.id;
    }

    /**
     * Sets the ID
     * 
     * @param id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns the map of registrations
     * 
     * @return map of (callback, registration) pairs
     */
    public Map getRegistrations() {
        return this.registrations;
    }

    /**
     * Sets the map of registrations
     * 
     * @param registrations
     */
    protected void setRegistrations(Map registrations) {
        this.registrations = registrations;
    }

    /**
     * Returns the name of the event
     * 
     * @return name of event
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the event
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Adds a dcsf component as a listener for this event. If the corresponding
     * registration already exists, it is marked as active.
     * 
     * @param event
     */
    public void addRegistration(ComponentDO listener) {
        if (this.registrations == null) {
            this.registrations = new HashMap();
        }

        EventRegistration registration = (EventRegistration) this.registrations.get(listener.getId());
        // Check if the registration already exists. If not we create a
        // registration record:
        if (registration == null) {
            registration = new EventRegistration();
            registration.setEvent(this);
            registration.setConsumer(listener);
            this.registrations.put(listener.getId(), registration);
        }
        registration.setIsActive(true);
        registration.touch();
    }

    /**
     * Removes the given listener from the list of listeners. This does not do a
     * hard-delete because we need to send disabled registrations as updates to
     * DCSF components and if we were to delete these we would not have a way to
     * do that. The isActive() field indicates whether this is an
     * active/inactive registration.
     * 
     * @param listener
     */
    public void disableRegistration(ComponentDO listener) {
        if (this.registrations != null) {
            EventRegistration registration = (EventRegistration) this.registrations.get(listener.getId());

            // Set the registration as inactive:
            if (registration != null) {
                registration.setIsActive(false);
                registration.touch();
            }
        }
    }

    /**
     * Deletes the given listener from the list of listeners. This does a hard
     * delete. (It is a special function and is not used in the normal operation
     * of the system).
     * 
     * @param listener
     */
    public void deleteRegistration(ComponentDO listener) {
        if (this.registrations != null) {
            EventRegistration registration = (EventRegistration) this.registrations.get(listener.getId());

            // Remove the registration:
            if (registration != null) {
                this.registrations.remove(listener.getId());
            }
        }
    }

    /**
     * Clears all the registrations associated with this event. This is only for
     * database cleanup purposes - and at present it is exclusively used for
     * JUnit testing.
     *  
     */
    public void deleteAllRegistrations() {
        if (this.registrations != null) {
            this.registrations.clear();
        }
    }
}