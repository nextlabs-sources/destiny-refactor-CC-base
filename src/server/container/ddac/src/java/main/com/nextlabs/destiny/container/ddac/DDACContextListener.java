/*
 * Created on Jul 26, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/ddac/src/java/main/com/nextlabs/destiny/container/ddac/DDACContextListener.java#1 $:
 */

package com.nextlabs.destiny.container.ddac;

import com.bluejungle.destiny.container.dcc.DCCContextListener;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;

public class DDACContextListener extends DCCContextListener {
    @Override
    public ServerComponentType getComponentType() {
        return DDACComponentImpl.COMPONENT_TYPE;
    }

    @Override
    public String getTypeDisplayName() {
        return "Dynamic Access Control";
    }
}
