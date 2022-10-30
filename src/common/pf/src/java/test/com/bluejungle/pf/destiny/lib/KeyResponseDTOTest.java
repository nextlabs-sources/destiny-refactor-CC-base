package com.bluejungle.pf.destiny.lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.bluejungle.pf.destiny.lib.KeyResponseDTO.KeyDTO;
import com.bluejungle.pf.destiny.lib.KeyResponseDTO.KeyRingDTO;

import junit.framework.TestCase;

public class KeyResponseDTOTest extends TestCase{
    
    public void testNull() throws Exception{
        KeyResponseDTO r = new KeyResponseDTO();
        KeyResponseDTO r2 = reread(r);
        
        List<KeyRingDTO> keyrings = r2.getKeyRings();
        assertNotNull(keyrings);
        assertEquals(0, keyrings.size());
    }
    
    private KeyResponseDTO reread(KeyResponseDTO r) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        r.writeExternal(oos);
        oos.flush();
        
        byte[] data = baos.toByteArray();
        
        oos.close();
        baos.flush();
        baos.close();
        
        
        
        assertNotNull(data);
        assertNotNull(data.length > 0);
        System.out.println("HKC:" + data.length);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        
        ObjectInput ooi = new ObjectInputStream(bais);
        KeyResponseDTO r2 = new KeyResponseDTO();
        r2.readExternal(ooi);
        ooi.close();
        bais.close();
        
        return r2;
    }
    
    public void testEmpty() throws Exception{
        KeyResponseDTO r = new KeyResponseDTO();
        r.setKeyRings(Collections.EMPTY_LIST);
        KeyResponseDTO r2 = reread(r);
        
        List<KeyRingDTO> keyrings = r2.getKeyRings();
        assertNotNull(keyrings);
        assertEquals(0, keyrings.size());
    }
    
    public void testOneKeyringNullKey() throws Exception{
        KeyResponseDTO r = new KeyResponseDTO();
        
        List<KeyRingDTO> keyRings = new ArrayList<KeyRingDTO>();
        keyRings.add(new KeyRingDTO("kr1", 99, null));
        r.setKeyRings(keyRings);
        try {
            reread(r);
            fail();
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }
    
    public void testOneKeyringNoKey() throws Exception{
        KeyResponseDTO r = new KeyResponseDTO();
        
        List<KeyRingDTO> keyRings = new ArrayList<KeyRingDTO>();
        keyRings.add(new KeyRingDTO("kr1", 99, Collections.EMPTY_LIST));
        r.setKeyRings(keyRings);
        KeyResponseDTO r2 = reread(r);
        
        keyRings = r2.getKeyRings();
        assertNotNull(keyRings);
        assertEquals(1, keyRings.size());
        
        KeyRingDTO keyring = keyRings.get(0);
        
        assertEquals("kr1", keyring.getName());
        assertEquals(99,    keyring.getTimestamp());
        assertEquals(0,     keyring.getKeys().size());
    }
    
    public void testOneKeyringOneKey() throws Exception{
        KeyResponseDTO r = new KeyResponseDTO();
        
        KeyDTO key1 = new KeyDTO(2, 4, new byte[] { 6, 2, 1, 12 },
                new byte[] { 5, 9 });
        
        List<KeyRingDTO> keyRings = new ArrayList<KeyRingDTO>();
        keyRings.add(new KeyRingDTO("kr1", 98, Collections.singletonList(key1)));
        r.setKeyRings(keyRings);
        KeyResponseDTO r2 = reread(r);
        
        keyRings = r2.getKeyRings();
        assertNotNull(keyRings);
        assertEquals(1, keyRings.size());
        
        KeyRingDTO keyring = keyRings.get(0);
        
        assertEquals("kr1", keyring.getName());
        assertEquals(98,    keyring.getTimestamp());
        assertEquals(1,     keyring.getKeys().size());
        
        KeyDTO key1b = keyring.getKeys().get(0);
        
        assertEquals(key1.getTimestamp(), key1b.getTimestamp());
        assertEquals(key1.getVersion(), key1b.getVersion());
        assertTrue(Arrays.equals(key1.getId(), key1b.getId()));
        assertTrue(Arrays.equals(key1.getKey(), key1b.getKey()));
    }
    
    public void test2Keyring2Key() throws Exception{
        KeyResponseDTO r = new KeyResponseDTO();
        
        KeyDTO key1a = new KeyDTO(2, 4, new byte[] { 6, 2, 1, 12 },
                new byte[] { 5, 9 });
        
        KeyDTO key1b = new KeyDTO(12, 14, new byte[] { 16, 12, 11, 112 },
                new byte[] { 15, 19 });
        
        KeyDTO key2a = new KeyDTO(22, 24, new byte[] { 26, 22, 21, -112 },
                new byte[] { 25, 29 });
        
        KeyDTO key2b = new KeyDTO(32, 34, new byte[] { 36, 32, 31, -12 },
                new byte[] { 35, 39 });
        
        List<KeyRingDTO> keyRings = new ArrayList<KeyRingDTO>();
        keyRings.add(new KeyRingDTO("kr1", 98, Arrays.asList(key1a, key1b)));
        keyRings.add(new KeyRingDTO("kr2", 96, Arrays.asList(key2a, key2b)));
        r.setKeyRings(keyRings);
        KeyResponseDTO r2 = reread(r);
        
        keyRings = r2.getKeyRings();
        assertNotNull(keyRings);
        assertEquals(2, keyRings.size());
        
        KeyRingDTO keyring = keyRings.get(0);
        
        assertEquals("kr1", keyring.getName());
        assertEquals(98,    keyring.getTimestamp());
        assertEquals(2,     keyring.getKeys().size());
        
//        KeyDTO key1b = keyring.getKeys().get(0);
//        
//        assertEquals(key1.getTimestamp(), key1b.getTimestamp());
//        assertEquals(key1.getVersion(), key1b.getVersion());
//        assertTrue(Arrays.equals(key1.getId(), key1b.getId()));
//        assertTrue(Arrays.equals(key1.getKey(), key1b.getKey()));
    }
    
    public void testKeyring100Keys() throws IOException {
        
        final byte[] keyId = new byte[] { 2, -1, 4, 4, 12, 23,-12 };
        final byte[] keyBytes = new byte[] { -5, -2 };
        
        KeyResponseDTO r = new KeyResponseDTO();
        final int numberOfKeys = 100;
        System.out.println("numberOfKeys=" + numberOfKeys);
        
        List<KeyDTO> keys = new LinkedList<KeyDTO>();
        for (int i = 0; i < numberOfKeys; i++) {
            KeyDTO key = new KeyDTO(2, 4, keyBytes, keyId);
            keys.add(key);
        }
        
        List<KeyRingDTO> keyRings = new ArrayList<KeyRingDTO>();
        keyRings.add(new KeyRingDTO("kr1", 98, keys));
        r.setKeyRings(keyRings);
        KeyResponseDTO r2 = reread(r);
        
        keyRings = r2.getKeyRings();
        assertNotNull(keyRings);
        assertEquals(1, keyRings.size());
        
        KeyRingDTO keyring = keyRings.get(0);
        
        assertEquals("kr1", keyring.getName());
        assertEquals(98,    keyring.getTimestamp());
        assertEquals(numberOfKeys,     keyring.getKeys().size());
        
        for(KeyDTO key : keyring.getKeys()){
            assertEquals(2, key.getVersion());
            assertEquals(4, key.getTimestamp());
            assertTrue(Arrays.equals(keyId, key.getId()));
            assertTrue(Arrays.equals(keyBytes, key.getKey()));
            
        }
    }
    
}
