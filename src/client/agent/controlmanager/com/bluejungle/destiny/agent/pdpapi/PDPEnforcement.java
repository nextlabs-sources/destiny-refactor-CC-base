package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 20, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/pdpapi/PDPEnforcement.java#1 $:
 */

public class PDPEnforcement implements IPDPEnforcement {
    private final String result;
    private final String[] obligations;

    public PDPEnforcement(String result, String[] obligations) {
        this.result = result;
        this.obligations = obligations;
    }

    public String getResult() {
        return result;
    }

    public String[] getObligations() {
        return obligations;
    }
}
