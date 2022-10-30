/*
 * Created on Jan 1, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.container.dms.data;

import java.util.Calendar;

import com.bluejungle.destiny.container.dcc.DCCComponentEnumType;
import com.bluejungle.destiny.container.dms.components.compmgr.IDCCComponentDO;
import com.bluejungle.version.IVersion;

/**
 * Objects of this class represent a component registered with the DMS and they
 * are persisted to the database.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 *  
 */
public class ComponentDO implements IDCCComponentDO {

    private static final int ACCEPTABLE_LOST_HEARTBEATS = 2;

    private Long id;
    private String name;
    private DCCComponentEnumType type;
    private String typeDisplayName;
    private String callbackURL;
    private Calendar lastHeartbeat;
    private int heartbeatRate;
    private String componentURL;
    private IVersion version;

    /**
     * 
     * Zero-argument Constructor
     *  
     */
    public ComponentDO() {
    }

    /**
     * Returns the id
     * 
     * @return
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the id
     * 
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns whether this componet is active or not based on the last
     * heartbeat, current time and the expected heartbeat time.
     * 
     * @return
     */
    public boolean isActive() {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long expectedInterval = this.getHeartbeatRate() * 1000;
        Calendar lastHeartbeat = this.getLastHeartbeat();

        // If the current time is later than the expected heartbeat time, we
        // consider this component disabled:
        if (currentTime < (lastHeartbeat.getTimeInMillis() + (expectedInterval * ACCEPTABLE_LOST_HEARTBEATS))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the heartbeat rate
     * 
     * @return integer representing the heartbeat interval in seconds
     */
    public int getHeartbeatRate() {
        return this.heartbeatRate;
    }

    /**
     * Sets the heartbeat rate
     * 
     * @param heartbeatRate
     *            integer representing the heartbeat interval in seconds
     */
    public void setHeartbeatRate(int heartbeatRate) {
        this.heartbeatRate = heartbeatRate;
    }

    /**
     * Returns last heartbeat timestamp
     * 
     * @return long value representing the time the heartbeat was received, in
     *         millis
     */
    public Calendar getLastHeartbeat() {
        return this.lastHeartbeat;
    }

    /**
     * Set the last heartbeat timestamp
     * 
     * @param lastHeartbeat
     *            long value representing the time the heartbeat was received,
     *            in millis
     */
    public void setLastHeartbeat(Calendar lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    /**
     * Returns the name of the component
     * 
     * @return String representing the name of the component
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the component
     * 
     * @param name
     *            string representing the name of the component
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the type of the component
     * 
     * @return the type of the component
     */
    public DCCComponentEnumType getType() {
        return this.type;
    }

    /**
     * Sets the type of the component
     * 
     * @param type
     *            component type to set
     */
    public void setType(DCCComponentEnumType type) {
        this.type = type;
    }

    /**
     * Returns the callback URL
     * 
     * @return string representing the callback URL for this component
     */
    public String getCallbackURL() {
        return this.callbackURL;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.compmgr.IDCCComponentDO#getLoadBalancerURL()
     */
    public String getLoadBalancerURL() {
        return this.componentURL;
    }

    /**
     * Sets the callback URL of this component
     * 
     * @param callbackURL
     *            string representing the callback URL of this component
     */
    public void setCallbackURL(String callbackURL) {
        this.callbackURL = callbackURL;
    }

    /**
     * Set the component url
     * 
     * @param string
     *            the component url
     */
    public void setComponentURL(String componentURL) {
        this.componentURL = componentURL;
    }

    /**
     * Retrieve the componentURL.
     * 
     * @return the componentURL.
     */
    public String getComponentURL() {
        return this.componentURL;
    }
    
	/**
	 * Returns the version of the component
	 * @return the version of the component
	 */
	public IVersion getVersion(){
	    return this.version;
	}

	/**
	 * sets the version of the component
	 */
	public void setVersion(IVersion version){
	    this.version = version;
	}

    public String getTypeDisplayName() {
        return typeDisplayName;
    }

    public void setTypeDisplayName(String typeDisplayName) {
        this.typeDisplayName = typeDisplayName;
    }
	
}