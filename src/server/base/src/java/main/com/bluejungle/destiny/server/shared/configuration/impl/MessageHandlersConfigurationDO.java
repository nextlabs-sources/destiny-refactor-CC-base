/*
 * Created on Jul 21, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.LinkedList;
import java.util.List;

import com.bluejungle.destiny.server.shared.configuration.IMessageHandlerConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IMessageHandlersConfigurationDO;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/impl/MessageHandlersConfigurationDO.java#1 $
 */

public class MessageHandlersConfigurationDO implements IMessageHandlersConfigurationDO {
    private List<IMessageHandlerConfigurationDO> handlerConfigs;
    
    public MessageHandlersConfigurationDO(){
        handlerConfigs = new LinkedList<IMessageHandlerConfigurationDO>();
    }
    
    /* (non-Javadoc)
     * @see com.bluejungle.destiny.server.shared.configuration.impl.IMessageHandlersConfigDO#getHandlerConfigs()
     */
    public List<IMessageHandlerConfigurationDO> getHandlerConfigs() {
        return handlerConfigs;
    }
    
    public void addMessageHandlerConfiguration(IMessageHandlerConfigurationDO config){
        handlerConfigs.add(config);
    }
}
