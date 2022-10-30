/*
 * Created on Jul 20, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dpc/src/java/main/com/nextlabs/destiny/container/dpc/DPCContextListener.java#1 $:
 */

package com.nextlabs.destiny.container.dpc;

import com.bluejungle.destiny.container.dcc.DCCContextListener;
import com.bluejungle.destiny.container.dcc.IDCCContainer;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;

public class DPCContextListener extends DCCContextListener {
    @Override
    public ServerComponentType getComponentType() {
        return DPCComponentImpl.COMPONENT_TYPE;
    }

    @Override
    protected Class<? extends IDCCContainer> getContainerClassName() {
        return DPCContainerImpl.class;
    }

    @Override
    public String getTypeDisplayName() {
        return "Policy Controller";
    }
}
