package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 20, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/embeddedapi/com/bluejungle/destiny/agent/pdpapi/PDPEnforcement.java#1 $:
 */

/**
 * The result of a policy evaluation. Includes the evaluation result (e.g. allow or deny) and any obligations
 */
public class PDPEnforcement implements IPDPEnforcement {
    private final String result;
    private final String[] obligations;

    /**
     * Create a new PDPEnforcement object
     * @param result the result
     * @param obligations the obligations. Use an empty array in preference to null if there are no obligations
     */
    public PDPEnforcement(String result, String[] obligations) {
        this.result = result;
        this.obligations = obligations;
    }

    /**
     * The result of the policy evaluation.
     * @return Possible results are "allow", "deny", "dontcare".
     */
    public String getResult() {
        return result;
    }

    /**
     * The obligations that must be performed by the PEP as a result of this evaluation
     * @return An array of <code>String</code> describing the obligation names and arguments. Consult NextLabs documentation for
     * details
     */
    public String[] getObligations() {
        return obligations;
    }
}
