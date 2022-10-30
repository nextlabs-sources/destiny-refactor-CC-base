package com.nextlabs.destiny.container.shared.customapps;

public class InvalidCustomAppException extends IllegalArgumentException {
    
    public InvalidCustomAppException(String msg) {
        super(msg);
    }

    public InvalidCustomAppException(String message, Throwable cause) {
        super(message, cause);
    }
}
