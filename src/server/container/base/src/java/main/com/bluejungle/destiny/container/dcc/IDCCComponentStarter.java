/*
 * Created on Dec 6, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcc;

import com.bluejungle.destiny.server.shared.registration.IDMSRegistrationListener;
import com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.PropertyKey;

/**
 * This is the DCC component starter. The DCC component starter is notified when
 * the DMS registration is complete. Its job is to create an instance of the DCC
 * component and start it.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcc/IDCCComponentStarter.java#5 $:
 */

public interface IDCCComponentStarter extends IDMSRegistrationListener {
    String COMP_NAME = "DCCComponentStarter";
    
    PropertyKey<Class<? extends IRegisteredDCCComponent>> DCC_COMPONENT_CLASSNAME_CONFIG_PARAM =
            new PropertyKey<Class<? extends IRegisteredDCCComponent>>("ComponentClassName");
    
    PropertyKey<String> DCC_COMPONENT_NAME_CONFIG_PARAM = 
            new PropertyKey<String>("ComponentName");
    
    PropertyKey<ServerComponentType> DCC_COMPONENT_TYPE_CONFIG_PARAM =
            new PropertyKey<ServerComponentType>("ComponentType");
    
    PropertyKey<Class<? extends IHeartbeatMgr>> HEARTBEAT_MGR_CLASSNAME_CONFIG_PARAM =
            new PropertyKey<Class<? extends IHeartbeatMgr>>("HeartbeatMgrClassName");
    
    PropertyKey<Object> LOCK_OBJECT_CONFIG_PARAM = 
            new PropertyKey<Object>("LockObject");
}
