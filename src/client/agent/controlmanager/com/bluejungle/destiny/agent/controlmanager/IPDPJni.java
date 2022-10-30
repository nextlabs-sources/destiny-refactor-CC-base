// All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
//Redwood City CA, Ownership remains with Blue Jungle Inc,
//All rights reserved worldwide.

package com.bluejungle.destiny.agent.controlmanager;

/**
 * This is for SDK!!! 
 * IOSWrapper exposes methods that will be implemented using JNI to call native OS calls.
 * 
 * @version $Id:
 *  //depot/main/Destiny/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/IOSWrapper.java#1
 *  
 */
public interface IPDPJni {

    /**
     * Enumeration of the Standard Attributes 
     *
     */
    public static final String CE_ATTR_CREATE_TIME     = "CE_ATTR_CREATE_TIME";
    public static final String CE_ATTR_LASTACCESS_TIME = "CE_ATTR_LASTACCESS_TIME";
    public static final String CE_ATTR_LASTWRITE_TIME  = "CE_ATTR_LASTWRITE_TIME";
    public static final String CE_ATTR_OWNER_NAME      = "CE_ATTR_OWNER_NAME";
    public static final String CE_ATTR_OWNER_ID        = "CE_ATTR_OWNER_ID";
    public static final String CE_ATTR_GROUP_ID        = "CE_ATTR_GROUP_ID";
    
    /**
     * Sends a policy evaluation response back to PDP. This API is used
     * only for the PDP policy requests.
     * 
     * @param uniqueNumber
     *            policy evaluation request number passed by the pdp. It must
     *            be passed back with the same value.
     * @param allow
     *            set to DENY(0), ALLOW(1), or DONTCARE(2)
     * @param attrs
     *            resulted obligations. 
     * @return
     */
    public boolean SendPolicyResponse(long handle, String uniqueNumber, long allow, String[] attrs);

    /**
     * Sends a multi-query evaluation response back to PDP.
     *
     * @param uniqueNumber policy evaluation request numer send by the pdp.
     * @param responses an array of responses. Each response is an array of two values. A integer containing
     *        the evaluation result as an integer (0, 1, or 2, for ALLOW, DENY, DONTCARE) and the obligations
     *        as an array of strings
     */
    public boolean SendPolicyMultiResponse(long handle, String uniqueNumber, Object[] responses);

    /**
     * Sends the reply to a service request
     *
     * @param handle socket handle
     * @param reqId the unique request id, sent by the client
     * @param response the service response
     */
    public boolean SendServiceResponse(long handle, String reqId, Object[] response);
}
