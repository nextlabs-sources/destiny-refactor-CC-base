package com.bluejungle.destiny.container.dcc.plugin;

/*
 * Created on Dec 08, 2010
 *
 * All sources, binaries and HTML pages (C) copyright 2010 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/base/src/java/main/com/bluejungle/destiny/container/dcc/plugin/IDCCServerPlugin.java#1 $:
 */

import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;

public interface IDCCServerPlugin
{
    void init(IRegisteredDCCComponent component);
}

