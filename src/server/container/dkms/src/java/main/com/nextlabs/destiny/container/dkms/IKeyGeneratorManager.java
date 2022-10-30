package com.nextlabs.destiny.container.dkms;


public interface IKeyGeneratorManager {
    
    byte[] generateKey(int length) throws KeyManagementException;
    
}
