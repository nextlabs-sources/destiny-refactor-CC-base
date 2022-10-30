package com.nextlabs.destiny.container.dkms;

import java.util.Collection;

import com.nextlabs.destiny.container.dkms.hibernateimpl.KeyRingDO;

public interface IKeyRing {

    KeyRingDO getKeyRingDO();
    
    String getName();
    
    IKey getKey(IKeyId keyId) throws KeyManagementException;

    Collection<IKey> getKeys() throws KeyManagementException;

    void setKey(IKey key) throws KeyManagementException;

    void deleteKey(IKeyId keyId) throws KeyManagementException;
    
    /**
     * close the keyring
     * @throws KeyManagementException
     */
    void close() throws KeyManagementException;
    
    boolean isClosed();
    
}
