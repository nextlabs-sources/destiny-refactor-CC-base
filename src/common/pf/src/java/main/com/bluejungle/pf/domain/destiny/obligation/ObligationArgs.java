/*
 * Created on Jun 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.obligation;

/**
 * Arguments object that needs to be passed to all the DObligations
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/obligation/ObligationArgs.java#1 $:
 */

public class ObligationArgs {
    private final String fromResName;
    private final String toResName;
    private final String actionName;
    private final String userName;    
    private final long userId;
    private final String hostName;
    private final long hostId;
    private final String appName;
    private final long appId;
    private final String effectName;
    private final String userResponse;
    private final long requestId;

    
    
    
    /**
     * Constructor
     * @param fromResName
     * @param toResName
     * @param actionName
     * @param userId
     * @param hostId
     * @param appId
     * @param effectName
     * @param userResponse
     * @param requestId
     */
    public ObligationArgs(final String fromResName, 
            final String toResName, 
            final String actionName, 
            final String userName,
            final long userId, 
            final String hostName,
            final long hostId, 
            final String appName,
            final long appId, 
            final String effectName, 
            final String userResponse, 
            final long requestId) {
        super();
        this.fromResName = fromResName;
        this.toResName = toResName;
        this.actionName = actionName;
        this.userName = userName;
        this.userId = userId;
        this.hostName = hostName;
        this.hostId = hostId;
        this.appName = appName;
        this.appId = appId;
        this.effectName = effectName;
        this.userResponse = userResponse;
        this.requestId = requestId;
    }
    /**
     * Returns the actionName.
     * @return the actionName.
     */
    public String getActionName() {
        return this.actionName;
    }
    /**
     * Returns the appId.
     * @return the appId.
     */
    public long getAppId() {
        return this.appId;
    }
    /**
     * Returns the effectName.
     * @return the effectName.
     */
    public String getEffectName() {
        return this.effectName;
    }
    /**
     * Returns the fromResName.
     * @return the fromResName.
     */
    public String getFromResName() {
        return this.fromResName;
    }
    /**
     * Returns the hostId.
     * @return the hostId.
     */
    public long getHostId() {
        return this.hostId;
    }
    /**
     * Returns the requestId.
     * @return the requestId.
     */
    public long getRequestId() {
        return this.requestId;
    }
    /**
     * Returns the toResName.
     * @return the toResName.
     */
    public String getToResName() {
        return this.toResName;
    }
    /**
     * Returns the userId.
     * @return the userId.
     */
    public long getUserId() {
        return this.userId;
    }
    /**
     * Returns the userResponse.
     * @return the userResponse.
     */
    public String getUserResponse() {
        return this.userResponse;
    }
    
    
    
    /**
     * Returns the appName.
     * @return the appName.
     */
    public String getAppName() {
        return this.appName;
    }
    /**
     * Returns the hostName.
     * @return the hostName.
     */
    public String getHostName() {
        return this.hostName;
    }
    /**
     * Returns the userName.
     * @return the userName.
     */
    public String getUserName() {
        return this.userName;
    }
    public String toString() {
        StringBuffer rv = new StringBuffer("fromResName: ");
        rv.append(fromResName);
        rv.append(" toResName: ").append(toResName);
        rv.append(" userName: ").append(userName);
        rv.append(" hostName: ").append(hostName);
        rv.append(" appName: ").append(appName);
        rv.append(" effectName: ").append(effectName);
        rv.append(" requestId: ").append(requestId);
        
        return rv.toString();
    }
}
