/*
 * Created on Sep 14, 2011
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.crypt;
/**
 * @author name
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/crypt/CryptoException.java#1 $
 * 
 * FIXME: This should be checked exception but I don't have time.  
 */
public class CryptoException extends RuntimeException {
	
	CryptoException() {
	}
	
	CryptoException(String message) {
		super(message);
	}
	
	CryptoException(Exception exception) {
        super(exception);
    }
	
}
