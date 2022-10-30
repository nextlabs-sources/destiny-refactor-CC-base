package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 18, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/embeddedapi/com/bluejungle/destiny/agent/pdpapi/IPDPResource.java#1 $:
 */

/**
 * The <code>IPDPResource</code> interface serves to identify interfaces or classes that contain resource
 * information and associated attributes
 */
public interface IPDPResource extends IPDPNamedAttributes
{
    public static final String REMOVABLEDEVICE="[removabledevice]";
    public static final String MYDOCUMENTS="[mydocuments]";
    public static final String MYDESKTOP="[mydesktop]";
}
