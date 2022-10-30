package com.bluejungle.destiny.container.dabs;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.pf.destiny.lib.KeyRequestDTO;
import com.bluejungle.pf.destiny.lib.KeyResponseDTO;
import com.nextlabs.destiny.services.keymanagement.KeyManagementDCCIF;
import com.nextlabs.destiny.services.keymanagement.types.KeyDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyIdDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyManagementFault;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingNamesDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingWithKeysDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingsWithKeysDTO;

import junit.framework.TestCase;

public class ServerKeyProviderTest extends TestCase {

    private class KeyManagementDCCIFMock implements KeyManagementDCCIF {

        @Override
        public KeyRingDTO createKeyRing(String keyRingName)
                throws RemoteException, UnauthorizedCallerFault,
                ServiceNotReadyFault, KeyManagementFault {
            throw new UnsupportedOperationException();
        }

        @Override
        public KeyRingNamesDTO getKeyRingNames() throws RemoteException,
                UnauthorizedCallerFault, ServiceNotReadyFault,
                KeyManagementFault {
            throw new UnsupportedOperationException();
        }

        @Override
        public KeyRingDTO getKeyRing(String keyRingName)
                throws RemoteException, UnauthorizedCallerFault,
                ServiceNotReadyFault, KeyManagementFault {
            throw new UnsupportedOperationException();
        }

        @Override
        public KeyRingWithKeysDTO getKeyRingWithKeys(String keyRingName)
                throws RemoteException, UnauthorizedCallerFault,
                ServiceNotReadyFault, KeyManagementFault {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteKeyRing(String keyRingName) throws RemoteException,
                UnauthorizedCallerFault, ServiceNotReadyFault,
                KeyManagementFault {
            throw new UnsupportedOperationException();
        }

        @Override
        public KeyIdDTO generateKey(String keyRingName, int keyLength)
                throws RemoteException, UnauthorizedCallerFault,
                ServiceNotReadyFault, KeyManagementFault {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setKey(String keyRingName, KeyDTO key)
                throws RemoteException, UnauthorizedCallerFault,
                ServiceNotReadyFault, KeyManagementFault {
            throw new UnsupportedOperationException();
        }

        @Override
        public KeyDTO getKey(String keyRingName, KeyIdDTO keyId)
                throws RemoteException, UnauthorizedCallerFault,
                ServiceNotReadyFault, KeyManagementFault {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteKey(String keyRingName, KeyIdDTO keyId)
                throws RemoteException, UnauthorizedCallerFault,
                ServiceNotReadyFault, KeyManagementFault {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getAllLatestModifiedDate() throws RemoteException,
                UnauthorizedCallerFault, ServiceNotReadyFault,
                KeyManagementFault {
            throw new UnsupportedOperationException();
        }

        @Override
        public KeyRingsWithKeysDTO getAllKeyRingsWithKeys()
                throws RemoteException, UnauthorizedCallerFault,
                ServiceNotReadyFault, KeyManagementFault {
            throw new UnsupportedOperationException();
        }
        
    }
    
    private class ServerKeyProviderMock extends ServerKeyProvider {

        KeyManagementDCCIF kmWS;
        
        public ServerKeyProviderMock(KeyManagementDCCIF kmWS) {
            this.kmWS = kmWS;
        }
        
        @Override
        KeyManagementDCCIF getKeyManagementIF() {
            return kmWS;
        }
    }
    
    private final Random r = new Random();
    
//    private void setLatestCacheUpdate(long value)
//            throws IllegalArgumentException, IllegalAccessException,
//            SecurityException, NoSuchFieldException {
//        Field field = ServerKeyProvider.class.getDeclaredField("latestCacheUpdate");
//        field.setAccessible(true);
//        field.set(null, value);
//    }
    
//    @Override
//    protected void setUp() throws Exception {
//        super.setUp();
//        setLatestCacheUpdate(0);
//    }
    
    public void testMismatchProviderName() {
        ServerKeyProviderMock skp = new ServerKeyProviderMock(new KeyManagementDCCIFMock());
        Serializable response = skp.serviceHeartbeatRequest(KeyRequestDTO.SERVICE_NAME + "ABC", new KeyRequestDTO());
        assertNull(response);
    }
    
    public void testMismatchRequestData() {
        ServerKeyProviderMock skp = new ServerKeyProviderMock(new KeyManagementDCCIFMock());
        Serializable response = skp.serviceHeartbeatRequest(KeyRequestDTO.SERVICE_NAME, "ABC");
        assertNull(response);
    }
    
    public void testBadConnection() {
        ServerKeyProviderMock skp = new ServerKeyProviderMock(new KeyManagementDCCIFMock(){
            @Override
            public long getAllLatestModifiedDate() throws RemoteException,
                    UnauthorizedCallerFault, ServiceNotReadyFault,
                    KeyManagementFault {
                throw new RemoteException();
            }
        });
        
        Serializable response = skp.serviceHeartbeatRequest(KeyRequestDTO.SERVICE_NAME, new KeyRequestDTO());
        assertNotNull(response);
        assertTrue(response instanceof KeyResponseDTO);
        assertTrue(((KeyResponseDTO)response).getKeyRings().isEmpty());
    }
    
    public void testBadGetKeyRings() {
        ServerKeyProviderMock skp = new ServerKeyProviderMock(new KeyManagementDCCIFMock(){
            @Override
            public long getAllLatestModifiedDate() throws RemoteException,
                    UnauthorizedCallerFault, ServiceNotReadyFault,
                    KeyManagementFault {
                return 1L;
            }

            @Override
            public KeyRingsWithKeysDTO getAllKeyRingsWithKeys()
                    throws RemoteException, UnauthorizedCallerFault,
                    ServiceNotReadyFault, KeyManagementFault {
                throw new RemoteException();
            }
        });
        
        Serializable response = skp.serviceHeartbeatRequest(KeyRequestDTO.SERVICE_NAME, new KeyRequestDTO());
        assertNotNull(response);
        assertTrue(response instanceof KeyResponseDTO);
        assertTrue(((KeyResponseDTO)response).getKeyRings().isEmpty());
    }
    
    public void testNullKeyRing() {
        ServerKeyProviderMock skp = new ServerKeyProviderMock(new KeyManagementDCCIFMock(){
            @Override
            public long getAllLatestModifiedDate() throws RemoteException,
                    UnauthorizedCallerFault, ServiceNotReadyFault,
                    KeyManagementFault {
                return 1L;
            }

            @Override
            public KeyRingsWithKeysDTO getAllKeyRingsWithKeys()
                    throws RemoteException, UnauthorizedCallerFault,
                    ServiceNotReadyFault, KeyManagementFault {
                return null;
            }
        });
        
        Serializable response = skp.serviceHeartbeatRequest(KeyRequestDTO.SERVICE_NAME, new KeyRequestDTO());
        assertNotNull(response);
        assertTrue(response instanceof KeyResponseDTO);
        assertEquals(0, ((KeyResponseDTO)response).getKeyRings().size());
    }
    
    public void testEmptyKeyRing() {
        ServerKeyProviderMock skp = new ServerKeyProviderMock(new KeyManagementDCCIFMock(){
            @Override
            public long getAllLatestModifiedDate() throws RemoteException,
                    UnauthorizedCallerFault, ServiceNotReadyFault,
                    KeyManagementFault {
                return 1L;
            }

            @Override
            public KeyRingsWithKeysDTO getAllKeyRingsWithKeys()
                    throws RemoteException, UnauthorizedCallerFault,
                    ServiceNotReadyFault, KeyManagementFault {
                return new KeyRingsWithKeysDTO();
            }
        });
        
        Serializable response = skp.serviceHeartbeatRequest(KeyRequestDTO.SERVICE_NAME, new KeyRequestDTO());
        assertNotNull(response);
        assertTrue(response instanceof KeyResponseDTO);
        assertEquals(0, ((KeyResponseDTO)response).getKeyRings().size());
    }
    
    public void testOneKeyRingNullKey() {
        final String keyRingName = "keyRing" + r.nextInt();
        
        ServerKeyProviderMock skp = new ServerKeyProviderMock(new KeyManagementDCCIFMock(){
            @Override
            public long getAllLatestModifiedDate() throws RemoteException,
                    UnauthorizedCallerFault, ServiceNotReadyFault,
                    KeyManagementFault {
                return 100L;
            }

            @Override
            public KeyRingsWithKeysDTO getAllKeyRingsWithKeys()
                    throws RemoteException, UnauthorizedCallerFault,
                    ServiceNotReadyFault, KeyManagementFault {
                
                KeyRingWithKeysDTO keyRing = new KeyRingWithKeysDTO(keyRingName, null, 20L, 100L);
                KeyRingsWithKeysDTO keyRings = new KeyRingsWithKeysDTO(new KeyRingWithKeysDTO[]{keyRing});
                return keyRings;
            }
        });
        
        Serializable response = skp.serviceHeartbeatRequest(KeyRequestDTO.SERVICE_NAME, new KeyRequestDTO());
        assertNotNull(response);
        assertTrue(response instanceof KeyResponseDTO);
        List<com.bluejungle.pf.destiny.lib.KeyResponseDTO.KeyRingDTO> keyRings = ((KeyResponseDTO)response).getKeyRings();
        assertEquals(1, keyRings.size());
        
        assertEquals(keyRingName, keyRings.get(0).getName());
        assertEquals(100L, keyRings.get(0).getTimestamp());
        assertEquals(0, keyRings.get(0).getKeys().size());
    }
    
    public void testOneKeyRingEmptyKey() {
        final String keyRingName = "keyRing" + r.nextInt();
        
        ServerKeyProviderMock skp = new ServerKeyProviderMock(new KeyManagementDCCIFMock(){
            @Override
            public long getAllLatestModifiedDate() throws RemoteException,
                    UnauthorizedCallerFault, ServiceNotReadyFault,
                    KeyManagementFault {
                return 100L;
            }

            @Override
            public KeyRingsWithKeysDTO getAllKeyRingsWithKeys()
                    throws RemoteException, UnauthorizedCallerFault,
                    ServiceNotReadyFault, KeyManagementFault {
                
                KeyRingWithKeysDTO keyRing = new KeyRingWithKeysDTO(keyRingName, new KeyDTO[0], 20L, 100L);
                KeyRingsWithKeysDTO keyRings = new KeyRingsWithKeysDTO(new KeyRingWithKeysDTO[]{keyRing});
                return keyRings;
            }
        });
        
        Serializable response = skp.serviceHeartbeatRequest(KeyRequestDTO.SERVICE_NAME, new KeyRequestDTO());
        assertNotNull(response);
        assertTrue(response instanceof KeyResponseDTO);
        List<com.bluejungle.pf.destiny.lib.KeyResponseDTO.KeyRingDTO> keyRings = ((KeyResponseDTO)response).getKeyRings();
        assertEquals(1, keyRings.size());
        
        assertEquals(keyRingName, keyRings.get(0).getName());
        assertEquals(100L, keyRings.get(0).getTimestamp());
        assertEquals(0, keyRings.get(0).getKeys().size());
    }
    
    public void testOneKeyRingOneKey() {
        final String keyRingName = "keyRing" + r.nextInt();
        
        final int keyVersion = r.nextInt();
        final byte[] keyId = new byte[3];
        r.nextBytes(keyId);
        final byte[] key = new byte[5];
        r.nextBytes(key);
        
        ServerKeyProviderMock skp = new ServerKeyProviderMock(new KeyManagementDCCIFMock(){
            @Override
            public long getAllLatestModifiedDate() throws RemoteException,
                    UnauthorizedCallerFault, ServiceNotReadyFault,
                    KeyManagementFault {
                return 100L;
            }

            @Override
            public KeyRingsWithKeysDTO getAllKeyRingsWithKeys()
                    throws RemoteException, UnauthorizedCallerFault,
                    ServiceNotReadyFault, KeyManagementFault {
                KeyDTO keyDTO = new KeyDTO(keyVersion, new KeyIdDTO(keyId, 90L), key);
                KeyRingWithKeysDTO keyRing = new KeyRingWithKeysDTO(
                        keyRingName, new KeyDTO[] { keyDTO }, 20L, 100L);
                KeyRingsWithKeysDTO keyRings = new KeyRingsWithKeysDTO(
                        new KeyRingWithKeysDTO[] { keyRing });
                return keyRings;
            }
        });
        
        Serializable response = skp.serviceHeartbeatRequest(KeyRequestDTO.SERVICE_NAME, new KeyRequestDTO());
        assertNotNull(response);
        assertTrue(response instanceof KeyResponseDTO);
        List<com.bluejungle.pf.destiny.lib.KeyResponseDTO.KeyRingDTO> keyRings = ((KeyResponseDTO)response).getKeyRings();
        assertEquals(1, keyRings.size());
        
        assertEquals(keyRingName, keyRings.get(0).getName());
        assertEquals(100L, keyRings.get(0).getTimestamp());
        assertEquals(1, keyRings.get(0).getKeys().size());
        
        com.bluejungle.pf.destiny.lib.KeyResponseDTO.KeyDTO keyDTO = keyRings.get(0).getKeys().get(0);
        
        assertEquals(90L, keyDTO.getTimestamp());
        assertEquals(keyVersion, keyDTO.getVersion());
        assertTrue(Arrays.equals(keyId, keyDTO.getId()));
        assertTrue(Arrays.equals(key, keyDTO.getKey()));
    }
    
    public void testNoUpdate() {
        final AtomicInteger step = new AtomicInteger(0);
        final AtomicBoolean isCalled = new AtomicBoolean(false);
        
        ServerKeyProviderMock skp = new ServerKeyProviderMock(new KeyManagementDCCIFMock(){
            @Override
            public long getAllLatestModifiedDate() throws RemoteException,
                    UnauthorizedCallerFault, ServiceNotReadyFault,
                    KeyManagementFault {
                switch (step.get()) {
                case 0:
                    return 1L;
                case 1:
                    return 1L;
                default:
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public KeyRingsWithKeysDTO getAllKeyRingsWithKeys()
                    throws RemoteException, UnauthorizedCallerFault,
                    ServiceNotReadyFault, KeyManagementFault {
                switch (step.get()) {
                case 0:
                    return null;
                }
                
                isCalled.set(true);
                throw new IllegalArgumentException();
            }
        });
        
        Serializable response = skp.serviceHeartbeatRequest(KeyRequestDTO.SERVICE_NAME, new KeyRequestDTO());
        assertNotNull(response);
        
        assertFalse(isCalled.get());
        
        step.set(1);
        
        response = skp.serviceHeartbeatRequest(KeyRequestDTO.SERVICE_NAME, new KeyRequestDTO());
        assertNotNull(response);
        assertFalse(isCalled.get());
    }
    
    public void testUpdate() {
        final String keyRingName = "keyRing" + r.nextInt();
        
        final int keyVersion = r.nextInt();
        final byte[] keyId = new byte[3];
        r.nextBytes(keyId);
        final byte[] key = new byte[5];
        r.nextBytes(key);
        
        final AtomicInteger step = new AtomicInteger(0);
        
        ServerKeyProviderMock skp = new ServerKeyProviderMock(new KeyManagementDCCIFMock(){
            @Override
            public long getAllLatestModifiedDate() throws RemoteException,
                    UnauthorizedCallerFault, ServiceNotReadyFault,
                    KeyManagementFault {
                switch (step.get()) {
                case 0:
                    return 1L;
                case 1:
                    return 15L;
                default:
                    throw new IllegalArgumentException();
                }
            }

            @Override
            public KeyRingsWithKeysDTO getAllKeyRingsWithKeys()
                    throws RemoteException, UnauthorizedCallerFault,
                    ServiceNotReadyFault, KeyManagementFault {
                switch (step.get()) {
                case 0:
                    return null;
                case 1:
                    KeyDTO keyDTO = new KeyDTO(keyVersion, new KeyIdDTO(keyId, 11L), key);
                    KeyRingWithKeysDTO keyRing = new KeyRingWithKeysDTO(
                            keyRingName, new KeyDTO[] { keyDTO }, 7L, 9L);
                    KeyRingsWithKeysDTO keyRings = new KeyRingsWithKeysDTO(
                            new KeyRingWithKeysDTO[] { keyRing });
                    return keyRings;
                default:
                    throw new IllegalArgumentException();
                }
            }
        });
        
        Serializable response = skp.serviceHeartbeatRequest(KeyRequestDTO.SERVICE_NAME, new KeyRequestDTO());
        assertNotNull(response);
        
        step.set(1);
        
        response = skp.serviceHeartbeatRequest(KeyRequestDTO.SERVICE_NAME, new KeyRequestDTO());
        assertNotNull(response);
        
        assertTrue(response instanceof KeyResponseDTO);
        List<com.bluejungle.pf.destiny.lib.KeyResponseDTO.KeyRingDTO> keyRings = ((KeyResponseDTO)response).getKeyRings();
        assertEquals(1, keyRings.size());
        
        assertEquals(keyRingName, keyRings.get(0).getName());
        assertEquals(9L, keyRings.get(0).getTimestamp());
        assertEquals(1, keyRings.get(0).getKeys().size());
        
        com.bluejungle.pf.destiny.lib.KeyResponseDTO.KeyDTO keyDTO = keyRings.get(0).getKeys().get(0);
        
        assertEquals(11L, keyDTO.getTimestamp());
        assertEquals(keyVersion, keyDTO.getVersion());
        assertTrue(Arrays.equals(keyId, keyDTO.getId()));
        assertTrue(Arrays.equals(key, keyDTO.getKey()));
        
    }
}
