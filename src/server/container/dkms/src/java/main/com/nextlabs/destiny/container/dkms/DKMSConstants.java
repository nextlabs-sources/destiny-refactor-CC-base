package com.nextlabs.destiny.container.dkms;

import com.bluejungle.destiny.server.shared.registration.ServerComponentType;

public final class DKMSConstants {
    
   
    
    public static final String COMPONENT_TYPE_NAME = "DKMS";
    
    public static final ServerComponentType COMPONENT_TYPE = 
        ServerComponentType.fromString(DKMSConstants.COMPONENT_TYPE_NAME);
    
    public static final String DATABASE_CONFIG_PROPERTY_PREFIX = "keydb.";
    
    public static final String DATABASE_HIBERNATE_CONFIG_PROERTY_PREFIX = DATABASE_CONFIG_PROPERTY_PREFIX + "hibernate.";
    
    public static final String PROPERTY_KEYSTORE_PASSWORD = "keystore.password";
    
    
    
    private DKMSConstants() {
    }
    
}
