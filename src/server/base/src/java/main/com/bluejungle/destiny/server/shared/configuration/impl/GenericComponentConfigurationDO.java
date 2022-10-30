package com.bluejungle.destiny.server.shared.configuration.impl;

import com.bluejungle.destiny.server.shared.configuration.IGenericComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;

public class GenericComponentConfigurationDO extends DCCComponentConfigurationDO implements IGenericComponentConfigurationDO {
    private ServerComponentType componentType;

    @Override
    public ServerComponentType getComponentType() {
        return componentType;
    }
    
    public void setName(String componentType){
        this.componentType = ServerComponentType.fromString(componentType);
    }
    
}
