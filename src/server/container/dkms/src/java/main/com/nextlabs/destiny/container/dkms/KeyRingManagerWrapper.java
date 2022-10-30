package com.nextlabs.destiny.container.dkms;

import java.io.Closeable;
import java.io.IOException;
import java.security.KeyStore.PasswordProtection;

import javax.security.auth.DestroyFailedException;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.comp.PropertyKey;
import com.nextlabs.destiny.container.dkms.impl.KeyRingManager;
public class KeyRingManagerWrapper implements 
        IHasComponentInfo<KeyRingManagerWrapper>
      , IConfigurable
      , IManagerEnabled
      , Closeable
{

    public static final PropertyKey<PasswordProtection> PROPERY_KEY_PASSWORD = 
            new PropertyKey<PasswordProtection>(KeyRingManagerWrapper.class, "keystorePassword");
            
    public static final ComponentInfo<KeyRingManagerWrapper> COMP_INFO =
            new ComponentInfo<KeyRingManagerWrapper>(
                    KeyRingManagerWrapper.class
                  , LifestyleType.SINGLETON_TYPE
    );
    
    private PasswordProtection password;
    private IComponentManager compMgr;
    
    @Override
    public ComponentInfo<KeyRingManagerWrapper> getComponentInfo() {
        return COMP_INFO;
    }
    
    IKeyRing createKeyRing(String name) throws KeyManagementException {
        IKeyRingManager keyRingManager = compMgr.getComponent(KeyRingManager.class);
        return keyRingManager.createKeyRing(name, new PasswordProtection(password.getPassword()));
    }

    IKeyRing getKeyRing(String name) throws KeyManagementException {
        IKeyRingManager keyRingManager = compMgr.getComponent(KeyRingManager.class);
        IKeyRing keyRing = keyRingManager.getKeyRing(name, new PasswordProtection(password.getPassword()));
        return keyRing;
    }
        
    @Override
    public void setConfiguration(IConfiguration config) {
        password = config.get(PROPERY_KEY_PASSWORD);
    }

    @Override
    public IConfiguration getConfiguration() {
        return null;
    }

    @Override
    public void close() throws IOException {
        if (password != null) {
            try {
                password.destroy();
            } catch (DestroyFailedException e) {
                throw new IOException(e);
            }
            password = null;
        }
    }
    
    @Override
    public void setManager(IComponentManager manager) {
        this.compMgr = manager;
    }

    @Override
    public IComponentManager getManager() {
        return compMgr;
    }
    
}
