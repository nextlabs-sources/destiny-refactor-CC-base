package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 19, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/embeddedapi/com/bluejungle/destiny/agent/pdpapi/IPDPSDKCallback.java#1 $:
 */

/**
 * A callback for the <code>PDPQueryDecisionEngine</code>
 */
public interface IPDPSDKCallback
{
    /**
     * An empty callback, which will force a synchronous call.
     */
    public final static IPDPSDKCallback NONE = new IPDPSDKCallback () {
        public void callback(IPDPEnforcement ignore) { }
    };

    /**
     * Invoked by the evaluation code when the policy evaluation has completed
     * @param result the enforcement result
     */
    public void callback(IPDPEnforcement result);
}
