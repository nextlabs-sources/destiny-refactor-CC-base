/*
 * Created on Jan 14, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.container.dcc;

/**
 * This class defines names of all DCC events
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public abstract class DCCEvents {

    /*
     * SYSTEM EVENT: This event is fired when any DCC component
     * registers/unregisters for a Distributed DCC Event. It is also called when
     * a component unregisters itself entirely with the DMS, implying that it
     * will not be listening to any more events. It is thus a system event used
     * to support the event broker framework that DMS implements.
     */
    public static final String EVENT_REGISTRATION_UPDATES_AVAILABLE = "EventRegistrationUpdatesAvailable";

    /*
     * This event is fired whenever the DMS has some updated information for the
     * dcc components. It is expected that on being notified of this event all
     * listening DCC components send a heartbeat to the DMS.
     */
    public static final String DMS_UPDATES_AVAILABLE = "DMSUpdatesAvailable";

}
