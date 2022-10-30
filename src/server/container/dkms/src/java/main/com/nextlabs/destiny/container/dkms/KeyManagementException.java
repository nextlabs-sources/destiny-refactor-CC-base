package com.nextlabs.destiny.container.dkms;

import java.util.Arrays;

public class KeyManagementException extends Exception {
    
    private static final long serialVersionUID = 1L;

    public KeyManagementException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyManagementException(String message) {
        super(message);
    }

    public KeyManagementException(Throwable cause) {
        super(cause);
    }
    
    public static KeyManagementException keyRingNotFound(String keyRingName){
        return new KeyManagementException("The keyRing with name (" + keyRingName + ") does not exist.");
    }
    
    public static KeyManagementException keyNotFound(IKeyId keyId){
        return new KeyManagementException("The key with keyId ("
                + Arrays.toString(keyId.getId()) + ", ts="
                + keyId.getCreationTimeStamp() + ") does not exist.");
    }
    
    public static KeyManagementException unableTo(String actionName, String info){
        return new KeyManagementException("Unable to " + actionName + ". " + info);
    }
}
