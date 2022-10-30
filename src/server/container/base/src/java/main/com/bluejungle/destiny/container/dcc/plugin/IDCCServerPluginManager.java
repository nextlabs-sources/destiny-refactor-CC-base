package com.bluejungle.destiny.container.dcc.plugin;

import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;

/*
 * Created on Dec 09, 2010
 *
 * All sources, binaries and HTML pages (C) copyright 2010 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/base/src/java/main/com/bluejungle/destiny/container/dcc/plugin/IDCCServerPluginManager.java#1 $:
 */

public interface IDCCServerPluginManager
{
    void initializePlugins(IRegisteredDCCComponent component);
}
