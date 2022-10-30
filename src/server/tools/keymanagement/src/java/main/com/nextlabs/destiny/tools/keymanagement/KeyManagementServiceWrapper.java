package com.nextlabs.destiny.tools.keymanagement;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.nextlabs.destiny.services.keymanagement.KeyManagementIF;
import com.nextlabs.destiny.services.keymanagement.types.KeyDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyIdDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyManagementFault;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingNamesDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingWithKeysDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingsWithKeysDTO;

public class KeyManagementServiceWrapper {
    private static final Log LOG = LogFactory.getLog(KeyManagementServiceWrapper.class);
    
    private final KeyManagementIF port;
    public KeyManagementServiceWrapper(KeyManagementIF keyManagementIF) {
        this.port = keyManagementIF;
    }
    
    public KeyManagementIF getKeyManagementIF(){
        return port;
    }
    
    private abstract class CommonRemoteExceptionHandler<T> {
        
        abstract T run() throws RemoteException;
        
        KeyManagementMgrException handleSpecific(RemoteException e) {
            LOG.error("", e);
            return KeyManagementMgrException.create(e);
        }
        
        T execute() throws KeyManagementMgrException{
            try {
                return run();
            } catch (KeyManagementFault e) {
                throw new KeyManagementMgrException(e.getMsg());
            } catch ( com.bluejungle.destiny.types.secure_session.v1.AccessDeniedFault e) {
                throw new KeyManagementMgrException(e);
            } catch (UnauthorizedCallerFault e) {
                throw new KeyManagementMgrException(e);
            } catch (ServiceNotReadyFault e) {
                throw new KeyManagementMgrException(e);
            } catch (RemoteException e) {
                throw handleSpecific(e);
            }
        }
    }
    
    public KeyRingDTO createKeyRing(final String keyRingName) 
            throws KeyManagementMgrException {
        return new CommonRemoteExceptionHandler<KeyRingDTO>() {
            @Override
            KeyRingDTO run() throws RemoteException {
                return port.createKeyRing(keyRingName);
            }
        }.execute();
    }

    public Set<String> getKeyRingNames() 
            throws KeyManagementMgrException {
        KeyRingNamesDTO keyRingNamesDTO = new CommonRemoteExceptionHandler<KeyRingNamesDTO>() {
            @Override
            KeyRingNamesDTO run() throws RemoteException {
                return port.getKeyRingNames();
            }
        }.execute();
        
        Set<String> names = new HashSet<String>();
        if (keyRingNamesDTO != null && keyRingNamesDTO.getKeyRingNames() != null) {
            Collections.addAll(names, keyRingNamesDTO.getKeyRingNames());
        }
        return names;
    }

    public KeyRingDTO getKeyRing(final String keyRingName) 
            throws KeyManagementMgrException {
        return new CommonRemoteExceptionHandler<KeyRingDTO>() {
            @Override
            KeyRingDTO run() throws RemoteException {
                return port.getKeyRing(keyRingName);
            }
        }.execute();
    }

    public KeyRingWithKeysDTO getKeyRingWithKeys(final String keyRingName) 
            throws KeyManagementMgrException {
        return new CommonRemoteExceptionHandler<KeyRingWithKeysDTO>() {
            @Override
            KeyRingWithKeysDTO run() throws RemoteException {
                return port.getKeyRingWithKeys(keyRingName);
            }
        }.execute();
    }

    public void deleteKeyRing(final String keyRingName)
            throws KeyManagementMgrException {
        new CommonRemoteExceptionHandler<Object>() {
            @Override
            Object run() throws RemoteException {
                port.deleteKeyRing(keyRingName);
                return null;
            }
        }.execute();
    }

    public KeyIdDTO generateKey(final String keyRingName, final int keyLength)
            throws KeyManagementMgrException {
        return new CommonRemoteExceptionHandler<KeyIdDTO>() {
            @Override
            KeyIdDTO run() throws RemoteException {
                return port.generateKey(keyRingName, keyLength);
            }
        }.execute();
    }

    public void setKey(final String keyRingName, final KeyDTO key)
            throws KeyManagementMgrException {
        new CommonRemoteExceptionHandler<Object>() {
            @Override
            Object run() throws RemoteException {
                port.setKey(keyRingName, key);
                return null;
            }
        }.execute();
    }

    public KeyDTO getKey(final String keyRingName, final KeyIdDTO keyId)
            throws KeyManagementMgrException {
        return new CommonRemoteExceptionHandler<KeyDTO>() {
            @Override
            KeyDTO run() throws RemoteException {
                return port.getKey(keyRingName, keyId);
            }
        }.execute();
    }

    public void deleteKey(final String keyRingName, final KeyIdDTO keyId)
            throws KeyManagementMgrException {
        new CommonRemoteExceptionHandler<Object>() {
            @Override
            Object run() throws RemoteException {
                port.deleteKey(keyRingName, keyId);
                return null;
            }
        }.execute();
    }

    public long getAllLatestModifiedDate() throws KeyManagementMgrException {
        return new CommonRemoteExceptionHandler<Long>() {
            @Override
            Long run() throws RemoteException {
                return port.getAllLatestModifiedDate();
            }
        }.execute();
    }

    public List<KeyRingWithKeysDTO> getAllKeyRingsWithKeys()
            throws KeyManagementMgrException {
        KeyRingsWithKeysDTO keyRingsWithKeysDTO = new CommonRemoteExceptionHandler<KeyRingsWithKeysDTO>() {
            @Override
            KeyRingsWithKeysDTO run() throws RemoteException {
                return port.getAllKeyRingsWithKeys();
            }
        }.execute();
        
        List<KeyRingWithKeysDTO> keyRings = new ArrayList<KeyRingWithKeysDTO>();
        if (keyRingsWithKeysDTO != null && keyRingsWithKeysDTO.getKeyRings() != null) {
            Collections.addAll(keyRings, keyRingsWithKeysDTO.getKeyRings());
        }
        return keyRings;
    }
    
}
