package com.nextlabs.destiny.container.dkms;

import java.security.KeyStore.PasswordProtection;

import com.bluejungle.destiny.container.dcc.BaseDCCComponentImpl;
import com.bluejungle.destiny.server.shared.configuration.IGenericComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.nextlabs.destiny.container.dkms.impl.KeyRingManager;
import com.nextlabs.destiny.container.shared.utils.DCCComponentHelper;

public class DKMSComponentImpl extends BaseDCCComponentImpl {
    @Override
    public ServerComponentType getComponentType() {
        return DKMSConstants.COMPONENT_TYPE;
    }
    
    @Override
    public void init() {
        super.init();
        
        final IComponentManager compMgr = getManager();
        
        DCCComponentHelper.initSecurityComponents(compMgr, getLog());
        
        final IDestinyConfigurationStore configMgr = getManager().getComponent(
                DestinyConfigurationStoreImpl.COMP_INFO);

        IGenericComponentConfigurationDO componentConfig = (IGenericComponentConfigurationDO) configMgr
                .retrieveComponentConfiguration(DKMSConstants.COMPONENT_TYPE_NAME);
        if (componentConfig == null) {
            throw new NullPointerException(
                    "Destiny Configuration is not definied for " + DKMSConstants.COMPONENT_TYPE_NAME);
        }
        KeyManagementRepository.create(compMgr, componentConfig.getProperties());
        
        String encodedPassword = (String)componentConfig.getProperties().get(DKMSConstants.PROPERTY_KEYSTORE_PASSWORD);
        if (encodedPassword == null) {
            throw new NullPointerException(
                    "'" + DKMSConstants.PROPERTY_KEYSTORE_PASSWORD + "' is not definied.");
        }
        
        String password = new ReversibleEncryptor().decrypt(encodedPassword);
        initKeyRingManager(new PasswordProtection(password.toCharArray()));
    }
    
    private void initKeyRingManager(PasswordProtection keystorePassword) {
        final IComponentManager compMgr = getManager();
        
        compMgr.getComponent(KeyRingManager.class);
        
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(KeyRingManagerWrapper.PROPERY_KEY_PASSWORD, keystorePassword);
        compMgr.getComponent(KeyRingManagerWrapper.COMP_INFO, config);
    }
}
