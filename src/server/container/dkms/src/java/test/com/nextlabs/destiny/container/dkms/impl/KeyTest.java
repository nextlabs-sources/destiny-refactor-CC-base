package com.nextlabs.destiny.container.dkms.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class KeyTest {
    
    @Test(expected = NullPointerException.class)
    public void nullKey() {
        new Key(new KeyId(new byte[]{1}, 2), 3, null);
    }
    
    @Test
    public void equalAndHash() {
        Key key1 = new Key(new KeyId(new byte[] { 1 }, 2), 3, new byte[] { 4 });
        Key key2 = new Key(new KeyId(new byte[] { 1 }, 2), 3, new byte[] { 4 });

        assertNotSame(key1, key2);

        assertTrue(key1.equals(key1));
        assertTrue(key1.equals(key2));
        assertTrue(key2.equals(key1));

        assertEquals(key1.hashCode(), key2.hashCode());
    }
}
