package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.LinkedList;
import java.util.List;

import com.bluejungle.destiny.server.shared.configuration.IGenericComponentConfigurationDO;

public class GenericComponentsConfigurationDO {
    private List<IGenericComponentConfigurationDO> genericComponents;
    
    public GenericComponentsConfigurationDO() {
        genericComponents = new LinkedList<IGenericComponentConfigurationDO>();
    }
    
    public List<IGenericComponentConfigurationDO> getGenericComponents(){
        return genericComponents;
    }
    
    public void addGenericComponent(IGenericComponentConfigurationDO config) {
        genericComponents.add(config);
    }
}
