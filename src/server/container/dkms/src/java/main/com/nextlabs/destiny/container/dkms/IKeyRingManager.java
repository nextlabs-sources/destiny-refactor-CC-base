package com.nextlabs.destiny.container.dkms;

import java.security.KeyStore.PasswordProtection;
import java.util.Set;

public interface IKeyRingManager {

    IKeyRing createKeyRing(String name, PasswordProtection password)
            throws KeyManagementException;
    
    IKeyRing getKeyRing(String name, PasswordProtection password)
            throws KeyManagementException;
    
    void updateKeyRing(IKeyRing keyRing) throws KeyManagementException;

    boolean deleteKeyRing(String name) throws KeyManagementException;
    
    boolean deleteKeyRing(IKeyRing keyRing) throws KeyManagementException;

    /**
     * 
     * @return empty set or set with values
     * @throws KeyManagementException
     */
    Set<String> getKeyRings() throws KeyManagementException;
 
    /**
     * @return a last modified date of all keyrings
     * @throws KeyManagementException
     */
    public long getLatestModifiedDate() throws KeyManagementException;
}
