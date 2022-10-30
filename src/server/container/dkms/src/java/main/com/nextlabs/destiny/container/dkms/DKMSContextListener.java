package com.nextlabs.destiny.container.dkms;

import com.bluejungle.destiny.container.dcc.DCCContextListener;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;

public class DKMSContextListener extends DCCContextListener {
    
    
   
    @Override
    public ServerComponentType getComponentType() {
        return DKMSConstants.COMPONENT_TYPE;
    }

    @Override
    public String getTypeDisplayName() {
        return "Key Management Server";
    }

}
