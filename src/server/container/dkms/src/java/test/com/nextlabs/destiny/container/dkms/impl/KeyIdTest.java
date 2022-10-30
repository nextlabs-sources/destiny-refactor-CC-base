package com.nextlabs.destiny.container.dkms.impl;

import static org.junit.Assert.*;

import org.junit.Test;

public class KeyIdTest {
    private static final long DEFUALT_TIMESTAMP = 3L;

    @Test
    public void nullHash() {
        KeyId keyId = new KeyId(null, DEFUALT_TIMESTAMP);
        
        assertNull(keyId.getId());
        assertEquals(DEFUALT_TIMESTAMP, keyId.getCreationTimeStamp());
        
        // no NPE
        keyId.toString();
        keyId.hashCode();
    }
    
    @Test
    public void emptyHash() {
        KeyId keyId = new KeyId(new byte[] {}, DEFUALT_TIMESTAMP);
        
        assertNotNull(keyId.getId());
        assertEquals(DEFUALT_TIMESTAMP, keyId.getCreationTimeStamp());
        
        // no NPE
        keyId.toString();
        keyId.hashCode();
    }
    
    @Test
    public void getAndSet() {
        byte[] hash = new byte[] { 1, 2, 3 };
        KeyId keyId = new KeyId(hash, DEFUALT_TIMESTAMP);
        
        assertArrayEquals(hash, keyId.getId());
        assertEquals(DEFUALT_TIMESTAMP, keyId.getCreationTimeStamp());
        
        
        long ts = 1L;
        keyId = new KeyId(hash, ts);
        
        assertArrayEquals(hash, keyId.getId());
        assertEquals(ts, keyId.getCreationTimeStamp());
    }
    
    @Test
    public void invalidTs() {
        byte[] hash = new byte[] { 1, 2, 3 };
        long ts = -123L;
        KeyId keyId = new KeyId(hash, ts);
        
        assertEquals(ts, keyId.getCreationTimeStamp());
    }
    
    @Test
    public void equalAndHashSameKeyTs() {
        byte[] hash = new byte[] { 1, 2, 3 };
        long ts = 123L;
        KeyId keyId1 = new KeyId(hash, ts);
        KeyId keyId2 = new KeyId(hash, ts);
        assertNotSame(keyId1, keyId2);
        
        assertTrue(keyId1.equals(keyId1));
        assertTrue(keyId1.equals(keyId2));
        assertTrue(keyId2.equals(keyId1));
     
        assertEquals(keyId1.hashCode(), keyId2.hashCode());
    }
    
    @Test
    public void equalAndHashSameKey() {
        byte[] hash = new byte[] { 1, 2, 3 };
        KeyId keyId1 = new KeyId(hash, 123L);
        KeyId keyId2 = new KeyId(hash, 456L);
        assertNotSame(keyId1, keyId2);
        
        assertFalse(keyId1.equals(keyId2));
        assertFalse(keyId2.equals(keyId1));
     
        assertFalse(keyId1.hashCode() == keyId2.hashCode());
    }

    @Test
    public void equalAndHashSameTs() {
        long ts = 123L;
        KeyId keyId1 = new KeyId(new byte[] { 1, 2, 3 }, ts);
        KeyId keyId2 = new KeyId(new byte[] { 4, 5, 6 }, ts);
        assertNotSame(keyId1, keyId2);
        
        assertFalse(keyId1.equals(keyId2));
        assertFalse(keyId2.equals(keyId1));
     
        assertFalse(keyId1.hashCode() == keyId2.hashCode());
    }
}
