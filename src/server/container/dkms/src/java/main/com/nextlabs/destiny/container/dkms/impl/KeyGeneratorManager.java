package com.nextlabs.destiny.container.dkms.impl;

import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;
import com.nextlabs.destiny.container.dkms.IKeyGeneratorManager;
import com.nextlabs.destiny.container.dkms.KeyManagementException;

public class KeyGeneratorManager implements IKeyGeneratorManager, IHasComponentInfo<IKeyGeneratorManager>{
    
    private static final ComponentInfo<IKeyGeneratorManager> COMP_INFO = 
        new ComponentInfo<IKeyGeneratorManager>(
            KeyGeneratorManager.class
          , LifestyleType.SINGLETON_TYPE
        );

    private static final Log LOG = LogFactory.getLog(KeyGeneratorManager.class);
    
    private static final String DEFAULT_KEY_GEN_ALGORITHM = "AES";
    
    private final Map<Integer, KeyGenerator> keyGeneratorCache = new HashMap<Integer, KeyGenerator>(1);
    
    
    @Override
    public ComponentInfo<IKeyGeneratorManager> getComponentInfo() {
        return COMP_INFO;
    }
    
    /**
     * return AES key and generate by using SHA1PRNG
     */
    public byte[] generateKey(int length) throws KeyManagementException {
        
        LOG.debug("generateKey with " + length + " bits");
        
        KeyGenerator keyGen = keyGeneratorCache.get(length);

        if (keyGen == null) {
            synchronized (keyGeneratorCache) {
                if (keyGen == null) {
                    keyGen = createKeyGenerator(length);
                    keyGeneratorCache.put(length, keyGen);
                }
            }
        }
        return keyGen.generateKey().getEncoded();
    }
    
    
    private KeyGenerator createKeyGenerator(int length) throws KeyManagementException {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(DEFAULT_KEY_GEN_ALGORITHM);

            //This is an algorithm-specific metric, specified in number of bits. 
            keyGen.init(length);

            return keyGen;
        } catch (NoSuchAlgorithmException e) {
            throw new KeyManagementException(e);
        } catch (InvalidParameterException e) {
            throw new KeyManagementException(e);
        }
    }

    
}
