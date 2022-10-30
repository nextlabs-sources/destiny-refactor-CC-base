package com.nextlabs.destiny.container.dkms;

import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.nextlabs.destiny.container.dkms.impl.DTOConverter;
import com.nextlabs.destiny.container.dkms.impl.Key;
import com.nextlabs.destiny.container.dkms.impl.KeyGeneratorManager;
import com.nextlabs.destiny.container.dkms.impl.KeyId;
import com.nextlabs.destiny.container.dkms.impl.KeyRingManager;
import com.nextlabs.destiny.services.keymanagement.KeyManagementIF;
import com.nextlabs.destiny.services.keymanagement.types.KeyDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyIdDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyManagementFault;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingNamesDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingWithKeysDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingsWithKeysDTO;

public class DKMSKeyManagementServiceImpl implements KeyManagementIF {

    private static final Log LOG = LogFactory.getLog(DKMSKeyManagementServiceImpl.class);
    
    private String toString(KeyIdDTO keyId) {
        StringBuilder sb = new StringBuilder("KeyId[");
        if(keyId != null){
            sb.append("hash=").append(Arrays.toString(keyId.getHash()))
              .append(", ts=").append(keyId.getTimestamp());
        }else{
            sb.append("null");
        }
        sb.append("]");
        return sb.toString();
    }
    
    private String toString(KeyDTO key) {
        StringBuilder sb = new StringBuilder("Key[");
        if(key != null){
            sb.append("version=").append(key.getVersion())
              .append(", keyId=").append(toString(key.getKeyId()))
              .append(", key=").append(mask(key.getKey()));
        }else{
            sb.append("null");
        }
        sb.append("]");
        return sb.toString();
    }
    
    private String mask(byte[] bytes){
        if(bytes == null){
            return "null";
        }
        
        
        if(bytes.length > 6){
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1; i++) {
                sb.append(Byte.toString(bytes[i])).append(",");
            }
            
            sb.append("(" + bytes.length + "),");
            
            for (int i = bytes.length - 1; i < bytes.length; i++) {
                sb.append(Byte.toString(bytes[i])).append(",");
            }
            
            return sb.toString();
        } else {
            // too short to mask
            return Arrays.toString(bytes);
        }
    }
    
    private IKeyRingManager getKrMgr() {
        return ComponentManagerFactory.getComponentManager().getComponent(KeyRingManager.class);
    }
    
    private KeyRingManagerWrapper getKrMgrWrapper() {
        return ComponentManagerFactory.getComponentManager().getComponent(KeyRingManagerWrapper.class);
    }
    
    private IKeyGeneratorManager getkeyGenMgr() {
        return ComponentManagerFactory.getComponentManager().getComponent(KeyGeneratorManager.class);
    }
    
    private abstract class Action<T> {
        T execute() throws KeyManagementFault {
            try {
                return run();
            } catch (Exception e) {
                throw new KeyManagementFault(e.toString());
            }
        }
        
        abstract T run() throws Exception;
    }
    
    /**
     * same as Action but no return 
     *
     */
    private abstract class ActionNR {
        void execute() throws KeyManagementFault {
            try {
                run();
            } catch (Exception e) {
                throw new KeyManagementFault(e.toString());
            }
        }
        
        abstract void run() throws Exception;
    }

    @Override
    public KeyRingDTO createKeyRing(final String keyRingName) throws 
            KeyManagementFault
          , UnauthorizedCallerFault
          , ServiceNotReadyFault
          , RemoteException
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("createKeyRing: keyRingName[" + keyRingName + "]");
        }
        return new Action<KeyRingDTO>() {
          @Override
          KeyRingDTO run() throws KeyManagementException {
              KeyRingManagerWrapper krMgr = getKrMgrWrapper();
              IKeyRing keyRing = krMgr.createKeyRing(keyRingName.toUpperCase());
              return DTOConverter.convertToKeyRingDTO(keyRing);
          }
      }.execute();
    }

    @Override
    public KeyRingNamesDTO getKeyRingNames() throws 
            KeyManagementFault
          , UnauthorizedCallerFault
          , ServiceNotReadyFault
          , RemoteException
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("getKeyRingNames:");
        }
        return new Action<KeyRingNamesDTO>() {
            @Override
            KeyRingNamesDTO run() throws KeyManagementException {
                IKeyRingManager krMgr = getKrMgr();
                Set<String> keyRingNames = krMgr.getKeyRings();
                return DTOConverter.toKeyRingNamesDTO(keyRingNames);
            }
        }.execute();
    }

    @Override
    public KeyRingDTO getKeyRing(final String keyRingName) throws 
            KeyManagementFault
          , UnauthorizedCallerFault
          , ServiceNotReadyFault
          , RemoteException
    {
  
        if (LOG.isTraceEnabled()) {
            LOG.trace("getKeyRing: keyRingName[" + keyRingName + "]");
        }
        return new Action<KeyRingDTO>() {
            @Override
            KeyRingDTO run() throws KeyManagementException {
                KeyRingManagerWrapper krMgr = getKrMgrWrapper();
                IKeyRing keyRing = krMgr.getKeyRing(keyRingName);
                if(keyRing == null){
                    throw KeyManagementException.keyRingNotFound(keyRingName.toUpperCase());
                }
                return DTOConverter.convertToKeyRingDTO(keyRing);
            }
        }.execute();
    }

    @Override
    public KeyRingWithKeysDTO getKeyRingWithKeys(final String keyRingName) throws
            KeyManagementFault
          , UnauthorizedCallerFault
          , ServiceNotReadyFault
          , RemoteException
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("getKeyRingWithKeys: keyRingName[" + keyRingName + "]");
        }
        return new Action<KeyRingWithKeysDTO>() {
            @Override
            KeyRingWithKeysDTO run() throws KeyManagementException {
                KeyRingManagerWrapper krMgr = getKrMgrWrapper();
                IKeyRing keyRing = krMgr.getKeyRing(keyRingName);
                if(keyRing == null){
                    throw KeyManagementException.keyRingNotFound(keyRingName.toUpperCase());
                }
                return DTOConverter.convertToKeyRingWithKeysDTO(keyRing);
            }
        }.execute();
    }

    @Override
    public void deleteKeyRing(final String keyRingName) throws
            KeyManagementFault
          , UnauthorizedCallerFault
          , ServiceNotReadyFault
          , RemoteException
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("deleteKeyRing: keyRingName[" + keyRingName + "]");
        }

        new ActionNR() {
            @Override
            void run() throws KeyManagementException, RuntimeException {
                IKeyRingManager krMgr = getKrMgr();
                boolean isDeleted = krMgr.deleteKeyRing(keyRingName.toUpperCase());
                if(!isDeleted){
                    throw KeyManagementException.unableTo("delete", "keyRingName=" + keyRingName);
                }
            }
        }.execute();
    }

    /*
     * TODO don't copy
     * same as com.nextlabs.service.keyservice.KeyServiceResponseFactory.HASH_LENGTH 
     */
    private static final int HASH_LENGTH = 32;
    
    /*
     * TODO don't copy
     * copy from com.nextlabs.service.keyservice.ArrayHelper
     */
    private static byte[] pad(byte[] input, int length) {
        return pad(input, length, (byte) 0);
    }
    
    /*
     * TODO don't copy
     * copy from com.nextlabs.service.keyservice.ArrayHelper
     */
    private static byte[] pad(byte[] input, int length, byte filler) {
        if (input.length < length) {
            byte[] output = new byte[length];
            Arrays.fill(output, filler);
            System.arraycopy(input, 0, output, 0, input.length);
            return output;
        }
        return input;
    }
    
    @Override
    public KeyIdDTO generateKey(final String keyRingName, final int keyLength) throws
            KeyManagementFault
          , UnauthorizedCallerFault
          , ServiceNotReadyFault
          , RemoteException
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("generateKey: keyRingName[" + keyRingName + "], keyLength=" + keyLength);
        }

        return new Action<KeyIdDTO>() {
            @Override
            KeyIdDTO run() throws KeyManagementException, NoSuchAlgorithmException {
                IKeyGeneratorManager keyGenMgr = getkeyGenMgr();
                byte[] keyValue = keyGenMgr.generateKey(keyLength);
                
                KeyRingManagerWrapper krMgr = getKrMgrWrapper();
                IKeyRing keyRing = krMgr.getKeyRing(keyRingName.toUpperCase());
                
                if(keyRing == null){
                    throw KeyManagementException.keyRingNotFound(keyRingName);
                }
                
                MessageDigest digest = MessageDigest.getInstance("SHA1");
                byte[] hash = digest.digest(keyValue);
                assert hash.length == 20;
                hash = pad(hash, HASH_LENGTH);
                // the correct is up to a second on the KeyService
                IKeyId keyId = new KeyId(hash, System.currentTimeMillis() / 1000 * 1000);
                IKey key = new Key(keyId, keyValue);
                keyRing.setKey(key);
                return DTOConverter.convertToKeyIdDTO(keyId);
            }
        }.execute();
    }

    @Override
    public void setKey(final String keyRingName, final KeyDTO keyDTO) throws
            KeyManagementFault
          , UnauthorizedCallerFault
          , ServiceNotReadyFault
          , RemoteException
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("setKey: keyRingName[" + keyRingName + "], " + toString(keyDTO));
        }

        new ActionNR() {
            @Override
            void run() throws KeyManagementException, NoSuchAlgorithmException {
                KeyRingManagerWrapper krMgr = getKrMgrWrapper();
                IKeyRing keyRing = krMgr.getKeyRing(keyRingName.toUpperCase());
                
                if (keyRing == null) {
                    throw KeyManagementException.keyRingNotFound(keyRingName);
                }
                
                IKey key = DTOConverter.convertToKey(keyDTO);
                keyRing.setKey(key);
            }
        }.execute();
    }

    @Override
    public KeyDTO getKey(final String keyRingName, final KeyIdDTO keyIdDTO) throws
            KeyManagementFault
          , UnauthorizedCallerFault
          , ServiceNotReadyFault
          , RemoteException
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("getKey: keyRingName[" + keyRingName + "], " + toString(keyIdDTO));
        }
        
        return new Action<KeyDTO>() {
            @Override
            KeyDTO run() throws KeyManagementException, NoSuchAlgorithmException {
                KeyRingManagerWrapper krMgr = getKrMgrWrapper();
                IKeyRing keyRing = krMgr.getKeyRing(keyRingName.toUpperCase());
                
                if (keyRing == null) {
                    throw KeyManagementException.keyRingNotFound(keyRingName);
                }
                IKeyId keyId = DTOConverter.convertToKeyId(keyIdDTO);
                IKey key = keyRing.getKey(keyId);
                
                if (key == null) {
                    throw KeyManagementException.keyNotFound(keyId);
                }
                
                return DTOConverter.convertToKeyDTO(key);
            }
        }.execute();
    }

    @Override
    public void deleteKey(final String keyRingName, final KeyIdDTO keyIdDTO) throws
            KeyManagementFault
          , UnauthorizedCallerFault
          , ServiceNotReadyFault
          , RemoteException
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("deleteKey: keyRingName[" + keyRingName + "], " + toString(keyIdDTO));
        }
        
        new ActionNR() {
            @Override
            void run() throws KeyManagementException, NoSuchAlgorithmException {
                KeyRingManagerWrapper krMgr = getKrMgrWrapper();
                IKeyRing keyRing = krMgr.getKeyRing(keyRingName.toUpperCase());
                if (keyRing == null) {
                    throw KeyManagementException.keyRingNotFound(keyRingName);
                }
                keyRing.deleteKey(DTOConverter.convertToKeyId(keyIdDTO));
            }
        }.execute();
    }

    @Override
    public long getAllLatestModifiedDate() throws
            KeyManagementFault
          , UnauthorizedCallerFault
          , ServiceNotReadyFault
          , RemoteException
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("getAllLatestModifiedDate ");
        }
        
        return new Action<Long>() {
            @Override
            Long run() throws KeyManagementException, NoSuchAlgorithmException {
                IKeyRingManager krMgr = getKrMgr();
                return krMgr.getLatestModifiedDate();
            }
        }.execute();
    }

    @Override
    public KeyRingsWithKeysDTO getAllKeyRingsWithKeys() throws
            KeyManagementFault
          , UnauthorizedCallerFault
          , ServiceNotReadyFault
          , RemoteException
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("getAllKeyRingWithKeys: ");
        }
        
        return new Action<KeyRingsWithKeysDTO>() {
            @Override
            KeyRingsWithKeysDTO run() throws KeyManagementException {
                KeyRingManagerWrapper krMgrW = getKrMgrWrapper();
                IKeyRingManager krMgr = getKrMgr();
                Set<String> names = krMgr.getKeyRings();
                
                List<KeyRingWithKeysDTO> keyRings = new ArrayList<KeyRingWithKeysDTO>(names.size());
                
                for (String name : names) {
                    IKeyRing keyRing = krMgrW.getKeyRing(name);
                    if (keyRing != null) {
                        // the keyRing maybe just deleted
                        keyRings.add(DTOConverter.convertToKeyRingWithKeysDTO(keyRing));
                    }
                }
                
                return new KeyRingsWithKeysDTO(keyRings.toArray(new KeyRingWithKeysDTO[keyRings.size()]));
            }
        }.execute();
    }
    
}
