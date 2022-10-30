package com.bluejungle.destiny.server.shared.configuration;

import com.bluejungle.destiny.server.shared.registration.ServerComponentType;

public interface IGenericComponentConfigurationDO extends IDCCComponentConfigurationDO {
    
    ServerComponentType getComponentType();
    
}
