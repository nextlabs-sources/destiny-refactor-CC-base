package com.nextlabs.destiny.container.dkms.impl;

import static org.junit.Assert.*;

import java.security.KeyStore.PasswordProtection;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.StaleObjectStateException;

import org.junit.Before;
import org.junit.Test;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.nextlabs.destiny.container.dkms.DKMSComponentTestBase;
import com.nextlabs.destiny.container.dkms.IKey;
import com.nextlabs.destiny.container.dkms.IKeyRing;
import com.nextlabs.destiny.container.dkms.IKeyRingManager;
import com.nextlabs.destiny.container.dkms.KeyManagementException;

public class KeyRingTest extends DKMSComponentTestBase {
    
    private static final String TEST_KEY_RING_NAME = "test";
    
    private IKeyRingManager keyRingManager;
    private IKeyRing keyRing;

    @Before
    public void init() throws HibernateException, KeyManagementException {
        super.initAll();
        initDataSource();
        keyRingManager = ComponentManagerFactory.getComponentManager().getComponent(KeyRingManager.class);
        assertNotNull(keyRingManager);
        
        
        keyRingManager.deleteKeyRing(TEST_KEY_RING_NAME);
        
        keyRing = keyRingManager.createKeyRing(TEST_KEY_RING_NAME, getPassword());
        assertNotNull(keyRing);
    }
    
    private PasswordProtection getPassword() {
        return new PasswordProtection("password".toCharArray());
    }
    
    @Test
    public void concurrentUpdate() throws KeyManagementException {
        
        IKeyRing keyRing2 = keyRingManager.getKeyRing(TEST_KEY_RING_NAME, getPassword());
        assertNotNull(keyRing2);
        
        assertNotSame(keyRing, keyRing2);
        assertNotSame(keyRing.getKeyRingDO(), keyRing2.getKeyRingDO());
        
        byte[] key1 = "k1".getBytes();
        byte[] value1 = "v1".getBytes();
        
        keyRing.setKey(new Key(new KeyId(key1, System.currentTimeMillis()), value1));
        
        byte[] key2 = "k2".getBytes();
        byte[] value2 = "v2".getBytes();
        
        try {
            keyRing2.setKey(new Key(new KeyId(key2, System.currentTimeMillis()), value2));
            fail();
        } catch (KeyManagementException e) {
            assertEquals(StaleObjectStateException.class, e.getCause().getClass());
        }
    }
    
    @Test
    public void setget() throws KeyManagementException {
        byte[] keyIdB = "k-setget".getBytes();
        byte[] value = "v-setget".getBytes();
        long ts = System.currentTimeMillis();
        
        KeyId keyId = new KeyId(keyIdB, ts);
        Key key = new Key(keyId, value);
        
        keyRing.setKey(key);
        IKey aKey = keyRing.getKey(keyId);
        
        assertArrayEquals(keyIdB, aKey.getKeyId().getId());
        assertEquals(ts, aKey.getKeyId().getCreationTimeStamp());
        assertArrayEquals(value, aKey.getEncoded());
    }
    
    @Test
    public void reopen() throws KeyManagementException {
        byte[] keyIdB = "k-reopen".getBytes();
        byte[] value = "v-reopen".getBytes();
        long ts = System.currentTimeMillis();
        
        KeyId keyId = new KeyId(keyIdB, ts);
        Key key = new Key(keyId, value);
        
        keyRing.setKey(key);
        keyRing.close();
        
        keyRing = keyRingManager.getKeyRing(TEST_KEY_RING_NAME, new PasswordProtection("password".toCharArray()));
        IKey aKey = keyRing.getKey(keyId);
        
        assertArrayEquals(keyIdB, aKey.getKeyId().getId());
        assertEquals(ts, aKey.getKeyId().getCreationTimeStamp());
        assertArrayEquals(value, aKey.getEncoded());
    }
}
