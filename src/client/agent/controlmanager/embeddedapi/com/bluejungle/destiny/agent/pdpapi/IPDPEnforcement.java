package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 19, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/embeddedapi/com/bluejungle/destiny/agent/pdpapi/IPDPEnforcement.java#1 $:
 */

/**
 * The result of a PDP evaluation. The user of this interface can get the result (as a string) and the obligations (if they exist)
 */
public interface IPDPEnforcement
{
    /**
     * The result of the policy evaluation.
     * @return Possible results are "allow", "deny", "dontcare".
     */
    public String getResult();

    /**
     * The obligations that must be performed by the PEP as a result of this evaluation
     * @return An array of <code>String</code> describing the obligation names and arguments. Consult NextLabs documentation for
     * details
     */
    public String[] getObligations();
}
